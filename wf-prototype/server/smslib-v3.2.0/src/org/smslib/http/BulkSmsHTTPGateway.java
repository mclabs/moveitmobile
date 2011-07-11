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
 * Gateway for BulkSMS bulk operator (http://www.bulksms.com) Outbound only -
 * implements HTTP interface.
 */
public class BulkSmsHTTPGateway extends HTTPGateway
{
	private String username, password;

	Object SYNC_Commander;

	public BulkSmsHTTPGateway(String id, String username, String password, Service srv)
	{
		super(id, srv);
		started = false;
		this.username = username;
		this.password = password;
		this.from = "";
		SYNC_Commander = new Object();
		attributes = AGateway.GatewayAttributes.SEND | AGateway.GatewayAttributes.CUSTOMFROM | AGateway.GatewayAttributes.BIGMESSAGES | AGateway.GatewayAttributes.FLASHSMS;
	}

	public void startGateway() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		logInfo("Starting gateway.");
		super.startGateway();
	}

	public void stopGateway() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		logInfo("Stopping gateway.");
		super.stopGateway();
	}

	public float queryBalance() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		URL url;
		List request = new ArrayList();
		List response;
		String reqLine;
		request.add(new HttpHeader("username", username, false));
		request.add(new HttpHeader("password", password, false));
		reqLine = "";
		for (int i = 0, n = request.size(); i < n; i++)
		{
			if (i != 0) reqLine += "&";
			reqLine += ((HttpHeader) request.get(i)).key + "=" + ((HttpHeader) request.get(i)).value;
		}
		url = new URL("http://bulksms.vsms.net/eapi/user/get_credits/1/1.1" + "?" + reqLine);
		synchronized (SYNC_Commander)
		{
			response = HttpGet(url);
		}
		if (((String) response.get(0)).charAt(0) == '0') return Float.parseFloat(((String) response.get(0)).substring(((String) response.get(0)).indexOf('|') + 1));
		else return -1;
	}

	public DeliveryStatuses queryMessage(String refNo) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		URL url;
		List request = new ArrayList();
		List response;
		String reqLine;
		request.add(new HttpHeader("username", username, false));
		request.add(new HttpHeader("password", password, false));
		request.add(new HttpHeader("batch_id", refNo, false));
		reqLine = "";
		for (int i = 0, n = request.size(); i < n; i++)
		{
			if (i != 0) reqLine += "&";
			reqLine += ((HttpHeader) request.get(i)).key + "=" + ((HttpHeader) request.get(i)).value;
		}
		url = new URL("http://bulksms.vsms.net/eapi/status_reports/get_report/2/2.0" + "?" + reqLine);
		synchronized (SYNC_Commander)
		{
			response = HttpGet(url);
		}
		if (((String) response.get(0)).indexOf("0|Results to follow") == 0)
		{
			StringTokenizer tokens = new StringTokenizer((String) response.get(2), ":");
			tokens.nextToken();
			deliveryErrorCode = Integer.parseInt(tokens.nextToken());
			switch (deliveryErrorCode)
			{
				case 11:
					return DeliveryStatuses.DELIVERED;
				case 0:
				case 10:
				case 12:
				case 63:
				case 64:
					return DeliveryStatuses.KEEPTRYING;
				default:
					return DeliveryStatuses.ABORTED;
			}
		}
		else return DeliveryStatuses.UNKNOWN;
	}

	public boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		URL url = null;
		List request = new ArrayList();
		List response;
		String reqLine;
		boolean ok = false;
		request.add(new HttpHeader("username", username, false));
		request.add(new HttpHeader("password", password, false));
		request.add(new HttpHeader("message", msg.getText(), false));
		request.add(new HttpHeader("msisdn", msg.getRecipient().substring(1), false));
		request.add(new HttpHeader("allow_concat_text_sms", "1", false));
		if (msg.getStatusReport()) request.add(new HttpHeader("want_report", "1", false));
		if (msg.getFlashSms()) request.add(new HttpHeader("msg_class", "0", false));
		if (msg.getFrom() != null && msg.getFrom().length() != 0) request.add(new HttpHeader("source_id", msg.getFrom(), false));
		else if (from != null && from.length() != 0) request.add(new HttpHeader("source_id", from, false));
		reqLine = "";
		for (int i = 0, n = request.size(); i < n; i++)
		{
			if (i != 0) reqLine += "&";
			reqLine += ((HttpHeader) request.get(i)).key + "=" + ((HttpHeader) request.get(i)).value;
		}
		url = new URL("http://bulksms.vsms.net/eapi/submission/send_sms/2/2.0" + "?" + reqLine);
		synchronized (SYNC_Commander)
		{
			response = HttpGet(url);
		}
		if (((String) response.get(0)).charAt(0) == '0')
		{
			StringTokenizer tokens = new StringTokenizer((String) response.get(0), "|");
			tokens.nextToken();
			tokens.nextToken();
			msg.setRefNo(tokens.nextToken());
			msg.setDispatchDate(new Date());
			msg.setGatewayId(gtwId);
			msg.setMessageStatus(MessageStatuses.SENT);
			incOutboundMessageCount();
			ok = true;
		}
		else
		{
			StringTokenizer tokens = new StringTokenizer((String) response.get(0), "|");
			switch (Integer.parseInt(tokens.nextToken()))
			{
				case 22:
					msg.setFailureCause(FailureCauses.GATEWAY_FAILURE);
					break;
				case 23:
					msg.setFailureCause(FailureCauses.GATEWAY_AUTH);
					break;
				case 24:
					msg.setFailureCause(FailureCauses.BAD_FORMAT);
					break;
				case 25:
				case 26:
				case 27:
				case 28:
					msg.setFailureCause(FailureCauses.NO_CREDIT);
					break;
				case 40:
					msg.setFailureCause(FailureCauses.GATEWAY_FAILURE);
					break;
			}
			msg.setRefNo(null);
			msg.setDispatchDate(null);
			msg.setMessageStatus(MessageStatuses.FAILED);
			ok = false;
		}
		return ok;
	}
}
