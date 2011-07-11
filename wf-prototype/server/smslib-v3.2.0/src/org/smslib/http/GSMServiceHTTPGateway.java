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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.smslib.*;

/**
 * Gateway for GSMService bulk operator (http://www.gsmservice.pl)
 * Outbound only - implements HTTP interface.
 * It may be useful only for Polish users.
 * 
 * @author Tomek Cejner
 */
public class GSMServiceHTTPGateway extends HTTPGateway
{
	private String username, password;
	private Object sync;

	public GSMServiceHTTPGateway(String id, String username, String password, Service srv)
	{
		super(id, srv);
		this.username = username;
		this.password = password;
		// sending only
		attributes = AGateway.GatewayAttributes.SEND | AGateway.GatewayAttributes.CUSTOMFROM;
		sync = new Object();
	}

	public boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		List response;
		URL url = null;
		boolean ok = false;
		try
		{
			String file = "/api/send.php?login=" + username + "&pass=" + password + "&showid=1&rodzaj=1&podpis=" + msg.getFrom() + "&numer=" + msg.getRecipient().substring(1) + "&tekst=" + URLEncoder.encode(msg.getText(), "ISO-8859-1") + ""; 
			if (msg.getFrom() != null && msg.getFrom().length() != 0)
			{
				file.concat("&podpis=" + URLEncoder.encode(msg.getFrom(), "ISO-8859-1"));
			}
			url = new URL("http", "bramka.gsmservice.pl", file);
			synchronized (sync)
			{
				response = HttpGet(url);
			}
			String first = (String) response.get(0);
			// Valid response is one line, and should match following regex:
			// Status:(\d+)(\|(\d+))?
			//
			// Where matches:
			// [1] = status code
			// [3] = message id (may be null if url parameter "showid" is set to
			// 0 or omitted)
			Matcher m = Pattern.compile("Status:(\\d+)(\\|(\\d+))?").matcher(first);
			if (m.matches())
			{
				msg.setRefNo(m.group(3));
				msg.setDispatchDate(new Date());
				int stat = Integer.parseInt(m.group(1));
				switch (stat)
				{
					case 3: // message sent
					case 10: // message in queue
					case 13: // message delivered
						msg.setMessageStatus(MessageStatuses.SENT);
						msg.setDispatchDate(new Date());
						msg.setGatewayId(gtwId);
						incOutboundMessageCount();
						ok = true;
						break;
					default:
						msg.setMessageStatus(MessageStatuses.FAILED);
						break;
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
}
