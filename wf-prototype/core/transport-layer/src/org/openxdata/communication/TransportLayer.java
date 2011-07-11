package org.openxdata.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connection;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.StreamConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.openxdata.communication.bluetooth.BluetoothClient;
import org.openxdata.communication.bluetooth.BluetoothClientListener;
import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.Settings;
import org.openxdata.model.ResponseHeader;
import org.openxdata.util.AlertMessage;
import org.openxdata.util.AlertMessageListener;
import org.openxdata.util.MenuText;
import org.openxdata.util.SimpleOrderedHashtable;

import com.jcraft.jzlib.ZInputStream;
import com.jcraft.jzlib.ZStreamException;


/**
 * Abstracts the communication details.
 * This class is threaded such that the user does not have to deal with threading issues
 * which are a must for midlets handling blocking calls. When requests are processed
 * the user is notified by callbacks through the BluetoothClientEventListener interface .
 * 
 * @author Daniel Kayiwa
 *
 */
public class TransportLayer implements Runnable, BluetoothClientListener, AlertMessageListener/*, MessageListener*/{
//	TODO Some users wanted the sending of forms to be done one by one instead of the entire batch such that failure
//	occurs on a form by form basis instead of the entire batch.

	/** Connection type not set. */
	public static final byte CON_TYPE_NULL = -1;

	/** HTTP connection. */
	public static final byte CON_TYPE_HTTP = 1;

	/** Bluetooth connection. */
	public static final byte CON_TYPE_BLUETOOTH = 2;

	/** Serial connection. eg USB,Infrared. */
	//public static final byte CON_TYPE_CABLE = 3;

	/** Sending data as an sms. */
	public static final byte CON_TYPE_SMS = 4;

	/** Sending data around as files on say memory card. */
	//public static final byte CON_TYPE_FILE = 5;

	public static final String KEY_BLUETOOTH_SERVER_ID = "BLUETOOTH_SERVER_ID";

	public static final String KEY_BLUETOOTH_DEVICE_NAME = "BLUETOOTH_DEVICE_NAME";

	public static final String KEY_HTTP_URL = "HTTP_URL";

	public static final String KEY_SERVER_HTTP_URL = "SERVER_HTTP_URL";
	public static final String KEY_SERVLET_HTTP_URL = "SERVLET_HTTP_URL";

	public static final String KEY_POST_DATA = "POST_DATA";

	public static final String KEY_SMS_DESTINATION_ADDRESS = "KEY_SMS_DESTINATION_ADDRESS";

	public static final String KEY_SMS_SOURCE_ADDRESS = "KEY_SMS_SOURCE_ADDRESS";

	/** HTTP connection. */
	public static final String CON_TYPE_NAME_HTTP = MenuText.HTTP();

	/** Bluetooth connection. */
	public static final String CON_TYPE_NAME_BLUETOOTH = MenuText.BLUETOOTH();

	/** Serial connection. eg USB,Infrared. */
	//public static final String CON_TYPE_NAME_CABLE = "Data Cable";

	/** Sending data as an sms. */
	//public static final String CON_TYPE_NAME_SMS = "SMS";

	/** Sending data around as files on say memory card. */
	//public static final String CON_TYPE_NAME_FILE = "File";



	public static final String BLUETOOTH_SERVER_ID = "F0E0D0C0B0A000908070605040302010";

	/** No action is currently in progress. */
	private int ACTION_NONE = 0;

	/** Currently processing a user request line form download, upload, etc. */
	private int ACTION_PROCESSING_REQUEST = 1;

	/** The connection type to use. */
	private byte conType = CON_TYPE_NULL;

	/** The connection parameters. */
	protected Hashtable conParams = new Hashtable();

	protected Vector connectionParameters = new Vector();

	/** Data request parameters. */
	protected Persistent dataInParams;

	/** Data receive parameters. */
	protected Persistent dataOutParams;

	/** Data to be sent. */
	protected Persistent dataIn;

	/** Data received. */
	protected Persistent dataOut;

	/** Reference to the listener for communication events. */
	protected TransportLayerListener eventListener;

	/** Flag which when true mean we are downloading data, orelse we are uploading. */
	protected boolean isDownload;

	/** The display reference for user selecting the connection type. */
	private Display display;

	/** The screen to display after closing our connection type selection screen. */
	private Displayable prevScreen;

	/** The alert, if any, to display after closing our connection type selection screen. */
	private Alert currentAlert;

	/** The action currently processed. */
	private int currentAction = ACTION_NONE;

	private SimpleOrderedHashtable conTypes;

