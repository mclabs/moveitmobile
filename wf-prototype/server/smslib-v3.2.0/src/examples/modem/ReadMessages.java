// ReadMessages.java - Sample application.
//
// This application shows you the basic procedure needed for reading
// SMS messages from your GSM modem, in synchronous mode.
//
// Operation description:
// The application setup the necessary objects and connects to the phone.
// As a first step, it reads all messages found in the phone.
// Then, it goes to sleep, allowing the asynchronous callback handlers to
// be called. Furthermore, for callback demonstration purposes, it responds
// to each received message with a "Got It!" reply.
//
// Tasks:
// 1) Setup Service object.
// 2) Setup one or more Gateway objects.
// 3) Attach Gateway objects to Service object.
// 4) Setup callback notifications.
// 5) Run

package examples.modem;

import java.util.*;
import org.smslib.*;
import org.smslib.modem.*;

public class ReadMessages
{
	private Service srv;

	public void doIt() throws Exception
	{
		List msgList;
		// Create the notification callback method for Inbound & Status Report
		// messages.
		InboundNotification inboundNotification = new InboundNotification();
		// Create the notification callback method for incoming voice calls.
		CallNotification callNotification = new CallNotification();
		try
		{
			System.out.println("Example: Read messages from a serial gsm modem.");
			System.out.println(Library.getLibraryDescription());
			System.out.println("Version: " + Library.getLibraryVersion());
			// Create new Service object - the parent of all and the main interface
			// to you.
			srv = new Service();
			// Create the Gateway representing the serial GSM modem.
			SerialModemGateway gateway = new SerialModemGateway("modem.com10", "COM10", 9600, "Nokia", "6020", srv);
			// Do we want the Gateway to be used for Inbound messages? If not,
			// SMSLib will never read messages from this Gateway.
			gateway.setInbound(true);
			// Do we want the Gateway to be used for Outbound messages? If not,
			// SMSLib will never send messages from this Gateway.
			gateway.setOutbound(true);
			gateway.setSimPin("0000");
			// Set up the notification methods.
			gateway.setInboundNotification(inboundNotification);
			//gateway.setCallNotification(callNotification);
			// Add the Gateway to the Service object.
			srv.addGateway(gateway);
			// Similarly, you may define as many Gateway objects, representing
			// various GSM modems, add them in the Service object and control all of them.
			//
			// Start! (i.e. connect to all defined Gateways)
			srv.startService();
			System.out.println();
			System.out.println("Modem Information:");
			System.out.println("  Manufacturer: " + gateway.getManufacturer());
			System.out.println("  Model: " + gateway.getModel());
			System.out.println("  Serial No: " + gateway.getSerialNo());
			System.out.println("  SIM IMSI: " + gateway.getImsi());
			System.out.println("  Signal Level: " + gateway.getSignalLevel() + "%");
			System.out.println("  Battery Level: " + gateway.getBatteryLevel() + "%");
			System.out.println();
			// Read Messages. The reading is done via the Service object and
			// affects all Gateway objects defined. This can also be more directed to a specific
			// Gateway - look the JavaDocs for information on the Service method calls.
			msgList = new ArrayList();
			srv.readMessages(msgList, MessageClasses.ALL);
			for (int i = 0; i < msgList.size(); i++)
				System.out.println(msgList.get(i));
			// Sleep now. Emulate real world situation and give a chance to the notifications
			// methods to be called in the event of message or voice call reception.
			System.out.println("Now Sleeping - Hit <enter> to terminate.");
			System.in.read();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			srv.stopService();
		}
	}

	public class InboundNotification implements IInboundMessageNotification
	{
		public void process(String gatewayId, MessageTypes msgType, String memLoc, int memIndex)
		{
			List msgList;
			if (msgType == MessageTypes.INBOUND)
			{
				System.out.println(">>> New Inbound message detected from Gateway: " + gatewayId + " : " + memLoc + " @ " + memIndex);
				try
				{
					// Read...
					msgList = new ArrayList();
					srv.readMessages(msgList, MessageClasses.UNREAD, gatewayId);
					for (int i = 0; i < msgList.size(); i++){
						System.out.println(msgList.get(i));
						srv.deleteMessage((InboundMessage)msgList.get(i));
					}
					// ...and reply.
					//for (int i = 0; i < msgList.size(); i ++)
					//{
					//	InboundMessage msg = (InboundMessage) msgList.get(i);
					//	srv.sendMessage(new OutboundMessage(msg.getOriginator(), "Got it!"), gatewayId);
					//}
				}
				catch (Exception e)
				{
					System.out.println("Oops, some bad happened...");
					e.printStackTrace();
				}
			}
			else if (msgType == MessageTypes.STATUSREPORT)
			{
				System.out.println(">>> New Status Report message detected from Gateway: " + gatewayId + " : " + memLoc + " @ " + memIndex);
			}
		}
	}

	public class CallNotification implements ICallNotification
	{
		public void process(String gatewayId, String callerId)
		{
			System.out.println(">>> New call detected from Gateway: " + gatewayId + " : " + callerId);
		}
	}

	public static void main(String args[])
	{
		ReadMessages app = new ReadMessages();
		try
		{
			app.doIt();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
