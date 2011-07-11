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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import org.smslib.*;

/**
* Gateway for GSMService bulk operator (http://www.comvision.pl/)
* Outbound only - implements HTTP interface.
* It may be useful only for Polish users.
* 
* @author Tomek Cejner
*/
public class ComVisionHTTPGateway extends HTTPGateway
{
	private String username;
	private String password;
	private Object sync;

	public ComVisionHTTPGateway(String id, String username, String password, Service srv)
	{
		super(id, srv);
		this.username = username;
		this.password = calculateMD5(password);
		// sending only
		attributes = AGateway.GatewayAttributes.SEND | AGateway.GatewayAttributes.CUSTOMFROM | AGateway.GatewayAttributes.DELIVERYREPORTS;
		sync = new Object();
	}

	public boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		List response;
		URL url = null;
		boolean ok = false;
		try
		{
			String file = "/send.do?username=" + username + "&password=" + password + "&to=" + msg.getRecipient().substring(1) + "&message=" + URLEncoder.encode(msg.getText(), "ISO-8859-1");
			if (msg.getFrom() != null)
			{
				file.concat("&from=" + URLEncoder.encode(msg.getFrom(), "ISO-8859-1"));
			}
			url = new URL("http", "api.smsapi.pl", file);
			synchronized (sync)
			{
				response = HttpGet(url);
			}
			String first = (String) response.get(0);
			// Response
			Matcher m = Pattern.compile("(OK|ERROR):(\\d+)(:(\\d+))?").matcher(first);
			if (m.matches())
			{
				String stat = m.group(1);
				if (stat.equals("OK"))
				{
					msg.setRefNo(m.group(2));
					msg.setMessageStatus(MessageStatuses.SENT);
					msg.setDispatchDate(new Date());
					msg.setGatewayId(gtwId);
					incOutboundMessageCount();
					ok = true;
				}
				else if (stat.equals("ERROR"))
				{
					FailureCauses c = FailureCauses.UNKNOWN;
					int err = Integer.parseInt(m.group(2));
					switch (err)
					{
						case 11:
						case 12:
						case 14:
						case 300:
							c = FailureCauses.BAD_FORMAT;
							break;
						case 13:
							c = FailureCauses.BAD_NUMBER;
							break;
						case 15:
						case 16:
						case 101:
						case 102:
							c = FailureCauses.GATEWAY_AUTH;
							break;
						case 103:
							c = FailureCauses.NO_CREDIT;
							break;
						case 200:
							c = FailureCauses.GATEWAY_FAILURE;
							break;
					}
					msg.setFailureCause(c);
					msg.setMessageStatus(MessageStatuses.FAILED);
					ok = false;
				}
			}
			else
			{
				logError("Invalid response from provider.");
			}
		}
		catch (MalformedURLException e)
		{
			logError("Malformed URL.", e);
		}
		catch (IOException e)
		{
			logError("I/O Error.", e);
		}
		return ok;
	}

	public float queryBalance() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		List response;
		URL url = null;
		String file = "/send.do?username=" + username + "&password=" + password + "&points=1";
		url = new URL("http", "api.smsapi.pl", file);
		response = HttpGet(url);
		String first = (String) response.get(0);
		Matcher m = Pattern.compile("(Points|ERROR): ?(\\d+)").matcher(first);
		if (m.matches())
		{
			String stat = m.group(1);
			if (stat.equals("Points"))
			{
				return Float.parseFloat(m.group(2));
			}
			else
			{
				return -1;
			}
		}
		return -1;
	}

	public DeliveryStatuses queryMessage(String refNo) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		DeliveryStatuses del = DeliveryStatuses.UNKNOWN;
		List response;
		URL url = null;
		String file = "/send.do?username=" + username + "&password=" + password + "&status=" + refNo;
		url = new URL("http", "api.smsapi.pl", file);
		response = HttpGet(url);
		String first = (String) response.get(0);
		Matcher m = Pattern.compile("(OK|ERROR):(\\d+)").matcher(first);
		if (m.matches())
		{
			String stat = m.group(1);
			if (stat.equals("OK"))
			{
				int code = Integer.parseInt(m.group(2));
				switch (code)
				{
					case 405:
					case 406:
						del = DeliveryStatuses.ABORTED;
						break;
					case 404:
						del = DeliveryStatuses.DELIVERED;
						break;
					case 402:
					case 403:
						del = DeliveryStatuses.KEEPTRYING;
						break;
					case 401:
						del = DeliveryStatuses.UNKNOWN;
						break;
				}
			}
		}
		return del;
	}
}