	private AlertMessage alertMsg;

	private String title = "Server Connection"; //TODO Should be parameterised.

	private ConnectionSettings conSettings;

	private String serviceUrl;

	private static final String KEY_CONNECTION_TYPE = "CONNECTION_TYPE";
	private static final String STORAGE_NAME_SETTINGS = "fcitmuk.DefaultTransportLayer";
	private static final String STORAGE_NAME_HTTP_SETTINGS = "fcitmuk.util.HttpSettings";

	private Connection con;
	BluetoothClient btClient;
	private boolean cancelled = false;
	private boolean cancelPrompt = false;

	/*private byte[] smsdata = null;
	private int smsTotalLen = 0;
	private int smsCurrentLen = 0;*/

	String userName = "";
	String password = "";

	private byte connectionRetries; 
	private byte streamRetries; 
	private static final byte MAX_CONNECTION_RETRIES = 3;

	private Thread thread;
	
	/** 
	 * api user custom progress message.
	 */
	private String progressMessage;

	
	/** Cconstructs a transport layer object. */
	public TransportLayer(){
		super();
		initConnectionTypes();
		conSettings = new ConnectionSettings();
	}

	/**
	 * Constructs a transport layer object with the following parameters:
	 * 
	 * @param display - reference to the display.
	 * @param displayable - the screen to show after closing any of ours.
	 */
	public TransportLayer(Display display, Displayable displayable){
		this();
		setDisplay(display);
		setPrevScreen(displayable);
	}

	protected void initConnectionTypes(){
		conTypes = new SimpleOrderedHashtable();

		//if(isHttpPresent())
		conTypes.put(new Byte(TransportLayer.CON_TYPE_HTTP), TransportLayer.CON_TYPE_NAME_HTTP);

		if(isBluetoothPresent())
			conTypes.put(new Byte(TransportLayer.CON_TYPE_BLUETOOTH), TransportLayer.CON_TYPE_NAME_BLUETOOTH);

		/*if(System.getProperty("microedition.commports") != null)
			conTypes.put(new Byte(TransportLayer.CON_TYPE_CABLE), TransportLayer.CON_TYPE_NAME_CABLE);*/

		//if(isSmsPresent())
		//	conTypes.put(new Byte(TransportLayer.CON_TYPE_SMS), TransportLayer.CON_TYPE_NAME_SMS);

		/*if(System.getProperty("microedition.io.file.FileConnection.version") != null)
			conTypes.put(new Byte(TransportLayer.CON_TYPE_FILE), TransportLayer.CON_TYPE_NAME_FILE);*/

		loadUserSettings(); //TODO Some issues to resolve here.
	}

	private boolean isBluetoothPresent(){
		boolean present = false;

		try{
			//new BluetoothClient(BLUETOOTH_SERVER_ID,this);
			Class.forName("javax.bluetooth.DiscoveryListener");
			present = true;
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}

		return present;
	}

	/*private boolean isHttpPresent(){
		boolean present = false;

		try{
			//new BluetoothClient(BLUETOOTH_SERVER_ID,this);
			Class.forName("javax.microedition.io.HttpConnection");
			present = true;
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}

		return present;
	}*/

	/*private boolean isSmsPresent(){
		boolean present = false;

		try{
			//new BluetoothClient(BLUETOOTH_SERVER_ID,this);
			Class.forName("javax.wireless.messaging.MessageConnection");
			present = true;
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}

		return present;
	}*/

	public static byte getConnectionType(String name){
		if(name.equals(TransportLayer.CON_TYPE_NAME_HTTP))
			return TransportLayer.CON_TYPE_HTTP;
		else if(name.equals(TransportLayer.CON_TYPE_NAME_BLUETOOTH))
			return TransportLayer.CON_TYPE_BLUETOOTH;
		/*else if(name.equals(TransportLayer.CON_TYPE_NAME_CABLE))
			return TransportLayer.CON_TYPE_CABLE;*/
		/*else if(name.equals(TransportLayer.CON_TYPE_NAME_SMS))
			return TransportLayer.CON_TYPE_SMS;*/
		/*else if(name.equals(TransportLayer.CON_TYPE_NAME_FILE))
			return TransportLayer.CON_TYPE_FILE;*/


		return TransportLayer.CON_TYPE_NULL;
	}

