package org.openxdata.forms;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.openxdata.communication.TransportLayer;
import org.openxdata.communication.TransportLayerListener;
import org.openxdata.db.OpenXdataDataStorage;
import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.Settings;
import org.openxdata.db.util.StorageListener;
import org.openxdata.model.FormData;
import org.openxdata.model.FormDef;
import org.openxdata.model.OpenXdataConstants;
import org.openxdata.model.QuestionData;
import org.openxdata.model.QuestionDef;
import org.openxdata.model.RepeatQtnsData;
import org.openxdata.model.RepeatQtnsDef;
import org.openxdata.model.SkipRule;
import org.openxdata.model.StudyDef;
import org.openxdata.model.StudyDefList;
import org.openxdata.model.ValidationRule;
import org.openxdata.mvc.Controller;
import org.openxdata.mvc.View;
import org.openxdata.util.AlertMessage;
import org.openxdata.util.AlertMessageListener;
import org.openxdata.util.DefaultCommands;
import org.openxdata.util.MenuText;


/**
 * Manages cordination of views within the application. It knows all the views.
 * Views do not know about each other. All they know is the controller and as a result
 * send requests to it. These requests normally require switching of views and its the 
 * controller that knows which view to switch to.
 * In otherwards, views speak to each other through the controller.
 * 
 * @author Daniel Kayiwa
 *
 */
public class OpenXdataController implements Controller, StorageListener, AlertMessageListener,TypeEditorListener, TransportLayerListener {

	private boolean studyEditingMode;
	private FormListener formEventListener;
	private Displayable prevScreen;
	private AlertMessage alertMsg;
	private Hashtable transitionTable;
	private View currentView;
	private Display display;

	/** A list of user defined data type editors. */
	private Hashtable typeEditors = new Hashtable(); //TODO This need to be implemmted such that we allow flexibilty of user defined type editors for those who dont want to subclass the default type editor.

	private FormView formViewer = new FormView();
	private FormDefListView formDefListViewer = new FormDefListView();
	private FormDataListView formDataListViewer = new FormDataListView();
	private TypeEditor typeEditor = new DefaultTypeEditor();
	private StudyListView studyListViewer = new StudyListView();
	private RptQtnsDataListView rptQtnsDataListViewer = new RptQtnsDataListView();

	private DownloadUploadManager downloadMgr;
	private UserManager userMgr;
	private FormManager formMgr;

	/** No alert is currently displayed. */
	private static final byte CA_NONE = -1;
	private static final byte CA_SELECT_FORM_AFTER_FORMS_DOWNLOAD = 1;
	private static final byte CA_SELECT_FORM_AFTER_STUDY_DOWNLOAD = 2;
	private static final byte CA_SELECT_FORM_AFTER_STUDY_SELECT = 3;
	private static final byte CA_DOWNLOAD_FORMS_AFTER_STUDY_SELECT = 4;

	private byte currentAction = CA_NONE;
	
	private LogoutListener logoutListener;

	public OpenXdataController(){

	}

	public void init(String title, Display display,FormListener formEventListener, Displayable currentScreen,TransportLayer transportLayer, LogoutListener logoutListener){
		this.formEventListener = formEventListener;
		this.logoutListener = logoutListener;
		if (currentScreen != null) {
			this.prevScreen = currentScreen;
		} else {
			if (GeneralSettings.isHideStudies()) {
				formDefListViewer.setStudy(null);
				this.prevScreen = formDefListViewer.getScreen();
			} else {
				this.prevScreen = studyListViewer.getScreen();
			}
		}
		this.display = display;

		StudyDefList studyDefList = OpenXdataDataStorage.getStudyList();
		if(studyDefList != null && studyDefList.getStudies() != null)
			setStudyList(studyDefList.getStudies());
		else{
			StudyDef studyDef = OpenXdataDataStorage.getStudy(OpenXdataConstants.DEFAULT_STUDY_ID);
			if(studyDef != null)
				setStudy(studyDef); //those that dont deal with studies but just forms are grouped in one study with this id
		}

		//AbstractView.setDisplay(display);
		//AbstractView.setTitle(title);

		setDefaults(title);
		alertMsg = new AlertMessage(display, title, this.prevScreen, this);
		transitionTable = new Hashtable();

		if(formDefListViewer.getStudy() != null){
			String name = formDefListViewer.getStudy().getName();
			if(name != null && name.trim().length() > 0)
				prevScreen.setTitle(title + " - " + name);
		}
	}

