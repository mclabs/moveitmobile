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
import org.smslib.*;
import org.smslib.modem.*;

public abstract class AATHandler
{
	ModemGateway gateway;
	String storageLocations;
	String description;
	String[] terminators;
	String asyncEventResponse;

	public AATHandler(ModemGateway gateway)
	{
		this.gateway = gateway;
		storageLocations = "";
	}

	public String getDescription()
	{
		return description;
	}

	public String getStorageLocations()
	{
		return storageLocations;
	}

	public void setStorageLocations(String loc)
	{
		storageLocations = loc;
	}

	public String[] getTerminators()
	{
		return terminators;
	}

	public abstract void sync() throws IOException, InterruptedException;

	public abstract void reset() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract void echoOff() throws IOException, InterruptedException;

	public abstract void init() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract boolean isAlive() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getSimStatus() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract boolean enterPin(String pin) throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract boolean setVerboseErrors() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract boolean setPduProtocol() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract boolean setTextProtocol() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract boolean setIndications() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getManufacturer() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getModel() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getSerialNo() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getImsi() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getSwVersion() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getBatteryLevel() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getSignalLevel() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract boolean setStorageLocation(String mem) throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract void switchToCmdMode() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract boolean keepGsmLinkOpen() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract int sendMessage(int size, String pdu, String phone, String text) throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String listMessages(MessageClasses messageClass) throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract boolean deleteMessage(int memIndex, String memLocation) throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getGprsStatus() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String send(String s) throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getNetworkRegistration() throws GatewayException, TimeoutException, IOException;

	public abstract void readStorageLocations() throws Exception;

	public abstract AsyncEvents processUnsolicitedEvents(String response) throws IOException;

	public abstract String getAsyncEventResponse(AsyncEvents event) throws TimeoutException, GatewayException, IOException, InterruptedException;

	public static AATHandler load(ModemGateway gateway, String gsmManuf, String gsmModel) throws RuntimeException
	{
		String BASE_HANDLER = ATHandler.class.getName();
		String[] handlerClassNames = { null, null, BASE_HANDLER };
		String[] handlerDescriptions = { null, null, "Generic" };
		StringBuffer handlerClassName = new StringBuffer(BASE_HANDLER);
		if (gsmManuf != null && gsmManuf.length() != 0)
		{
			handlerClassName.append("_").append(gsmManuf);
			handlerClassNames[1] = handlerClassName.toString();
			handlerDescriptions[1] = gsmManuf + " (Generic)";
			if (gsmModel != null && gsmModel.length() != 0)
			{
				handlerClassName.append("_").append(gsmModel);
				handlerClassNames[0] = handlerClassName.toString();
				handlerDescriptions[0] = gsmManuf + " " + gsmModel;
			}
		}
		AATHandler atHandler = null;
		for (int i = 0; i < 3; ++i)
		{
			try
			{
				if (handlerClassNames[i] != null)
				{
					Class handlerClass = Class.forName(handlerClassNames[i]);
					java.lang.reflect.Constructor handlerConstructor = handlerClass.getConstructor(new Class[] { ModemGateway.class });
					atHandler = (AATHandler) handlerConstructor.newInstance(new Object[] { gateway });
					atHandler.description = handlerDescriptions[i];
					break;
				}
			}
			catch (Exception ex)
			{
				if (i == 2)
				{
					ex.printStackTrace();
					throw new RuntimeException("Class AATHandler: Cannot initialize handler!");
				}
			}
		}
		return atHandler;
	}
}
