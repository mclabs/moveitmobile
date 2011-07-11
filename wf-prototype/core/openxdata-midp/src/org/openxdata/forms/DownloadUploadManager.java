package org.openxdata.forms;

import java.util.Vector;

import javax.microedition.lcdui.Displayable;

import org.openxdata.communication.ConnectionSettings;
import org.openxdata.communication.TransportLayer;
import org.openxdata.communication.TransportLayerListener;
import org.openxdata.db.OpenXdataDataStorage;
import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentInt;
import org.openxdata.db.util.PersistentString;
import org.openxdata.model.FormData;
import org.openxdata.model.FormDataSummary;
import org.openxdata.model.FormDataSummaryList;
import org.openxdata.model.FormDef;
import org.openxdata.model.LanguageList;
import org.openxdata.model.MenuTextList;
import org.openxdata.model.OpenXdataConstants;
import org.openxdata.model.RequestHeader;
import org.openxdata.model.ResponseHeader;
import org.openxdata.model.StudyData;
import org.openxdata.model.StudyDataList;
import org.openxdata.model.StudyDef;
import org.openxdata.model.StudyDefList;
import org.openxdata.model.UserList;
import org.openxdata.model.UserListStudyDefList;
import org.openxdata.model.UserStudyDefLists;
import org.openxdata.util.AlertMessage;
import org.openxdata.util.AlertMessageListener;
import org.openxdata.util.MenuText;


/**
 * 
 * @author daniel
 *
 */
public class DownloadUploadManager implements TransportLayerListener,AlertMessageListener {

	/** No alert is currently displayed. */
	private static final byte CA_NONE = -1;

	/** Current alert is for form download confirmation. */
	private static final byte CA_FORMS_DOWNLOAD = 1;

	/** Current alert is for study list download confirmation. */
	private static final byte CA_STUDY_LIST_DOWNLOAD = 2;

	/** Current alert is for data upload confirmation. */
	private static final byte CA_DATA_UPLOAD = 3;

	/** Current alert is for dsiplay of an error message. */
	private static final byte CA_ERROR_MSG_DISPLAY = 4;

	/** Current action is for users download. */
	private static final byte CA_USERS_DOWNLOAD = 5;

	/** Current action is for languages download. */
	private static final byte CA_LANGUAGES_DOWNLOAD = 6;

	/** Current action is for menu text download. */
	private static final byte CA_MENU_TEXT_DOWNLOAD = 7;

	/** Current alert is for form download confirmation. */
	private static final byte CA_ALL_FORMS_DOWNLOAD = 8;
	
	/** Reference to the commnunication layer. */
	private TransportLayer transportLayer;

	/** Reference to the communication parameter. */
	private RequestHeader requestHeader;

	private ResponseHeader responseHeader;

	private OpenXdataController controller;

	private StudyDef currentStudy;

	//private String title;

	private byte currentAction = CA_NONE;

	private AlertMessage alertMsg;

	//private Displayable prevScreen;

	private Vector studyList;

	private String userName;

	private String password;

	private TransportLayerListener transportLayerListener;

	/** Keeps track of the current number of form data being uploaded to the server. */
	private int currentDataCount = 0;
	
	/** Keeps track of the current form data being uploaded to the server */
	private FormDataSummaryList currentDataSummary = null;
	
	/** The total number of forms of data that needs to be uploaded to the server. */
	private int totalDataCount = 0;

	/** The current form data that is being uploaded to the server. */
	private FormData formData = null;

	/** The id of the study to which the form data that is being uploaded to the server belongs. */
	private int studyId;

	/** The list of form data that has not yet been uploaded to the server. */
	private StudyDataList studyDataList = null;

	public DownloadUploadManager(TransportLayer transportLayer,OpenXdataController controller, String title,TransportLayerListener transportLayerListener) {
		this.transportLayer = transportLayer;
		this.controller = controller;
		//this.title = title;
		this.transportLayerListener = transportLayerListener; // for propagating back transport layer events.

		this.alertMsg = new AlertMessage(this.transportLayer.getDisplay(),title, this.transportLayer.getPrevScreen(), this);

		this.requestHeader = new RequestHeader();
		this.responseHeader = new ResponseHeader();
	}

	public void setTransportLayerListener(TransportLayerListener transportLayerListener){
		this.transportLayerListener = transportLayerListener;
	}
	
