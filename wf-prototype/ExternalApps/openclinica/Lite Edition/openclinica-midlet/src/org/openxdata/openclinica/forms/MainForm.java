package org.openxdata.openclinica.forms;

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

import org.openxdata.communication.ConnectionParameter;
import org.openxdata.communication.TransportLayer;
import org.openxdata.communication.TransportLayerListener;
import org.openxdata.db.OpenXdataDataStorage;
import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.Settings;
import org.openxdata.db.util.StorageListener;
import org.openxdata.forms.DateSettings;
import org.openxdata.forms.FormListener;
import org.openxdata.forms.FormManager;
import org.openxdata.forms.LogonListener;
import org.openxdata.forms.UserManager;
import org.openxdata.model.FormData;
import org.openxdata.model.FormDef;
import org.openxdata.model.OpenXdataConstants;
import org.openxdata.model.QuestionData;
import org.openxdata.model.ResponseHeader;
import org.openxdata.openclinica.CrfDef;
import org.openxdata.openclinica.StudyEvent;
import org.openxdata.openclinica.StudyEventList;
import org.openxdata.openclinica.StudyRequestHeader;
import org.openxdata.openclinica.Subject;
import org.openxdata.openclinica.SubjectData;
import org.openxdata.openclinica.SubjectEvent;
import org.openxdata.openclinica.SubjectForm;
import org.openxdata.openclinica.VarNames;
import org.openxdata.openclinica.db.OpenclinicaDataStorage;
import org.openxdata.util.AlertMessage;
import org.openxdata.util.AlertMessageListener;
import org.openxdata.util.DefaultCommands;
import org.openxdata.util.Utilities;


/** This is the main midlet that displays the main user inteface for openclinica. 
 * 
 * @author Daniel Kayiwa
 *
 */
public class MainForm extends MIDlet  implements CommandListener,FormListener,StorageListener,AlertMessageListener, TransportLayerListener,LogonListener, Runnable{

	/** Reference to the current display. */
	private Display display;

	/** The main menu screen. */   
	private List mainList;

	/** Screen for displaying a list of subjects searched. */
	private List subjectList;

	/** Screen for displaying details of the selected subject. eg Bio data, last visit date,etc. */
	private List subjectDetailList;

	private List formList;

	private List subjectEventList;

	/** Screen for entering subject search criteria. */
	private SubjectSearchForm subjectSearchForm;

	/** Index for search subject menu item. */
	private static final int INDEX_SEARCH_SUBJECT = 0;

	/** Index for new subject menu item. */
	//private static final int INDEX_NEW_SUBJECT = 1;

	/** Index for selecting an encounter form menu item. */
	//private static final int INDEX_SELECT_FORM = 2;

	/** Index for selecting a study menu item. */
	private static final int INDEX_SELECT_STUDY = 1;

	/** Index for downloading study list menu item. */
	private static final int INDEX_DOWNLOAD_STUDY_LIST = 2;

	///** Index for downloading studies menu item. */
	//private static final int INDEX_DOWNLOAD_STUDIES= 3;

	/** Index for downloading cohorts menu item. */
	//private static final int INDEX_DOWNLOAD_COHORTS= 5;

	/** Index for download subjects menu item. */
	private static final int INDEX_DOWNLOAD_SUBJECTS = 3;
	
	/** Index for download crfs menu item. */
	private static final int INDEX_DOWNLOAD_STUDY_EVENTS = 4;

	/** Index for download crfs menu item. */
	private static final int INDEX_DOWNLOAD_CRFS = 5;

	/** Index for upload data menu item. */
	private static final int INDEX_UPLOAD_DATA = 6;

	/** Index for specifying settings like server connection parameters. */
	private static final int INDEX_SETTINGS = 7;

	/** Index for selecting a study menu item. */
	private static final int INDEX_LOGOUT = 8;

	/** Application tittle. */
	private static final String TITLE = "OpenClinica 1.0";

