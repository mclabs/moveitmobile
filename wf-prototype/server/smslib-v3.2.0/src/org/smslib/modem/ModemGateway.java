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

import java.util.*;
import java.util.regex.*;
import java.io.*;
import org.smslib.*;
import org.smslib.modem.athandler.*;

/**
 * Class representing GSM modems or phones. Extends AGateway with modem specific
 * operations.
 */
public class ModemGateway extends AGateway
{
	private String modemDevice;

	private int modemParms;

	private String manufacturer;

	private String model;

	private AModemDriver driver;

	private AATHandler atHandler;

	private String simPin, simPin2;

	private int outMpRefNo;

	private List mpMsgList;

	private String smscNumber;

	ModemGateway(ModemTypes type, String id, String modemDevice, int modemParms, String manufacturer, String model, Service srv)
	{
		super(id, srv);
		started = false;
		this.modemDevice = modemDevice;
		this.modemParms = modemParms;
		this.manufacturer = manufacturer;
		this.model = model;
		attributes = AGateway.GatewayAttributes.SEND | AGateway.GatewayAttributes.RECEIVE | AGateway.GatewayAttributes.BIGMESSAGES | AGateway.GatewayAttributes.WAPSI | AGateway.GatewayAttributes.PORTADDRESSING | AGateway.GatewayAttributes.FLASHSMS | AGateway.GatewayAttributes.DELIVERYREPORTS;
		if (type == ModemTypes.SERIAL) driver = new SerialModemDriver(this, this.modemDevice + ":" + this.modemParms);
		else driver = new IPModemDriver(this, this.modemDevice + ":" + this.modemParms);
		atHandler = AATHandler.load(this, this.manufacturer, this.model);
		logInfo("Using " + atHandler.getDescription() + " AT Handler");
		simPin = "";
		simPin2 = "";
		outMpRefNo = new Random().nextInt();
		if (outMpRefNo < 0) outMpRefNo *= -1;
		outMpRefNo %= 65536;
		mpMsgList = new ArrayList();
		smscNumber = "";
	}