	public void downloadAllForms(Displayable currentScreen, String userName,String password,boolean confirm) {
		this.userName = userName;
		this.password = password;
		transportLayer.setPrevScreen(currentScreen);
		alertMsg.setPrevScreen(currentScreen);
		currentAction = CA_ALL_FORMS_DOWNLOAD; 

		if (confirm) {
			if (OpenXdataDataStorage.hasFormData()) {
				alertMsg.show(MenuText.UN_UPLOADED_DATA_PROMPT() + " " + MenuText.FORMS());
				currentAction = CA_NONE;
			}
			else {
				alertMsg.showConfirm(MenuText.DOWNLOAD_FORMS_PROMPT());
			}
		}
		else {
			downloadForms();
		}
	}

	public void downloadStudyForms(Displayable currentScreen, String userName,String password,boolean confirm) {
		this.userName = userName;
		this.password = password;
		transportLayer.setPrevScreen(currentScreen);
		alertMsg.setPrevScreen(currentScreen);

		currentStudy = controller.getCurrentStudy();
		if (!GeneralSettings.isHideStudies() && currentStudy == null) {
			currentAction = CA_NONE;
			//alertMsg.show("Please first select a study.");
		} 
		else {
			currentAction = CA_FORMS_DOWNLOAD; // CA_USERS_DOWNLOAD; CA_FORMS_DOWNLOAD; First dowload the list of users.

			if(confirm){
				StudyDef sd = controller.getCurrentStudy();
				if(getCollectedStudyData(sd.getId(),sd.getForms()) != null){
					if (GeneralSettings.isHideStudies()) {
						alertMsg.show(MenuText.UN_UPLOADED_DATA_PROMPT()+" " + MenuText.FORMS());
					} else {
						alertMsg.show(MenuText.STUDY() + " " + getCurrentStudyName() + " " + MenuText.UPLOAD_BEFORE_DOWNLOAD_PROMPT());
					}
					currentAction = CA_NONE;
				}
				else {
					if (GeneralSettings.isHideStudies()) {
						alertMsg.showConfirm(MenuText.DOWNLOAD_FORMS_PROMPT());
					} else {
						alertMsg.showConfirm(MenuText.DOWNLOAD_STUDY_FORMS_PROMPT() + getCurrentStudyName());
					}
				}
			}
			else {
				downloadForms();
			}
		}
	}

	public String getCurrentStudyName(){
		return "{"+ controller.getCurrentStudy().getName()+ " ID:"+ controller.getCurrentStudy().getId() + "}";
	}

	public void downloadForms(Displayable currentScreen, Vector studyList, String userName,String password,boolean confirm) {
		this.userName = userName;
		this.password = password;
		//this.prevScreen = currentScreen;
		this.studyList = studyList;

		currentStudy = null;
		currentAction = CA_FORMS_DOWNLOAD; // CA_USERS_DOWNLOAD; CA_FORMS_DOWNLOAD; First dowload the list of users.

		if(confirm){
			if(!isThereCollectedData(MenuText.FORMS(),studyList))
				alertMsg.showConfirm(MenuText.DOWNLOAD_FORMS_PROMPT());
		}
		else
			downloadForms();
	}

	public boolean isThereCollectedData(String name, Vector studyList){
		this.studyList = studyList;

		//These two lines below are replaced by a more memory efficient check as done by MoTeCH
		//StudyDataList studyDataList = getCollectedData();
		//if(!(studyDataList == null || studyDataList.getStudies() == null || studyDataList.getStudies().size() == 0)){
		if(OpenXdataDataStorage.hasFormData()){
			this.currentAction = CA_NONE;
			this.alertMsg.show(MenuText.UN_UPLOADED_DATA_PROMPT() + " " + name + ".");
			return true;
		} 

		return false;
	}

	public void downloadStudies(Displayable currentScreen, Vector studyList, String userName,String password, boolean confirm) {
		this.userName = userName;
		this.password = password;
		//this.prevScreen = currentScreen;
		this.studyList = studyList;

		currentAction = CA_STUDY_LIST_DOWNLOAD;

		if(confirm){
			if(!isThereCollectedData(MenuText.STUDIES(),studyList))
				alertMsg.showConfirm(MenuText.DOWNLOAD_STUDIES_PROMPT());
		}
		else
			downloadStudies();
	}

