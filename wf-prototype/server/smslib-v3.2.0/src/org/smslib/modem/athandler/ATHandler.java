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

package org.smslib.modem.athandler;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import org.smslib.*;
import org.smslib.modem.*;

public class ATHandler extends AATHandler
{
	protected AModemDriver modemDriver;

	public ATHandler(ModemGateway gateway)
	{
		super(gateway);
		modemDriver = gateway.getModemDriver();
		terminators = new String[13];
		terminators[0] = "OK\\s";
		terminators[1] = "\\s*[\\p{ASCII}]*\\s+OK\\s";
		terminators[2] = "ERROR\\s";
		terminators[3] = "ERROR:\\s*\\d+\\s";
		terminators[4] = "\\+CM[ES]\\s+ERROR:\\s*\\d+\\s";
		terminators[5] = "\\+CPIN:\\s*READY\\s";
		terminators[6] = "\\+CPIN:\\s*SIM\\s*BUSY\\s";
		terminators[7] = "\\+CPIN:\\s*SIM\\s*PIN\\s";
		terminators[8] = "\\+CPIN:\\s*SIM\\s*PIN2\\s";
		terminators[9] = "\\+CMTI:\\s*\\p{Punct}[\\p{ASCII}]+\\p{Punct}\\p{Punct}\\s*\\d+\\s";
		terminators[10] = "\\+CDSI:\\s*\\p{Punct}[\\p{ASCII}]+\\p{Punct}\\p{Punct}\\s*\\d+\\s";
		terminators[11] = "RING\\s";
		terminators[12] = "\\+CLIP:\\s*\\p{Punct}[\\p{ASCII}]*\\p{Punct}\\p{Punct}\\s*\\d+\\s";
	}

	public void sync() throws IOException, InterruptedException
	{
		modemDriver.write("ATZ\r");
		Thread.sleep(gateway.getService().S.AT_WAIT);
	}