	private void loadUserSettings(){
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		if(settings.getSetting(KEY_CONNECTION_TYPE) != null)
			conType = Byte.parseByte(settings.getSetting(KEY_CONNECTION_TYPE)); //CON_TYPE_BLUETOOTH - 1;

		Enumeration keys = settings.keys();
		while(keys.hasMoreElements()){
			String key = (String)keys.nextElement();
			if(isConnectionParam(key))
				conParams.put(key, settings.getSetting(key));
		}

		settings = new Settings(STORAGE_NAME_HTTP_SETTINGS,true);
		keys = settings.keys();
		while(keys.hasMoreElements()){
			String key = (String)keys.nextElement();
			connectionParameters.addElement(new ConnectionParameter(TransportLayer.CON_TYPE_HTTP,key,settings.getSetting(key)));
		}
	}

	private static boolean isConnectionParam(String key){
		return !key.equals("LAST_SELECTED_MAIN_MENU_ITEM"); //TODO This is very very bad design. Should change it.
	}

	protected void addConnectionType(byte Id, String name){
		conTypes.put(new Byte(Id), name);
	}

	/**
	 * Sets the value of a communication parameter. If it already exists, it is
	 * overwritten.
	 * 
	 * @param key
	 * @param value
	 */
	public void setCommunicationParameter(String key, String value){
		conParams.put(key, value);
	}

	/**
	 * Sets the value of a communication parameter if it does not already exist.
	 * 
	 * @param key
	 * @param value
	 */
	public void setDefaultCommunicationParameter(String key, String value){
		if(!conParams.containsKey(key))
			conParams.put(key, value);
	}

	protected TransportLayerListener getEventListener(){
		return this.eventListener;
	}

	/**
	 * Downloads data over the transport layer.
	 * 
	 * @param dataInParams - Data input connection parameters. eg which request type.
	 * @param dataIn - Data to be sent if any.
	 * @param dataOutParams - Data received parameters. eg failure or success status.
	 * @param dataOut - Data received.
	 * @param eventListener - Reference to listener for communication events.
	 */
	public synchronized void download(Persistent dataInParams, Persistent dataIn, Persistent dataOutParams, Persistent dataOut,TransportLayerListener eventListener, String userName, String password, String progressMessage){

		if (thread != null) {
			thread = null;
		}

		saveParameters(dataInParams,dataIn,dataOutParams,dataOut,eventListener,true,userName, password,progressMessage);

		thread = new Thread(this);
		thread.start();
		//handleRequest();
	}

	/**
	 * Uploads data over the transport layer.
	 * 
	 * @param dataInParams - Data input connection parameters. eg which request type.
	 * @param dataIn - Data to be sent.
	 * @param dataOutParams - Data received parameters. eg failure or success status.
	 * @param dataOut - Data received if any.
	 * @param eventListener - Reference to listener to communication events.
	 */
	public void upload(Persistent dataInParams, Persistent dataIn, Persistent dataOutParams, Persistent dataOut,TransportLayerListener eventListener, String userName, String password, String progressMessage){
		saveParameters(dataInParams,dataIn,dataOutParams,dataOut,eventListener,false,userName, password,progressMessage);
		
		new Thread(this).start();
	}

	/**
	 * Saves parameters to class level variables.
	 * 
	 * @param dataInParams - Data input connection parameters. eg which request type.
	 * @param dataIn - Data to be sent if any.
	 * @param dataOutParams - Data received parameters. eg failure or success status.
	 * @param dataOut - Data received if any.
	 * @param eventListener - Reference to listener to communication events.
	 */
	private void saveParameters(Persistent dataInParams, Persistent dataIn, Persistent dataOutParams, Persistent dataOut,TransportLayerListener eventListener, boolean isDownload, String userName, String password, String progressMessage){
		this.dataInParams = dataInParams;
		this.dataIn = dataIn;
		this.dataOutParams = dataOutParams;
		this.dataOut = dataOut;
		this.eventListener = eventListener;
		this.isDownload = isDownload;
		this.userName = userName;
		this.password = password;
		this.progressMessage = progressMessage;
	}

	/** 
	 * Called when the thread starts to run the user request.
	 *
	 */
	protected void handleRequest() {
		cancelled = false;
		btClient = null;

		//TODO Need to parameterize the title.
		alertMsg  = new AlertMessage(display, title, prevScreen,this);

		showConnectionProgress(MenuText.CONNECTING_TO_SERVER());

		streamRetries = 0;

		switch(conType){
			case CON_TYPE_HTTP:
				connectHttp();
				break;
			case CON_TYPE_BLUETOOTH:
				connectBluetooth();
				break;
				/*case CON_TYPE_CABLE:
				connectSerial();
				break;*/
				/*case CON_TYPE_SMS:
				connectSMS();
				break;
				/*case CON_TYPE_FILE:
				connectFile();
				break;*/
			case CON_TYPE_NULL:
				this.currentAction = this.ACTION_PROCESSING_REQUEST;
				getUserSettings(this.display,this.prevScreen,userName,password);
				break;
			default:
				break;
		}
	}

