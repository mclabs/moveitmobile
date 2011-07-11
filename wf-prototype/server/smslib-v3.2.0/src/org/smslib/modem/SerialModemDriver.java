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

// Import javax.comm if you are using JavaComm v2.0
import javax.comm.*;
// Import gnu.io if you are using RxTx
// import gnu.io.*;
import java.util.*;
import java.io.*;
import org.smslib.*;

class SerialModemDriver extends AModemDriver implements SerialPortEventListener
{
	private String comPort;

	private int baudRate;

	private CommPortIdentifier portId;

	private SerialPort serialPort;

	private InputStream in;

	private OutputStream out;

	private ThreadReader threadReader;

	SerialModemDriver(ModemGateway gateway, String deviceParms)
	{
		super(gateway, deviceParms);
		StringTokenizer tokens = new StringTokenizer(deviceParms, ":");
		comPort = tokens.nextToken();
		baudRate = Integer.parseInt(tokens.nextToken());
		serialPort = null;
		if (gateway.getService().S.SERIAL_NOFLUSH) gateway.logInfo("Comm port flushing is disabled.");
		if (gateway.getService().S.SERIAL_POLLING) gateway.logInfo("Using polled serial port mode.");
	}

	void connectPort() throws GatewayException, IOException, InterruptedException
	{
		try
		{
			gateway.logInfo("Opening: " + comPort + " @" + baudRate);
			portId = CommPortIdentifier.getPortIdentifier(comPort);
			serialPort = (SerialPort) portId.open("org.smslib", 1971);
			in = serialPort.getInputStream();
			out = serialPort.getOutputStream();
			if (!gateway.getService().S.SERIAL_POLLING)
			{
				serialPort.notifyOnDataAvailable(true);
				serialPort.notifyOnOutputEmpty(true);
			}
			serialPort.notifyOnBreakInterrupt(true);
			serialPort.notifyOnFramingError(true);
			serialPort.notifyOnOverrunError(true);
			serialPort.notifyOnParityError(true);
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN);
			serialPort.addEventListener(this);
			serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			serialPort.setInputBufferSize(gateway.getService().S.SERIAL_BUFFER_SIZE);
			serialPort.setOutputBufferSize(gateway.getService().S.SERIAL_BUFFER_SIZE);
			serialPort.enableReceiveThreshold(1);
			serialPort.enableReceiveTimeout(gateway.getService().S.SERIAL_TIMEOUT);
			if (gateway.getService().S.SERIAL_POLLING) threadReader = new ThreadReader(gateway);
		}
		catch (NoSuchPortException e)
		{
			throw new GatewayException("The selected comm port does not exist.");
		}
		catch (PortInUseException e)
		{
			throw new GatewayException("Comm port selected is currently in use.");
		}
		catch (TooManyListenersException e)
		{
			throw new GatewayException("Too many listeners on selected comm port.");
		}
		catch (UnsupportedCommOperationException e)
		{
			throw new GatewayException("Comm parameters are not supported on this comm port");
		}
	}

	void disconnectPort() throws IOException, InterruptedException
	{
		synchronized (SYNC_Reader)
		{
			if (gateway.getService().S.SERIAL_POLLING)
			{
				if (threadReader != null) 
				{
					threadReader.interrupt();
					threadReader.join();
				}
				threadReader = null;
			}
			if (serialPort != null) serialPort.close();
			gateway.logInfo("Closing: " + comPort + " @" + baudRate);
		}
	}

	void clear() throws IOException
	{
		while (portHasData())
			read();
	}

	boolean portHasData() throws IOException
	{
		return (in.available() > 0);
	}

	public void serialEvent(SerialPortEvent event)
	{
		switch (event.getEventType())
		{
			case SerialPortEvent.BI:
				break;
			case SerialPortEvent.OE:
				gateway.logError("Overrun Error!");
				break;
			case SerialPortEvent.FE:
				gateway.logError("Framing Error!");
				break;
			case SerialPortEvent.PE:
				gateway.logError("Parity Error!");
				break;
			case SerialPortEvent.CD:
				break;
			case SerialPortEvent.CTS:
				break;
			case SerialPortEvent.DSR:
				break;
			case SerialPortEvent.RI:
				break;
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
				break;
			case SerialPortEvent.DATA_AVAILABLE:
				if (!gateway.getService().S.SERIAL_POLLING)
				{
					synchronized (SYNC_Reader)
					{
						dataReceived = true;
						SYNC_Reader.notifyAll();
					}
				}
				break;
		}
	}

	public void write(char c) throws IOException
	{
		out.write(c);
		if (!gateway.getService().S.SERIAL_NOFLUSH) out.flush();
	}

	int read() throws IOException
	{
		return in.read();
	}

	private class ThreadReader extends Thread
	{
		private AGateway gtw;

		public ThreadReader(AGateway gtw)
		{
			this.gtw = gtw;
			start();
		}

		public void run()
		{
			gtw.logDebug("ThreadReader started.");
			while (true)
			{
				try
				{
					sleep(gateway.getService().S.SERIAL_POLLING_INTERVAL);
					if (portHasData())
					{
						synchronized (SYNC_Reader)
						{
							dataReceived = true;
							SYNC_Reader.notifyAll();
						}
					}
				}
				catch (InterruptedException e)
				{
					break;
				}
				catch (Exception e)
				{
					gtw.logError("ThreadReader error. ", e);
				}
			}
			gtw.logDebug("ThreadReader stopped.");
		}
	}
}