	public void downloadLanguages(Displayable currentScreen, Vector studyList, String userName,String password, boolean confirm) {
		this.userName = userName;
		this.password = password;
		//this.prevScreen = currentScreen;
		alertMsg.setPrevScreen(currentScreen); //TODO Need to fix this hack
		this.studyList = studyList;

		currentAction = CA_LANGUAGES_DOWNLOAD;

		if(confirm)
			alertMsg.showConfirm(MenuText.DOWNLOAD_LANGUAGES_PROMPT());
		else
			downloadLanguages();
	}

	public void downloadMenuText(Displayable currentScreen, Vector studyList, String userName,String password, boolean confirm) {
		this.userName = userName;
		this.password = password;
		//this.prevScreen = currentScreen;
		alertMsg.setPrevScreen(currentScreen); //TODO Need to fix this hack
		this.studyList = studyList;

		currentAction = CA_MENU_TEXT_DOWNLOAD;
		downloadMenuText();
	}

	public void uploadData(Displayable currentScreen, Vector studyList, FormData formData, String userName, String password) {
		this.userName = userName;
		this.password = password;
		this.studyList = studyList;
		this.formData = formData;
		this.setPrevSrceen(currentScreen);
		alertMsg.setPrevScreen(currentScreen);

		if (formData == null && (studyList == null || studyList.size() == 0)) {
			currentAction = CA_ERROR_MSG_DISPLAY;
			alertMsg.show(MenuText.DOWNLOAD_FORMS_FIRST());
		} 
		else {
			currentAction = CA_DATA_UPLOAD;
			alertMsg.showConfirm(MenuText.UPLOAD_DATA_PROMPT());
		}
	}

	private void downloadStudies() {
		alertMsg.showProgress(MenuText.STUDY_LIST_DOWNLOAD(),MenuText.DOWNLOADING_STUDY_LIST());

		requestHeader.setLocale(LanguageSettings.getLocale());
		requestHeader.setAction(RequestHeader.ACTION_DOWNLOAD_STUDY_LIST);

		setCommunicationParams();
		transportLayer.download(requestHeader, null, responseHeader, new StudyDefList(), this, userName, password, null);
	}

	private void downloadForms() {
		alertMsg.showProgress(MenuText.FORM_DOWNLOAD(), MenuText.DOWNLOADING_FORMS());
		
		Persistent dataOut = new UserStudyDefLists();
		byte action = RequestHeader.ACTION_DOWNLOAD_USERS_AND_FORMS;
		if (currentAction == CA_ALL_FORMS_DOWNLOAD) {
			dataOut = new UserListStudyDefList();
			action = RequestHeader.ACTION_DOWNLOAD_USERS_AND_ALL_FORMS;
		}

		requestHeader.setLocale(LanguageSettings.getLocale());
		requestHeader.setAction(action); // ACTION_DOWNLOAD_STUDY_FORMS

		Persistent studyIdParam = new PersistentInt(OpenXdataConstants.NULL_ID);

		if (this.currentStudy != null){
			if(GeneralSettings.isUseStudyNumericId()) {
				studyIdParam = new PersistentInt(currentStudy.getId());
			}
			else {
				studyIdParam = new PersistentString(this.currentStudy.getVariableName());
			}
		}
		
		setCommunicationParams();
		transportLayer.download(requestHeader, studyIdParam, responseHeader, dataOut, this, userName, password, null); // StudyDef
	}

	private void downloadUsers() {
		alertMsg.showProgress(MenuText.FORM_DOWNLOAD(), MenuText.DOWNLOADING_USERS());

		requestHeader.setLocale(LanguageSettings.getLocale());
		requestHeader.setAction(RequestHeader.ACTION_DOWNLOAD_USERS);
		setCommunicationParams();
		transportLayer.download(requestHeader, null, responseHeader,new UserList(), this, userName, password,null);
	}

	private void downloadLanguages() {
		alertMsg.showProgress(MenuText.LANGUAGE_DOWNLOAD(), MenuText.DOWNLOADING_LANGUAGES());

		requestHeader.setLocale(LanguageSettings.getLocale());
		requestHeader.setAction(RequestHeader.ACTION_DOWNLOAD_LANGUAGES);
		setCommunicationParams();
		transportLayer.download(requestHeader, null, responseHeader,new LanguageList(), this, userName, password,null);
	}