	/**
	 * Writes and writes data to and from the streams.
	 * @param dos - Stream to write data to.
	 * @param dis - Stream to read data from.
	 * @throws IOException - Thrown when there is a problem for a read or write operation.
	 */
	protected void handleStreams(DataOutputStream dos, DataInputStream dis) throws IOException {
		try{			
			showConnectionProgress(progressMessage != null ? progressMessage : MenuText.TRANSFERING_DATA());

			if(dataInParams != null) //for now http will not send this value.
				dataInParams.write(dos); 

			if(dataIn != null)
				dataIn.write(dos);

			dos.flush(); //if you dont do this, the client will block on a read.
			dos.close();
			dos = null; //Not setting to null results in KErrCouldNotConnect on the client.

			readResponseData(dis);

		}catch(Exception e){
			this.eventListener.errorOccured(MenuText.PROBLEM_HANDLING_STREAMS(),e);
			//e.printStackTrace(); //TODO May need to report this to user.
		}finally{
			try{				
				dis.close();
				dis = null;
			}catch(Exception ex){
				//ex.printStackTrace();
			}
		}
	}

	private void readResponseData(DataInputStream dis) throws Exception{
		dis = getDecompressedStream(dis);

		dataOutParams.read(dis); //This should never be null
		byte status = ((ResponseHeader)dataOutParams).getStatus();
		if(status == ResponseHeader.STATUS_SUCCESS){
			if(dataOut != null) //FO cases where we are not getting any data back.
				dataOut.read(dis); //When the out param shows failure status, this can be null hence throwing EOF exceptions.

			if(this.isDownload)
				this.eventListener.downloaded(dataOutParams,dataOut);
			else
				this.eventListener.uploaded(dataOutParams,dataOut);
		}
		else{
			String s = MenuText.SERVER_PROCESS_FAILURE();
			if (status == ResponseHeader.STATUS_ACCESS_DENIED)
				s = MenuText.ACCESS_DENIED();
			else if (status == ResponseHeader.STATUS_PERMISSION_DENIED)
				s = MenuText.DEVICE_PERMISSION_DENIED();
			this.eventListener.errorOccured(s, null);
		}
	}

	/**
	 * Handles File communications. eg memory card transfer.
	 * 
	 * @throws IOException - Thrown when there is a problem reading from or writting
	 * 						 to the connection stream.
	 */
	/*protected void connectFile() throws IOException{

		//connectSocketServer();
		connectSocketClint();
 	}*/

	/*private void connectSocketServer() throws IOException{

		alertMsg.showProgress(title,System.getProperty("microedition.hostname")+"=Waiting socket connection...");

		ServerSocketConnection server = (ServerSocketConnection)Connector.open("socket://:");

		alertMsg.showProgress(title,"Listening at address="+server.getLocalAddress()+" & Port="+server.getLocalPort());

        // Wait for a connection.
		SocketConnection con = (SocketConnection)server.acceptAndOpen();
        //si.setText("Connection accepted");

		alertMsg.showProgress(title,"Connection accepted. Processing...");

		InputStream is = con.openInputStream();
		OutputStream os = con.openOutputStream();
 	}*/

	/*private void connectSocketClint() throws IOException{

		alertMsg.showProgress(title,"Socket connecting...");

		SocketConnection con = (SocketConnection)Connector.open("socket://10.10.3.76:2105");

		alertMsg.showProgress(title,"Connection accepted. Processing...");

		InputStream is = con.openInputStream();
		OutputStream os = con.openOutputStream();

		os.write(1);
		os.flush();

		alertMsg.showProgress(title,"Processing...");
		//InputStream is = con.openInputStream();
		//OutputStream os = con.openOutputStream();
 	}*/

