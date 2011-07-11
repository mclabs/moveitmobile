package org.openxdata.forms;

import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.openxdata.db.OpenXdataDataStorage;
import org.openxdata.db.util.Settings;
import org.openxdata.model.FormData;
import org.openxdata.model.FormDef;
import org.openxdata.model.OpenXdataConstants;
import org.openxdata.model.StudyDef;
import org.openxdata.model.StudyDefList;
import org.openxdata.mvc.AbstractView;
import org.openxdata.util.AlertMessage;
import org.openxdata.util.AlertMessageListener;
import org.openxdata.util.DefaultCommands;
import org.openxdata.util.MenuText;


/**
 * Display a list of form defintions.
 * 
 * @author Daniel Kayiwa.
 *
 */
public class FormDefListView extends AbstractView implements AlertMessageListener{

	private StudyDefList studyDefList;
	private StudyDef studyDef;
	private AlertMessage alertMsg;
	private int lastSelectionIndex = 0;

	private final byte CA_NONE = 0;
	private final byte CA_ERROR = 3;

	private byte currentAction = CA_NONE;

	private static final String KEY_LAST_SELECTED_FORMDEF =  "LAST_SELECTED_FORMDEF";
	private Vector formDefList;

	public FormDefListView() {
		screen = new List(MenuText.SELECT_FORM() + " - "+title , Choice.IMPLICIT);
		((List)screen).setFitPolicy(List.TEXT_WRAP_ON);
		
		screen.setCommandListener(this);
		
		if (!GeneralSettings.isMainMenu()) {
			if (GeneralSettings.isHideStudies()) {
				screen.addCommand(DefaultCommands.cmdExit);
			} else {
				screen.addCommand(DefaultCommands.cmdBack);
			}
			screen.addCommand(DefaultCommands.cmdSel);
			screen.addCommand(DefaultCommands.cmdDownloadForm);
			screen.addCommand(DefaultCommands.cmdUploadData);
			screen.addCommand(DefaultCommands.cmdSettings);
		} else {
			screen.addCommand(DefaultCommands.cmdSel);
			screen.addCommand(DefaultCommands.cmdBack);
		}

	}

	public void showFormList(FormListener formListener){
		showFormList(null,formListener);
	}

	/**
	 * Displays the list of forms in a study.
	 * 
	 * @param studyId - the numeric unique identifier of the study.
	 */
	public void showFormList(StudyDef currentStudy, FormListener formListener) {
		if (currentStudy != null) {
			studyDef = currentStudy;
		}
		
		screen.setTitle(MenuText.SELECT_FORM() + " - "+title);
		alertMsg = new AlertMessage(display, title, screen, this);

		((List)screen).deleteAll();
		try{
			if (!GeneralSettings.isHideStudies() && studyDef == null)
				alertMsg.show(MenuText.NO_SELECTED_STUDY());
			else {
				formDefList = copyFormDefs();
				if(formDefList != null && formDefList.size() > 0){		
					boolean showList = true;
					if(formListener != null)
						showList = formListener.beforeFormDefListDisplay(formDefList);

					if(showList){
						for(int i=0; i<formDefList.size(); i++)
							((List)screen).append(((FormDef)formDefList.elementAt(i)).getName(), null);

						Settings settings = new Settings(OpenXdataConstants.STORAGE_NAME_EPIHANDY_SETTINGS,true);
						String val = settings.getSetting(KEY_LAST_SELECTED_FORMDEF);
						if(val != null)
							lastSelectionIndex = Integer.parseInt(val);

						if(lastSelectionIndex < formDefList.size())
							((List)screen).setSelectedIndex(lastSelectionIndex, true);

						display.setCurrent(screen);
					}
				}
				else
					alertMsg.show(MenuText.NO_STUDY_FORMS());
			}
		}
		catch(Exception e){
			alertMsg.showError("Prrr"+ e.getMessage());
		}
	}

