package org.openxdata.communication.sms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.fcitmuk.communication.sms.util.SmsAssembler;
import org.smslib.IInboundMessageNotification;
import org.smslib.IOutboundMessageNotification;
import org.smslib.InboundMessage;
import org.smslib.MessageClasses;
import org.smslib.MessageEncodings;
import org.smslib.MessageTypes;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.modem.SerialModemGateway;


/**
 * For development: If you get the error like selected com port does not exist, ensure that you do the following:
 * 
 * The file javax.comm.properties should be in: JDKPATH\jre\lib
 * The file comm.jar should be in JDKPATH\jre\lib\ext
 * The file win32com.dll should be in JDKPATH\bin
 * 
 * If after setting all the above, u still get the same error message, check to confirm that
 * your IDE uses the same JDK as specified in the above JDKPATH. For example, eclipse
 * has a tendency of sometimes using the JRE by default. Or the IDE may be using a 
 * different version of the JDK from the one you have set above (e.g using 1.6 when you
 * have set 1.5)
 * 
 * For runtime installation:
 * 
 * The file win32com.dll should be in JREPATH\bin  {if missing, u may get the error: "Error loading win32com: java.lang.UnsatisfiedLinkError: no win32com in java.library.path"}
 * The file javax.comm.properties should be in JREPATH\lib  {if missing, you will not find port. }
 * The file comm.jar should be in JREPATH\lib\ext
 * 
 * The file win32com.dll should be in ..\windows\system32 if u get "Error loading win32com: java.lang.UnsatisfiedLinkError: no win32com in java.library.path"
 * If you are running this SMS server in a container line Tomcat, you may need to restart it.
 * 
 * If your modem is on a phone whose inbox has many messages, you may need to delete them in order
 * for the server to receive more messages.
 * 
 * As for now, we have no support for receiving concatenated messages (more than 160 chars). This is work in progress.
 * 
 * When a call is received during sending, it may fail and make some retries, which if fail, it
 * attempts to restart the modem. The restart has so far not worked properly. So i advise you to do a call
 * divert on the modem atleast for the period of sending the SMS es.
 * 
 * @author Daniel Kayiwa
 *
 */
public class SMSServer implements Runnable,IOutboundMessageNotification,IInboundMessageNotification{

	/** The size of a message part. */
	private static final int MSG_PART_SIZE = 1000;

	private static final int LEN_BYTE_SIZE = 4;

	/** Creates notifier and accepts clients to be processed. */
	private Thread accepterThread;

	/** Process the particular client from queue. */
	private ClientProcessor processor;

	/** The sms service. */
	private Service srv;

	/** The unique identifier of the modem. */
	private String id;

	/** The comm port that the modem is connected to. */
	private String comPort;

	/** The port to send the message to. */
	private int msgDstPort;

	/** The port to send the message from. */
	private int msgSrcPort;

	/** The manufacturer of the modem. */
	private String manufacturer;

	/** The model of the modem. */
	private String model;

	/** The baud rate of the serial connection.*/
	private int baudRate;

	/** The sms. */
	private SMSServerListener eventListener;

	/** Becomes 'true' when this component is finalized. */
	private boolean isClosed;

	/** Flag determining whether to use polling for inbound messages.
	 * This is useful for modems like HUAWEI E220 whose servce indications
	 * do not work for inbound message notifications.
	 */
	private boolean useInboundPolling = false;

	/** The sleep interval for each polling for inbound messages. */
	private int inboundPollingInterval = 30000;


	/** 
	 * Constructs a new SMS Server. 
	 * 
	 * @param id
	 *            Your own ID for addressing this gateway.
	 * @param comPort
	 *            The comm port to which this modem is connected. For example,
	 *            COM1 or /dev/ttyS1.
	 * @param baudRate
	 *            The baud rate of the serial connection.
	 * @param manufacturer
	 *            The manufacturer, for example "Nokia".
	 * @param model
	 *            The model, for example "6130"
	 */
	public SMSServer(String id, String comPort, int msgSrcPort, int msgDstPort,int baudRate, String manufacturer, String model, boolean useInboundPolling, int inboundPollingInterval, SMSServerListener eventListener){
		this.id = id;
		this.comPort = comPort;
		this.msgSrcPort = msgSrcPort;
		this.msgDstPort = msgDstPort;
		this.baudRate = baudRate;
		this.manufacturer = manufacturer;
		this.model = model;
		this.useInboundPolling = useInboundPolling;
		this.inboundPollingInterval = inboundPollingInterval;
		this.eventListener = eventListener;
	}

	/**
	 * Starts running server for a serially connected gsm modem or phone.
	 **/ 
	public void start(){		
		// we have to initialize a system in different thread...
		accepterThread = new Thread(this);
		accepterThread.start();
	}