	private void downloadMenuText() {
		alertMsg.showProgress(MenuText.MENU_TEXT_DOWNLOAD(), MenuText.DOWNLOADING_MENU_TEXT());

		requestHeader.setLocale(LanguageSettings.getLocale());
		requestHeader.setAction(RequestHeader.ACTION_DOWNLOAD_MENU_TEXT);
		setCommunicationParams();
		transportLayer.download(requestHeader, null, responseHeader,new MenuTextList(), this, userName, password,null);
	}

	/** Uploads collected data to the server. */
	private void uploadData() {
		alertMsg.showProgress(MenuText.DATA_UPLOAD(), MenuText.UPLOADING_DATA());

		totalDataCount = currentDataCount = 0;
		currentDataSummary = new FormDataSummaryList();
		studyDataList = getCollectedData();
		if (studyDataList == null || studyDataList.getStudies() == null || studyDataList.getStudies().size() == 0) {
			this.currentAction = CA_NONE;
			this.alertMsg.show(MenuText.NO_UPLOAD_DATA());
		} 
		else{
			currentDataCount = 1;
			uploadFormData(); // start upload of first item
		}
	}
	
	private void uploadFormData(){
		Vector studies = studyDataList.getStudies();
		for(int index = 0; index < studyDataList.getStudies().size(); index++){
			StudyData studyData = (StudyData)studies.elementAt(index);
			studyId = studyData.getId();
			
			Vector forms = studyData.getForms();
			if (forms.size() > 0) {
				formData = (FormData)forms.elementAt(0);
				forms.removeElementAt(0);
				// note: one formData uploaded at a time
				break;
			}
		}
		
		requestHeader.setLocale(LanguageSettings.getLocale());
		requestHeader.setAction(RequestHeader.ACTION_UPLOAD_DATA);
		setCommunicationParams();
		transportLayer.upload(requestHeader, 
				new StudyDataList(new StudyData(studyId,formData)), 
				responseHeader, new FormDataSummaryList(), this, userName, password,
				"Uploading " + currentDataCount + " of " + totalDataCount);
	}

	/**
	 * Sets the communication parameters which depend on the connection type and
	 * current action.
	 * 
	 */
	public void setCommunicationParams() {
		String url = ConnectionSettings.getHttpUrl();
		if (url != null) {
			transportLayer.setCommunicationParameter(TransportLayer.KEY_HTTP_URL, url);
		}
		requestHeader.setUserName(userName);
		requestHeader.setPassword(password);
	}

	public void updateCommunicationParams() {
		byte prevCurrentAction = currentAction;

		switch(requestHeader.getAction()){
		case RequestHeader.ACTION_DOWNLOAD_USERS_AND_FORMS:
		case RequestHeader.ACTION_DOWNLOAD_STUDY_FORMS:
			currentAction = CA_FORMS_DOWNLOAD;
			break;
		case RequestHeader.ACTION_DOWNLOAD_USERS:
			currentAction = CA_USERS_DOWNLOAD;
			break;
		case RequestHeader.ACTION_DOWNLOAD_STUDY_LIST:
			currentAction = CA_STUDY_LIST_DOWNLOAD;
			break;
		case RequestHeader.ACTION_UPLOAD_DATA:
			currentAction = CA_DATA_UPLOAD;
			break;
		case RequestHeader.ACTION_DOWNLOAD_LANGUAGES:
			currentAction = CA_LANGUAGES_DOWNLOAD;
			break;
		case RequestHeader.ACTION_DOWNLOAD_MENU_TEXT:
			currentAction = CA_MENU_TEXT_DOWNLOAD;
			break;
		}

		setCommunicationParams();

		currentAction = prevCurrentAction;
	}