	/** Status to download a list of subjects from the server. */
	public static final byte ACTION_DOWNLOAD_SUBJECTS = 6;

	/** Status to download a list of study events from the server. */
	public static final byte ACTION_DOWNLOAD_STUDY_EVENTS = 19;


	/** Reference to epihandy form manager. */
	private FormManager formMgr;

	/** Reference to the transportLayer. */
	private TransportLayer transportLayer;

	private AlertMessage alertMsg;

	/** No alert is currently displayed. */
	private static final byte CA_NONE = -1;

	/** Current alert is for subject download confirmation. */
	private static final byte CA_SUBJECT_DOWNLOAD = 1;

	/** Current alert is for subject search. */
	private static final byte CA_SUBJECT_SEARCH = 2;

	/** Current alert is for crf download confirmation. */
	private static final byte CA_CRF_DOWNLOAD = 5;

	private static final byte CA_SUBJECT_EVENTS = 6;

	/** Current alert is for study events download confirmation. */
	private static final byte CA_STUDY_EVENTS_DOWNLOAD = 7;
	
	private static final byte CA_CRF_LIST = 8;

	private static byte currentAction = CA_NONE;


	/** The current subject. */
	private Subject subject;

	/** List of subjects. */
	private Vector subjects;

	/** List of study events for the selected subject. */
	private Vector studyEvents;

	/** The current study event. */
	StudyEvent studyEvent;

	//private Vector subjectEvents;

	/** The user manager object. */
	private UserManager userMgr;

	/** The current selected index of the main menu. For now, this is used to keep track of
	 * the user's action to return to after successfully logging in. This happens when the user
	 * tries to do something before logging in, and the logon mananer intervenes by requiring the
	 * user to first login. This happens after downloading forms because a new list of users is got
	 * which makes void the current users info. */
	private int selectedIndex = OpenXdataConstants.NO_SELECTION;


	private static final String KEY_LAST_SELECTED_MAIN_MENU_ITEM =  "LAST_SELECTED_MAIN_MENU_ITEM";
	private static final String STORAGE_NAME_SETTINGS = "STORAGE_OPENCLINICA_SETTINGS";

	private static String NAME_SUBJECT_DOWNLOAD_URL = "Subjects download url:";


	/** Construct the main UI midlet. */
	public MainForm() {
		super();

		display = Display.getDisplay(this);

		initMainList();

		transportLayer = new TransportLayer();
		transportLayer.setDisplay(display);
		transportLayer.setPrevScreen(mainList);
		transportLayer.setDefaultCommunicationParameter(TransportLayer.KEY_BLUETOOTH_SERVER_ID, /*"F0E0D0C0B0A000908070605040302010"*/ "F0E0D0C0B0A000908070605040301117");
		transportLayer.setDefaultCommunicationParameter(TransportLayer.KEY_HTTP_URL, "");
		transportLayer.setDefaultCommunicationParameter(TransportLayer.KEY_SMS_DESTINATION_ADDRESS,"sms://+256714380638"); //256712330386 //256782380638 "sms://+256782380638:1234"
		transportLayer.setDefaultCommunicationParameter(TransportLayer.KEY_SMS_SOURCE_ADDRESS,"sms://:1234"); 
		transportLayer.addConnectionParameter(new ConnectionParameter(TransportLayer.CON_TYPE_HTTP,NAME_SUBJECT_DOWNLOAD_URL,"http://localhost:8888/openclinica"));
		//transportLayer.addConnectionParameter(new ConnectionParameter(TransportLayer.CON_TYPE_HTTP,NAME_COHORT_DOWNLOAD_URL,"http://localhost:8080/openclinica/module/xforms/subjectDownload.form?downloadCohorts=true"));

		formMgr = //new FormManager(TITLE,display,this, mainList,transportLayer,this);
		new FormManager(TITLE, display, null, mainList, transportLayer, formMgr, null);
		FormManager.setGlobalInstance(formMgr);
		//FormManager.useStudyNumericId = false;
		
		OpenXdataDataStorage.storageListener = this;
		OpenclinicaDataStorage.storageListener = this;
		
		alertMsg = new AlertMessage(this.display, TITLE, this.mainList,this);
	}

