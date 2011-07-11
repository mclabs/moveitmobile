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
import java.io.*;
import org.apache.commons.net.telnet.*;
import org.smslib.*;

class IPModemDriver extends AModemDriver
{
	private String ipAddress;
	private int ipPort;
	private TelnetClient tc;
	private InputStream in;
	private OutputStream out;
	private Peeker peeker;
	TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
	EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
	SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);

	IPModemDriver(ModemGateway gateway, String deviceParms)
	{
		super(gateway, deviceParms);
		StringTokenizer tokens = new StringTokenizer(deviceParms, ":");
		ipAddress = tokens.nextToken();
		ipPort = Integer.parseInt(tokens.nextToken());
		tc = null;
	}

	void connectPort() throws GatewayException, IOException, InterruptedException
	{
		try
		{
			gateway.logInfo("Opening: " + ipAddress + " @" + ipPort);
			tc = new TelnetClient();
			tc.addOptionHandler(ttopt);
			tc.addOptionHandler(echoopt);
			tc.addOptionHandler(gaopt);
			tc.connect(ipAddress, ipPort);
			in = tc.getInputStream();
			out = tc.getOutputStream();
			peeker = new Peeker();
		}
		catch (InvalidTelnetOptionException e)
		{
			throw new GatewayException("Unsupported telnet option for the selected IP connection.");
		}
	}

	void disconnectPort() throws IOException, InterruptedException
	{
		gateway.logInfo("Closing: " + ipAddress + " @" + ipPort);
		synchronized (SYNC_Reader)
		{
			if (tc != null) tc.disconnect();
			tc = null;
			peeker.interrupt();
			peeker.join();
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

	public void write(char c) throws IOException
	{
		out.write((short) c);
		out.flush();
	}

	int read() throws IOException
	{
		return in.read();
	}

	private class Peeker extends Thread
	{
		public Peeker()
		{
			setPriority(MIN_PRIORITY);
			start();
		}

		public void run()
		{
			gateway.logDebug("Peeker started.");
			while (true)
			{
				try
				{
					if (tc != null)
					{
						if (portHasData())
						{
							synchronized (SYNC_Reader)
							{
								dataReceived = true;
								SYNC_Reader.notifyAll();
							}
						}
					}
					sleep(gateway.getService().S.SERIAL_POLLING_INTERVAL);
				}
				catch (InterruptedException e)
				{
					if (tc == null) break;
				}
				catch (Exception e)
				{
				}
			}
			gateway.logDebug("Peeker stopped.");
		}
	}
}