	/**
	 * Handles HTTP communications. eg GPRS.
	 */
	protected void connectHttp() {
		try {
			String HTTP_URL = (String)conParams.get(TransportLayer.KEY_HTTP_URL);  
			con = (HttpConnection)Connector.open(HTTP_URL);
			showConnectionProgress(progressMessage != null ? progressMessage : MenuText.TRANSFERING_DATA());

			((HttpConnection)con).setRequestMethod(HttpConnection.POST);
			((HttpConnection)con).setRequestProperty("Content-Type","application/octet-stream");
			((HttpConnection)con).setRequestProperty("User-Agent","Profile/MIDP-2.0 Configuration/CLDC-1.0");
			((HttpConnection)con).setRequestProperty("Content-Language", "en-US");

			DataOutputStream dos = ((HttpConnection)con).openDataOutputStream();
			if(dataInParams != null)
				dataInParams.write(dos);

			if(dataIn != null){
				dataIn.write(dos);
				dos.flush();
			}

			int status  = ((HttpConnection)con).getResponseCode();
			if (status != HttpConnection.HTTP_OK) {
				this.eventListener.errorOccured(
						MenuText.RESPONSE_CODE_FAIL() + status + ". " +
						MenuText.SERVER_INVALID_URL(), 
						null);
			}
			else {//TODO May need some more specific failure codes
				//For HTTP, we get only data back. Status is via HTTP status code. So no need of readin the data in param.				
				//if(dataOut != null) //There are cases where we are not getting any data back apart fomr the HTTP status code.					
				//	readHttpData(((HttpConnection)con).openDataInputStream());

				readResponseData(((HttpConnection)con).openDataInputStream());
			}
		}
		catch (SecurityException e) {
			this.eventListener.errorOccured(MenuText.DEVICE_PERMISSION_DENIED(),e);
		}
		catch (Exception e) {
			this.eventListener.errorOccured(
					MenuText.PROBLEM_HANDLING_REQUEST() + 
					MenuText.SERVER_INVALID_URL() +
					" Error: " + e.getMessage() + ". " , 
					null);
			e.printStackTrace();
		}
		finally {
			try {
				con.close();
			} catch (Exception e) {}
		}
	}

	/*private void readHttpData(DataInputStream dis) throws Exception{

		try{			
			dataOut.read(getDecompressedStream(dis));

			if(this.isDownload)
				this.eventListener.downloaded(dataOutParams,dataOut);
			else
				this.eventListener.uploaded(dataOutParams,dataOut);
		}
		catch(Exception e){
			throw new DataReadException("Problem reading data from the server." +(e.getMessage() != null ? e.getMessage() : ""));
		}
	}*/

	/**
	 * Gets
	 * 
	 * @param dis
	 * @return
	 * @throws IOException
	 */
	private DataInputStream getDecompressedStream(DataInputStream dis) throws IOException{
		//GZIPInputStream gzip  = new GZIPInputStream(dis);
		ZInputStream gzip  = new ZInputStream(dis);
		return new DataInputStream(gzip);
	}

	/** Resets the concatenated SMS counters. */
	/*private void resetSmsDataParams(){
		/*smsdata = null;
		smsCurrentLen = 0;
		smsTotalLen = 0;*/

	/*TransportLayerStorage.getInstance().deleteSmsAssemblerData();
	}*/

	/**
	 * Handles SMS communications..
	 * 
	 * @throws IOException - Thrown when there is a problem reading from or writting
	 * 						 to the connection stream.
	 */
	/*protected void connectSMS() throws IOException,Exception{

		closeConnection();

		//Try checking just incase user closed before receiving the complete set of
		//concatenated sms es.
		String[] connections = PushRegistry.listConnections(true);
		if(connections != null && connections.length > 0)
			handleIncomingSmsData(connections[0]);
		else{
			//TransportLayerStorage.getInstance().deleteSmsAssemblerData();

			String destAddress = (String)conParams.get(TransportLayer.KEY_SMS_DESTINATION_ADDRESS); //"sms://+256772963035:1234"; //"sms://+256772963035:1234";;
			String srcAddress = (String)conParams.get(TransportLayer.KEY_SMS_SOURCE_ADDRESS); //"sms://:3333";
			con = (MessageConnection)Connector.open(srcAddress);
			((MessageConnection)con).setMessageListener(this);

			showConnectionProgress("SMS Connected to server ["+destAddress+"]. Tranfering data...");

			//PushRegistry.listConnections(true);

			/*BinaryMessage msg = (BinaryMessage)((MessageConnection)con).newMessage(MessageConnection.BINARY_MESSAGE);
			msg.setPayloadData(getPayLoadData());
			msg.setAddress(destAddress);
			((MessageConnection)con).send(msg);*/

	/*sendMessage(destAddress,getPayLoadData());

			showConnectionProgress("Sent Message to ["+destAddress+"]. Waiting for reply...");
		}
	}*/