	private void setDefaults(String title){
		studyListViewer.setController(this);
		formDefListViewer.setController(this);
		formDataListViewer.setController(this);
		formViewer.setController(this);
		typeEditor.setController(this);

		studyListViewer.setDisplay(display);
		formDefListViewer.setDisplay(display);
		formDataListViewer.setDisplay(display);
		formViewer.setDisplay(display);
		typeEditor.setDisplay(display);

		studyListViewer.setTitle(title);
		formDefListViewer.setTitle(title);
		formDataListViewer.setTitle(title);
		formViewer.setTitle(title);
		typeEditor.setTitle(title);
	}

	public void setPrevScreen(Displayable prevScreen){
		this.prevScreen = prevScreen;
	}
	
	public Displayable getPrevScreen() {
		return this.prevScreen;
	}

	public void setStudyEditingMode(boolean studyEditingMode){
		this.studyEditingMode = studyEditingMode;
	}

	private void showErrorMessage(String text, Exception e){
		/*if(e != null)
			e.printStackTrace();*/
		alertMsg.showError(text);
	}

	public void clearFormDataList(FormData formData, boolean deleteAll) {
		formDataListViewer.clearFormDataList(formData,deleteAll);		
	}	
	
	/**
	 * Shows a form given its data.
	 */
	public void showForm(boolean studyEditingMode,FormData formData, boolean allowDelete,Displayable currentScreen){
		try{
			//Null check is to prevent bug where we cant get off form list to go backwards.
			//This happens when clients dirrectly call showForm() and want back to go to
			//one of their form lists. (eg openclinica client)
			if (currentView == null)
				this.prevScreen = currentScreen;
			
			setStudyEditingMode(studyEditingMode);
			boolean displayForm = formEventListener.beforeFormDisplay(formData);
			if(displayForm){
				FireSkipRules(formData);
				this.formViewer.showForm(formData,formEventListener,allowDelete);
				saveCurrentView(formViewer);
			}
		}
		catch(Exception e){
			showErrorMessage("Exception:"+ e.getMessage(),e);
		}
	}

	/**
	 * Shows a form given its variable name.
	 */
	public void showForm(boolean studyEditingMode,String formVarName,boolean showNew,Displayable currentScreen){

		if(!formsDownloaded2())
			return;

		FormDef def = formDefListViewer.getStudy().getForm(formVarName);
		if(def == null){
			showErrorMessage(MenuText.NO_FORM_DEF() + " " + formVarName,null);
			return;
		}

		setStudyEditingMode(studyEditingMode);
		if(showNew){
			FormData data = new FormData(def);
			FireSkipRules(data);
			showForm(studyEditingMode,data,false,currentScreen);
		}
		else
			showFormDataList(def);
	}

	public void startEdit(QuestionData currentQuestion,int pos, int count){		
		//Inform the user that we are about to start editing.
		boolean edit = true;
		edit = formEventListener.beforeQuestionEdit(currentQuestion);

		//Check to see if the user (of the API) has not cancelled editing.
		if(!edit)
			return;

		TypeEditor editor = typeEditor;
		Byte type = new Byte(currentQuestion.getDef().getType());
		if(typeEditors.containsKey(type))
			editor = (TypeEditor)typeEditors.get(type);

		FormData formData = formViewer.getFormData();
		ValidationRule rule = formData.getDef().getValidationRule(currentQuestion.getId());
		if(rule != null)
			rule.setFormData(formData);
		editor.setTitle(formData.getDef().getName()+ " - " + formViewer.getTitle());
		editor.startEdit(currentQuestion,rule, GeneralSettings.isSingleQtnEdit() ,pos,count,this);
		//no need to save the current view since its managed by the form viewer.
	}

	/**
	 * Sets a custom editor of a question type.
	 * 
	 * @param type - the question type.
	 * @param typeEditor - the editor.
	 */
	public void setTypeEditor(byte type, TypeEditor typeEditor){
		this.typeEditors.put(new Byte(type), typeEditor);
	}