	/**
	 * Called after data has been successfully downloaded.
	 * 
	 * @param dataOutParams -
	 *            the parameters sent with the data.
	 * @param dataOut -
	 *            the downloaded data.
	 */
	public void downloaded(Persistent dataOutParams, Persistent dataOut) {
		String message = MenuText.PROBLEM_SAVING_DOWNLOAD();
		boolean wasUserDownload = false, errorsOccured = false;;
		try {
			if (currentAction == CA_STUDY_LIST_DOWNLOAD) {
				deleteAllForms(); //delete all existing forms to prevent bugs of orphan forms who studies are no longer on server, and more.
				OpenXdataDataStorage.saveStudyList((StudyDefList) dataOut);
				this.controller.setStudyList(((StudyDefList) dataOut).getStudies());
				message = ((StudyDefList) dataOut).getStudies().size()+" "+MenuText.STUDY_DOWNLOAD_SAVED();
			} 
			else if (currentAction == CA_USERS_DOWNLOAD) {
				OpenXdataDataStorage.saveUsers((UserList) dataOut);
				wasUserDownload = true;
				alertMsg.showProgress(MenuText.FORM_DOWNLOAD(),((UserList)dataOut).size()+" "+MenuText.USER_DOWNLOAD_SAVED());
				currentAction = CA_FORMS_DOWNLOAD;
				downloadForms();
				alertMsg.showProgress(MenuText.FORM_DOWNLOAD(), MenuText.DOWNLOADING_FORMS());
			} 
			else if (currentAction == CA_FORMS_DOWNLOAD) {
				UserStudyDefLists lists = ((UserStudyDefLists) dataOut);

				UserList users = lists.getUsers();
				OpenXdataDataStorage.saveUsers(users);

				StudyDef studyDef = lists.getStudyDef();
				//TODO these three lines have been put from some clients like openclinica
				//which may have a different oc study id from that of oxd study id
				studyDef.setId(controller.getCurrentStudy().getId());
				studyDef.setName(controller.getCurrentStudy().getName());
				studyDef.setVariableName(controller.getCurrentStudy().getVariableName());

				OpenXdataDataStorage.saveStudy(studyDef);
				this.controller.setStudy(studyDef);

				if(studyDef.getForms() == null || studyDef.getForms().size() == 0)
					message = MenuText.NO_SERVER_STUDY_FORMS() + "  {"+ studyDef.getName()+ " ID:"+ studyDef.getId() + "}?";
				else
					message = studyDef.getForms().size()+" " +MenuText.FORM_DOWNLOAD_SAVED();

				/*
				 * EpihandyDataStorage.saveStudy((StudyDef)dataOut);
				 * this.controller.setStudy((StudyDef)dataOut); message = "Forms
				 * downloaded and saved successfully";
				 */
			}
			else if (currentAction == CA_LANGUAGES_DOWNLOAD) {
				LanguageList languages = (LanguageList)dataOut;
				OpenXdataDataStorage.saveLanguages(languages);

				if(languages.size() == 0)
					message = MenuText.NO_LANGUAGES();
				else
					message = languages.size()+" " + MenuText.LANGUAGE_DOWNLOAD_SAVED();
			}
			else if (currentAction == CA_MENU_TEXT_DOWNLOAD) {
				MenuTextList menuTextList = (MenuTextList)dataOut;
				OpenXdataDataStorage.saveMenuText(menuTextList);

				MenuText.setMenuTextList(menuTextList);

				if(menuTextList.size() == 0)
					message = MenuText.NO_MENU_TEXT();
				else
					message = menuTextList.size()+" " + MenuText.MENU_TEXT_DOWNLOAD_SAVED();
			}
			else if (currentAction == CA_ALL_FORMS_DOWNLOAD) {
				UserListStudyDefList lists = ((UserListStudyDefList) dataOut);

				UserList users = lists.getUsers();
				OpenXdataDataStorage.saveUsers(users);

				StudyDefList studyDefList = lists.getStudyDefList();
				OpenXdataDataStorage.saveStudyList(studyDefList);
				controller.setStudyList(studyDefList);
				Vector studies = studyDefList.getStudies();
				for (int i=0; i<studies.size(); i++) {
					StudyDef studyDef = (StudyDef) studies.elementAt(i);
					OpenXdataDataStorage.saveStudy(studyDef);
				}

				int totalForms = lists.totalForms();
				if (totalForms == 0)
					message = MenuText.NO_FORM_DEF();
				else
					message = totalForms+" " +MenuText.FORM_DOWNLOAD_SAVED();
			}

			//if (transportLayerListener != null && !wasUserDownload)
			//	transportLayerListener.downloaded(dataOutParams, dataOut);

		} catch (Exception e) {
			errorsOccured = true;
			e.printStackTrace();
			message += e.getMessage();
		}

		if (!wasUserDownload) { // after downloading users, we want to continue downloading forms.
			currentAction = CA_NONE;
			alertMsg.show(message);
		}

		if (!errorsOccured && transportLayerListener != null && !wasUserDownload)
			transportLayerListener.downloaded(dataOutParams, dataOut);
	}