	/*public void handleIncomingSmsData(String connectionAddress){
		try{
			showConnectionProgress("Processing incoming SMS data...");
			con = (MessageConnection)Connector.open(connectionAddress);
			((MessageConnection)con).setMessageListener(this);
		}
		catch(Exception e){
			eventListener.errorOccured("Problem handling incoming sms data",e);
		}
	}*/

	/*private byte[] getPayLoadData(){
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ZOutputStream zip  = new ZOutputStream(baos,JZlib.Z_BEST_COMPRESSION);
			DataOutputStream dos = new DataOutputStream(baos);

			if(dataInParams != null)
				dataInParams.write(dos);
			if(dataIn != null)
				dataIn.write(dos);

			dos.flush();
			zip.finish();

			return baos.toByteArray();
		}
		catch(Exception e){
			this.eventListener.errorOccured("Problem getting payload data",e);
			//e.printStackTrace();
		}

		return null;
	}*/

	/*private void sendMessage(String address, byte[] srcBytes) throws Exception{
		/** The size of a message part. */
	/*final int MSG_PART_SIZE = 1000;  		
		final int LEN_BYTE_SIZE = 4;

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

			BinaryMessage msg = (BinaryMessage)((MessageConnection)con).newMessage(MessageConnection.BINARY_MESSAGE);
			msg.setPayloadData(dstBytes);
			msg.setAddress(address);
			((MessageConnection)con).send(msg);

			test(dstBytes);
		}
	}*/

	/*private void test(byte[] payload){

	}*/

	/*private void setBytesLength(byte[] bytes, int len){
		byte[] sizeBytes = getBytes(len);
		bytes[3] = sizeBytes[3];
		bytes[2] = sizeBytes[2];
		bytes[1] = sizeBytes[1];
		bytes[0] = sizeBytes[0];
	}*/

	/*private byte[] getBytes(int num){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.writeInt(num);
		}catch(Exception e){return null;}

		return baos.toByteArray();
	}*/

	/**
	 * 
	 * @throws IOException- Thrown when there is a problem reading from or writting
	 * 						 to the connection stream.
	 * @throws Exception- Thrown when serial communication is not implemented.
	 */
	/*protected void connectSerial() throws IOException,Exception {

		CommConnection  con = null;

		try{
			//"comm:IR0";
			//String CON_STR = (String)conParams.get("CON_STR"); // + ";baudrate=115200;" nokiacomm
			String url = "comm:" + getPort() + ";baudrate=9600";

			showConnectionProgress("Connecting to server with url " + url + " ...");

			con = (CommConnection)Connector.open(url,Connector.READ_WRITE,true);

			showConnectionProgress("Connected to server with url " + url + " ...");

			//TODO May need to check success status
			handleStreams(con.openDataOutputStream(),con.openDataInputStream());
		}finally{
			if(con != null)
				con.close();
		}
	}

	private String getPort() throws Exception{
		String port1;
		 String ports = System.getProperty("microedition.commports");
		 if(ports == null)
			 throw new Exception("Serial Communication not implemented.");

		 int comma = ports.indexOf(',');
		 if (comma > 0) {
		     // Parse the first port from the available ports list.
		     port1 = ports.substring(0, comma);
		 } else {
		     // Only one serial port available.
		     port1 =ports;
		 }
		 return port1;
	}*/

	private synchronized boolean openBluetoothConnection(){	
		try{
			connectionRetries++;
			con = null;
			con = (StreamConnection)Connector.open(serviceUrl,Connector.READ_WRITE,true);
			return true;
		}catch(Exception e){
			//Thread.currentThread().notifyAll();
			if(con != null){
				try{
					con.close();
				}catch(Exception ex){
					//ex.printStackTrace();
				}
				con = null; //Not setting this to null results into KErrAlreadyExists  failures on the client
			}
			if(connectionRetries  > MAX_CONNECTION_RETRIES)
				return false;
			else
				openBluetoothConnection();
		}
		return false;
	}

