package org.openxdata.communication;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;

import org.openxdata.db.util.Settings;
import org.openxdata.mvc.AbstractView;
import org.openxdata.util.DefaultCommands;
import org.openxdata.util.MenuText;
import org.openxdata.util.SimpleOrderedHashtable;

/**
 * This class shows existing connection parameters and lets the user modify them,
 * before passing them over.
 * 
 * @author Daniel
 *
 */
public class ConnectionSettings extends AbstractView {

	private static final int CA_NONE = 0;
	private static final int CA_CON_TYPES = 1;
	private static final int CA_CON_PARAMS = 2;
	
	private static final String STORAGE_NAME_SETTINGS = "fcitmuk.DefaultTransportLayer";

	String userName = "";
	String password = "";

	/** The connection type to use. */
	private byte conType = TransportLayer.CON_TYPE_NULL;

	/** The connection parameters. */
	protected Hashtable conParams = new Hashtable();

	/** The connection parameters. */
	protected Vector connectionParameters = new Vector();

	private SimpleOrderedHashtable conTypes;

	/** Reference to the parent. */
	TransportLayer defTransLayer;

	private int currentAction = CA_NONE;

	public ConnectionSettings(){

	}

	/** 
	 * Displays a screen for the user to select a connection type.
	 * 
	 * @param display - reference to the current display.
	 * @param prevScreen - the screen to display after dismissing our screens.
	 */
	public void getUserSettings(Display display, Displayable prevScreen, byte conType, SimpleOrderedHashtable conTypes,Hashtable conParams, TransportLayer defTransLayer, String name, String password, Vector connectionParameters){
		AbstractView.display = display;
		this.prevScreen = prevScreen;
		this.conTypes = conTypes;
		this.conParams = conParams;
		this.connectionParameters = connectionParameters;
		this.defTransLayer = defTransLayer;

		// If there is only one option, ask for params directly
		if (conTypes.size() == 1) {
			this.conType = fromIndexToConType(0);
			showConParams();
			return;
		}

		currentAction = CA_CON_TYPES;

		screen = new List(MenuText.CONNECTION_TYPE(), Choice.IMPLICIT);
		((List)screen).setFitPolicy(List.TEXT_WRAP_ON);

		//TODO This hashtable does not maintain the order on devices like sony erickson.
		Enumeration keys = conTypes.keys();
		Object key;
		while(keys.hasMoreElements()){
			key = keys.nextElement();
			((List)screen).append(conTypes.get(key).toString(), null);
		}

		screen.addCommand(DefaultCommands.cmdOk);
		screen.addCommand(DefaultCommands.cmdCancel);
		byte index = fromConTypeToIndex(conType);
		if(index >= 0 && index < ((List)screen).size())
			((List)screen).setSelectedIndex(index,true);
		screen.setCommandListener(this);

		AbstractView.display.setCurrent(screen);
	}

	private byte fromConTypeToIndex(byte conType){
		return (byte)conTypes.getIndex(new Byte(conType));
	}

