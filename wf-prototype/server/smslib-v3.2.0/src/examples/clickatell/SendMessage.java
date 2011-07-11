// SendMessage.java - Sample application.
//
// This application shows you the basic procedure for sending messages.
// You will find how to send synchronous and asynchronous messages.
//
// For asynchronous dispatch, the example application sets a callback
// notification, to see what's happened with messages.
//
// Bulk Operator used: Clickatell (http://www.clickatell.com)
// Please look the ClickatellHTTPGateway documentation for details.

package examples.clickatell;

import org.smslib.*;
import org.smslib.http.*;

public class SendMessage
{
	public void doIt() throws Exception
	{
		Service srv;
		OutboundMessage msg;
		OutboundNotification outboundNotification = new OutboundNotification();

		System.out.println("Example: Send message from Clickatell HTTP Interface.");
		System.out.println(Library.getLibraryDescription());
		System.out.println("Version: " + Library.getLibraryVersion());

		srv = new Service();
		ClickatellHTTPGateway gateway = new ClickatellHTTPGateway("clickatell.http.1", "api_id", "username", "password", srv);
		gateway.setOutbound(true);
		gateway.setOutboundNotification(outboundNotification);
		srv.addGateway(gateway);

		srv.startService();

		// Send a message synchronously.
		msg = new OutboundMessage("+30...", "Hello World!");
		msg.setFrom("CUSTOMFROM");

		// Ask for coverage.
		System.out.println("Is recipient's network covered? : " + gateway.queryCoverage(msg));

		srv.sendMessage(msg);
		System.out.println(msg);

		// Now query the service to find out our credit balance.
		System.out.println("Remaining credit: " + gateway.queryBalance());

		// Send some messages in async mode...
		// After this, your IOutboundMessageNotification method will be called
		// for each message sent out.
		//msg = new OutboundMessage("+30...", "Max");
		//srv.queueMessage(msg, "clickatell.http.1", AbstractGateway.Priority.HIGH);
		//msg = new OutboundMessage("+30...", "Min");
		//srv.queueMessage(msg, "clickatell.http.1", AbstractGateway.Priority.LOW);

		System.out.println("Now Sleeping - Hit <enter> to terminate.");
		System.in.read();

		srv.stopService();
	}

	public class OutboundNotification implements IOutboundMessageNotification
	{
		public void process(String gatewayId, OutboundMessage msg)
		{
			System.out.println("Outbound handler called from Gateway: " + gatewayId);
			System.out.println(msg);
		}
	}

	public static void main(String args[])
	{
		SendMessage app = new SendMessage();
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