	/**
	 * Handles bluetooth communications.
	 *
	 */
	protected void connectBluetooth() {
		try{
			streamRetries++;

			if(serviceUrl == null){
				showConnectionProgress(MenuText.GETTING_BLUETOOTH_URL());
				String bluetooth_server_id = (String)conParams.get(TransportLayer.KEY_BLUETOOTH_SERVER_ID);
				String deviceName = (String)conParams.get(TransportLayer.KEY_BLUETOOTH_DEVICE_NAME);
				btClient = new BluetoothClient(bluetooth_server_id,this);
				serviceUrl = btClient.getServiceUrl(deviceName);
			}

			if(serviceUrl == null)
				return; //An error message was already reported by whatever made this null.

			showConnectionProgress(MenuText.OPENING_BLUETOOTH_CONNECTION());

			//TODO May need to check success status
			connectionRetries = 0;
			if(openBluetoothConnection()){
				showConnectionProgress(MenuText.GETTINGS_STREAM());
				handleStreams(((StreamConnection)con).openDataOutputStream(),((StreamConnection)con).openDataInputStream());
			}
			else
				eventListener.errorOccured(MenuText.OPEN_CONNECTION_FAIL(), null);
		}
		catch(Exception ex){
			//if(streamRetries > 2){
			this.eventListener.errorOccured(MenuText.PROBLEM_OPENING_STREAMS(),ex);
			//ex.printStackTrace();
			/*}
			else{
				//serviceUrl = null;
				showConnectionProgress("Refreshing bluetooth service connection.........");
				//serviceUrl = btClient.getServiceUrlMini();
				connectBluetooth();
			}*/
		}finally{
			if(con != null){
				try{
					con.close();
				}catch(Exception ex){
					//ex.printStackTrace();
				}
				con = null; //Not setting this to null results into KErrAlreadyExists  failures on the client
			}
		}
	}

	/** 
	 * Called when the thread starts executing.
	 */
	public void run(){
		handleRequest();
	}