	/** Stops editing of a question. */
	public void endEdit(boolean save, QuestionData data, Command cmd){
		if(save){
			save = this.formEventListener.afterQuestionEdit(data);

			if(save){
				FireSkipRules(formViewer.getFormData());
				formViewer.getFormData().buildQuestionDataDescription();

				int type = data.getDef().getType();
				if(type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC)
					formViewer.getFormData().updateDynamicOptions(data,false);
			}
		}

		this.formViewer.onEndEdit(save == true,cmd);
		//no saving of current view since it was type editor displayed.
	}

	/** Fires rules in the form. */
	private void FireSkipRules(FormData formData){		
		Vector rules = formData.getDef().getSkipRules();
		if(rules != null && rules.size() > 0){
			for(int i=0; i<rules.size(); i++){
				//EpiHandySkipRule rule = (EpiHandySkipRule)rules.elementAt(i);
				SkipRule rule = (SkipRule)rules.elementAt(i);
				rule.fire(formData);
			}
		}
	}

	public void saveForm(FormData formData){
		//Give the user of the API a chance to do custom validation.
		boolean save = true;
		boolean isNew = formData.isNew();
		//The formViewer is the one to raise this event as failed validation may require redisplay 
		//of the form which is easier in that class than the controller.
		//formEventListener.beforeFormSaved(formData,save,isNew);
		if(save){
			saveFormData(formData);
			formEventListener.afterFormSaved(formData,isNew);
		}
	}

	/** Saves the current form data. */
	public void saveFormData(FormData formData){	
		boolean isNew = formData.isNew();

		formData.setDateValue("/"+formData.getDef().getVariableName()+"/endtime", new Date());

		if (OpenXdataDataStorage.saveFormData(formDefListViewer.getStudy().getId(),formData)) {
			currentView = (View)transitionTable.get(formViewer);
			transitionTable.remove(formViewer);

			if(this.studyEditingMode) {
				formDataListViewer.onFormSaved(formData, isNew);
			} else {
				display.setCurrent(this.prevScreen);
			}
		}
	}

	//garisa lodge
	public void deleteForm(FormData formData, View sender){
		if(sender == formViewer && formDataListViewer.hasSelectedForm()){
			formDataListViewer.deleteCurrentForm();
			//handleCancelCommand(sender);
		}
		else{
			boolean delete = true;
			delete  = formEventListener.beforeFormDelete(formData);

			if(delete){
				OpenXdataDataStorage.deleteFormData(formDefListViewer.getStudy().getId(),formData);
				formEventListener.afterFormDelete(formData);
				//handleCancelCommand(sender);
			}
		}
	}

	public Vector getStudyList(){
		Vector list = studyListViewer.getStudyList();

		if(list == null)
			return null;

		for(byte i=0; i<list.size(); i++){
			StudyDef studyDef = (StudyDef)list.elementAt(i);

			if (studyDef.getForms() == null || studyDef.getForms().size() == 0) {
				StudyDef retStudyDef = OpenXdataDataStorage.getStudy(studyDef.getId());
				if (retStudyDef != null) { //This can be null for studies whose forms have not yet been downloaded.
					studyDef.setForms(retStudyDef.getForms());
				}
			}
		}
		return studyListViewer.getStudyList();
	}

	public Vector getStudyListWithForms(){
		return studyListViewer.getStudyList();
	}

	public void setStudyList(Vector list){
		studyListViewer.setStudyList(list);

		//Get last selected study, if any, and set it as the default one.
		StudyDef study = null;
		Settings settings = new Settings(OpenXdataConstants.STORAGE_NAME_EPIHANDY_SETTINGS,true);
		String val = settings.getSetting(OpenXdataConstants.KEY_LAST_SELECTED_STUDY);
		
		if (!GeneralSettings.isHideStudies()) {
			if (val != null) {
				for(int i=0; i<list.size(); i++){
					study = (StudyDef)list.elementAt(i);
					if(study.getId() == Integer.parseInt(val)){
						formDefListViewer.setStudy(getStudyWithForms(null,study));
						break;
					}
				}
			}
	
			if (study == null) {
				formDefListViewer.setStudy(getStudyWithForms(list,(StudyDef)list.elementAt(0))); //should have at least one study.
			}
			
			if (!GeneralSettings.isMainMenu()) {
				studyListViewer.showStudyList(list);
			}
		}
	}

