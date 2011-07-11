package org.openxdata.forms;

import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.openxdata.db.util.Settings;
import org.openxdata.model.OpenXdataConstants;
import org.openxdata.model.StudyDef;
import org.openxdata.mvc.AbstractView;
import org.openxdata.util.DefaultCommands;
import org.openxdata.util.MenuText;


/**
 * 
 * @author daniel
 *
 */
public class StudyListView extends AbstractView implements CommandListener  {
	private Vector studyList;

	public StudyListView(){
		screen = new List(MenuText.SELECT_STUDY()+" - "+title , Choice.IMPLICIT);
		((List)screen).setFitPolicy(List.TEXT_WRAP_ON);
		
		screen.setCommandListener(this);
		if (!GeneralSettings.isMainMenu()) {
			screen.addCommand(DefaultCommands.cmdExit);
			screen.addCommand(DefaultCommands.cmdSel);
			screen.addCommand(DefaultCommands.cmdDownloadStudy);
			screen.addCommand(DefaultCommands.cmdUploadData);
			screen.addCommand(DefaultCommands.cmdSettings);
		} else {
			screen.addCommand(DefaultCommands.cmdSel);
			screen.addCommand(DefaultCommands.cmdCancel);
		}
	}

	public void showStudyList(Vector studyList){
		this.studyList = studyList;

		screen.setTitle(MenuText.SELECT_STUDY()+" - "+title);
		((List)screen).deleteAll();
		StudyDef study; int selectedIndex = OpenXdataConstants.NO_SELECTION;
		Settings settings = new Settings(OpenXdataConstants.STORAGE_NAME_EPIHANDY_SETTINGS,true);
		String val = settings.getSetting(OpenXdataConstants.KEY_LAST_SELECTED_STUDY);

		for(int i=0; i<studyList.size(); i++){
			study = (StudyDef)studyList.elementAt(i);
			if(selectedIndex == OpenXdataConstants.NO_SELECTION && val != null){
				if(study.getId() == Byte.parseByte(val))
					selectedIndex = i;
			}

			((List)screen).append(study.getName(), null);
		}

		if(selectedIndex != OpenXdataConstants.NO_SELECTION)
			((List)screen).setSelectedIndex(selectedIndex, true);

		display.setCurrent(screen);
	}

	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		OpenXdataController controller = getOpenXdataController();
		if (c == List.SELECT_COMMAND || c == DefaultCommands.cmdOk || c == DefaultCommands.cmdSel) {
			StudyDef selectedStudy = (StudyDef) studyList.elementAt(((List) d).getSelectedIndex());
			if (selectedStudy != null) {
				controller.execute(this, DefaultCommands.cmdOk, selectedStudy);
			} 
		}
		else if (c == DefaultCommands.cmdCancel) {
			controller.execute(this, DefaultCommands.cmdCancel, null);
		}
		else if (c == DefaultCommands.cmdSettings) {
			controller.displayUserSettings(this.getScreen());
		}
		else if (c == DefaultCommands.cmdDownloadStudy) {
			controller.downloadStudies();
		}
		else if (c == DefaultCommands.cmdUploadData) {
			controller.uploadData(this.getScreen());
		}
		else if (c == DefaultCommands.cmdExit) {
			controller.logout();
		}
	}

	public void setStudyList(Vector list){
		studyList = list;
	}

	public Vector getStudyList(){
		return studyList;
	}

	private OpenXdataController getOpenXdataController(){
		return (OpenXdataController)controller;
	}
}