	private void initMainList(){
		//TODO These strings may need to be localised.
		mainList = new List(TITLE, Choice.IMPLICIT);
		mainList.insert(INDEX_SEARCH_SUBJECT, "Select Subject", null);
		mainList.insert(INDEX_SELECT_STUDY, "Change Study", null);
		mainList.insert(INDEX_DOWNLOAD_STUDY_LIST, "Download Studies", null);
		mainList.insert(INDEX_DOWNLOAD_SUBJECTS, "Download Subjects", null);
		mainList.insert(INDEX_DOWNLOAD_STUDY_EVENTS, "Download Events", null);
		mainList.insert(INDEX_DOWNLOAD_CRFS, "Download CRFs", null);
		mainList.insert(INDEX_UPLOAD_DATA, "Upload Data", null);
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
		formMgr.setUserManager(userMgr);
		//display.setCurrent(mainList);
		//System.out.println(org.fcitmuk.db.util.PersistentHelper.getSize(StudyDefTest.getTestStudyDef()));
		//System.out.println(getAppProperty("SMS-Port"));
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
		//display.setCurrent(mainList);
		handledCancelCommand(d);
	}

	/**
	 * Handles the cancel command.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handledCancelCommand(Displayable d){
		if(d == subjectDetailList)
			this.display.setCurrent(subjectList);
		else if(d == subjectList)
			this.display.setCurrent(subjectSearchForm);
		else if(d == subjectEventList)
			this.display.setCurrent(subjectDetailList);
		else if(d == formList)
			this.display.setCurrent(subjectEventList);
		else
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
		else if(currentScreen == subjectList)
			selectSubject(selectedIndex);
		else if(currentScreen == subjectDetailList)
			displaySubjectEvents();
		else if(currentScreen == subjectEventList)
			selectEvent(selectedIndex);
		else if(currentScreen == formList){
			CrfDef crf = (CrfDef)studyEvent.getCrfs().elementAt(selectedIndex);
			FormDef formDef = formMgr.getCurrentStudy().getFormWithKey(crf.getOid());

			if(formDef == null){
				currentAction = CA_CRF_LIST;
				alertMsg.show("No CRF version downloaded with OID = " + crf.getOid());
			}
			else{
				int formDataRecordId = OpenclinicaDataStorage.getSubjectFormRecordId(subject.getSubjectId(), formDef.getId(), studyEvent.getEventId());
				if(formDataRecordId != OpenXdataConstants.NULL_ID)
					formMgr.showForm(false,formMgr.getCurrentStudy().getId(), formDef, formDataRecordId,true,formList);
				else{
					FormData formData = new FormData(formDef);
					formData.setTextValue("/ODM/ClinicalData/@UserID", userMgr.getUserId()+"");
					formData.setTextValue("/ODM/ClinicalData/@StudyOID", formMgr.getCurrentStudy().getVariableName());
					formData.setTextValue("/ODM/ClinicalData/SubjectData/@SubjectKey", subject.getOid());
					formData.setTextValue("/ODM/ClinicalData/SubjectData/StudyEventData/@StudyEventOID", studyEvent.getOid());
					
					formMgr.showForm(false,formData,false,formList);
				}
			}
		}
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
		
		/*System.out.println(formMgr.getCurrentStudy());
		System.out.println(selectedIndex);
		if(formMgr.getCurrentStudy() == null && (selectedIndex == INDEX_SEARCH_SUBJECT ||
				selectedIndex == INDEX_DOWNLOAD_CRFS || selectedIndex == INDEX_DOWNLOAD_SUBJECTS ||
				selectedIndex == INDEX_DOWNLOAD_STUDY_EVENTS)){
			
			alertMsg.show("Please first download studies");
			return;
		}*/