	public void setStudy(StudyDef studyDef){
		if(this.studyListViewer.getStudyList() == null || this.studyListViewer.getStudyList().size() == 0){
			Vector vect = new Vector();
			vect.addElement(studyDef);
			setStudyList(vect);
		}
		else {
			formDefListViewer.setStudy(studyDef);
		}
		if (!GeneralSettings.isMainMenu()) {
			formDefListViewer.showFormList(studyDef, formEventListener);
		}
	}
	
	public void setStudyList(StudyDefList studyDefList) {
		formDefListViewer.setStudies(studyDefList);
		if (!GeneralSettings.isMainMenu()) {
			formDefListViewer.showFormList(formEventListener);
		}
	}

	public void showFormDefList(StudyDef studyDef){
		if(formsDownloaded2()){
			this.formDefListViewer.showFormList(studyDef,formEventListener);
			saveCurrentView(formDefListViewer);
		}
	}

	public void showFormDataList(FormDef formDef){
		boolean display = true;
		FormDef frmDef = new FormDef(formDef);
		display = formEventListener.beforeFormDataListDisplay(frmDef);

		if(display){
			this.formDataListViewer.showFormList(frmDef, OpenXdataDataStorage.getFormData(formDefListViewer.getStudy().getId(), formDef.getId()));
			saveCurrentView(formDataListViewer);
		}
	}

	public void selectStudy(boolean forEditing){
		if(studyListViewer.getStudyList() == null || studyListViewer.getStudyList().size() == 0){
			//showErrorMessage("Please first download Study List.",null);
			if(currentAction != CA_DOWNLOAD_FORMS_AFTER_STUDY_SELECT)
				currentAction  = CA_SELECT_FORM_AFTER_STUDY_DOWNLOAD;
			downloadMgr.setTransportLayerListener(this);
			downloadMgr.downloadStudies(prevScreen,this.studyListViewer.getStudyList(),userMgr.getUserName(), userMgr.getPassword(),false);
		}
		else{
			this.setStudyEditingMode(forEditing);
			this.studyListViewer.showStudyList(studyListViewer.getStudyList());
			//saveCurrentView(studyListViewer);
		}
	}
	
	public void downloadStudies() {
		downloadMgr.downloadStudies(prevScreen, getStudyList(), userMgr.getUserName(), userMgr.getPassword(), true);
	}
	
	public void downloadForms() {
		downloadMgr.downloadForms(prevScreen, getStudyList(), userMgr.getUserName(), userMgr.getPassword(), true);
	}
	
	public void downloadStudyForms(Displayable currentScreen) {
		downloadMgr.downloadStudyForms(currentScreen, userMgr.getUserName(), userMgr.getPassword(), true);
	}
	
	public void displayUserSettings(Displayable currentScreen) {
		formMgr.displayUserSettings(display, currentScreen);
	}
	
	public void logout() {
		logoutListener.onLogout();
	}
	
	public void uploadData(Displayable currentScreen, Vector studyList) {
		downloadMgr.uploadData(currentScreen, studyList, null, userMgr.getUserName(), userMgr.getPassword());
	}
	
	public void uploadData(Displayable currentScreen, FormData formData) {
		downloadMgr.uploadData(currentScreen, null, formData, userMgr.getUserName(), userMgr.getPassword());
	}
	
	public void uploadData(Displayable currentScreen) {
		downloadMgr.uploadData(currentScreen, getStudyList(), null, userMgr.getUserName(), userMgr.getPassword());
	}

	public void closeStudyList(boolean save, StudyDef studyDef) {
		StudyDef currentStudy = getCurrentStudy();

		if (studyDef.getId() != currentStudy.getId()) {
			// only open selected study if a different/new study is selected
			studyDef = getStudyWithForms(null,studyDef);
	
			if (save) {
				//Save settings for next run (i think this is always set??)
				Settings settings = new Settings(OpenXdataConstants.STORAGE_NAME_EPIHANDY_SETTINGS,true);
				settings.setSetting(OpenXdataConstants.KEY_LAST_SELECTED_STUDY,String.valueOf(studyDef.getId()));
				settings.saveSettings();
	
				formDefListViewer.setStudy(studyDef);
	
				prevScreen.setTitle(alertMsg.getTitle() + " - " + studyDef.getName());
			}
		} else {
			studyDef = currentStudy; // currentStudy could contain forms
		}
		
		if (studyEditingMode || !GeneralSettings.isMainMenu()) {
			showFormDefList(studyDef);
		} else {
			display.setCurrent(prevScreen);
		}
	}