	private byte fromIndexToConType(int index){
		return ((Byte)conTypes.keyAt(index)).byteValue();
	}

	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		try{
			if(c == DefaultCommands.cmdOk || c == List.SELECT_COMMAND)
				handleOkCommand(d);
			else if(c == DefaultCommands.cmdCancel)
				handleCancelCommand(d);
		}
		catch(Exception e){
			//alertMsg.showError(e.getMessage());
			//e.printStackTrace();
		}
	}

	/**
	 * Processes the cancel command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleCancelCommand(Displayable d){
		if(currentAction == CA_CON_PARAMS && conTypes.size() > 1){
			currentAction = CA_CON_TYPES;
			display.setCurrent(screen);
		}
		else
			defTransLayer.onConnectionSettingsClosed(false);
	}

	/**
	 * Processes the OK command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleOkCommand(Displayable d){
		if(currentAction == CA_CON_PARAMS){
			if(d != null){
				if(conType == TransportLayer.CON_TYPE_HTTP){
					conParams.put(TransportLayer.KEY_SERVER_HTTP_URL, ((TextField)((Form)d).get(0)).getString().trim());

					for(int i=0; i<connectionParameters.size(); i++)
					{
						ConnectionParameter conParam = (ConnectionParameter)connectionParameters.elementAt(i);
						if(conParam.getConnectionType() == TransportLayer.CON_TYPE_HTTP)
							conParam.setValue(((TextField)((Form)d).get(i+2)).getString());
					}
				}
				else if(conType == TransportLayer.CON_TYPE_BLUETOOTH){
					conParams.put(TransportLayer.KEY_BLUETOOTH_SERVER_ID, ((TextField)((Form)d).get(0)).getString());
					conParams.put(TransportLayer.KEY_BLUETOOTH_DEVICE_NAME, ((TextField)((Form)d).get(1)).getString());
				}
				else if(conType == TransportLayer.CON_TYPE_SMS){
					conParams.put(TransportLayer.KEY_SMS_DESTINATION_ADDRESS, ((TextField)((Form)d).get(0)).getString());
					conParams.put(TransportLayer.KEY_SMS_SOURCE_ADDRESS, ((TextField)((Form)d).get(1)).getString());
				}
			}
			defTransLayer.onConnectionSettingsClosed(true);
		}
		else{
			conType = fromIndexToConType(((List)d).getSelectedIndex());
			showConParams();
		}
	}

	private void showConParams(){
		currentAction = CA_CON_PARAMS;

		if(conType == TransportLayer.CON_TYPE_HTTP)
			showHttpConParams();
		else if(conType == TransportLayer.CON_TYPE_BLUETOOTH)
			showBluetoothConParams();
		//else if(conType == TransportLayer.CON_TYPE_SMS)
		//	showSMSConParams();
		else
			handleOkCommand(null);
	}

	private void showHttpConParams(){
		Form frm = new Form(this.title);

		// NOTE: see build-configuration.xml to find out about class TransportConstants

		String s = (String)conParams.get(TransportLayer.KEY_SERVER_HTTP_URL);
		if (s == null) s = TransportConstants.SERVER_URL;
		TextField txtField = new TextField(MenuText.SERVER_URL(),s.trim(),500,TextField.ANY);
		frm.append(txtField);

		for(int i=0; i<connectionParameters.size(); i++)
		{
			ConnectionParameter conParam = (ConnectionParameter)connectionParameters.elementAt(i);
			if(conParam.getConnectionType() == TransportLayer.CON_TYPE_HTTP)
			{
				txtField = new TextField(conParam.getName(),conParam.getValue(),500,TextField.ANY);
				frm.append(txtField);
			}
		}

		frm.addCommand(DefaultCommands.cmdCancel);
		frm.addCommand(DefaultCommands.cmdOk);
		frm.setCommandListener(this);

		display.setCurrent(frm);
	}

	private void showBluetoothConParams(){
		Form frm = new Form(this.title);

		TextField txtField = new TextField(MenuText.BLUETOOTH_SERVICE_ID(),(String)conParams.get(TransportLayer.KEY_BLUETOOTH_SERVER_ID),100,TextField.ANY);
		frm.append(txtField);

		txtField = new TextField(MenuText.BLUETOOTH_DEVICE_NAME(),(String)conParams.get(TransportLayer.KEY_BLUETOOTH_DEVICE_NAME),100,TextField.ANY);
		frm.append(txtField);

		frm.addCommand(DefaultCommands.cmdCancel);
		frm.addCommand(DefaultCommands.cmdOk);
		frm.setCommandListener(this);

		display.setCurrent(frm);
	}

	/*private void showSMSConParams(){
		Form frm = new Form(this.title);

		TextField txtField = new TextField("Server Address:",(String)conParams.get(TransportLayer.KEY_SMS_DESTINATION_ADDRESS),100,TextField.ANY);
		frm.append(txtField);

		txtField = new TextField("Source Address:",(String)conParams.get(TransportLayer.KEY_SMS_SOURCE_ADDRESS),100,TextField.ANY);
		frm.append(txtField);

		frm.addCommand(DefaultCommands.cmdCancel);
		frm.addCommand(DefaultCommands.cmdOk);
		frm.setCommandListener(this);

		display.setCurrent(frm);
	}*/

	public byte getConType(){
		return conType;
	}

	public Hashtable getConParams(){
		return conParams;
	}

	public Vector getConnectionParameters(){
		return connectionParameters;
	}
	
	public static String getHttpUrl() {
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		String serverUrl = settings.getSetting(TransportLayer.KEY_SERVER_HTTP_URL);
		return serverUrl + "/" + TransportConstants.SERVLET_URL;
	}
}