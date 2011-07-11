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

package org.smslib.test;

import java.io.*;
import java.util.*;
import org.smslib.*;

/**
 * TestGateway - virtual gateway to simulate sending messages to make testing
 * easier.
 */
public class TestGateway extends AGateway
{
	private int refCounter = 0;
	private int counter = 0;
	/**
	 * After how much sent messages next one should fail setting to 2 makes two
	 * messages sent, and then one failed.
	 */
	protected int failCycle;

	public TestGateway(String id, Service srv)
	{
		super(id, srv);
		attributes = GatewayAttributes.SEND;
		this.outbound = true;
		this.inbound = false;
	}

	public boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		// simulate delay
		Thread.sleep(500);
		counter++;
		if ((failCycle > 0) && (counter >= failCycle))
		{
			msg.setFailureCause(FailureCauses.GATEWAY_FAILURE);
			failCycle = 0;
			return false;
		}
		else
		{
			msg.setDispatchDate(new Date());
			msg.setMessageStatus(MessageStatuses.SENT);
			msg.setRefNo(Integer.toString(++refCounter));
			msg.setGatewayId(getGatewayId());
			logInfo("Send to: " + msg.getRecipient() + " via: " + msg.getGatewayId());
			return true;
		}
	}

	public int getFailCycle()
	{
		return failCycle;
	}

	/**
	 * Set fail cycle value. This is count of successfully sent messages that is
	 * followed by one failed message.
	 * 
	 * @param failCycle
	 *            Set to zero to disable failures.
	 */
	public void setFailCycle(int failCycle)
	{
		this.failCycle = failCycle;
	}
}
