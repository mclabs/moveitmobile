// SMSLib for Java v3
// A Java API library for sending and receiving SMS via a GSM modem
// or other supported gateways.
// Web Site: http://www.smslib.org
//
// Copyright (C) 2002-2008, Thanasis Delenikas, Athens/GREECE.
// SMSLib is distributed under the terms of the Apache License version 2.0
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.smslib.modem;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import org.smslib.*;

/**
 * Abstract implementation of a generic GSM modem driver.
 */
public abstract class AModemDriver
{
	private static final String rxErrorWithCode = "\\s*[\\p{ASCII}]*\\s*\\+(CM[ES])\\s+ERROR: (\\d+)\\s";

	private static final String rxPlainError = "\\s*[\\p{ASCII}]*\\s*ERROR\\s";

	protected Object SYNC_Reader, SYNC_Commander;

	protected ModemGateway gateway;

	protected boolean dataReceived;

	private volatile boolean connected;

	private CharQueue queue;

	private ModemReader modemReader;

	private KeepAlive keepAlive;

	private AsyncNotifier asyncNotifier;

	/**
	 * Code of last error
	 * 
	 * <pre>
	 *   -1 = empty or invalid response
	 *    0 = OK
	 * 5xxx = CME error xxx
	 * 6xxx = CMS error xxx
	 * 9000 = ERROR
	 * </pre>
	 */
	private int lastError;

	static int OK = 0;

	AModemDriver(ModemGateway gateway, String deviceParms)
	{
		SYNC_Reader = new Object();
		SYNC_Commander = new Object();
		this.gateway = gateway;
		connected = false;
		dataReceived = false;
		queue = new CharQueue();
	}

	abstract void connectPort() throws GatewayException, IOException, InterruptedException;

	abstract void disconnectPort() throws IOException, InterruptedException;

	abstract void clear() throws IOException;