		switch(selectedIndex){
		case INDEX_SELECT_STUDY:
			this.formMgr.selectStudy(false);
			break;
			/*case INDEX_NEW_SUBJECT:        		
			createNewSubject();
			break;*/
		case INDEX_SEARCH_SUBJECT:
			searchSubject(selectedIndex);
			break;
		case INDEX_DOWNLOAD_STUDY_LIST:
			formMgr.downloadStudies(mainList);
			break;
		case INDEX_DOWNLOAD_CRFS:
			downloadCrfs();
			break;
		case INDEX_UPLOAD_DATA:
			this.formMgr.uploadData(mainList);
			break;
		case INDEX_DOWNLOAD_SUBJECTS:
			downloadSubjects();
			break;
		case INDEX_DOWNLOAD_STUDY_EVENTS:
			downloadStudyEvents();
			break;
		case INDEX_LOGOUT:
			logout();
			break;
		case INDEX_SETTINGS:
			formMgr.displayUserSettings(display, mainList);
			break;
		}

		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		settings.setSetting(KEY_LAST_SELECTED_MAIN_MENU_ITEM, String.valueOf(selectedIndex));
		settings.saveSettings();
	}

	/** Downloads patiens from the server. */
	private void downloadSubjects(){
		currentAction = CA_SUBJECT_DOWNLOAD;
		alertMsg.showConfirm("Do you really want to download subjects?");
	}

	/** Downloads crfs from the server. */
	private void downloadCrfs(){
		if(!formMgr.isThereCollectedData("CRFs")){
			currentAction = CA_CRF_DOWNLOAD;
			alertMsg.showConfirm("Do you really want to download CRFs?");
		}
	}
	
	/** Downloads crfs from the server. */
	private void downloadStudyEvents(){
		if(!formMgr.isThereCollectedData("Events")){
			currentAction = CA_STUDY_EVENTS_DOWNLOAD;
			alertMsg.showConfirm("Do you really want to download study events?");
		}
	}

	/** Displays a list of subjects according to the user search criteria of id and name. */
	private void displaySubjectList(){
		subjectList = new List("Subjects - " + TITLE,Choice.IMPLICIT);
		subjectList.addCommand(DefaultCommands.cmdOk);
		subjectList.addCommand(DefaultCommands.cmdBack);

		currentAction = CA_SUBJECT_SEARCH;

		subjects  = getSubjects(subjectSearchForm.getPersonId(),subjectSearchForm.getStudySubjectId());
		if(subjects != null && subjects.size() > 0){
			for(int i=0; i<subjects.size(); i++)
				subjectList.append(((Subject)subjects.elementAt(i)).toString(),null);

			subjectList.setCommandListener(this);
			display.setCurrent(subjectList);
		}
		else
			alertMsg.show("No subjects found. If you have just uploaded data including new subjects, you may need to download subjects again.");
	}


	private void displaySubjectEvents(){
		subjectEventList = new List("Events - " + TITLE,Choice.IMPLICIT);
		subjectEventList.addCommand(DefaultCommands.cmdOk);
		subjectEventList.addCommand(DefaultCommands.cmdBack);

		currentAction = CA_SUBJECT_EVENTS;

		studyEvents = new Vector();
		Vector events = subject.getEvents();

		if(events != null && events.size() > 0){
			for(int i=0; i<events.size(); i++){
				SubjectEvent subjectEvent = (SubjectEvent)events.elementAt(i);
				StudyEvent event = OpenclinicaDataStorage.getStudyEvent(formMgr.getCurrentStudy().getId(), subjectEvent.getEventId());
				subjectEventList.append(event.getName(),null);
				studyEvents.addElement(event);
			}

			subjectEventList.setCommandListener(this);
			display.setCurrent(subjectEventList);
		}
		else
			alertMsg.show("No events scheduled for this subject.");
	}


	/** Displays a list of forms. */
	private void displayFormList(){	
		formMgr.selectForm(true, display.getCurrent());
	}

	/**
	 * Handles the ok command.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleOkCommand(Displayable d){
		if(d == subjectSearchForm)
			displaySubjectList();
		else if(d == subjectDetailList)
			displaySubjectEvents();
		else if(d == subjectEventList)
			displayFormList();
		else if(d == subjectList)
			handleListSelectCommand(subjectList.getSelectedIndex());
		else if(d == mainList)
			handleListSelectCommand(mainList.getSelectedIndex());
	}

	/** 
	 * Displays details for the selected subject.
	 * These details, for now, are bio data, but could include things like last visit date, etc.
	 * 
	 * @param selectedIndex - the index of the currently selected subject.
	 */
	private void selectSubject(int selectedIndex){
		subjectDetailList = new List("Subject Details - " + TITLE,Choice.IMPLICIT);
		subjectDetailList.addCommand(DefaultCommands.cmdOk);
		subjectDetailList.addCommand(DefaultCommands.cmdBack);

		subject = (Subject)subjects.elementAt(selectedIndex);
		fillSubjectDetails(subjectDetailList);

		subjectDetailList.setCommandListener(this);	
		display.setCurrent(subjectDetailList);
	}

	private void selectEvent(int selectedIndex){
		studyEvent = (StudyEvent)studyEvents.elementAt(selectedIndex);
		Vector crfs = studyEvent.getCrfs();

		formList = new List("Forms - " + TITLE,Choice.IMPLICIT);
		for(int index = 0; index < crfs.size(); index++)
			formList.append(((CrfDef)crfs.elementAt(index)).getName(),null);

		formList.addCommand(DefaultCommands.cmdOk);
		formList.addCommand(DefaultCommands.cmdBack);
		formList.setCommandListener(this);	
		display.setCurrent(formList);
	}

	/**
	 * Fills a list with subject details.
	 * 
	 * @param subjectDetails - the list to fill with subject details.
	 * @param pt - the subject whose details to fill the list with.
	 */
	private void fillSubjectDetails(List subjectDetails){
		String s = "";
		if(subject.getPersonId()!= null)
			s = subject.getPersonId();
		subjectDetails.append("PersonID: " + s, null);

		s = "";
		if(subject.getStudySubjectId() != null)
			s = subject.getStudySubjectId();
		subjectDetails.append("StudySubjectID: " + s, null);

		s = "";
		if(subject.getSecondaryId() != null)
			s = subject.getSecondaryId();
		subjectDetails.append("SecondaryID: " + s, null);

		s = "";
		if(subject.getGender() != null)
			s = subject.getGender();
		subjectDetails.append("Gender: " + s, null);

		s = "";
		if(subject.getBirthDate() != null)
			s = Utilities.dateToString(subject.getBirthDate(),DateSettings.getDateFormat());
		subjectDetails.append("BirthDate: " + s, null);
	}

	//(int subjectId, String prefix, String familyName, String middleName, String givenName, String gender, Date birthDate, String subjectIdentifier, boolean isNew, String subjectGuid, int locationID)

	/** 
	 * Loads subjects from persistent storage. 
	 * 
	 */
	private Vector getSubjects(String personId, String studySubjectId){
		return OpenclinicaDataStorage.getSubjects(personId,studySubjectId,formMgr.getCurrentStudy().getId());
	}


	/** Displays the new subject form. */
	private void createNewSubject(){
		subject = null;
		formMgr.showForm(false,VarNames.SUBJECT_FORM,true,display, mainList,this);
	}

	/** Displays the search subject form. */
	private void searchSubject(int selectedIndex){
		subjectSearchForm = new SubjectSearchForm("Search Subject - " + TITLE);
		subjectSearchForm.addCommand(DefaultCommands.cmdOk);
		subjectSearchForm.addCommand(DefaultCommands.cmdCancel);
		subjectSearchForm.setCommandListener(this);
		display.setCurrent(subjectSearchForm);
	}

	/**
	 * Called by the epihandy form manager when a form has been closed without saving.
	 * 
	 * @param data - the data in the form that has been cancelled.
	 */
	public void afterFormCancelled(FormData data){
		Alert alert = new Alert("FormCancelled","The CRF has not been saved",null,AlertType.CONFIRMATION);
		alert.setTimeout(Alert.FOREVER);
		//display.setCurrent(alert);
	}

	/**
	 * Called by the epihandy form manager when a form is about to be displayed.
	 * 
	 * @param data - the data in the form that is to be displayed.
	 * 
	 */
	public void beforeFormDisplay(FormData data, boolean display){
		if(subject != null && data.isNew()){
			/*data.setTextValue(VarNames.FORM_SUBJECT_NAME, subject.getName());
			data.setTextValue(VarNames.FORM_FAMILY_NAME, subject.getFamilyName());
			data.setTextValue(VarNames.FORM_GIVEN_NAME, subject.getGivenName());
			data.setTextValue(VarNames.FORM_MIDDLE_NAME, subject.getMiddleName());
			data.setTextValue(VarNames.FORM_PREFIX, subject.getPrefix());
			data.setTextValue(VarNames.FORM_GENDER, subject.getGender());
			data.setValue(VarNames.FORM_SUBJECT_ID, subject.getSubjectId());
			data.setDateValue(VarNames.FORM_ENCOUNTER_DATETIME, new java.util.Date());
			data.setDateValue(VarNames.FORM_BIRTHDATE, subject.getBirthDate());
			data.setOptionValue(VarNames.FORM_PROVIDER_ID, String.valueOf(userMgr.getUserId()));
			data.setOptionValueIfOne(VarNames.FORM_LOCATION_ID);

			SubjectFieldList fields = OpenclinicaDataStorage.getSubjectFields();
			SubjectFieldValueList fieldVals = OpenclinicaDataStorage.getSubjectFieldValues();
			if(fields != null && fieldVals != null){
				for(int i=0; i<fields.size(); i++){
					SubjectField field = fields.getField(i);
					if(data.containsQuestion(field.getName()))
						data.setValue(field.getName(), fieldVals.getPatintFiledValue(field.getId(), subject.getSubjectId()));
				}
			}*/
		}
	}

	public boolean beforeFormDataListDisplay(FormDef formDef, boolean display){
		/*if(subject != null){
			display = false;
			int formDataRecordId = OpenclinicaDataStorage.getSubjectFormRecordId(subject.getSubjectId(), formDef.getId());
			if(formDataRecordId != OpenXdataConstants.NULL_ID)
				formMgr.showForm(false,OpenXdataConstants.DEFAULT_STUDY_ID, formDef, formDataRecordId,true,formList);
			else
				formMgr.showForm(false,new FormData(formDef),false,formList);
		}*/

		return display;
	}

	public void afterFormDisplay(FormData data, boolean save){

	}

	public void beforeQuestionEdit(QuestionData data, boolean edit){

	}

	public void afterQuestionEdit(QuestionData data, boolean save){

	}

	public void beforeFormCancelled(FormData data, boolean cancel){

	}

	public void beforeFormSaved(FormData formData, boolean save,boolean isNew){

		if(isNew && formData.getDef().getVariableName().equals(VarNames.SUBJECT_FORM)){
			Subject pt = new Subject();
			pt.setGender(formData.getOptionValue(VarNames.SUBJECT_GENDER));
			pt.setBirthDate(formData.getDateValue(VarNames.SUBJECT_BIRTH_DATE));

			pt.setNewSubject(true);

			OpenclinicaDataStorage.saveSubject(pt,formMgr.getCurrentStudy().getId());

			formData.setValue(VarNames.SUBJECT_SUBJECT_ID, pt.getSubjectId());
		}

	}

	public boolean beforeFormDelete(FormData data, boolean delete){
		return delete;
	}

	public void afterFormDelete(FormData data){
		if(subject != null)
			OpenclinicaDataStorage.deleteSubjectForm(subject.getSubjectId(), data.getDefId(), studyEvent.getEventId());
	}

	/**
	 * @see org.fcitmuk.epihandy.midp.forms.FormListener#afterFormSaved(org.fcitmuk.epihandy.FormData,java.lang.boolean)
	 */
	public void afterFormSaved(FormData formData, boolean isNew){
		if(isNew && subject != null){
			SubjectForm subjectForm = new SubjectForm(subject.getSubjectId(),formData.getRecordId(),studyEvent.getEventId());
			OpenclinicaDataStorage.saveSubjectForm(formData.getDefId(), subjectForm);
		}

		alertMsg.show("CRF Saved Successfully.");
	}


	/**
	 * Called when an error occurs during any operation.
	 * 
	 * @param errorMessage - the error message.
	 * @param e - the exception, if any, that did lead to this error.
	 */
	public void errorOccured(String errorMessage, Exception e){
		currentAction = CA_NONE; //if not set to this value, the alert will be on forever.
		if(e != null)
			errorMessage += " : "+ e.getMessage();
		alertMsg.showError(errorMessage);
	}

	public void cancelled(){
		display.setCurrent(mainList);
	}

	private void startSubjectDownload(){
		currentAction = CA_SUBJECT_DOWNLOAD;
		StudyRequestHeader comnParam = new StudyRequestHeader();
		comnParam.setAction(ACTION_DOWNLOAD_SUBJECTS);
		comnParam.setUserName(userMgr.getUserName());
		comnParam.setPassword(userMgr.getPassword());
		comnParam.setStudyId(formMgr.getCurrentStudy().getId());
		//String url = "http://localhost:8080/openclinica/module/xforms/subjectDownload.form?downloadSubjects=true&uname="+userMgr.getUserName()+"&pw="+userMgr.getPassword();
		transportLayer.setCommunicationParameter(TransportLayer.KEY_HTTP_URL, transportLayer.getConnectionParameterValue(TransportLayer.CON_TYPE_HTTP, NAME_SUBJECT_DOWNLOAD_URL)+"?uname="+userMgr.getUserName()+"&pw="+userMgr.getPassword());
		transportLayer.download(comnParam, null, new ResponseHeader(), new SubjectData(), this,userMgr.getUserName(),userMgr.getPassword(),null);		
	}

	private void startCrfDownload(){
		currentAction = CA_NONE;
		formMgr.downloadStudyForms(mainList,false);
	}
	
	private void startStudyEventsDownload(){
		currentAction = CA_STUDY_EVENTS_DOWNLOAD;
		StudyRequestHeader comnParam = new StudyRequestHeader();
		comnParam.setAction(ACTION_DOWNLOAD_STUDY_EVENTS);
		comnParam.setUserName(userMgr.getUserName());
		comnParam.setPassword(userMgr.getPassword());
		comnParam.setStudyId(formMgr.getCurrentStudy().getId());
		transportLayer.setCommunicationParameter(TransportLayer.KEY_HTTP_URL, transportLayer.getConnectionParameterValue(TransportLayer.CON_TYPE_HTTP, NAME_SUBJECT_DOWNLOAD_URL)+"?uname="+userMgr.getUserName()+"&pw="+userMgr.getPassword());
		transportLayer.download(comnParam, null, new ResponseHeader(), new StudyEventList(), this,userMgr.getUserName(),userMgr.getPassword(),null);		
	}


	/**
	 * Called after data has been successfully downloaded.
	 * 
	 * @param dataOutParams - the parameters sent with the data.
	 * @param dataOut - the downloaded data.
	 */
	public void downloaded(Persistent dataOutParams, Persistent dataOut){		
		if(currentAction == CA_SUBJECT_DOWNLOAD){
			String message = "Problem saving downloaded subjects";
			try{
				if(dataOut != null && ((SubjectData)dataOut).getSubjects() != null && ((SubjectData)dataOut).getSubjects().size() > 0){
					OpenclinicaDataStorage.saveSubjectData((SubjectData)dataOut,formMgr.getCurrentStudy().getId());
					message = ((SubjectData)dataOut).getSubjects().size() + " Subject(s) downloaded and saved successfully";
				}
				else
					message = "No subjects found on the server in study: " + formMgr.getCurrentStudy().getName();
			}catch(Exception e){
				message += e.getMessage();
			}

			currentAction = CA_NONE;				
			alertMsg.show(message);
		}
		else if(currentAction == CA_STUDY_EVENTS_DOWNLOAD){
			String message = null;
			try{
				StudyEventList studyEvents = (StudyEventList)dataOut;
				if(studyEvents != null && studyEvents.getEvents() != null && studyEvents.getEvents().size() > 0){
					OpenclinicaDataStorage.saveStudyEvents(formMgr.getCurrentStudy().getId(), studyEvents);
					message = studyEvents.getEvents().size() + " study event(s) downloaded and saved successfully.";
				}
				else
					message = "No study events found.";
			}catch(Exception e){
				message = "Problem saving downloaded study events " + e.getMessage();
			}

			currentAction = CA_NONE;
			alertMsg.show(message);
			
		}
		else //Must be forms download because we have only two kinds of downloads.
			userMgr.logOut();
	}

	//not used for now
	public void uploaded(Persistent dataOutParams, Persistent dataOut){
		OpenclinicaDataStorage.deleteSubjectForms(formMgr.getStudyList());

		//when user closes and reopens, this is reset hence introducing a bug.
		//if(newSubjectsCreated) //If nay new subjects were created, user, we need to redownload subjects to get their server assigned subject ids.

		//For costly data transfers like sms, we dont need to unnecessarily force users
		//to redownload subjects every after a data upload.
		//OpenclinicaDataStorage.deleteSubjects();
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

	public boolean beforeFormDefListDisplay(Vector formDefList){
		for(int i=0; i<formDefList.size(); i++){
			if(((FormDef)formDefList.elementAt(i)).getVariableName().equals(VarNames.SUBJECT_FORM)){
				formDefList.removeElementAt(i);
				break;
			}
		}
		return true;
	}	

	public void updateCommunicationParams(){

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

	public void onAlertMessage(byte msg){
		if(msg == AlertMessageListener.MSG_OK){
			if(currentAction == CA_SUBJECT_DOWNLOAD)
				startSubjectDownload();
			else if(currentAction == CA_SUBJECT_SEARCH)
				display.setCurrent(subjectSearchForm);
			else if(currentAction == CA_CRF_DOWNLOAD)
				startCrfDownload();
			else if(currentAction == CA_STUDY_EVENTS_DOWNLOAD)
				startStudyEventsDownload();
			else if(currentAction == CA_SUBJECT_EVENTS)
				display.setCurrent(subjectList);
			else if(currentAction == CA_CRF_LIST)
				display.setCurrent(formList);
			/*else if(currentAction == CA_FORMS_DOWNLOAD){
				currentAction = CA_NONE;
				formMgr.downloadStudyForms(mainList,false);
			}*/
			else
				alertMsg.turnOffAlert();
		}
		else{
			currentAction = CA_NONE;
			alertMsg.turnOffAlert();
		}
	}
	
	public void run(){
		//startCrfDownload2();
		//formMgr.downloadStudyForms(mainList,false);
	}
}

//Subject table field attributes server load,phone save,phone populate, and delete on subject download.
//create new subject form
//allow creation of new subjects together with entry for new forms
//submit all this new subject data correctly to the server.
//Subject details should be in new subject form
//separation of connection parameters and working on url parameters append of login info.