	/**
	 * Gets a study definition having forms instead of a blank one which just lists 
	 * the name and some little meta data about a study. Such blank studies
	 * are contained in StudyDefList objects.
	 * 
	 * @param studyList list of study definitions.
	 * @param studyDef a default study without forms, to return just incase none is found with forms.
	 * @return the study with forms if found, else the passed in default.
	 */
	private StudyDef getStudyWithForms(Vector studyList, StudyDef studyDef){

		StudyDef retStudyDef = null;

		if(studyList != null){ //get for all studies as passed
			for(int i=0; i<studyList.size(); i++){
				retStudyDef = (StudyDef)studyList.elementAt(i);
				retStudyDef = OpenXdataDataStorage.getStudy(retStudyDef.getId());

				if(retStudyDef != null)
					break;
			}
		}
		else //get only for the passed in study
			retStudyDef = OpenXdataDataStorage.getStudy(studyDef.getId());

		//If no forms saved yet, this will be null, hence we preserve the
		//blank study, to hold study info for the time being, until when we have forms
		if(retStudyDef == null)
			retStudyDef = studyDef;

		return retStudyDef;
	}

	public Vector getForms(){
		if(formDefListViewer.getStudy() != null)
			return this.formDefListViewer.getStudy().getForms();
		return null;
	}

	public void errorOccured(String errorMessage, Exception e){
		if(e != null)
			errorMessage += " : " + e.getMessage();
		showErrorMessage(errorMessage,e);
	}

	/**
	 * This is callback when one hits the Ok button for an alert message.
	 */
	public void onAlertMessage(byte msg){
		display.setCurrent(prevScreen);
	}

	/**
	 * Any view where the user hits the cancel command, calls into this method
	 * to allow the controller display the previous view.
	 */
	public void handleCancelCommand(Object viewer){
		View view = (View)transitionTable.get(viewer);
		if(view != null){
			transitionTable.remove(viewer);
			view.show();
			currentView = view;
		}
		else{
			display.setCurrent(prevScreen);
			currentView = null;
		}
	}

	/**
	 * Before displaying a view, saves the one which was current.
	 * This is for rembering view to diplay on closing one or pressing the back button.
	 * 
	 * @param newView - the view which is to be displayed.
	 */
	private void saveCurrentView(View newView){
		if(currentView != null) { // && !(view.equals(currentView))
			transitionTable.put(newView, currentView);
		}

		currentView = newView;
	}

	/**
	 * For the curren study, shows alist of forms and allows one to start entering data in any selected one.
	 */
	public void selectForm(boolean editingMode, Displayable currentScreen){
		setStudyEditingMode(editingMode);
		setPrevScreen(currentScreen);

		if(!GeneralSettings.isHideStudies() && formDefListViewer.getStudy() == null){
			currentAction  = CA_SELECT_FORM_AFTER_STUDY_SELECT;
			this.selectStudy(editingMode);
		}
		else if(formsDownloaded2()){
			formDefListViewer.showFormList(formEventListener);
			saveCurrentView(formDefListViewer);
		}
		else
			currentAction  = CA_SELECT_FORM_AFTER_FORMS_DOWNLOAD;
	}

	private boolean formsDownloaded2(){
		boolean bReturn = false;

		/*String studyName = "";
		if(formDefListViewer.getStudy() != null)
			studyName = " for "+formDefListViewer.getStudy().getName();

		if(formDefListViewer.getStudy() == null || formDefListViewer.getStudy().getForms() == null || formDefListViewer.getStudy().getForms().size() == 0)
			showErrorMessage("Please first download forms"+studyName,null);
		else
			bReturn = true;*/
		
		if(formDefListViewer.getStudy() == null || formDefListViewer.getStudy().getForms() == null || formDefListViewer.getStudy().getForms().size() == 0) {
			if (GeneralSettings.isHideStudies()) {
				currentAction  = CA_SELECT_FORM_AFTER_FORMS_DOWNLOAD;
				downloadMgr.setTransportLayerListener(this);
				downloadMgr.downloadAllForms(prevScreen,userMgr.getUserName(), userMgr.getPassword(),false);
			} else {
				currentAction  = CA_SELECT_FORM_AFTER_FORMS_DOWNLOAD;
				downloadMgr.setTransportLayerListener(this);
				downloadMgr.downloadStudyForms(prevScreen,userMgr.getUserName(), userMgr.getPassword(),false);
				//downloadMgr.downloadForms(prevScreen,userMgr.getUserName(), userMgr.getPassword(),false);
			}
		} else {
			bReturn = true;
		}

		return bReturn;
	}

