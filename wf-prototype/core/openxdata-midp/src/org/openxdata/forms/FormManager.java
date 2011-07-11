package org.openxdata.forms;


import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.openxdata.communication.TransportLayer;
import org.openxdata.communication.TransportLayerListener;
import org.openxdata.db.util.Persistent;
import org.openxdata.model.FormData;
import org.openxdata.model.FormDef;
import org.openxdata.model.QuestionData;
import org.openxdata.model.QuestionDef;
import org.openxdata.model.StudyDef;
import org.openxdata.mvc.AbstractView;
import org.openxdata.util.DefaultCommands;
import org.openxdata.util.MenuText;


/** 
 * Handles display of forms for data entry. This class acts as a facade to the epihandy forms engine.
 * This class uses the epihandy controller to manage view interactions with the user.
 * It also handles security issues.
 * 
 * @author Daniel Kayiwa
 *
 */
public class FormManager implements TransportLayerListener{
	
	/** Refefence to current display. */
	//private Display display;
	
	/** The application title. This is for displaying in things like alert titles. */
	private String title;
	
	/** The screen to display after all our screens have been closed.
	 * This, for the user of this class, would be the current screen displayed
	 * before a method of this class is called.
	 */
	private Displayable prevScreen;
	
	private DownloadUploadManager downloadMgr;
	private OpenXdataController controller;
	private UserManager userMgr;
	TransportLayer transportLayer;
	TransportLayerListener transportLayerListener;
	//private boolean downloadingForms = false;
	
	private static FormManager instance;
	
	
	public static FormManager getInstance(){
		return instance;
	}
	
	//TODO This is a temporary hack and should be dealt with smartly.
	public static void setGlobalInstance(FormManager formManager){
		instance = formManager;
	}
	
	/**
	 * Creates a new instance of form manager.
	 * 
	 * @param title - the title of the application. This is to be used for titles like in alerts.
	 * @param display - a reference to the display.
	 * @param formEventListener - a listener to the form events.
	 * @param currentScreen - the screen currently displayed.
	 * @param transportLayer - a reference to the transportLayer object.
	 * @param transportLayerListener - a reference to the listener to transport layer events.
	 */
	public FormManager(String title,Display display, FormListener formEventListener, Displayable currentScreen,TransportLayer transportLayer,TransportLayerListener transportLayerListener, LogoutListener logoutListener){
		//this.display = display;
		this.title = title;		
		this.prevScreen = currentScreen;
		this.transportLayerListener = transportLayerListener;
		this.transportLayer = transportLayer;
		AbstractView.display = display;
				
		controller = new OpenXdataController();
		controller.init(title, display, formEventListener, currentScreen, transportLayer, logoutListener);
		
		if (currentScreen == null) {
			// it will be initialised by the controller if it was null
			this.prevScreen = controller.getPrevScreen();
			transportLayer.setPrevScreen(this.prevScreen);
		}
		
		//Just testing type editor extension;
		RepeatTypeEditor rptEditor = new RepeatTypeEditor();
		rptEditor.setController(controller);
		controller.setTypeEditor(QuestionDef.QTN_TYPE_REPEAT, rptEditor);
		
		MultmediaTypeEditor mmEditor = new MultmediaTypeEditor();
		mmEditor.setController(controller);
		controller.setTypeEditor(QuestionDef.QTN_TYPE_IMAGE, mmEditor);
		controller.setTypeEditor(QuestionDef.QTN_TYPE_VIDEO, mmEditor);
		controller.setTypeEditor(QuestionDef.QTN_TYPE_AUDIO, mmEditor);
		
		userMgr = new UserManager(display,prevScreen,title,null);
		downloadMgr = new DownloadUploadManager(transportLayer, controller, title,this);
		
		((OpenXdataController)controller).setDownloadManager(downloadMgr);
		((OpenXdataController)controller).setUserManager(userMgr);
		((OpenXdataController)controller).setFormManager(this);
		
		if(GeneralSettings.isOkOnRight()){
			DefaultCommands.cmdOk = new Command(MenuText.OK(), Command.CANCEL, 1);
			DefaultCommands.cmdSave = new Command(MenuText.SAVE(),Command.CANCEL,2);
		}
		
		QuestionData.dateDisplayFormat = DateSettings.getDateFormat();
	}