	private void showConnectionProgress(String message){
			alertMsg.showProgress(title, (progressMessage != null && serviceUrl != null) ? progressMessage : message);
	}
	
	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void onConnectionSettingsClosed(boolean save) {
		try{
			if(save){
				serviceUrl = null; //May need to get a new url just incase the bluetooth address is changed by user.

				conType = conSettings.getConType();
				conParams = conSettings.getConParams();
				connectionParameters = conSettings.getConnectionParameters();

				Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);

				Enumeration keys = conParams.keys();
				while(keys.hasMoreElements()){
					String key = (String)keys.nextElement();
					settings.setSetting(key, (String)conParams.get(key));
				}

				settings.setSetting(KEY_CONNECTION_TYPE, String.valueOf(conType));
				settings.saveSettings();

				settings = new Settings(STORAGE_NAME_HTTP_SETTINGS,true);	

				for(int i=0; i<connectionParameters.size(); i++)
				{
					ConnectionParameter conParam = (ConnectionParameter)connectionParameters.elementAt(i);
					if(conParam.getValue() != null)
						settings.setSetting(conParam.getName(), conParam.getValue());
				}
				settings.saveSettings();
			}

			if(eventListener != null)
				eventListener.updateCommunicationParams();

			if(currentAlert != null)
				display.setCurrent(currentAlert, prevScreen);

			//Check to see if we were invoked due to a user request where
			//the connection type was not specified.
			if(save && this.currentAction == this.ACTION_PROCESSING_REQUEST)
				new Thread(this).start(); 
			else
				display.setCurrent(prevScreen);

			this.currentAction = this.ACTION_NONE;
		}
		catch(Exception e){
			alertMsg.showError(e.getMessage());
			//e.printStackTrace();
		}
	}

	public int getConType() {
		return conType;
	}

	/** Connection type. Set to -1 if you want to display a select connection type screen. */
	public void setConType(byte conType) {
		this.conType = conType;
	}

	public Display getDisplay() {
		return display;
	}

	public void setDisplay(Display display) {
		this.display = display;
	}

	public Displayable getPrevScreen() {
		return prevScreen;
	}

	public void setPrevScreen(Displayable prevScreen) {
		this.prevScreen = prevScreen;
		if (alertMsg != null) {
			alertMsg.setPrevScreen(prevScreen);
		}
	}

	public Alert getCurrentAlert() {
		return currentAlert;
	}

	public void setCurrentAlert(Alert currentAlert) {
		this.currentAlert = currentAlert;
	}

	public void errorOccured(String errorMessage, Exception e){
		if(!cancelled) //If user canclled, we dont need to bubble the connection interrupt error message.
			this.eventListener.errorOccured(errorMessage, e);
	}

	public void onAlertMessage(byte msg){
		if(msg == AlertMessageListener.MSG_OK){
			//For now, cancelling is only for bluetooth since it seems to be the slow one
			//when searching for bluetooth devices and services.
			if(cancelPrompt){
				if(btClient != null) 
					btClient.cancelSearch();
				cancelled = true;
				cancelPrompt = false;
			}

			if(con != null){
				try{
					con.close();
				}catch(IOException e){
					//e.printStackTrace();
				}
			}
			display.setCurrent(prevScreen);
		}
		else{
			if(cancelPrompt){ //User was asked if really want to cancel current operation and said NO.
				alertMsg.showProgress(); //continue with the current progress operation.
				cancelPrompt = false;
			}
			else{
				if(!cancelled){ //if not already cancelled
					alertMsg.showConfirm(MenuText.OPERATION_CANCEL_PROMPT());
					cancelPrompt = true;
				}
				else
					display.setCurrent(prevScreen);
			}
		}
	}

	/*private int getSize(byte[] b){
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b));
		try{
			return dis.readInt();
		}catch(IOException e){return -1;}
	}*/

	//public void notifyIncomingMessage(MessageConnection msgCon){
	/*try{
			Message msg = msgCon.receive();

			if(msg instanceof BinaryMessage){
				BinaryMessage binMsg = (BinaryMessage)msg;
				byte[] payload = binMsg.getPayloadData();

				SmsAssemblerData assemblerData = TransportLayerStorage.getInstance().getSmsAssemblerData();
				if(assemblerData == null)
					assemblerData = new SmsAssemblerData();

				/*if(smsdata == null){ //first send, this will be null
					smsTotalLen = getSize(payload); //read off first four bytes for total length.
					smsdata = new byte[smsTotalLen]; //create storage space.
					smsCurrentLen = payload.length - 4; //actual data is minus the four length bytes.
					System.arraycopy(payload,4,smsdata,0,smsCurrentLen);
				}
				else{ //this should be a send after the first one
					System.arraycopy(payload,0,smsdata,smsCurrentLen,payload.length);
					smsCurrentLen = (smsCurrentLen + payload.length);
				}*/				

	//if(smsCurrentLen == smsTotalLen){ //if all bytes have been read.
	/*if(SmsAssembler.assembleMessage(assemblerData,payload)){
					DataInputStream dis = new DataInputStream(new ByteArrayInputStream(assemblerData.getSmsData()));
					dis = this.getDecompressedStream(dis);

					if(dataOut != null)
						dataOut.read(dis);
					if(this.isDownload)
						this.eventListener.downloaded(dataOutParams,dataOut);
					else
						this.eventListener.uploaded(dataOutParams,dataOut);

					//resetSmsDataParams();
					TransportLayerStorage.getInstance().deleteSmsAssemblerData();
				}
				else
					showConnectionProgress("Transferred " + assemblerData.getSmsCurrentLen() + " of " + assemblerData.getSmsTotalLen() + " bytes. " + (assemblerData.getSmsTotalLen()-assemblerData.getSmsCurrentLen()) + " left.");
			}
			else
				this.eventListener.errorOccured("notifyIncomingMessage: Some non non binary text message.",null);
		}
		catch(Exception e){
			this.eventListener.errorOccured("Problem handling notifyIncomingMessage",e);
			//e.printStackTrace();
		}*/
	//}

	/*private void closeConnection(){
		try{
			if(con != null){
				con.close();
				con = null;
			}
		}catch(Exception e){}
	}*/

	/** 
	 * Displays a screen for the user to select a connection type.
	 * 
	 * @param display - reference to the current display.
	 * @param prevScreen - the screen to display after dismissing our screens.
	 */
	public void getUserSettings(Display display, Displayable prevScreen, String name, String password){
		this.prevScreen = prevScreen;
		conSettings.setDisplay(display);
		conSettings.setPrevScreen(prevScreen);
		conSettings.setTitle(MenuText.CONNECTION_SETTINGS());
		conSettings.getUserSettings(display, prevScreen,conType,conTypes,conParams,this,name,password,connectionParameters);
	}

	/**
	 * Addss a new connection parameter.
	 * 
	 * @param conParam
	 */
	public void addConnectionParameter(ConnectionParameter conParam)
	{
		for(int i=0; i<connectionParameters.size(); i++)
		{
			ConnectionParameter cp = (ConnectionParameter)connectionParameters.elementAt(i);
			if(cp.getName().equals(conParam.getName()))
				return; //If parameter alread exists, then we preserve the user customized value and dont overwrite it with the api hard coded one.
		}

		connectionParameters.addElement(conParam);
	}

	public String getConnectionParameterValue(int conType, String name)
	{		
		for(int i=0; i<connectionParameters.size(); i++)
		{
			ConnectionParameter cp = (ConnectionParameter)connectionParameters.elementAt(i);
			if(cp.getConnectionType() == conType && cp.getName().equals(name))
				return cp.getValue(); //If parameter alread exists, then we preserve the user customized value and dont overwrite it with the api hard coded one.
		}

		return null;
	}
}