	/**
	 * Called after data has been successfully downloaded.
	 * 
	 * @param dataOutParams -
	 *            the parameters sent with the data.
	 * @param dataOut -
	 *            the downloaded data.
	 */
	public void downloaded1(Persistent dataOutParams, Persistent dataOut) {
		String message = MenuText.PROBLEM_SAVING_DOWNLOAD();
		boolean wasUserDownload = false, errorsOccured = false;;
		try {
			if (currentAction == CA_STUDY_LIST_DOWNLOAD) {
				deleteAllForms(); //delete all existing forms to prevent bugs of orphan forms who studies are no longer on server, and more.
				OpenXdataDataStorage.saveStudyList((StudyDefList) dataOut);
				this.controller.setStudyList(((StudyDefList) dataOut).getStudies());
				message = ((StudyDefList) dataOut).getStudies().size()+" "+MenuText.STUDY_DOWNLOAD_SAVED();
			} 
			else if (currentAction == CA_USERS_DOWNLOAD) {
				OpenXdataDataStorage.saveUsers((UserList) dataOut);
				wasUserDownload = true;
				alertMsg.showProgress(MenuText.FORM_DOWNLOAD(),((UserList)dataOut).size()+" "+MenuText.USER_DOWNLOAD_SAVED());
				currentAction = CA_FORMS_DOWNLOAD;
				downloadForms();
				alertMsg.showProgress(MenuText.FORM_DOWNLOAD(), MenuText.DOWNLOADING_FORMS());
			} 
			else if (currentAction == CA_FORMS_DOWNLOAD) {
				UserStudyDefLists lists = ((UserStudyDefLists) dataOut);
	
				UserList users = lists.getUsers();
				OpenXdataDataStorage.saveUsers(users);
	
				StudyDef studyDef = lists.getStudyDef();
				//TODO these three lines have been put from some clients like openclinica
				//which may have a different oc study id from that of oxd study id
				studyDef.setId(controller.getCurrentStudy().getId());
				studyDef.setName(controller.getCurrentStudy().getName());
				studyDef.setVariableName(controller.getCurrentStudy().getVariableName());
	
				OpenXdataDataStorage.saveStudy(studyDef);
				this.controller.setStudy(studyDef);
	
				if(studyDef.getForms() == null || studyDef.getForms().size() == 0)
					message = MenuText.NO_SERVER_STUDY_FORMS() + "  {"+ studyDef.getName()+ " ID:"+ studyDef.getId() + "}?";
				else
					message = studyDef.getForms().size()+" " +MenuText.FORM_DOWNLOAD_SAVED();
	
				/*
				 * EpihandyDataStorage.saveStudy((StudyDef)dataOut);
				 * this.controller.setStudy((StudyDef)dataOut); message = "Forms
				 * downloaded and saved successfully";
				 */
			}
			else if (currentAction == CA_LANGUAGES_DOWNLOAD) {
				LanguageList languages = (LanguageList)dataOut;
				OpenXdataDataStorage.saveLanguages(languages);
	
				if(languages.size() == 0)
					message = MenuText.NO_LANGUAGES();
				else
					message = languages.size()+" " + MenuText.LANGUAGE_DOWNLOAD_SAVED();
			}
			else if (currentAction == CA_MENU_TEXT_DOWNLOAD) {
				MenuTextList menuTextList = (MenuTextList)dataOut;
				OpenXdataDataStorage.saveMenuText(menuTextList);
	
				MenuText.setMenuTextList(menuTextList);
	
				if(menuTextList.size() == 0)
					message = MenuText.NO_MENU_TEXT();
				else
					message = menuTextList.size()+" " + MenuText.MENU_TEXT_DOWNLOAD_SAVED();
			}
			else if (currentAction == CA_ALL_FORMS_DOWNLOAD) {
				UserListStudyDefList lists = ((UserListStudyDefList) dataOut);
	
				UserList users = lists.getUsers();
				OpenXdataDataStorage.saveUsers(users);
	
				StudyDefList studyDefList = lists.getStudyDefList();
				OpenXdataDataStorage.saveStudyList(studyDefList);
				controller.setStudyList(studyDefList);
				Vector studies = studyDefList.getStudies();
				for (int i=0; i<studies.size(); i++) {
					StudyDef studyDef = (StudyDef) studies.elementAt(i);
					OpenXdataDataStorage.saveStudy(studyDef);
				}
	
				int totalForms = lists.totalForms();
				if (totalForms == 0)
					message = MenuText.NO_FORM_DEF();
				else
					message = totalForms+" " +MenuText.FORM_DOWNLOAD_SAVED();
			}
	
			//if (transportLayerListener != null && !wasUserDownload)
			//	transportLayerListener.downloaded(dataOutParams, dataOut);
	
		} catch (Exception e) {
			errorsOccured = true;
			e.printStackTrace();
			message += e.getMessage();
		}
	
		if (!wasUserDownload) { // after downloading users, we want to continue downloading forms.
			currentAction = CA_NONE;
			alertMsg.show(message);
		}
	
		if (!errorsOccured && transportLayerListener != null && !wasUserDownload)
			transportLayerListener.downloaded(dataOutParams, dataOut);
	}