	public void reset() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		modemDriver.write("\u001b");
	}

	public void echoOff() throws IOException, InterruptedException
	{
		modemDriver.write("ATE0\r");
		Thread.sleep(gateway.getService().S.AT_WAIT);
		modemDriver.clearBuffer();
	}

	public void init() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		modemDriver.write("AT+CLIP=1\r");
		modemDriver.getResponse();
		modemDriver.write("AT+COPS=0\r");
		modemDriver.getResponse();
	}

	public boolean isAlive() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		modemDriver.write("AT\r");
		modemDriver.getResponse();
		return (modemDriver.isOk());
	}

	public String getSimStatus() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		modemDriver.write("AT+CPIN?\r");
		return (modemDriver.getResponse());
	}

	public boolean enterPin(String pin) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		modemDriver.write("AT+CPIN=\"_1_\"\r".replaceAll("_1_", pin));
		modemDriver.getResponse();
		return (modemDriver.isOk());
	}

	public boolean setVerboseErrors() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		modemDriver.write("AT+CMEE=1\r");
		modemDriver.getResponse();
		return (modemDriver.isOk());
	}

	public boolean setPduProtocol() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		modemDriver.write("AT+CMGF=0\r");
		modemDriver.getResponse();
		return (modemDriver.isOk());
	}

	public boolean setTextProtocol() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		modemDriver.write("AT+CMGF=1\r");
		modemDriver.getResponse();
		if (modemDriver.isOk())
		{
			modemDriver.write("AT+CSCS=\"HEX\"\r");
			modemDriver.getResponse();
			return (modemDriver.isOk());
		}
		else return false;
	}

	public boolean setIndications() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		while (true)
		{
			modemDriver.write("AT+CNMI=?\r");
			try
			{
				CNMIDetector c = new CNMIDetector(modemDriver.getResponse(), gateway);
				modemDriver.write(c.getATCommand());
				modemDriver.getResponse();
				return (modemDriver.isOk());
			}
			catch (Exception e)
			{
				gateway.logWarn("Retrying the detection of CNMI, modem busy?", e);
				Thread.sleep(gateway.getService().S.AT_WAIT_CNMI);
			}
		}
	}

	public String getManufacturer() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		modemDriver.write("AT+CGMI\r");
		return (modemDriver.getResponse());
	}

	public String getModel() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		modemDriver.write("AT+CGMM\r");
		return (modemDriver.getResponse());
	}

	public String getSerialNo() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		modemDriver.write("AT+CGSN\r");
		return (modemDriver.getResponse());
	}

	public String getImsi() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		modemDriver.write("AT+CIMI\r");
		return (modemDriver.getResponse());
	}

	public String getSwVersion() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		modemDriver.write("AT+CGMR\r");
		return (modemDriver.getResponse());
	}

	public String getBatteryLevel() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		modemDriver.write("AT+CBC\r");
		return (modemDriver.getResponse());
	}

	public String getSignalLevel() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		modemDriver.write("AT+CSQ\r");
		return (modemDriver.getResponse());
	}

	public boolean setStorageLocation(String mem) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (mem.equalsIgnoreCase("--")) return true;
		else
		{
			modemDriver.write("AT+CPMS=\"" + mem + "\"\r");
			modemDriver.getResponse();
			return (modemDriver.isOk());
		}
	}

	public void switchToCmdMode() throws IOException
	{
		modemDriver.write("+++");
		java.util.Date start = new java.util.Date();
		while (new java.util.Date().getTime() - start.getTime() <= gateway.getService().S.AT_WAIT_CMD)
			try { Thread.sleep(100); } catch (InterruptedException e) {}
	}

	public boolean keepGsmLinkOpen() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		modemDriver.write("AT+CMMS=1\r");
		modemDriver.getResponse();
		return (modemDriver.isOk());
	}

	public int sendMessage(int size, String pdu, String phone, String text) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		int responseRetries, errorRetries;
		String response;
		int refNo = -1;

		if (gateway.getProtocol() == MessageProtocols.PDU)
		{
			errorRetries = 0;
			while (true)
			{
				responseRetries = 0;
				modemDriver.write("AT+CMGS=\"_1_\"\r".replaceAll("\"_1_\"", "" + size));
				Thread.sleep(gateway.getService().S.AT_WAIT_CGMS);
				while (!modemDriver.dataAvailable())
				{
					responseRetries++;
					if (responseRetries == gateway.getService().S.OUTBOUND_RETRIES) throw new GatewayException("Gateway is not responding, max number of retries reached.");
					gateway.logWarn("ATHandler().SendMessage(): Still waiting for response (I) (" + responseRetries + ")...");
					Thread.sleep(gateway.getService().S.OUTBOUND_RETRY_WAIT);
				}
				responseRetries = 0;
				modemDriver.clearBuffer();
				modemDriver.write(pdu);
				modemDriver.write((char) 26);
				response = modemDriver.getResponse();
				while (response.length() == 0)
				{
					responseRetries++;
					if (responseRetries == gateway.getService().S.OUTBOUND_RETRIES) throw new GatewayException("Gateway is not responding, max number of retries reached.");
					gateway.logWarn("ATHandler().SendMessage(): Still waiting for response (II) (" + responseRetries + ")...");
					Thread.sleep(gateway.getService().S.OUTBOUND_RETRY_WAIT);
					response = modemDriver.getResponse();
				}
				if (modemDriver.getLastError() == 0)
				{
					Matcher m = Pattern.compile("\\s*\\+CMGS: (\\d+)").matcher(response);
					if (m.find())
					{
						refNo = Integer.parseInt(m.group(1));
					}
					else
					{
						// Message-Reference ID not returned
						refNo = -1;
					}
					break;
				}
				else if (modemDriver.getLastError() > 0)
				{
					// CMS or CME error could happen here
					errorRetries++;
					if (errorRetries == gateway.getService().S.OUTBOUND_RETRIES)
					{
						gateway.logError(modemDriver.getLastErrorText() + ": Quit retrying, message lost...");
						refNo = -1;
						break;
					}
					else
					{
						gateway.logWarn(modemDriver.getLastErrorText() + ": Retrying...");
						Thread.sleep(gateway.getService().S.OUTBOUND_RETRY_WAIT);
					}
				}
				else refNo = -1;
			}
		}
		else if (gateway.getProtocol() == MessageProtocols.TEXT)
		{
			modemDriver.write("AT+CMGS=\"_1_\"\r".replaceAll("_1_", phone));
			modemDriver.clearBuffer();
			modemDriver.write(text);
			Thread.sleep(gateway.getService().S.AT_WAIT_CGMS);
			modemDriver.write((char) 26);
			response = modemDriver.getResponse();
			if (response.indexOf("OK\r") >= 0)
			{
				int i;
				String tmp = "";
				i = response.indexOf(":");
				while (!Character.isDigit(response.charAt(i)))
					i++;
				while (Character.isDigit(response.charAt(i)))
				{
					tmp += response.charAt(i);
					i++;
				}
				refNo = Integer.parseInt(tmp);
			}
			else refNo = -1;
		}
		return refNo;
	}

	public String listMessages(MessageClasses messageClass) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (gateway.getProtocol() == MessageProtocols.PDU)
		{
			if (messageClass == MessageClasses.ALL) modemDriver.write("AT+CMGL=4\r");
			else if (messageClass == MessageClasses.UNREAD) modemDriver.write("AT+CMGL=0\r");
			else if (messageClass == MessageClasses.READ) modemDriver.write("AT+CMGL=1\r");
		}
		else if (gateway.getProtocol() == MessageProtocols.TEXT)
		{
			if (messageClass == MessageClasses.ALL) modemDriver.write("AT+CMGL=\"ALL\"\r");
			else if (messageClass == MessageClasses.UNREAD) modemDriver.write("AT+CMGL=\"REC UNREAD\"\r");
			else if (messageClass == MessageClasses.READ) modemDriver.write("AT+CMGL=\"REC READ\"\r");
		}
		return modemDriver.getResponse();
	}

	public boolean deleteMessage(int memIndex, String memLocation)throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (!setStorageLocation(memLocation)) return false;
		modemDriver.write("AT+CMGD=_1_\r".replaceAll("_1_", "" + memIndex));
		modemDriver.getResponse();
		return (modemDriver.isOk());
	}

	public String getGprsStatus() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		modemDriver.write("AT+CGATT?\r");
		return (modemDriver.getResponse());
	}

	public String send(String s) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		modemDriver.write(s);
		return (modemDriver.getResponse());
	}

	public String getNetworkRegistration() throws GatewayException, TimeoutException, IOException
	{
		modemDriver.write("AT+CREG?\r");
		return (modemDriver.getResponse());
	}

	public void readStorageLocations() throws Exception
	{
		String response, loc;
		StringTokenizer tokens;
		modemDriver.write("AT+CPMS?\r");
		response = modemDriver.getResponse();
		if (response.indexOf("+CPMS:") >= 0)
		{
			response = response.replaceAll("\\s*\\+CPMS:\\s*", "");
			tokens = new StringTokenizer(response, ",");
			while (tokens.hasMoreTokens())
			{
				loc = tokens.nextToken().replaceAll("\"", "");
				if (storageLocations.indexOf(loc) < 0) storageLocations += loc;
				tokens.nextToken();
				tokens.nextToken();
			}
		}
	}

	public AsyncEvents processUnsolicitedEvents(String response) throws IOException
	{
		AsyncEvents event = AsyncEvents.NOTHING;

		if (response.indexOf("+CMTI") >= 0) event = AsyncEvents.INBOUNDMESSAGE;
		else if (response.indexOf("+CDSI") >= 0) event = AsyncEvents.INBOUNDSTATUSREPORTMESSAGE;
		else if (response.indexOf("RING") >= 0) event = AsyncEvents.NOTHING;
		else if (response.indexOf("+CLIP") >= 0) event = AsyncEvents.INBOUNDCALL;
		if (event != AsyncEvents.NOTHING) asyncEventResponse = response;
		return event;
	}

	public String getAsyncEventResponse(AsyncEvents event) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		String response = "";

		if (event == AsyncEvents.INBOUNDMESSAGE)
		{
			gateway.logDebug("Inbound message detected!");
			response = asyncEventResponse;
		}
		else if (event == AsyncEvents.INBOUNDSTATUSREPORTMESSAGE)
		{
			gateway.logDebug("Inbound status report message detected!");
			response = asyncEventResponse;
		}
		else if (event == AsyncEvents.INBOUNDCALL)
		{
			gateway.logDebug("Inbound call detected!");
			response = asyncEventResponse;
			try
			{
				switchToCmdMode();
			}
			catch (Exception e)
			{
			}
			gateway.getModemDriver().write("ATH\r");
			gateway.getModemDriver().getResponse();
		}
		return response;
	}
}