	void connect() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		String response;
		synchronized (SYNC_Commander)
		{
			connectPort();
			connected = true;
			keepAlive = new KeepAlive();
			modemReader = new ModemReader(keepAlive);
			asyncNotifier = new AsyncNotifier();
			clearBuffer();
			gateway.getATHandler().reset();
			gateway.getATHandler().sync();
			gateway.getATHandler().echoOff();
			while (true)
			{
				response = gateway.getATHandler().getSimStatus();
				while (response.indexOf("BUSY") >= 0)
				{
					gateway.logDebug("SIM found busy, waiting...");
					Thread.sleep(gateway.getService().S.AT_WAIT_SIMPIN);
					response = gateway.getATHandler().getSimStatus();
				}
				if (response.indexOf("SIM PIN2") >= 0)
				{
					gateway.logDebug("SIM requesting PIN2.");
					if ((gateway.getSimPin2() == null) || (gateway.getSimPin2().length() == 0)) throw new GatewayException("The GSM modem requires SIM PIN2 to operate.");
					if (!gateway.getATHandler().enterPin(gateway.getSimPin2())) throw new GatewayException("SIM PIN2 provided is not accepted by the GSM modem.");
					Thread.sleep(gateway.getService().S.AT_WAIT_SIMPIN);
				}
				else if (response.indexOf("SIM PIN") >= 0)
				{
					gateway.logDebug("SIM requesting PIN.");
					if ((gateway.getSimPin() == null) || (gateway.getSimPin().length() == 0)) throw new GatewayException("The GSM modem requires SIM PIN to operate.");
					if (!gateway.getATHandler().enterPin(gateway.getSimPin())) throw new GatewayException("SIM PIN provided is not accepted by the GSM modem.");
					Thread.sleep(gateway.getService().S.AT_WAIT_SIMPIN);
				}
				if (response.indexOf("READY") >= 0) break;
				else
				{
					gateway.logWarn("Cannot understand SIMPIN response: " + response + ", will wait for a while...");
					Thread.sleep(gateway.getService().S.AT_WAIT_SIMPIN);
				}
			}
			gateway.getATHandler().echoOff();
			gateway.getATHandler().init();
			gateway.getATHandler().echoOff();
			waitForNetworkRegistration();
			gateway.getATHandler().setVerboseErrors();
			if (gateway.getATHandler().getStorageLocations().length() == 0)
			{
				try
				{
					gateway.getATHandler().readStorageLocations();
					gateway.logInfo("MEM: Storage Locations Found: " + gateway.getATHandler().getStorageLocations());
				}
				catch (Exception e)
				{
					gateway.getATHandler().setStorageLocations("--");
					gateway.logWarn("Storage locations could *not* be retrieved, will proceed with defaults.", e);
				}
			}
			if (!gateway.getATHandler().setIndications()) gateway.logWarn("Callback indications were *not* set succesfully!");
			if (gateway.getProtocol() == MessageProtocols.PDU)
			{
				if (!gateway.getATHandler().setPduProtocol()) throw new GatewayException("The GSM modem does not support the PDU protocol.");
			}
			else if (gateway.getProtocol() == MessageProtocols.TEXT)
			{
				if (!gateway.getATHandler().setTextProtocol()) throw new GatewayException("The GSM modem does not support the TEXT protocol.");
			}
		}
	}

	void disconnect() throws IOException, InterruptedException
	{
		synchronized (SYNC_Commander)
		{
			connected = false;
			if (keepAlive != null)
			{
				gateway.logDebug("Trying to shutdown keepAlive thread...");
				keepAlive.interrupt();
				keepAlive.join();
				keepAlive = null;
			}
			if (asyncNotifier != null)
			{
				gateway.logDebug("Trying to shutdown asyncNotifier thread...");
				asyncNotifier.interrupt();
				asyncNotifier.join();
				asyncNotifier = null;
			}
			if (modemReader != null)
			{
				gateway.logDebug("Trying to shutdown modemReader thread...");
				modemReader.interrupt();
				modemReader.join();
				modemReader = null;
			}
			disconnectPort();
		}
	}

	public abstract void write(char c) throws IOException;

	abstract int read() throws IOException;

	abstract boolean portHasData() throws IOException;

	public boolean dataAvailable() throws IOException, InterruptedException
	{
		return (queue.peek() == -1 ? false : true);
	}

	public void write(String s) throws IOException
	{
		gateway.logDebug("SEND :" + formatLog(new StringBuffer(s)));
		for (int i = 0; i < s.length(); i++)
			write(s.charAt(i));
	}

	public String getResponse() throws GatewayException, TimeoutException, IOException
	{
		StringBuffer buffer;
		String response;
		byte c;
		boolean terminate;
		int i;
		String terminators[];
		lastError = -1;
		terminators = gateway.getATHandler().getTerminators();
		buffer = new StringBuffer(gateway.getService().S.SERIAL_BUFFER_SIZE);
		try
		{
			while (true)
			{
				while ((queue.peek() == 0x0a) || (queue.peek() == 0x0d))
					queue.get();
				while (true)
				{
					c = queue.get();
					if (gateway.getService().S.DEBUG_QUEUE) gateway.logDebug("OUT READER QUEUE : " + (int) c + " / " + (char) c);
					if (c != 0x0a) buffer.append((char) c);
					else break;
				}
				if (buffer.charAt(buffer.length() - 1) != 0x0d) buffer.append((char) 0x0d);
				response = buffer.toString();
				terminate = false;
				for (i = 0; i < terminators.length; i++)
					if (response.matches(terminators[i]))
					{
						terminate = true;
						break;
					}
				if (terminate) break;
			}
			gateway.logDebug("BUFFER: " + buffer);
			if (i >= terminators.length - 4)
			{
				AsyncEvents event = gateway.getATHandler().processUnsolicitedEvents(buffer.toString());
				if ((event == AsyncEvents.INBOUNDMESSAGE) || (event == AsyncEvents.INBOUNDSTATUSREPORTMESSAGE) || (event == AsyncEvents.INBOUNDCALL)) asyncNotifier.setEvent(event);
				return getResponse();
			}
			// Try to interpret error code
			if (response.matches(rxErrorWithCode))
			{
				Pattern p = Pattern.compile(rxErrorWithCode);
				Matcher m = p.matcher(response);
				if (m.find())
				{
					try
					{
						if (m.group(1).equals("CME"))
						{
							int code = Integer.parseInt(m.group(2));
							lastError = 5000 + code;
						}
						else if (m.group(1).equals("CMS"))
						{
							int code = Integer.parseInt(m.group(2));
							lastError = 6000 + code;
						}
						else throw new GatewayException("Invalid error response: " + m.group(1));
					}
					catch (NumberFormatException e)
					{
						gateway.logDebug("Error on number conversion while interpreting response: ");
						throw new GatewayException("Cannot convert error code number.");
					}
				}
				else throw new GatewayException("Cannot match error code. Should never happen!");
			}
			else if (response.matches(rxPlainError)) lastError = 9000;
			else if (response.indexOf("OK") >= 0) lastError = 0;
			else lastError = 10000;
			gateway.logDebug("RECV :" + formatLog(buffer));
		}
		catch (InterruptedException e)
		{
			gateway.logWarn("GetResponse() Interrupted.", e);
		}
		return buffer.toString();
	}

	public void clearBuffer() throws IOException, InterruptedException
	{
		synchronized (SYNC_Commander)
		{
			gateway.logDebug("clearBuffer() called.");
			Thread.sleep(gateway.getService().S.SERIAL_CLEAR_WAIT);
			clear();
			queue.clear();
		}
	}

	boolean waitForNetworkRegistration() throws GatewayException, TimeoutException, IOException, InterruptedException
	{
		StringTokenizer tokens;
		String response;
		int answer;
		while (true)
		{
			response = gateway.getATHandler().getNetworkRegistration();
			if (response.indexOf("ERROR") > 0) return false;
			response = response.replaceAll("\\s+OK\\s+", "");
			response = response.replaceAll("\\s+", "");
			response = response.replaceAll("\\+CREG:", "");
			tokens = new StringTokenizer(response, ",");
			tokens.nextToken();
			try
			{
				answer = Integer.parseInt(tokens.nextToken());
			}
			catch (Exception e)
			{
				answer = -1;
			}
			switch (answer)
			{
				case 0:
					gateway.logError("GSM: Auto-registration disabled!");
					throw new GatewayException("GSM Network Auto-Registration disabled!");
				case 1:
					gateway.logInfo("GSM: Registered to home network.");
					return true;
				case 2:
					gateway.logWarn("GSM: Not registered, searching for network...");
					break;
				case 3:
					gateway.logError("GSM: Network registration denied!");
					throw new GatewayException("GSM Network Registration denied!");
				case 4:
					gateway.logError("GSM: Unknown registration error!");
					throw new GatewayException("GSM Network Registration error!");
				case 5:
					gateway.logError("GSM: Registered to foreign network (roaming).");
					return true;
				case -1:
					gateway.logInfo("GSM: Invalid CREG response.");
					throw new GatewayException("GSM: Invalid CREG response.");
			}
			Thread.sleep(gateway.getService().S.AT_WAIT_NETWORK);
		}
	}

	private String formatLog(StringBuffer s)
	{
		StringBuffer response = new StringBuffer();
		int i;
		char c;
		for (i = 0; i < s.length(); i++)
		{
			c = s.charAt(i);
			switch (c)
			{
				case 13:
					response.append("(cr)");
					break;
				case 10:
					response.append("(lf)");
					break;
				case 9:
					response.append("(tab)");
					break;
				default:
					if (((int) c >= 32) && ((int) c < 128))
					{
						response.append(c);
					}
					else
					{
						response.append("(" + (int) c + ")");
					}
					break;
			}
		}
		return response.toString();
	}

	private class CharQueue
	{
		byte[] buffer;

		int bufferStart, bufferEnd;

		public CharQueue()
		{
			buffer = new byte[gateway.getService().S.SERIAL_BUFFER_SIZE];
			bufferStart = 0;
			bufferEnd = 0;
		}

		public synchronized void put(byte c)
		{
			buffer[bufferEnd] = c;
			bufferEnd++;
			if (bufferEnd == gateway.getService().S.SERIAL_BUFFER_SIZE) bufferEnd = 0;
			if (gateway.getService().S.DEBUG_QUEUE) gateway.logDebug("IN READER QUEUE : " + (int) c + " / " + (char) c);
			notifyAll();
		}

		public synchronized byte get() throws TimeoutException, InterruptedException
		{
			byte c;
			while (true)
			{
				try
				{
					if (bufferStart == bufferEnd) wait(gateway.getService().S.SERIAL_TIMEOUT);
					if (bufferStart == bufferEnd) throw new TimeoutException("No response from device.");
					c = buffer[bufferStart];
					bufferStart++;
					if (bufferStart == gateway.getService().S.SERIAL_BUFFER_SIZE) bufferStart = 0;
					return c;
				}
				catch (InterruptedException e)
				{
					if (gateway.getStarted()) gateway.logWarn("Ignoring InterruptedException in Queue.get().");
					else
					{
						gateway.logWarn("Re-throwing InterruptedException in Queue.get() - should be during shutdown...");
						throw new InterruptedException();
					}
				}
			}
		}

		public synchronized byte peek() throws InterruptedException
		{
			while (true)
			{
				try
				{
					if (bufferStart == bufferEnd) wait(gateway.getService().S.SERIAL_TIMEOUT);
					if (bufferStart == bufferEnd) return -1;
					return buffer[bufferStart];
				}
				catch (InterruptedException e)
				{
					if (gateway.getStarted()) gateway.logWarn("Ignoring InterruptedException in Queue.peek().", e);
					else
					{
						gateway.logWarn("Re-throwing InterruptedException in Queue.peek() - should be during shutdown...", e);
						throw new InterruptedException();
					}
				}
			}
		}

		public synchronized String peek(int sizeToRead)
		{
			int i, size;
			StringBuffer result;
			size = sizeToRead;
			if (bufferStart == bufferEnd) return "";
			result = new StringBuffer(size);
			i = bufferStart;
			while (size > 0)
			{
				if ((buffer[i] != 0x0a) && (buffer[i] != 0x0d))
				{
					result.append((char) buffer[i]);
					size--;
				}
				i++;
				if (i == gateway.getService().S.SERIAL_BUFFER_SIZE) i = 0;
				if (i == bufferEnd) break;
			}
			return result.toString();
		}

		public synchronized void clear()
		{
			bufferStart = 0;
			bufferEnd = 0;
		}

		public void dump()
		{
			int i;
			i = bufferStart;
			while (i < bufferEnd)
			{
				System.out.println(buffer[i] + " -> " + (char) buffer[i]);
				i++;
			}
		}
	}

	private class ModemReader extends Thread
	{
		private KeepAlive keepAlive;

		public ModemReader(KeepAlive keepAlive)
		{
			this.keepAlive = keepAlive;
			start();
			gateway.logDebug("ModemReader thread started.");
		}

		public void run()
		{
			int c;
			String data;
			while (connected)
			{
				try
				{
					synchronized (SYNC_Reader)
					{
						if (!dataReceived) SYNC_Reader.wait();
						if (!connected) break;
						c = read();
						while (c != -1)
						{
							queue.put((byte) c);
							if (!portHasData()) break;
							c = read();
						}
						dataReceived = false;
					}
					data = queue.peek(6);
					if ((data.indexOf("CMTI") >= 0) || (data.indexOf("CSDI") >= 0) || (data.indexOf("RING") >= 0)) keepAlive.interrupt();
				}
				catch (InterruptedException e)
				{
					if (!connected) break;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			gateway.logDebug("ModemReader thread ended.");
		}
	}

	private class KeepAlive extends Thread
	{
		public KeepAlive()
		{
			setPriority(MIN_PRIORITY);
			start();
			gateway.logDebug("ModemDriver: KeepAlive thread started.");
		}

		public void run()
		{
			while (true)
			{
				try
				{
					try
					{
						sleep(gateway.getService().S.SERIAL_KEEPALIVE_INTERVAL);
					}
					catch (InterruptedException e)
					{
					}
					if (!connected) break;
					synchronized (SYNC_Commander)
					{
						if (!connected) break;
						gateway.logDebug("** KeepAlive START **");
						if (!gateway.getATHandler().isAlive()) gateway.setGatewayStatus(GatewayStatuses.RESTART);
						gateway.logDebug("** KeepAlive END **");
					}
				}
				catch (Exception e)
				{
					gateway.logError("ModemDriver: KeepAlive Error.", e);
					gateway.setGatewayStatus(GatewayStatuses.RESTART);
				}
			}
			gateway.logDebug("ModemDriver: KeepAlive thread ended.");
		}
	}

	private class AsyncNotifier extends Thread
	{
		private AsyncEvents event;

		private Object SYNC;

		public AsyncNotifier()
		{
			SYNC = new Object();
			setPriority(MIN_PRIORITY);
			start();
			gateway.logDebug("AsyncNotifier thread started.");
		}

		protected void setEvent(AsyncEvents event)
		{
			synchronized (SYNC)
			{
				this.event = event;
				SYNC.notify();
			}
		}

		protected String getMemLoc(String indication)
		{
			Pattern p = Pattern.compile("\\+?\"\\S+\"");
			Matcher m = p.matcher(indication);
			if (m.find()) return indication.substring(m.start(), m.end()).replaceAll("\"", "");
			else return "";
		}

		protected int getMemIndex(String indication)
		{
			Pattern p = Pattern.compile("\\+?\\d+");
			Matcher m = p.matcher(indication);
			if (m.find()) return Integer.parseInt(indication.substring(m.start(), m.end()).replaceAll("\"", ""));
			else return -1;
		}

		protected String getOriginator(String indication)
		{
			Pattern p = Pattern.compile("\\+?\"\\S+\"");
			Matcher m = p.matcher(indication);
			if (m.find()) return indication.substring(m.start(), m.end()).replaceAll("\"", "");
			else return "";
		}

		public void run()
		{
			String response;
			while (true)
			{
				try
				{
					synchronized (SYNC)
					{
						SYNC.wait();
						if (!connected) break;
						synchronized (SYNC_Commander)
						{
							if (event == AsyncEvents.INBOUNDMESSAGE)
							{
								event = AsyncEvents.NOTHING;
								response = gateway.getATHandler().getAsyncEventResponse(AsyncEvents.INBOUNDMESSAGE);
								if (gateway.getInboundNotification() != null) gateway.getInboundNotification().process(gateway.getGatewayId(), MessageTypes.INBOUND, getMemLoc(response), getMemIndex(response));
							}
							else if (event == AsyncEvents.INBOUNDSTATUSREPORTMESSAGE)
							{
								event = AsyncEvents.NOTHING;
								response = gateway.getATHandler().getAsyncEventResponse(AsyncEvents.INBOUNDSTATUSREPORTMESSAGE);
								if (gateway.getInboundNotification() != null) gateway.getInboundNotification().process(gateway.getGatewayId(), MessageTypes.STATUSREPORT, getMemLoc(response), getMemIndex(response));
							}
							else if (event == AsyncEvents.INBOUNDCALL)
							{
								event = AsyncEvents.NOTHING;
								//synchronized (SYNC_Commander)
								//{
								response = gateway.getATHandler().getAsyncEventResponse(AsyncEvents.INBOUNDCALL);
								//}
								if (gateway.getCallNotification() != null) gateway.getCallNotification().process(gateway.getGatewayId(), getOriginator(response));
							}
						}
					}
				}
				catch (InterruptedException e)
				{
					if (!connected) break;
				}
				catch (Exception e)
				{
				}
			}
			gateway.logDebug("AsyncNotifier thread ended.");
		}
	}

	public int getLastError()
	{
		return lastError;
	}

	public String getLastErrorText()
	{
		if (lastError == 0) return "OK";
		else if (lastError == -1) return "Invalid or empty response";
		else if ((lastError / 1000) == 5) return "CME Error " + (lastError % 1000);
		else if ((lastError / 1000) == 6) return "CMS Error " + (lastError % 1000);
		else return "Error: unknown";
	}

	public boolean isOk()
	{
		return (lastError == OK);
	}
}