	/**
	 * Called after data has been successfully uploaded.
	 * 
	 * @param dataOutParams -
	 *            parameters sent after data has been uploaded.
	 * @param dataOut -
	 *            data sent after the upload.
	 */
	public void uploaded(Persistent dataOutParams, Persistent dataOut) {
		String message = MenuText.DATA_UPLOAD_PROBLEM();

		if (currentAction == CA_DATA_UPLOAD) {
			try {
				ResponseHeader status = (ResponseHeader) dataOutParams;
				if (status.isSuccess()) {
					//if(GeneralSettings.deleteDataAfterUpload()){
						//EpihandyDataStorage.deleteData(new StudyDefList(studyList));
						//assert(formData != null);
						OpenXdataDataStorage.deleteFormData(studyId, formData);
						if(OpenXdataDataStorage.getFormData(studyId, formData.getDefId()) != null){
							int size = OpenXdataDataStorage.getFormData(studyId, formData.getDefId()).size();
							controller.clearFormDataList(formData,false);							
						}else{
							controller.clearFormDataList(formData,true);
						}
						
					//}
						
					if (dataOut instanceof FormDataSummaryList) {
						FormDataSummaryList thisDataSummary = (FormDataSummaryList) dataOut;
						currentDataSummary.addFormDataSummaries(thisDataSummary.getFormDataSummaries());
					}

					if(currentDataCount == totalDataCount){
						//This builds up the text mesage of session id's to the mobile display
						StringBuffer summaryMessage = new StringBuffer();
						summaryMessage.append("\n\nSession reference(s):");
						Vector list = currentDataSummary.getFormDataSummaries();
						for (int i=0, n=list.size(); i<n; i++) {
							FormDataSummary summary = (FormDataSummary)list.elementAt(i);
							summaryMessage.append("\n'");
							summaryMessage.append(summary.getDescription());
							summaryMessage.append("' = ");
							summaryMessage.append(summary.getFormDataId());
						}
						message = MenuText.DATA_UPLOAD_SUCCESS() + summaryMessage.toString();
						
						if (transportLayerListener != null)
							transportLayerListener.uploaded(dataOutParams, dataOut);
					}
					else{
						currentDataCount++;
						uploadFormData();
						return;
					}
				}
			} catch (Exception e) {
				//e.printStackTrace();
				message = MenuText.PROBLEM_CLEANING_STORE();
			}
		} else
			message = MenuText.UNKNOWN_UPLOAD();

		currentAction = CA_NONE;
		alertMsg.show(message);
	}

	/**
	 * Called when an error occurs during any operation.
	 * 
	 * @param errorMessage -
	 *            the error message.
	 * @param e -
	 *            the exception, if any, that did lead to this error.
	 */
	public void errorOccured(String errorMessage, Exception e) {
		currentAction = CA_NONE; // if not set to this value, the alert will
		// be on forever.
		if (e != null) {
			//e.printStackTrace();
			errorMessage += " : " + e.getMessage();
		}
		alertMsg.show(errorMessage);
	}