	public void setUserManager(UserManager userManager){
		this.userMgr = userManager;
		((OpenXdataController)controller).setUserManager(userMgr);
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Sets a custom editor of a question type.
	 * 
	 * @param type - the question type.
	 * @param typeEditor - the editor.
	 */
	public void setTypeEditor(byte type, TypeEditor typeEditor){
		controller.setTypeEditor(type, typeEditor);
	}
	
	/*public void setFormViewer(FormView formViewer){
		//this.typeEditors.put(new Byte(type), typeEditor);
	}
	
	public void setFormListViewer(FormDataListView formListViewer){
		//this.typeEditors.put(new Byte(type), typeEditor);
	}
	
	public void setStudyListViewer(StudyListView studyListViewer){
		//this.typeEditors.put(new Byte(type), typeEditor);
	}*/
	
	private boolean isUserLoggedOn(){
		if(userMgr.isLoggedOn())
			return true;
		else
			userMgr.logOn();
		return false;
	}
	
	/**
	 * Displays a list of studies.
	 *
	 *@param forEditing - when true, means that if a study is selected, 
	 *					  we should display its forms for editing.
	 */
	public void selectStudy(boolean forEditing){
		if(isUserLoggedOn())
			this.controller.selectStudy(forEditing);
	}
	
	public StudyDef getCurrentStudy(){
		return this.controller.getCurrentStudy();
	}
	
	public void showForm(boolean studyEditingMode,String formVarName,boolean showNew,Display display,Displayable currentScreen,FormListener formEventListener){	
		//this.display = display;
		this.prevScreen = currentScreen;
		if(isUserLoggedOn())
			this.controller.showForm(studyEditingMode,formVarName,showNew,currentScreen);
	}
	
	/**
	 * Displays a form for editing.
	 * 
	 * @param data - the form data.
	 */
	public void showForm(boolean studyEditingMode,FormData data, boolean allowDelete,Displayable currentScreen){
		this.prevScreen = currentScreen;
		if(isUserLoggedOn())
			this.controller.showForm(studyEditingMode,data,allowDelete,currentScreen);
	}
	
	public void showForm(boolean studyEditingMode,int studyId,FormDef formDef, int formDataRecordId,boolean allowDelete,Displayable currentScreen){
		if(isUserLoggedOn())
			this.controller.showForm(studyEditingMode,studyId, formDef,formDataRecordId, allowDelete,currentScreen);
	}
	
	public void downloadForms(Displayable currentScreen, boolean confirm){
		if(isUserLoggedOn())
			downloadMgr.downloadForms(prevScreen,controller.getStudyList(),userMgr.getUserName(), userMgr.getPassword(),confirm);
	}
	
	public void downloadStudyForms(Displayable currentScreen, boolean confirm){
		if(isUserLoggedOn())
			downloadMgr.downloadStudyForms(prevScreen,userMgr.getUserName(), userMgr.getPassword(),confirm);
	}
	
	/**
	 * Downloads studies from the server.
	 * 
	 * @param currentScreen - the currently displayed screen.
	 */
	//TODO Downloading new studies may need to clear up existing forms and data in the no longer existing studies.
		   //Collected form data cant be seen for when the study is no longer existant, yet this data is still on the phone.
	public void downloadStudies(Displayable currentScreen){
		if(isUserLoggedOn())	
			downloadMgr.downloadStudies(prevScreen,controller.getStudyList(),userMgr.getUserName(), userMgr.getPassword(),true);
	}
	
	public boolean isThereCollectedData(String name){
		return downloadMgr.isThereCollectedData(name,getStudyList());
	}
	
	public void downloadLanguages(Displayable currentScreen, boolean confirm){
		if(isUserLoggedOn())
			downloadMgr.downloadLanguages(currentScreen,controller.getStudyList(),userMgr.getUserName(), userMgr.getPassword(),confirm);
	}
	
	public void downloadMenuText(Displayable currentScreen, boolean confirm){
		if(isUserLoggedOn())
			downloadMgr.downloadMenuText(currentScreen,controller.getStudyList(),userMgr.getUserName(), userMgr.getPassword(),confirm);
	}
	
	/**
	 * Uplaoads collected form data to the server.
	 * 
	 * @param currentScreen - the currently displayed screen.
	 */
	public void uploadData(Displayable currentScreen){
		if(isUserLoggedOn())
			downloadMgr.uploadData(prevScreen, controller.getStudyList(), null, userMgr.getUserName(), userMgr.getPassword());
	}
	
	public void selectForm(boolean studyEditingMode,Displayable currentScreen){
		if(isUserLoggedOn())
			this.controller.selectForm(studyEditingMode,currentScreen);
	}
	
	/**
	 * Gets a list of studies.
	 * 
	 * @return
	 */
	public Vector getStudyList(){
		return controller.getStudyList();
	}
	
	public Vector getForms(){
		return controller.getForms();
	}
	
	public void uploaded(Persistent dataOutParams, Persistent dataOut){
		if(transportLayerListener != null)
			transportLayerListener.uploaded(dataOutParams, dataOut);
	}
	
	public void downloaded(Persistent dataOutParams, Persistent dataOut){
		if(transportLayerListener != null)
			transportLayerListener.downloaded(dataOutParams, dataOut);
	}
	
	public void errorOccured(String errorMessage, Exception e){
		if(transportLayerListener != null)
			transportLayerListener.errorOccured(errorMessage, e);
	}
	
	public void cancelled(){
		if(transportLayerListener != null)
			transportLayerListener.cancelled();
	}

	
	public void displayUserSettings(Display display, Displayable prevScreen){
		UserSettings userSettings = new UserSettings();
		userSettings.display(display, prevScreen, transportLayer,userMgr.getUserName(),userMgr.getPassword());
	}
	
	public void updateCommunicationParams(){
		
	}
	
	public void restorePrevScreen(){
		downloadMgr.setPrevSrceen(transportLayer.getPrevScreen());
	}
	
	public Displayable getPrevScreen() {
		return this.prevScreen;
	}
}
