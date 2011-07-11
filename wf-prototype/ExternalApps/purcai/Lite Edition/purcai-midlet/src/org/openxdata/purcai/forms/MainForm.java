package org.openxdata.purcai.forms;



import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;

import org.openxdata.communication.TransportLayer;
import org.openxdata.communication.TransportLayerListener;
import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.Settings;
import org.openxdata.db.util.StorageListener;
import org.openxdata.forms.FormListener;
import org.openxdata.forms.LogonListener;
import org.openxdata.forms.UserManager;
import org.openxdata.forms.UserSettings;
import org.openxdata.model.FormData;
import org.openxdata.model.FormDef;
import org.openxdata.model.OpenXdataConstants;
import org.openxdata.model.QuestionData;
import org.openxdata.purcai.forms.PurcaiTransportLayer;
import org.openxdata.util.AlertMessage;
import org.openxdata.util.AlertMessageListener;
import org.openxdata.util.DefaultCommands;


/** This is the main midlet that displays the main user inteface for purcai. 
 * 
 * @author Daniel Kayiwa
 *
 */
public class MainForm extends MIDlet  implements CommandListener,FormListener,StorageListener,AlertMessageListener, TransportLayerListener,LogonListener{
	
	/** Reference to the current display. */
	private Display display;
	
	/** The main menu screen. */
	private List mainList;
			
	/** Index for selecting an encounter form menu item. */
	private static final int INDEX_ENTER_MARKS = 0;
	
	/** Index for downloading forms menu item. */
	private static final int INDEX_DOWNLOAD_MARK_SHEET = 1;
	
	/** Index for downloading forms menu item. */
	private static final int INDEX_DOWNLOAD_CLASS_SUBJECT = 2;
	
	/** Index for uploading data menu item. */
	private static final int INDEX_UPLOAD_MARKS = 3;
	
	/** Index for specifying settings like server connection parameters. */
	private static final int INDEX_SETTINGS = 4;
	
	/** Index for selecting a study menu item. */
	private static final int INDEX_LOGOUT = 5;
	
	/** Application tittle. */
	private static final String TITLE = "PurcAI 1.0";
	
	/** List of forms for the current study. */
	//private Vector forms;
	
	/** Reference to epihandy form manager. */
	//private FormManager formMgr;
	
	/** Reference to the transportLayer. */
	private TransportLayer transportLayer;
	
	private AlertMessage alertMsg;
	
	/** The user manager object. */
	private UserManager userMgr;
	
	/** The current selected index of the main menu. For now, this is used to keep track of
	 * the user's action to return to after successfully logging in. This happens when the user
	 * tries to do something before logging in, and the logon mananer intervenes by requiring the
	 * user to first login. This happens after downloading forms because a new list of users is got
	 * which makes void the current users info. */
	private int selectedIndex = OpenXdataConstants.NO_SELECTION;
	
		
	private static final String KEY_LAST_SELECTED_MAIN_MENU_ITEM =  "LAST_SELECTED_MAIN_MENU_ITEM";
	private static final String STORAGE_NAME_SETTINGS = "STORAGE_NAME_PURCAI_SETTINGS";
	
	PurcaiController controller;
	
	/** Construct the main UI midlet. */
	public MainForm() {
		super();
		
		display = Display.getDisplay(this);
		
		initMainList();
		
		transportLayer = new PurcaiTransportLayer();
		transportLayer.setDisplay(display);
		transportLayer.setPrevScreen(mainList);
		transportLayer.setCommunicationParameter(TransportLayer.KEY_BLUETOOTH_SERVER_ID, /*"F0E0D0C0B0A000908070605040302010"*/ "F0E0D0C0B0A000908070605040301111");
		transportLayer.setCommunicationParameter(TransportLayer.KEY_HTTP_URL, "");
		transportLayer.setCommunicationParameter(TransportLayer.KEY_SMS_DESTINATION_ADDRESS,"sms://+256712330386"); //256782380638 "sms://+256782380638:1234"
		transportLayer.setCommunicationParameter(TransportLayer.KEY_SMS_SOURCE_ADDRESS,"sms://:1234"); 
	
		alertMsg = new AlertMessage(this.display, TITLE, this.mainList,this);

		//formMgr = new FormManager(TITLE,display,this, mainList,transportLayer,this);
		
	}
	
