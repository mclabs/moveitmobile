package org.openxdata.forms;

import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.openxdata.db.OpenXdataDataStorage;
import org.openxdata.model.FormData;
import org.openxdata.model.FormDef;
import org.openxdata.model.StudyDef;
import org.openxdata.mvc.AbstractView;
import org.openxdata.util.AlertMessage;
import org.openxdata.util.AlertMessageListener;
import org.openxdata.util.DefaultCommands;
import org.openxdata.util.MenuText;


/**
 * Display a list of data collected forms.
 * 
 * @author Daniel Kayiwa.
 *
 */
public class FormDataListView extends AbstractView implements AlertMessageListener {	

	private Vector formDataList;
	private FormDef formDef;
	private boolean deleting = false;
	private AlertMessage alertMsg;

	private final byte CA_NONE = 0;
	private final byte CA_ERROR = 1;

	private byte currentAction = CA_NONE;

	public FormDataListView(){

	}

	/**
	 * Displays data collected for a form type.
	 * 
	 * @param def - the form definition.
	 */
	public void showFormList(FormDef def, Vector frmDataList){
		
		try{
			this.formDef = def;
			this.formDataList = frmDataList;
			
			screen = new List(def.getName()+ " - " + MenuText.DATA_LIST() + " - " + title, Choice.IMPLICIT );
			((List)screen).setFitPolicy(List.TEXT_WRAP_ON);
			
			alertMsg = new AlertMessage(display, title, screen, this);

			if(formDataList != null){
				for(int i=0; i<formDataList.size(); i++){
					FormData data = (FormData)formDataList.elementAt(i);
					data.setDef(def);
					data.buildDataDescription();
					((List)screen).append(data.toString(), null);
				}
			}
			else{
				//commandAction(DefaultCommands.cmdNew,screen);
				//return;
				formDataList = new Vector();
			}
			
			screen.setCommandListener(this);
			screen.addCommand(DefaultCommands.cmdNew);
			screen.addCommand(DefaultCommands.cmdBack);
			screen.addCommand(DefaultCommands.cmdUploadData);
			screen.addCommand(DefaultCommands.cmdUploadAllFormData);			
			if(formDataList.size() > 0)
				screen.addCommand(DefaultCommands.cmdDelete);
			screen.addCommand(DefaultCommands.cmdMainMenu);

			/*if((this.currentFormDataIndex != EpihandyConstants.NO_SELECTION) && (this.currentFormDataIndex < formDataList.size()))
				mainList.setSelectedIndex(this.currentFormDataIndex, true);*/
			
			display.setCurrent(screen);
		}
		catch(Exception ex){
			currentAction = CA_ERROR;
			//TODO Changing form definition corrupts existing data. So it's safe to first upload all form collected data before downloading new form definitions.
			alertMsg.showError(MenuText.DATA_LIST_DISPLAY_PROBLEM()+ " " + ex.getMessage());
			ex.printStackTrace();
		}
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
				getOpenXdataController().showForm(true,(FormData)this.formDataList.elementAt(((List)d).getSelectedIndex()),true,prevScreen);
			else if(c == DefaultCommands.cmdBack)
				getOpenXdataController().handleCancelCommand(this);
			else if(c == DefaultCommands.cmdNew)
				getOpenXdataController().showForm(true,new FormData(this.formDef),false,prevScreen);
			else if(c == DefaultCommands.cmdDelete)
				handleDeleteCommand(d);
			else if(c == DefaultCommands.cmdMainMenu)
				getOpenXdataController().backToMainMenu();
			else if(c == DefaultCommands.cmdUploadAllFormData){
				int currentFormId = formDef.getId();//current form
				Vector studyList = new Vector();
				FormDef fd = getOpenXdataController().getCurrentStudy().getForm(currentFormId);
				Vector forms = new Vector();
				forms.addElement(fd);
				StudyDef sd = new StudyDef(); //StudyDef sd = getOpenXdataController().getCurrentStudy();
				sd.setId(getOpenXdataController().getCurrentStudy().getId());
				sd.setForms(forms);		
				studyList.addElement(sd);
				getOpenXdataController().uploadData(this.getScreen(), studyList);

			}else if(c == DefaultCommands.cmdUploadData){
				FormData formData = (FormData)this.formDataList.elementAt(((List)screen).getSelectedIndex());		
				getOpenXdataController().uploadData(this.getScreen(), formData);
			}
		}
		catch(Exception e){
			alertMsg.showError(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void clearFormDataList(FormData formData, boolean deleteAll) {
		List formDataListSc = ((List)screen);
		//formDataList.delete(formDataList.getSelectedIndex());
		for(int i=0; i<formDataListSc.size(); i++){
			String title = formDataListSc.getString(i);
			if(title.equals(formData.getDataDescription())){
				formDataListSc.delete(i);
				formDataList.removeElementAt(i);				
			}
		}
		if(deleteAll){
			//No formdata therefore clear screen
			((List)screen).deleteAll();
		}
	}

	/**
	 * Processes the cancel command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	/*private void handleBackCommand(Displayable d){
		getEpihandyController().handleCancelCommand(this);
	}

	/**
	 * Processes the new command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	/*private void handleNewCommand(Displayable d){
		getEpihandyController().showForm(true,new FormData(this.formDef),false);
	}

	/**
	 * Processes the OK command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	/*private void handleOkCommand(Displayable d){
		getEpihandyController().showForm(true,(FormData)this.formDataList.elementAt(((List)d).getSelectedIndex()),true);
	}

	/**
	 * Processes the delete command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleDeleteCommand(Displayable d){
		FormData formData = (FormData)this.formDataList.elementAt(((List)screen).getSelectedIndex());
		alertMsg.showConfirm(MenuText.FORM_DELETE_PROMPT() + " " + formData.toString());
		deleting = true;
	}

	public void deleteCurrentForm(){
		int index = ((List)screen).getSelectedIndex();
		FormData formData = (FormData)this.formDataList.elementAt(index);

		getOpenXdataController().deleteForm(formData,this);

		((List)screen).delete(index);
		formDataList.removeElementAt(index);

		if(formDataList.size() == 0)
			screen.removeCommand(DefaultCommands.cmdDelete);
	}
	
	public boolean hasSelectedForm(){
		return screen != null;
	}

	public void onFormSaved(FormData formData,boolean isNew){
		formData.buildDataDescription();
		
		if (isNew) {			
			formDataList.addElement(formData);
			((List)screen).append(formData.toString(), null);
			if(formDataList.size() == 1)
				screen.addCommand(DefaultCommands.cmdDelete);
			((List)screen).setSelectedIndex(formDataList.size()-1, true);	
		} else{
			formDataList.setElementAt(formData, ((List)screen).getSelectedIndex());
			((List)screen).set(((List)screen).getSelectedIndex(),formData.toString(), null);				
		}

		//display.setCurrent(screen);
		//TODO Should this mouthing really be done here or somewhere elese?
		alertMsg.show(MenuText.FORM_SAVE_SUCCESS());
	}

	public void onAlertMessage(byte msg){
		if(msg == AlertMessageListener.MSG_OK){
			if(deleting){
				deleting = false;
				deleteCurrentForm();
			}
			else if(currentAction == CA_ERROR){
				getOpenXdataController().handleCancelCommand(this);
				return;
			}
		}

		display.setCurrent(screen);
	}

	public Vector getFormDataList(){
		return formDataList;
	}

	private OpenXdataController getOpenXdataController(){
		return (OpenXdataController)controller;
	}
}
