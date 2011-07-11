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

package org.smslib.http;

import java.util.*;
import java.io.*;
import java.net.*;
import org.smslib.*;

/**
 * Gateway for Clickatell bulk operator (http://www.clickatell.com)
 * Outbound only - implements HTTP interface.
 */
public class ClickatellHTTPGateway extends HTTPGateway
{
	private String apiId, username, password;

	private String sessionId;

	private KeepAlive keepAlive;

	Object SYNC_Commander;

	public ClickatellHTTPGateway(String id, String apiId, String username, String password, Service srv)
	{
		super(id, srv);
		started = false;
		this.apiId = apiId;
		this.username = username;
		this.password = password;
		this.sessionId = null;
		this.from = "";
		SYNC_Commander = new Object();
		attributes = AGateway.GatewayAttributes.SEND | AGateway.GatewayAttributes.WAPSI | AGateway.GatewayAttributes.CUSTOMFROM | AGateway.GatewayAttributes.BIGMESSAGES | AGateway.GatewayAttributes.FLASHSMS;
	}

	public void startGateway() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		logInfo("Starting gateway.");
		connect();
		super.startGateway();
	}

	public void stopGateway() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		logInfo("Stopping gateway.");
		super.stopGateway();
		sessionId = null;
		if (keepAlive != null)
		{
			keepAlive.interrupt();
			keepAlive.join();
			keepAlive = null;
		}
	}

	public float queryBalance() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		URL url;
		List request = new ArrayList();
		List response;
		if (sessionId == null) throw new GatewayException("Internal Clickatell Gateway error.");
		url = new URL("http://api.clickatell.com/http/getbalance");
		request.add(new HttpHeader("session_id", sessionId, false));
		synchronized (SYNC_Commander)
		{
			response = HttpPost(url, request);
		}
		if (((String) response.get(0)).indexOf("Credit:") == 0) return Float.parseFloat(((String) response.get(0)).substring(((String) response.get(0)).indexOf(':') + 1));
		else return -1;
	}

	public boolean queryCoverage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		URL url;
		List request = new ArrayList();
		List response;
		if (sessionId == null) throw new GatewayException("Internal Clickatell Gateway error.");
		url = new URL("http://api.clickatell.com/utils/routeCoverage.php");
		request.add(new HttpHeader("session_id", sessionId, false));
		request.add(new HttpHeader("msisdn", msg.getRecipient().substring(1), false));
		synchronized (SYNC_Commander)
		{
			response = HttpPost(url, request);
		}
		if (((String) response.get(0)).indexOf("OK") == 0) return true;
		else return false;
	}

	public DeliveryStatuses queryMessage(String refNo) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		URL url;
		List request = new ArrayList();
		List response;
		int pos;
		if (sessionId == null) throw new GatewayException("Internal Clickatell Gateway error.");
		url = new URL("http://api.clickatell.com/http/querymsg");
		request.add(new HttpHeader("session_id", sessionId, false));
		request.add(new HttpHeader("apimsgid", refNo, false));
		synchronized (SYNC_Commander)
		{
			response = HttpPost(url, request);
		}
		pos = ((String) response.get(0)).indexOf("Status:");
		deliveryErrorCode = Integer.parseInt(((String) response.get(0)).substring(pos + 7).trim());
		switch (deliveryErrorCode)
		{
			case 1:
				return DeliveryStatuses.UNKNOWN;
			case 2:
			case 3:
			case 8:
			case 11:
				return DeliveryStatuses.KEEPTRYING;
			case 4:
				return DeliveryStatuses.DELIVERED;
			case 5:
			case 6:
			case 7:
				return DeliveryStatuses.ABORTED;
			case 9:
			case 10:
				return DeliveryStatuses.ABORTED;
			case 12:
				return DeliveryStatuses.ABORTED;
			default:
				return DeliveryStatuses.UNKNOWN;
		}
	}

	void connect() throws GatewayException, IOException
	{
		try
		{
			if (!authenticate()) throw new GatewayException("Cannot authenticate to Clickatell.");
			keepAlive = new KeepAlive();
		}
		catch (MalformedURLException e)
		{
			throw new GatewayException("Internal Clickatell Gateway error.");
		}
	}

	private boolean authenticate() throws IOException, MalformedURLException
	{
		URL url;
		List request = new ArrayList();
		List response;
		logDebug("Authenticate().");
		url = new URL("http://api.clickatell.com/http/auth");
		request.add(new HttpHeader("api_id", apiId, false));
		request.add(new HttpHeader("user", username, false));
		request.add(new HttpHeader("password", password, false));
		synchronized (SYNC_Commander)
		{
			response = HttpPost(url, request);
		}
		if (((String) response.get(0)).indexOf("ERR:") == 0)
		{
			sessionId = null;
			return false;
		}
		else
		{
			sessionId = ((String) response.get(0)).substring(4);
			return true;
		}
	}

	private boolean ping() throws IOException, MalformedURLException
	{
		URL url;
		List request = new ArrayList();
		List response;
		logDebug("Ping()");
		url = new URL("http://api.clickatell.com/http/ping");
		request.add(new HttpHeader("session_id", sessionId, false));
		synchronized (SYNC_Commander)
		{
			response = HttpPost(url, request);
		}
		if (((String) response.get(0)).indexOf("ERR:") == 0) return false;
		else return true;
	}

	public boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		URL url;
		List request = new ArrayList();
		List response;
		int requestFeatures = 0;
		boolean ok = false;
		if (sessionId == null)
		{
			logError("No session defined.");
			msg.setFailureCause(FailureCauses.GATEWAY_FAILURE);
			return false;
		}
		logDebug("sendMessage()");
		try
		{
			if (msg.getType() == MessageTypes.OUTBOUND) url = new URL("http://api.clickatell.com/http/sendmsg");
			else if (msg.getType() == MessageTypes.WAPSI) url = new URL("http://api.clickatell.com/mms/si_push");
			else
			{
				msg.setFailureCause(FailureCauses.BAD_FORMAT);
				logError("Incorrect message format.");
				return false;
			}
			request.add(new HttpHeader("session_id", sessionId, false));
			request.add(new HttpHeader("to", msg.getRecipient().substring(1), false));
			request.add(new HttpHeader("concat", "3", false));
			if (msg.getPriority() == MessagePriorities.LOW) request.add(new HttpHeader("queue", "3", false));
			else if (msg.getPriority() == MessagePriorities.NORMAL) request.add(new HttpHeader("queue", "2", false));
			else if (msg.getPriority() == MessagePriorities.HIGH) request.add(new HttpHeader("queue", "1", false));
			if (msg.getFrom() != null && msg.getFrom().length() != 0) request.add(new HttpHeader("from", msg.getFrom(), false));
			else if (from != null && from.length() != 0) request.add(new HttpHeader("from", from, false));
			if (msg.getFlashSms()) request.add(new HttpHeader("msg_type", "SMS_FLASH", false));
			
			if (msg.getType() == MessageTypes.OUTBOUND)
			{
				if (msg.getEncoding() == MessageEncodings.ENC7BIT) request.add(new HttpHeader("text", msg.getText(), false));
				else if (msg.getEncoding() == MessageEncodings.ENCUCS2)
				{
					request.add(new HttpHeader("unicode", "1", false));
					request.add(new HttpHeader("text", msg.getText(), true));
				}
			}
			else if (msg.getType() == MessageTypes.WAPSI)
			{
				request.add(new HttpHeader("si_id", msg.getId(), false));
				if (((OutboundWapSIMessage) msg).getCreateDate() != null) request.add(new HttpHeader("si_created", formatDateUTC(((OutboundWapSIMessage) msg).getCreateDate()), false));
				if (((OutboundWapSIMessage) msg).getExpireDate() != null) request.add(new HttpHeader("si_expires", formatDateUTC(((OutboundWapSIMessage) msg).getExpireDate()), false));
				request.add(new HttpHeader("si_action", formatSignal(((OutboundWapSIMessage) msg).getSignal()), false));
				request.add(new HttpHeader("si_url", ((OutboundWapSIMessage) msg).getUrl().toString(), false));
				request.add(new HttpHeader("si_text", msg.getText(), false));
			}

			if (msg.getStatusReport()) request.add(new HttpHeader("deliv_ack", "1", false));
			if ((from != null && from.length() != 0) || (msg.getFrom() != null && msg.getFrom().length() != 0)) requestFeatures += 16 + 32;
			if (msg.getFlashSms()) requestFeatures += 512;
			if (msg.getStatusReport()) requestFeatures += 8192;
			request.add(new HttpHeader("req_feat", "" + requestFeatures, false));
			synchronized (SYNC_Commander)
			{
				response = HttpPost(url, request);
			}
			if (((String) response.get(0)).indexOf("ID:") == 0)
			{
				msg.setRefNo(((String) response.get(0)).substring(4));
				msg.setDispatchDate(new Date());
				msg.setGatewayId(gtwId);
				msg.setMessageStatus(MessageStatuses.SENT);
				incOutboundMessageCount();
				ok = true;
			}
			else if (((String) response.get(0)).indexOf("ERR:") == 0)
			{
				switch (Integer.parseInt(((String) response.get(0)).substring(5, 8)))
				{
					case 1:
					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
					case 7:
						msg.setFailureCause(FailureCauses.GATEWAY_AUTH);
						break;
					case 101:
					case 102:
					case 105:
					case 106:
					case 107:
					case 112:
					case 116:
					case 120:
						msg.setFailureCause(FailureCauses.BAD_FORMAT);
						break;
					case 114:
						msg.setFailureCause(FailureCauses.NO_ROUTE);
						break;
					case 301:
					case 302:
						msg.setFailureCause(FailureCauses.NO_CREDIT);
						break;
					default:
						msg.setFailureCause(FailureCauses.UNKNOWN);
						break;
				}
				msg.setRefNo(null);
				msg.setDispatchDate(null);
				msg.setMessageStatus(MessageStatuses.FAILED);
				ok = false;
			}
		}
		catch (MalformedURLException e)
		{
			logError("Malformed URL.", e);
		}
		catch (IOException e)
		{
			logError("I/O error.", e);
		}
		return ok;
	}

	private String formatDateUTC(Date d)
	{
		String strDate = "", tmp = "";
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		strDate = String.valueOf(cal.get(Calendar.YEAR));
		tmp = String.valueOf(cal.get(Calendar.MONTH) + 1);
		if (tmp.length() != 2) tmp = "0" + tmp;
		strDate += "-" + tmp;
		tmp = String.valueOf(cal.get(Calendar.DATE));
		if (tmp.length() != 2) tmp = "0" + tmp;
		strDate += "-" + tmp;
		tmp = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
		if (tmp.length() != 2) tmp = "0" + tmp;
		strDate += "T" + tmp;
		tmp = String.valueOf(cal.get(Calendar.MINUTE));
		if (tmp.length() != 2) tmp = "0" + tmp;
		strDate += ":" + tmp;
		tmp = String.valueOf(cal.get(Calendar.SECOND));
		if (tmp.length() != 2) tmp = "0" + tmp;
		strDate += ":" + tmp + "Z";
		return strDate;
	}

	private String formatSignal(WapSISignals signal)
	{
		if (signal == WapSISignals.NONE) return "signal-none";
		else if (signal == WapSISignals.LOW) return "signal-low";
		else if (signal == WapSISignals.MEDIUM) return "signal-medium";
		else if (signal == WapSISignals.HIGH) return "signal-high";
		else if (signal == WapSISignals.DELETE) return "signal-delete";
		else return "signal-none";
	}

	private class KeepAlive extends Thread
	{
		public KeepAlive()
		{
			setPriority(MIN_PRIORITY);
			start();
		}

		public void run()
		{
			logDebug("KeepAlive thread started.");
			while (true)
			{
				try
				{
					sleep(10 * 60 * 1000);
					if (sessionId == null) break;
					logDebug("** KeepAlive START **");
					synchronized (SYNC_Commander)
					{
						ping();
					}
					logDebug("** KeepAlive END **");
				}
				catch (InterruptedException e)
				{
					if (sessionId == null) break;
				}
				catch (Exception e)
				{
				}
			}
			logDebug("KeepAlive thread ended.");
		}
	}
}