	private void initMainList(){
		//TODO These strings may need to be localised.
		mainList = new List(TITLE, Choice.IMPLICIT);
		mainList.insert(INDEX_ENTER_MARKS, "Enter Marks", null);
		mainList.insert(INDEX_DOWNLOAD_MARK_SHEET, "Download Mark Sheet", null);
		mainList.insert(INDEX_DOWNLOAD_CLASS_SUBJECT, "Download Classes & Subjects", null);
		mainList.insert(INDEX_UPLOAD_MARKS, "Upload Marks", null);
		mainList.insert(INDEX_SETTINGS, "Settings", null);
		mainList.insert(INDEX_LOGOUT, "Logout", null);

		mainList.addCommand(DefaultCommands.cmdOk);
		mainList.addCommand(DefaultCommands.cmdExit);
		
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		String val = settings.getSetting(KEY_LAST_SELECTED_MAIN_MENU_ITEM);
		if(val != null)
			mainList.setSelectedIndex(Integer.parseInt(val),true);
		
		mainList.setCommandListener(this);
	}

	protected void destroyApp(boolean arg0) {
	}

	protected void pauseApp() {
	}

	protected void startApp() {
		userMgr = new UserManager(display,mainList,TITLE,this);
		userMgr.logOn();
		//formMgr.setUserManager(userMgr);
		controller = new PurcaiController(display,mainList,transportLayer,userMgr);
	}

	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		try{
	        if (c == DefaultCommands.cmdExit)
	        	handledExitCommand();
	        else if(c == List.SELECT_COMMAND)
	        	handleListSelectCommand(((List)d).getSelectedIndex());
	        else if(c == DefaultCommands.cmdCancel)
	        	handledCancelCommand(d);
	        else if(c == DefaultCommands.cmdOk)
	        	handleOkCommand(d);
	        else if(c == DefaultCommands.cmdBack)
	        	handledBackCommand(d);
		}
		catch(Exception e){
			alertMsg.showError(e.getMessage());
			e.printStackTrace();
		}
    }
	
	/**
	 * Handles the back command.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handledBackCommand(Displayable d){
		handledCancelCommand(d);
	}
	
	/**
	 * Handles the cancel command.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handledCancelCommand(Displayable d){
    	this.display.setCurrent(mainList);
	}
	
	/**
	 * Handles the exit command.
	 *
	 */
	private void handledExitCommand(){
		destroyApp(false);
        notifyDestroyed();
	}
	
	/**
	 * Handles the list selection command.
	 * 
	 * @param selectedIndex - the index of the selected item.
	 */
	private void handleListSelectCommand(int selectedIndex){
		Displayable currentScreen = display.getCurrent();
		
		if(currentScreen == mainList)
			handleMainListSelectCommand(selectedIndex);
	}
	
	/**
	 * Handles the main list selection command.
	 * 
	 * @param selectedIndex - the index of the selected item.
	 */
	private void handleMainListSelectCommand(int selectedIndex){
		
		this.selectedIndex = selectedIndex;
		
		if(!userMgr.isLoggedOn()){
			userMgr.logOn();
			return;
		}
		
		switch(selectedIndex){
		case INDEX_DOWNLOAD_MARK_SHEET:
			controller.downloadMarkSheet();
			break;
		case INDEX_UPLOAD_MARKS:
			controller.uploadMarkSheet();
			break;
		case INDEX_ENTER_MARKS:
			controller.enterMarks(display, mainList);
			break;
		case INDEX_LOGOUT:
			logout();
			break;
		case INDEX_SETTINGS:
			/*transportLayer.setCurrentAlert(null);
			transportLayer.getUserSettings(display, mainList, userMgr.getUserName(),userMgr.getPassword());*/
			UserSettings userSettings = new UserSettings();
			userSettings.display(display, mainList, transportLayer,userMgr.getUserName(),userMgr.getPassword());
			break;
		case INDEX_DOWNLOAD_CLASS_SUBJECT:
			controller.downloadTestData();
			break;
		}
		
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		settings.setSetting(KEY_LAST_SELECTED_MAIN_MENU_ITEM, String.valueOf(selectedIndex));
		settings.saveSettings();
	}
	
	/**
	 * Handles the ok command.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleOkCommand(Displayable d){
		handleListSelectCommand(mainList.getSelectedIndex());
	}

	/**
	 * Called when an error occurs during any operation.
	 * 
	 * @param errorMessage - the error message.
	 * @param e - the exception, if any, that did lead to this error.
	 */
	public void errorOccured(String errorMessage, Exception e){
		if(e != null)
			errorMessage += " : "+ e.getMessage();
		alertMsg.showError(errorMessage);
	}
	
	public void cancelled(){
		display.setCurrent(mainList);
	}
	
	public void onAlertMessage(byte msg){
		alertMsg.turnOffAlert();
	}
	
	/**
	 * Called after data has been successfully downloaded.
	 * 
	 * @param dataOutParams - the parameters sent with the data.
	 * @param dataOut - the downloaded data.
	 */
	public void downloaded(Persistent dataOutParams, Persistent dataOut){
		userMgr.logOut();
	}
	
	//not used for now
	public void uploaded(Persistent dataOutParams, Persistent dataOut){
	}
	
	public boolean onLoggedOn(){
		boolean displayPrevScreen = false;
		if(selectedIndex != OpenXdataConstants.NO_SELECTION)
			handleMainListSelectCommand(selectedIndex);
		else
			displayPrevScreen = true;
		
		return displayPrevScreen;
	}
	
	public void onLogonCancel(){
		if(selectedIndex == OpenXdataConstants.NO_SELECTION)
			handledExitCommand();
		else
			display.setCurrent(mainList);
	}
	
	private void logout(){
		/** If this is not reset, after loggin in, we shall wrongly execute an action that
		 * the user did not intend to.*/
		this.selectedIndex = OpenXdataConstants.NO_SELECTION;
		
		userMgr.logOut();
		userMgr.logOn();
	}
	
	public void updateCommunicationParams(){
		
	}
	
	
	/**
	 * Called by the epihandy form manager when a form has been closed without saving.
	 * 
	 * @param data - the data in the form that has been cancelled.
	 */
	public void afterFormCancelled(FormData data){
		Alert alert = new Alert("FormCancelled","The form has not been saved",null,AlertType.CONFIRMATION);
		alert.setTimeout(Alert.FOREVER);
	}
	
	/**
	 * @see org.fcitmuk.epihandy.midp.forms.FormListener#afterFormSaved(org.fcitmuk.epihandy.FormData,java.lang.boolean)
	 */
	public void afterFormSaved(FormData formData, boolean isNew){		
		alertMsg.show("Form Saved Successfully.");
	}
	
	public boolean beforeFormCancelled(FormData data){
		return true;
	}
	
	public boolean beforeFormSaved(FormData data, boolean isNew){
		return true;
	}
	
	public boolean beforeFormDisplay(FormData data){
		return true;
	}
	//public boolean afterFormDisplay(FormData data); //is this event usefull?
	
	public boolean beforeQuestionEdit(QuestionData data){
		return true;
	}
	
	public boolean afterQuestionEdit(QuestionData data){
		return true;
	}
	
	//public boolean beforeRuleFire(SkipRule rule,QuestionData data);
	//public boolean afterRuleFire(SkipRule rule,QuestionData data);
	
	public boolean beforeFormDataListDisplay(FormDef formDef){
		return true;
	}
	
	public boolean beforeFormDelete(FormData data){
		return true;
	}
	
	public void afterFormDelete(FormData data){
		
	}
	
	public boolean beforeFormDefListDisplay(Vector formDefList){
		return true;
	}
}