	public void startGateway() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		logInfo("Starting gateway...");
		driver.connect();
		super.startGateway();
		logInfo("Gateway started.");
	}

	public void stopGateway() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		logInfo("Stopping gateway...");
		super.stopGateway();
		driver.disconnect();
		logInfo("Gateway stopped.");
	}

	public void readMessages(List msgList, MessageClasses msgClass) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		synchronized (driver.SYNC_Commander)
		{
			if (protocol == MessageProtocols.PDU) readMessagesPDU(msgList, msgClass, 0);
			else if (protocol == MessageProtocols.TEXT) readMessagesTEXT(msgList, msgClass, 0);
		}
	}

	public InboundMessage readMessage(String memLoc, int memIndex) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		List msgList;
		synchronized (driver.SYNC_Commander)
		{
			msgList = new ArrayList();
			readMessages(msgList, MessageClasses.ALL);
			for (int i = 0, n = msgList.size(); i < n; i++)
				if ((((InboundMessage) msgList.get(i)).getMemIndex() == memIndex) && (((InboundMessage) msgList.get(i)).getMemLocation().equalsIgnoreCase(memLoc))) return (InboundMessage) msgList.get(i);
			return null;
		}
	}

	public boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		synchronized (driver.SYNC_Commander)
		{
			if (protocol == MessageProtocols.PDU) return sendMessagePDU(msg);
			else if (protocol == MessageProtocols.TEXT) return sendMessageTEXT(msg);
			else return false;
		}
	}

	public boolean deleteMessage(InboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		synchronized (driver.SYNC_Commander)
		{
			if (msg.getMemIndex() >= 0) return deleteMessage(msg.getMemIndex(), msg.getMemLocation());
			else if ((msg.getMemIndex() == -1) && (msg.getMpMemIndex().length() != 0))
			{
				StringTokenizer tokens = new StringTokenizer(msg.getMpMemIndex(), ",");
				while (tokens.hasMoreTokens())
					deleteMessage(Integer.parseInt(tokens.nextToken()), msg.getMemLocation());
			}
			return true;
		}
	}

	private boolean deleteMessage(int memIndex, String memLocation) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		return atHandler.deleteMessage(memIndex, memLocation);
	}

	private boolean sendMessagePDU(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		int j, refNo;
		String pdu;
		boolean ok = false;
		atHandler.keepGsmLinkOpen();
		if (!msg.isBig())
		{
			pdu = msg.getPDU(smscNumber, 0, 0);
			j = pdu.length();
			j /= 2;
			if (smscNumber == null); // Do nothing on purpose!
			else if (smscNumber.length() == 0) j--;
			else
			{
				int smscNumberLen = smscNumber.length();
				if (smscNumber.charAt(0) == '+') smscNumberLen--;
				if (smscNumberLen % 2 != 0) smscNumberLen++;
				int smscLen = (2 + smscNumberLen) / 2;
				j = j - smscLen - 1;
			}
			refNo = atHandler.sendMessage(j, pdu, null, null);
			if (refNo >= 0)
			{
				msg.setGatewayId(gtwId);
				msg.setRefNo("" + refNo);
				msg.setDispatchDate(new Date());
				msg.setMessageStatus(MessageStatuses.SENT);
				incOutboundMessageCount();
				ok = true;
			}
			else
			{
				msg.setRefNo(null);
				msg.setDispatchDate(null);
				msg.setMessageStatus(MessageStatuses.FAILED);
				ok = false;
			}
		}
		else
		{
			for (int partNo = 1; partNo <= msg.getNoOfParts(); partNo++)
			{
				pdu = msg.getPDU(smscNumber, outMpRefNo, partNo);
				j = pdu.length();
				j /= 2;
				if (smscNumber == null)
				; // Do nothing on purpose!
				else if (smscNumber.length() == 0) j--;
				else
				{
					int smscNumberLen = smscNumber.length();
					if (smscNumber.charAt(0) == '+') smscNumberLen--;
					if (smscNumberLen % 2 != 0) smscNumberLen++;
					int smscLen = (2 + smscNumberLen) / 2;
					j = j - smscLen - 1;
				}
				refNo = atHandler.sendMessage(j, pdu, null, null);
				if (refNo >= 0)
				{
					msg.setGatewayId(gtwId);
					msg.setRefNo("" + refNo);
					msg.setDispatchDate(new Date());
					msg.setMessageStatus(MessageStatuses.SENT);
					incOutboundMessageCount();
					ok = true;
				}
				else
				{
					msg.setRefNo(null);
					msg.setDispatchDate(null);
					msg.setMessageStatus(MessageStatuses.FAILED);
				}
			}
			outMpRefNo = (outMpRefNo + 1) % 65536;
		}
		return ok;
	}

	private boolean sendMessageTEXT(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		// TODO: (TD) Implement Text Mode.
		return false;
	}

	private void readMessagesTEXT(List msgList, MessageClasses msgClass, int limit) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		int i, j, memIndex;
		byte[] bytes;
		String response, line, msgText, originator, dateStr, refNo;
		BufferedReader reader;
		StringTokenizer tokens;
		InboundMessage msg;
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		if (limit < 0) limit = 0;
		for (int ml = 0; ml < (atHandler.getStorageLocations().length() / 2); ml++)
		{
			if (atHandler.setStorageLocation(atHandler.getStorageLocations().substring((ml * 2), (ml * 2) + 2)))
			{
				response = atHandler.listMessages(msgClass);
				response = response.replaceAll("\\s+OK\\s+", "\nOK");
				reader = new BufferedReader(new StringReader(response));
				for (;;)
				{
					line = reader.readLine().trim();
					if (line == null) break;
					line = line.trim();
					if (line.length() > 0) break;
				}
				while (true)
				{
					if (line == null) break;
					line = line.trim();
					if (line.length() <= 0 || line.equalsIgnoreCase("OK")) break;
					i = line.indexOf(':');
					j = line.indexOf(',');
					memIndex = Integer.parseInt(line.substring(i + 1, j).trim());
					tokens = new StringTokenizer(line, ",");
					tokens.nextToken();
					tokens.nextToken();
					if (Character.isDigit(tokens.nextToken().trim().charAt(0)))
					{
						line = line.replaceAll(",,", ", ,");
						tokens = new StringTokenizer(line, ",");
						tokens.nextToken();
						tokens.nextToken();
						tokens.nextToken();
						refNo = tokens.nextToken();
						tokens.nextToken();
						dateStr = tokens.nextToken().replaceAll("\"", "");
						cal1.set(Calendar.YEAR, 2000 + Integer.parseInt(dateStr.substring(0, 2)));
						cal1.set(Calendar.MONTH, Integer.parseInt(dateStr.substring(3, 5)) - 1);
						cal1.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateStr.substring(6, 8)));
						dateStr = tokens.nextToken().replaceAll("\"", "");
						cal1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dateStr.substring(0, 2)));
						cal1.set(Calendar.MINUTE, Integer.parseInt(dateStr.substring(3, 5)));
						cal1.set(Calendar.SECOND, Integer.parseInt(dateStr.substring(6, 8)));
						dateStr = tokens.nextToken().replaceAll("\"", "");
						cal2.set(Calendar.YEAR, 2000 + Integer.parseInt(dateStr.substring(0, 2)));
						cal2.set(Calendar.MONTH, Integer.parseInt(dateStr.substring(3, 5)) - 1);
						cal2.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateStr.substring(6, 8)));
						dateStr = tokens.nextToken().replaceAll("\"", "");
						cal2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dateStr.substring(0, 2)));
						cal2.set(Calendar.MINUTE, Integer.parseInt(dateStr.substring(3, 5)));
						cal2.set(Calendar.SECOND, Integer.parseInt(dateStr.substring(6, 8)));
						msg = new StatusReportMessage(refNo, memIndex, atHandler.getStorageLocations().substring((ml * 2), (ml * 2) + 2), cal1.getTime(), cal2.getTime());
						msg.setGatewayId(gtwId);
						logDebug("IN-DTLS: MI:" + msg.getMemIndex());
						msgList.add(msg);
						incInboundMessageCount();
					}
					else
					{
						line = line.replaceAll(",,", ", ,");
						tokens = new StringTokenizer(line, ",");
						tokens.nextToken();
						tokens.nextToken();
						originator = tokens.nextToken().replaceAll("\"", "");
						tokens.nextToken();
						dateStr = tokens.nextToken().replaceAll("\"", "");
						cal1.set(Calendar.YEAR, 2000 + Integer.parseInt(dateStr.substring(0, 2)));
						cal1.set(Calendar.MONTH, Integer.parseInt(dateStr.substring(3, 5)) - 1);
						cal1.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateStr.substring(6, 8)));
						dateStr = tokens.nextToken().replaceAll("\"", "");
						cal1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dateStr.substring(0, 2)));
						cal1.set(Calendar.MINUTE, Integer.parseInt(dateStr.substring(3, 5)));
						cal1.set(Calendar.SECOND, Integer.parseInt(dateStr.substring(6, 8)));
						msgText = reader.readLine().trim();
						bytes = new byte[msgText.length() / 2];
						j = 0;
						for (i = 0; i < msgText.length(); i += 2)
						{
							bytes[j] = Byte.parseByte(msgText.substring(i, i + 2), 16);
							j++;
						}
						msgText = GSMAlphabet.bytesToString(bytes);
						msg = new InboundMessage(cal1.getTime(), originator, msgText, memIndex, atHandler.getStorageLocations().substring((ml * 2), (ml * 2) + 2));
						msg.setGatewayId(gtwId);
						logDebug("IN-DTLS: MI:" + msg.getMemIndex());
						msgList.add(msg);
						incInboundMessageCount();
					}
					line = reader.readLine().trim();
					while (line.length() == 0)
						line = reader.readLine().trim();
				}
				reader.close();
			}
		}
	}

	private void readMessagesPDU(List msgList, MessageClasses messageClass, int limit) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		int i, j, memIndex;
		String response, line, pdu;
		BufferedReader reader;
		InboundMessage mpMsg;
		if (limit < 0) limit = 0;
		mpMsg = null;
		for (int ml = 0; ml < (atHandler.getStorageLocations().length() / 2); ml++)
		{
			if (atHandler.setStorageLocation(atHandler.getStorageLocations().substring((ml * 2), (ml * 2) + 2)))
			{
				response = atHandler.listMessages(messageClass);
				response = response.replaceAll("\\s+OK\\s+", "\nOK");
				reader = new BufferedReader(new StringReader(response));
				for (;;)
				{
					line = reader.readLine().trim();
					if (line == null) break;
					line = line.trim();
					if (line.length() > 0) break;
				}
				while (true)
				{
					if (line == null) break;
					line = line.trim();
					if (line.length() <= 0 || line.equalsIgnoreCase("OK")) break;
					i = line.indexOf(':');
					j = line.indexOf(',');
					memIndex = Integer.parseInt(line.substring(i + 1, j).trim());
					pdu = reader.readLine().trim();
					try
					{
						if (isInboundMessage(pdu))
						{
							InboundMessage msg;
							msg = new InboundMessage(pdu, memIndex, atHandler.getStorageLocations().substring((ml * 2), (ml * 2) + 2));
							msg.setGatewayId(gtwId);
							logDebug("IN-DTLS: MI:" + msg.getMemIndex() + " REF:" + msg.getMpRefNo() + " MAX:" + msg.getMpMaxNo() + " SEQ:" + msg.getMpSeqNo());
							if (msg.getMpRefNo() == 0)
							{
								if (mpMsg != null) mpMsg = null;
								msgList.add(msg);
								incInboundMessageCount();
							}
							else
							{
								int k, l;
								List tmpList;
								InboundMessage listMsg;
								boolean found, duplicate;
								found = false;
								for (k = 0; k < mpMsgList.size(); k++)
								{
									tmpList = (List) mpMsgList.get(k);
									listMsg = (InboundMessage) tmpList.get(0);
									if (listMsg.getMpRefNo() == msg.getMpRefNo())
									{
										duplicate = false;
										for (l = 0; l < tmpList.size(); l++)
										{
											listMsg = (InboundMessage) tmpList.get(l);
											if (listMsg.getMpSeqNo() == msg.getMpSeqNo())
											{
												duplicate = true;
												break;
											}
										}
										if (!duplicate) tmpList.add(msg);
										found = true;
										break;
									}
								}
								if (!found)
								{
									tmpList = new ArrayList();
									tmpList.add(msg);
									mpMsgList.add(tmpList);
								}
							}
						}
						else if (isStatusReportMessage(pdu))
						{
							StatusReportMessage msg;
							msg = new StatusReportMessage(pdu, memIndex, atHandler.getStorageLocations().substring((ml * 2), (ml * 2) + 2));
							msg.setGatewayId(gtwId);
							msgList.add(msg);
							incInboundMessageCount();
						}
						else
						{
							UnknownMessage msg;
							msg = new UnknownMessage(pdu, memIndex, atHandler.getStorageLocations().substring((ml * 2), (ml * 2) + 2));
							msg.setGatewayId(gtwId);
							msgList.add(msg);
							incInboundMessageCount();
						}
					}
					catch (Exception e)
					{
						UnknownMessage msg;
						msg = new UnknownMessage(pdu, memIndex, atHandler.getStorageLocations().substring((ml * 2), (ml * 2) + 2));
						msg.setGatewayId(gtwId);
						msgList.add(msg);
						incInboundMessageCount();
						logError("Unhandled SMS in inbox, skipping...", e);
					}
					line = reader.readLine().trim();
					while (line.length() == 0)
						line = reader.readLine().trim();
					if ((limit > 0) && (msgList.size() == limit)) break;
				}
				reader.close();
			}
		}
		checkMpMsgList(msgList);
	}

	private void checkMpMsgList(List msgList)
	{
		int k, l, m;
		List tmpList;
		InboundMessage listMsg, mpMsg;
		boolean found;
		mpMsg = null;
		logDebug("CheckMpMsgList(): MAINLIST: " + mpMsgList.size());
		for (k = 0; k < mpMsgList.size(); k++)
		{
			tmpList = (List) mpMsgList.get(k);
			logDebug("CheckMpMsgList(): SUBLIST[" + k + "]: " + tmpList.size());
			listMsg = (InboundMessage) tmpList.get(0);
			found = false;
			if (listMsg.getMpMaxNo() == tmpList.size())
			{
				found = true;
				for (l = 0; l < tmpList.size(); l++)
					for (m = 0; m < tmpList.size(); m++)
					{
						listMsg = (InboundMessage) tmpList.get(m);
						if (listMsg.getMpSeqNo() == (l + 1))
						{
							if (listMsg.getMpSeqNo() == 1)
							{
								mpMsg = listMsg;
								mpMsg.setMpMemIndex(mpMsg.getMemIndex());
							}
							else
							{
								if (mpMsg != null)
								{
									mpMsg.addText(listMsg.getText());
									mpMsg.addPduUserData(listMsg.getPduUserData());
									mpMsg.setMpSeqNo(listMsg.getMpSeqNo());
									mpMsg.setMpMemIndex(listMsg.getMemIndex());
									if (listMsg.getMpSeqNo() == listMsg.getMpMaxNo())
									{
										mpMsg.setMemIndex(-1);
										msgList.add(mpMsg);
										incInboundMessageCount();
										mpMsg = null;
									}
								}
							}
							break;
						}
					}
				tmpList.clear();
				tmpList = null;
			}
			if (found)
			{
				mpMsgList.remove(k);
				k--;
			}
		}
	}

	private boolean isInboundMessage(String pdu)
	{
		int index, i;
		i = Integer.parseInt(pdu.substring(0, 2), 16);
		index = (i + 1) * 2;
		i = Integer.parseInt(pdu.substring(index, index + 2), 16);
		if ((i & 0x03) == 0) return true;
		else return false;
	}

	private boolean isStatusReportMessage(String pdu)
	{
		int index, i;
		i = Integer.parseInt(pdu.substring(0, 2), 16);
		index = (i + 1) * 2;
		i = Integer.parseInt(pdu.substring(index, index + 2), 16);
		if ((i & 0x02) == 2) return true;
		else return false;
	}

	/**
	 * Sets the SIM PIN.
	 * 
	 * @param simPin
	 *            The SIM PIN.
	 */
	public void setSimPin(String simPin)
	{
		this.simPin = simPin;
	}

	/**
	 * Sets the SIM PIN 2.
	 * 
	 * @param simPin
	 *            The SIM PIN 2.
	 */
	public void setSimPin2(String simPin)
	{
		this.simPin2 = simPin;
	}

	/**
	 * Returns the SIM PIN.
	 * 
	 * @return The SIM PIN.
	 */
	public String getSimPin()
	{
		return simPin;
	}

	/**
	 * Returns the SIM PIN 2.
	 * 
	 * @return The SIM PIN 2.
	 */
	public String getSimPin2()
	{
		return simPin2;
	}

	public AModemDriver getModemDriver()
	{
		return driver;
	}

	protected AATHandler getATHandler()
	{
		return atHandler;
	}

	/**
	 * Returns the Manufacturer string of the modem or phone.
	 * 
	 * @return The Manufacturer string.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 */
	public String getManufacturer() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		String response;
		synchronized (driver.SYNC_Commander)
		{
			response = atHandler.getManufacturer();
			if (response.indexOf("ERROR") >= 0) return "N/A";
			response = response.replaceAll("\\s+OK\\s+", "");
			return response;
		}
	}

	/**
	 * Returns the Model string.
	 * 
	 * @return The Model string.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 */
	public String getModel() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		String response;
		synchronized (driver.SYNC_Commander)
		{
			response = atHandler.getModel();
			if (response.indexOf("ERROR") >= 0) return "N/A";
			response = response.replaceAll("\\s+OK\\s+", "");
			return response;
		}
	}

	/**
	 * Returns the Serial Number of the modem.
	 * 
	 * @return The Serial Number.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 */
	public String getSerialNo() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		String response;
		synchronized (driver.SYNC_Commander)
		{
			response = atHandler.getSerialNo();
			if (response.indexOf("ERROR") >= 0) return "N/A";
			response = response.replaceAll("\\s+OK\\s+", "");
			return response;
		}
	}

	/**
	 * Returns the IMSI (International Mobile Subscriber Identity) number.
	 * <p>
	 * This number is stored in the SIM. Since this number may be used for
	 * several illegal activities, the method is remarked. If you wish to see
	 * your IMSI, just uncomment the method.
	 * 
	 * @return The IMSI.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 */
	public String getImsi() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		// return "** MASKED **";
		// IMSI is masked on purpose.
		// Uncomment following code for IMSI to be reported.
		//
		return "* MASKED *";
		/*
		 * synchronized (driver.SYNC_Commander) { String response; response =
		 * atHandler.getImsi(); if (response.indexOf("ERROR") >= 0) return
		 * "N/A"; response = response.replaceAll("\\s+OK\\s+", ""); return
		 * response; }
		 */
	}

	/**
	 * Returns the modem's firmware version.
	 * 
	 * @return The modem's firmware version.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 */
	public String getSwVersion() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		String response;
		synchronized (driver.SYNC_Commander)
		{
			response = atHandler.getSwVersion();
			if (response.indexOf("ERROR") >= 0) return "N/A";
			response = response.replaceAll("\\s+OK\\s+", "");
			return response;
		}
	}

	boolean getGprsStatus() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		synchronized (driver.SYNC_Commander)
		{
			return (atHandler.getGprsStatus().matches("\\+CGATT[\\p{ASCII}]*1\\sOK\\s"));
		}
	}

	/**
	 * Returns the battery level (0-100).
	 * 
	 * @return The battery level.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 */
	public int getBatteryLevel() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		String response;
		synchronized (driver.SYNC_Commander)
		{
			response = atHandler.getBatteryLevel();
			if (response.indexOf("ERROR") >= 0) return 0;
			Matcher m = Pattern.compile("\\+CBC: (\\d+),\\s*(\\d+)").matcher(response);
			if (m.find()) return Integer.parseInt(m.group(2));
			else return 0;
		}
	}

	/**
	 * Returns the signal level (0-100). Although the number returned is 0-100,
	 * the actual signal level is a logarithmic value.
	 * 
	 * @return The signal level.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 */
	public int getSignalLevel() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		String response;
		StringTokenizer tokens;
		synchronized (driver.SYNC_Commander)
		{
			response = atHandler.getSignalLevel();
			if (response.indexOf("ERROR") >= 0) return 0;
			response = response.replaceAll("\\s+OK\\s+", "");
			tokens = new StringTokenizer(response, ":,");
			tokens.nextToken();
			return (Integer.parseInt(tokens.nextToken().trim()) * 100 / 31);
		}
	}

	/**
	 * Returns the SMSC number used by SMSLib. If no SMSC number has been set
	 * with setSmscNumber() call, this method returns nothing.
	 * 
	 * @return The SMSC number.
	 * @see #setSmscNumber(String)
	 */
	public String getSmscNumber()
	{
		return smscNumber;
	}

	/**
	 * Sets the SMSC number used by SMSLib.
	 * <p>
	 * Note that in most cases, you will <b>not</b> need to call this method,
	 * as the modem knows the SMSC it should use by reading the SIM card. In
	 * rare cases when the modem/phone cannot read the SMSC from the SIM card or
	 * you would like to set a different SMSC than the default, you can use this
	 * method.
	 * 
	 * @param smscNumber
	 *            The SMSC number used from now on.
	 */
	public void setSmscNumber(String smscNumber)
	{
		this.smscNumber = smscNumber;
	}
}