	public void cancelled() {
		if (transportLayerListener != null)
			transportLayerListener.cancelled();
	}

	/**
	 * Called when the OK commad of an alert is clicked.
	 */
	public void onAlertMessage(byte msg) {
		if(msg == AlertMessageListener.MSG_OK){
			if (currentAction == CA_STUDY_LIST_DOWNLOAD) 
				downloadStudies();
			else if (currentAction == CA_USERS_DOWNLOAD /* CA_FORMS_DOWNLOAD */)
				downloadUsers();
			else if (currentAction == CA_FORMS_DOWNLOAD) // TODO May need to be done after downloading users.
				downloadForms();
			else if (currentAction == CA_DATA_UPLOAD) 
				uploadData();
			else if (currentAction == CA_LANGUAGES_DOWNLOAD)
				downloadLanguages();
			else
				alertMsg.turnOffAlert();
		}
		else
			alertMsg.turnOffAlert();
	}

	private StudyDataList getCollectedData() {
		StudyDataList studyDatalist = new StudyDataList();
		
		if (formData != null) {
			StudyData studyData = new StudyData(controller.getCurrentStudy().getId());
			studyData.addForm(formData);
			studyDatalist.addStudy(studyData);
			totalDataCount = 1;
		} else {
			if(studyList == null)
				return null;
			
			for (int i = 0; i < studyList.size(); i++){
				StudyDef studyDef = (StudyDef) studyList.elementAt(i);
				if(studyDef.getForms() == null || studyDef.getForms().isEmpty()){
					//Study list always has no forms, so we have to get them from the database.
					studyDef = OpenXdataDataStorage.getStudy(studyDef.getId());				
				}
		
				//If no forms downloaded yet, then we don't expect any data to save.
				if(studyDef != null){
					StudyData studyData = getCollectedStudyData(studyDef.getId(),studyDef.getForms());
					if(studyData != null)
						studyDatalist.addStudy(studyData);
				}
			}
		}

		return studyDatalist;
	}

	private void setFormDefs(Vector formDatas, FormDef formDef) {
		for (int i = 0; i < formDatas.size(); i++) {
			FormData formData = (FormData) formDatas.elementAt(i);
			formData.setDef(formDef);
		}
	}

	/*private void fillCollectedStudyData(StudyDef studyDef,StudyDataList studyDataList) {
		StudyData studyData = new StudyData(studyDef.getId());
		Vector formDefs = studyDef.getForms();
		if (formDefs != null && formDefs.size() > 0) {
			for (int i = 0; i < formDefs.size(); i++) {
				FormDef formDef = ((FormDef) formDefs.elementAt(i));
				Vector formDatas = EpihandyDataStorage.getFormData(studyDef.getId(), formDef.getId());
				if (formDatas != null) {
					setFormDefs(formDatas, formDef); // These are for writing to stream but they are not persisted.
					studyData.addForms(formDatas);
				}
			}
			if (studyData.getForms() != null && studyData.getForms().size() > 0)
				studyDataList.addStudy(studyData);
		}
	}*/

	public StudyData getCollectedStudyData(int id, Vector formDefs) {
		StudyData studyData = new StudyData(id);
		if (formDefs != null && formDefs.size() > 0) {
			for (int i = 0; i < formDefs.size(); i++) {
				FormDef formDef = ((FormDef) formDefs.elementAt(i));
				Vector formDatas = OpenXdataDataStorage.getFormData(id, formDef.getId());
				
				if (formDatas != null) {
					setFormDefs(formDatas, formDef); // These are for writing to stream but they are not persisted.
					studyData.addForms(formDatas);
					totalDataCount += formDatas.size();
				}
			}
			
			if (studyData.getForms() != null && studyData.getForms().size() > 0)
				return studyData;
		}

		return null;
	}

	public void setPrevSrceen(Displayable screen){
		alertMsg.setPrevScreen(screen); //TODO Need to fix this hack
	}


	/**
	 * Deletes all form definitions.
	 *
	 */
	private void deleteAllForms(){
		Vector list = controller.getStudyList();
		if(list == null)
			return;

		for(byte i=0; i<list.size(); i++)
			OpenXdataDataStorage.deleteStudy((StudyDef)list.elementAt(i));
	}
}
