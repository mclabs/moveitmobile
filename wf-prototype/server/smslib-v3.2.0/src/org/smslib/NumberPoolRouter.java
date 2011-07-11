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

package org.smslib;

import java.util.*;

public class NumberPoolRouter extends Router
{
	/**
	 * Map number prefixes to gateways string identifiers
	 */
	protected Map assignments;

	public NumberPoolRouter(Service service)
	{
		super(service);
		assignments = new HashMap();
	}

	/**
	 * Create assignment of recipient number prefix to gateway.
	 * 
	 * @param prefix
	 *            Phone number prefix
	 * @param gw
	 *            Gateway to be assigned.
	 */
	public void assign(String prefix, AGateway gw)
	{
		assignments.put(prefix, gw.getGatewayId());
	}

	public void customRouting(OutboundMessage msg)
	{
		// iterate over all defined prefixes when message recipient does not match,
		// remove gateway from candidate list
		Iterator it = assignments.keySet().iterator();
		while (it.hasNext())
		{
			String prefix = (String) it.next();
			if (msg.getRecipient().startsWith(prefix))
				allowed.add(service.findGateway((String) assignments.get(prefix)));
		}
	}
}