	public void showForm(boolean studyEditingMode,int studyId,FormDef formDef, int formDataRecordId,boolean allowDelete,Displayable currentScreen){
		FormData formData = OpenXdataDataStorage.getFormData(studyId, formDef.getId(), formDataRecordId);
		formData.setDef(formDef);
		showForm(studyEditingMode,formData,allowDelete,currentScreen);
	}

	public void execute(View view, Object cmd, Object data){
		if(cmd == DefaultCommands.cmdCancel)
			display.setCurrent(prevScreen);
		else{
			if(view instanceof StudyListView)
				closeStudyList(true,(StudyDef)data);
		}
	}

	public StudyDef getCurrentStudy(){
		StudyDef study = formDefListViewer.getStudy();

		if(study == null && !GeneralSettings.isHideStudies()){
			currentAction = CA_DOWNLOAD_FORMS_AFTER_STUDY_SELECT;
			this.selectStudy(this.studyEditingMode);
		}
		return study;
	}

	public void showRepeatQtnsDataList(RepeatQtnsDef repeatQtnsDef){
		//this.formDataListViewer.showFormList(formDef, EpihandyDataStorage.getFormData(formDefListViewer.getStudy().getId(), formDef.getId()));
		//saveCurrentView(formDataListViewer);

		//rptQtnsDataListViewer.showQtnDataList(rptQtnsDef, rptQtnsDataList);
		saveCurrentView(rptQtnsDataListViewer);

	}

	public void showRepeatQtnsRow(RepeatQtnsData repeatQtnsData){

	}

	public boolean studiesDownloaded(){
		return !(studyListViewer.getStudyList() == null || studyListViewer.getStudyList().size() == 0);
	}

	public boolean formsDownloaded(){
		return !(formDefListViewer.getStudy() == null || formDefListViewer.getStudy().getForms() == null || formDefListViewer.getStudy().getForms().size() == 0);
	}

	public void backToMainMenu(){
		display.setCurrent(prevScreen);
	}

	public void setDownloadManager(DownloadUploadManager downloadMgr){
		this.downloadMgr = downloadMgr;
	}

	public void setUserManager(UserManager userMgr){
		this.userMgr = userMgr;
	}
	
	public void setFormManager(FormManager formMgr) {
		this.formMgr = formMgr;
	}

	public void downloaded(Persistent dataOutParams, Persistent dataOut) {
		if(currentAction == CA_SELECT_FORM_AFTER_FORMS_DOWNLOAD)
			selectForm(this.studyEditingMode, prevScreen);
		else if(currentAction == CA_SELECT_FORM_AFTER_STUDY_DOWNLOAD){
			if(studyListViewer.getStudyList().size() == 1){
				//selectForm(this.studyEditingMode, prevScreen);
				//currentAction = CA_SELECT_FORM_AFTER_FORMS_DOWNLOAD;
				//return;
				this.execute(studyListViewer, DefaultCommands.cmdOk, studyListViewer.getStudyList().elementAt(0));
				currentAction = CA_SELECT_FORM_AFTER_FORMS_DOWNLOAD;
				return;

				//this.setStudyEditingMode(this.studyEditingMode);
				//this.studyListViewer.showStudyList(studyListViewer.getStudyList());
				//closeStudyList(true,(StudyDef)studyListViewer.getStudyList().elementAt(0));
			}
			else
				selectStudy(this.studyEditingMode);
		}
		else if(currentAction == CA_DOWNLOAD_FORMS_AFTER_STUDY_SELECT)
			this.downloadMgr.downloadStudyForms(prevScreen, userMgr.getUserName(), userMgr.getPassword(), false);
		//this.downloadMgr.downloadForms(prevScreen, userMgr.getUserName(), userMgr.getPassword(), false);
		//CA_SELECT_FORM_AFTER_FORMS_DOWNLOAD
		//if(currentAction != CA_SELECT_FORM_AFTER_STUDY_DOWNLOAD)
		currentAction = CA_NONE;
	}

	public void uploaded(Persistent dataOutParams, Persistent dataOut) {

	}

	public void cancelled(){

	}

	public void updateCommunicationParams(){

	}
}