	private Vector copyFormDefs(){
		Vector forms = new Vector();
		if (studyDefList != null) {
			Vector studyList = studyDefList.getStudies();
			for (byte i=0; i<studyList.size(); i++) {
				StudyDef studyDef = (StudyDef)studyList.elementAt(i);
				Vector formList = studyDef.getForms();
				for (byte j=0; j<formList.size(); j++) {
					forms.addElement(formList.elementAt(j));
				}
			}
		} else if (studyDef != null) {
			Vector formList = studyDef.getForms();
			for (byte i=0; i<formList.size(); i++) {
				forms.addElement(formList.elementAt(i));
			}			
		}
		return forms;
	}

	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		OpenXdataController controller = getOpenXdataController();
		if(c == DefaultCommands.cmdSel || c == List.SELECT_COMMAND)
			handleOkCommand(d);
		else if(c == DefaultCommands.cmdBack)
			getOpenXdataController().handleCancelCommand(this);
		else if (c == DefaultCommands.cmdSettings) {
			controller.displayUserSettings(this.getScreen());
		}
		else if (c == DefaultCommands.cmdDownloadForm) {
			controller.downloadStudyForms(this.getScreen());
		}
		else if (c == DefaultCommands.cmdUploadData) {
			if (studyDefList != null) {
				controller.uploadData(this.getScreen(), studyDefList.getStudies());
			} else {
				controller.uploadData(this.getScreen());
			}
		}
		else if (c == DefaultCommands.cmdExit) {
			controller.logout();
		}
	}

	/**
	 * Processes the cancel command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	/*private void handleBackCommand(Displayable d){
	*/

	/**
	 * Processes the OK command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleOkCommand(Displayable d){
		try {
			Settings settings = new Settings(OpenXdataConstants.STORAGE_NAME_EPIHANDY_SETTINGS,true);
			settings.setSetting(KEY_LAST_SELECTED_FORMDEF, String.valueOf(lastSelectionIndex));
			settings.saveSettings();
			
			lastSelectionIndex = ((List)screen).getSelectedIndex();
			FormDef fdef = (FormDef)formDefList.elementAt(lastSelectionIndex);
			Vector formData = OpenXdataDataStorage.getFormData(getStudy().getId(), fdef.getId());
			if(formData != null && !formData.isEmpty()){
				getOpenXdataController().showFormDataList((FormDef)formDefList.elementAt(lastSelectionIndex));			
			} else {
				getOpenXdataController().showFormDataList((FormDef)formDefList.elementAt(lastSelectionIndex));
				getOpenXdataController().showForm(true, new FormData(fdef), false, getScreen());
			}
			
		}
		catch(Exception ex){
			//TODO Looks like we should help the user out of this by say enabling them
			//delete any existing data.
			String s = MenuText.FORM_DATA_DISPLAY_PROBLEM();
			if(ex.getMessage() != null && ex.getMessage().trim().length() > 0)
				s = ex.getMessage();
			currentAction = CA_ERROR;
			alertMsg.showError(s);
			//ex.printStackTrace();
		}
	}

	public void onAlertMessage(byte msg){
		if(msg == AlertMessageListener.MSG_OK){
			if(currentAction == CA_ERROR)
				show();
			else
				getOpenXdataController().handleCancelCommand(this);

			currentAction = CA_NONE;
		}
		else
			getOpenXdataController().handleCancelCommand(this);
	}

	public void setStudy(StudyDef study){
		studyDef = study;
		studyDefList = null;
	}

	public StudyDef getStudy() {
		StudyDef returnStudy = studyDef;
		if (studyDef == null && studyDefList != null) {
			int index = ((List)screen).getSelectedIndex();
			FormDef fdef = null;
			if (formDefList != null && index >= 0 && index < formDefList.size()) {
				fdef = (FormDef)formDefList.elementAt(index);
			}
			returnStudy = getStudyFromForm(fdef);
		}
		return returnStudy;
	}
	
	private StudyDef getStudyFromForm(FormDef formDef) {
		StudyDef returnStudy = null;
		if (formDef != null) {
			Vector studies = studyDefList.getStudies();
			for (byte i=0; i<studies.size(); i++) {
				StudyDef sd = (StudyDef)studies.elementAt(i); 
				if (sd.getForm(formDef.getId()) != null) {
					returnStudy = sd;
					break;
				}
			}
		}
		if (returnStudy == null) {
			System.out.println("Could not find Study, so using first in the list");
			returnStudy = studyDefList.getFirstStudy();
		}
		return returnStudy;
	}
	
	public void setStudies(StudyDefList studies){
		studyDef = null;
		studyDefList = studies;
	}

	private OpenXdataController getOpenXdataController(){
		return (OpenXdataController)controller;
	}
}