	public synchronized void stop(){
		destroy();
	}

	/**
	 * Called to run the SMS server.
	 */
	public void run(){
		try{
			srv = new Service();
			System.out.println("Starting " + id + " at com port:="+comPort);
			SerialModemGateway gateway = new SerialModemGateway(id, comPort, baudRate, manufacturer, model, srv);
			gateway.setInbound(true);
			gateway.setOutbound(true);
			gateway.setSimPin("0000");

			// Create the notification callback method for Inbound & Status Report messages.
			gateway.setInboundNotification(this);
			gateway.setOutboundNotification(this);

			srv.addGateway(gateway);
			srv.startService();

//			ok, start processor now
			processor = new ClientProcessor(srv,eventListener);

			System.out.println("Waiting for " + id + " Connections at com port:="+comPort+" ...");

			//For some modems with no service indications support for new message
			//arrival, we are using polling to get these inbound messages.
			if(useInboundPolling){
				while(true){
					processInboundMessages(id);
					Thread.sleep(inboundPollingInterval);
				}
			}

		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Destroy a work with bluetooth - exits the accepting
	 * thread and close notifier.
	 */
	public void destroy() {

		System.out.println("Stopping " + id + " at com port:="+comPort);

		isClosed = true;

		// wait for acceptor thread is done
		try {
			accepterThread.join();
		} catch (InterruptedException e) {} // ignore

		// finalize processor
		if (processor != null) {
			processor.destroy(true);
		}

		processor = null;

		try{
			srv.stopService();

			System.out.println("Stopped SMS Service at com port:="+comPort);
		}
		catch(Exception e){
			e.printStackTrace();
		} //ignore
	}

	/**
	 * Informs the event listener about a problem.
	 * 
	 * @param message - the error message.
	 * @param e - the exception, if any, that caused this problem.
	 */
	private void raiseError(String message, Exception e){
		this.eventListener.errorOccured("Error getting data stream",e);
	}

	/**
	 * Processes and inbound SMS.
	 * 
	 * @param gatewayId the id of the gateway sending the sms.
	 * @param msgType the type of message.
	 * @param memLoc
	 * @param memIndex
	 */
	public void process(String gatewayId, MessageTypes msgType, String memLoc, int memIndex)
	{
		if (msgType == MessageTypes.INBOUND)
		{
			System.out.println(">>> New Inbound message detected from Gateway: " + gatewayId + " : " + memLoc + " @ " + memIndex);
			processInboundMessages(gatewayId);
		}
		else if (msgType == MessageTypes.STATUSREPORT)
			System.out.println(">>> New Status Report message detected from Gateway: " + gatewayId + " : " + memLoc + " @ " + memIndex);
		else
			System.out.println("Unknown Message Type");
	}

	private void processInboundMessages(String gatewayId){
		try
		{
			List msgList = new ArrayList();
			srv.readMessages(msgList, MessageClasses.UNREAD, gatewayId);
			for (int i = 0; i < msgList.size(); i++)
				processor.addMessage((InboundMessage)msgList.get(i));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			eventListener.errorOccured("Problem occured while processing msg", e);
		}
	}

	/**
	 * Processes an outbound SMS.
	 * 
	 * @param gatewayId
	 * @param msg
	 */
	public void process(String gatewayId, OutboundMessage msg)
	{
		System.out.println("Outbound handler called from Gateway: " + gatewayId);
		System.out.println(msg);
	}

	/**
	 * Organizes the queue of clients to be processed,
	 * processes the clients one by one until destroyed.
	 */
	private class ClientProcessor implements Runnable {
		private Thread processorThread;
		private Vector queue = new Vector();
		private SMSServerListener eventListener;
		private Service srv;
		private SmsAssembler smsAssembler;

		ClientProcessor(Service srv, SMSServerListener eventListener) {
			this.srv = srv;
			this.eventListener = eventListener;
			processorThread = new Thread(this);
			processorThread.start();
			smsAssembler = new SmsAssembler();
		}

		public void run() {
			while (!isClosed) {
				// wait for new task to be processed
				synchronized (this) {
					if (queue.size() == 0) {
						try {
							wait();
						} catch (InterruptedException e) {
							raiseError("Unexpected exception: " , e);
							destroy(false);

							return;
						}
					}
				}

				synchronized (this) {
					// may be awaked by "destroy" method.
					if (isClosed)
						return;

					InboundMessage msg = (InboundMessage)queue.firstElement();
					queue.removeElementAt(0);
					processMessage(msg);
				}
			}
		}

		/** Adds the message to queue and notifies the thread. */
		private void addMessage(InboundMessage msg) {
			synchronized (this) {
				queue.addElement(msg);
				notify();
			}
		}

		/** Closes the connections and . */
		private void destroy(boolean needJoin) {
			InboundMessage msg;

			synchronized (this) {
				notify();

				while (queue.size() != 0) {
					msg = (InboundMessage)queue.firstElement();
					queue.removeElementAt(0);

					try {
						srv.deleteMessage(msg);
					} catch (Exception e) {} // ignore
				}
			}

			// wait until dispatching thread is done
			try {
				processorThread.join();
			} catch (InterruptedException e) {} // ignore
		}

		private void processMessage(InboundMessage inMsg) {

			try{
				System.out.println(inMsg);

				if(inMsg.getEncoding() == MessageEncodings.ENC8BIT){
					System.out.println("text length="+inMsg.getText().length());
					System.out.println("default byte length="+inMsg.getText().getBytes().length);
					System.out.println("utf byte length="+inMsg.getText().getBytes("UTF-8").length);
					if(smsAssembler.assembleMessage(inMsg.getOriginator(),inMsg.getText().getBytes(/*"UTF-8"*/))){
						DataInputStream dis = new DataInputStream(new ByteArrayInputStream(smsAssembler.getPayloadData(inMsg.getOriginator())));
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						DataOutputStream dos = new DataOutputStream(baos);

						eventListener.processMessage(dis, dos);
						sendMessage(inMsg.getOriginator(),baos.toByteArray());

						System.out.println("COMPLETE REPLY SENT.");
					}

					srv.deleteMessage(inMsg);
				}
				else{
					//System.out.println("UNACCEPTED MESSAGE ENCODING. NOT PROCESSED.");

					/*ByteArrayOutputStream baos = new ByteArrayOutputStream();
					DataOutputStream dos = new DataOutputStream(baos);

					eventListener.processMessage(getDataInputStream(inMsg), dos);

					DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
					String report = dis.readUTF();
					System.out.println(report);

					OutboundMessage outMsg = new OutboundMessage(inMsg.getOriginator(),report);
    				srv.sendMessage(outMsg);

					System.out.println("COMPLETE REPLY SENT.");*/

					String text = eventListener.processMessage(inMsg.getOriginator(), inMsg.getText());

					if(text != null && text.trim().length() > 0){
						OutboundMessage outMsg = new OutboundMessage(inMsg.getOriginator(),text);
						srv.sendMessage(outMsg);
					}
				}		
			}
			catch(Exception e){
				e.printStackTrace();
				raiseError("Problem processiong incoming message.",e);
			}
		}

		/*private DataInputStream getDataInputStream(InboundMessage inMsg) throws Exception{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos= new DataOutputStream(baos);
			dos.writeUTF(inMsg.getOriginator());
			dos.writeUTF(inMsg.getText());
			return new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
		}*/

		private byte[] getBytes(int num){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			try{
				dos.writeInt(num);
			}catch(Exception e){return null;}

			return baos.toByteArray();
		}

		private void setBytesLength(byte[] bytes, int len){
			byte[] sizeBytes = getBytes(len);
			bytes[3] = sizeBytes[3];
			bytes[2] = sizeBytes[2];
			bytes[1] = sizeBytes[1];
			bytes[0] = sizeBytes[0];
		}

		/**
		 * Sends a text message to an address.
		 * 
		 * @param address
		 * @param srcBytes
		 */
		private void sendMessage(String address, String text){

		}

		/**
		 * Sends a binary message to an address.
		 * 
		 * @param address
		 * @param srcBytes
		 */
		private void sendMessage(String address, byte[] srcBytes){
			try{
				int len = srcBytes.length; byte[] dstBytes;
				int count = (len/MSG_PART_SIZE)+1;
				int srcStartIndex = 0, destStartIndex=0,copyLen = MSG_PART_SIZE,bytesCopied=0;
				for(int i=1; i<=count; i++){
					srcStartIndex = (i-1)*MSG_PART_SIZE;
					if(bytesCopied + copyLen > len)
						copyLen = len-bytesCopied;

					destStartIndex = ((i==1) ? LEN_BYTE_SIZE : 0);
					dstBytes = new byte[copyLen+destStartIndex];

					//Use the first four bytes for the total message length.
					if(i == 1)
						setBytesLength(dstBytes,len);

					//Add user data from the fifth byte onwards.
					System.arraycopy(srcBytes, srcStartIndex, dstBytes, destStartIndex, copyLen);
					bytesCopied += copyLen;

					OutboundMessage outMsg = new OutboundMessage(address,dstBytes);
					outMsg.setDstPort(msgDstPort);
					outMsg.setSrcPort(msgSrcPort);
					srv.sendMessage(outMsg);
					System.out.println(new String(dstBytes));
					System.out.println("next...........................");	
				}
			}
			catch(Exception e){
				e.printStackTrace();
				raiseError("Problem sending reply.",e);
			}
		}
	}
}
