// SendMessage.java - Sample application.
//
// This application shows you the basic procedure for sending messages.
// You will find how to send synchronous and asynchronous messages.
//
// For asynchronous dispatch, the example application sets a callback
// notification, to see what's happened with messages.
//
// Bulk Operator used: BULKSMS (http://www.bulksms.com)
// Please look the BulkSmsHTTPGateway documentation for details.

package examples.bulksms;

import org.smslib.*;
import org.smslib.http.*;

public class SendMessage
{
	public void doIt() throws Exception
	{
		Service srv;
		OutboundMessage msg;

		System.out.println("Example: Send message from BulkSMS HTTP Interface.");
		System.out.println(Library.getLibraryDescription());
		System.out.println("Version: " + Library.getLibraryVersion());

		srv = new Service();
		BulkSmsHTTPGateway gateway = new BulkSmsHTTPGateway("bulksms.http.1", "username", "password", srv);
		gateway.setOutbound(true);
		srv.addGateway(gateway);

		srv.startService();

		// Query the service to find out our credit balance.
		System.out.println("Remaining credit: " + gateway.queryBalance());

		// Send a message synchronously.
		msg = new OutboundMessage("+30...", "Hello World!");
		srv.sendMessage(msg);
		System.out.println(msg);

		System.out.println("Now Sleeping - Hit <enter> to terminate.");
		System.in.read();

		srv.stopService();
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
