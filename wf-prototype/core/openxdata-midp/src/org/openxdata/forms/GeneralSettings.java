package org.openxdata.forms;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

import org.openxdata.db.util.Settings;
import org.openxdata.mvc.AbstractView;
import org.openxdata.util.DefaultCommands;
import org.openxdata.util.MenuText;
import org.openxdata.util.Utilities;

public class GeneralSettings extends AbstractView{

	private static final String KEY_SINGLE_QUESTION_EDIT = "SINGLE_QUESTION_EDIT";
	public static final String KEY_QUESTION_NUMBERING = "QUESTION_NUMBERING";
	public static final String KEY_OK_ON_RIGHT = "OK_ON_RIGHT";
	//public static final String KEY_DELETE_DATA_AFTER_UPLOAD = "DELETE_DATA_AFTER_UPLOAD";
	public static final String KEY_HIDE_STUDIES = "HIDE_STUDIES";
	public static final String KEY_USE_STUDY_NUMERIC_ID = "USE_STUDY_NUMERIC_ID";
	public static final String KEY_MAIN_MENU = "MAIN_MENU";
	
	public static final String STORAGE_NAME_SETTINGS = "fcitmuk.GeneralSettings";
	
	private ChoiceGroup currentCtrl;
	
	public void display(Display display, Displayable prevScreen){
		
		setDisplay(display);
		setPrevScreen(prevScreen);
		
		screen = new Form(MenuText.SETTINGS());
		currentCtrl = new ChoiceGroup(MenuText.SETTINGS(),Choice.MULTIPLE);
			
		screen.addCommand(DefaultCommands.cmdOk);
		screen.addCommand(DefaultCommands.cmdCancel);
		
		currentCtrl.append(MenuText.SINGLE_QUESTION_EDIT(), null);
		currentCtrl.append(MenuText.NUMBERING(), null);
		currentCtrl.append(MenuText.OK_ON_RIGHT(), null);
		//currentCtrl.append(MenuText.DELETE_AFTER_UPLOAD(), null);
		currentCtrl.append(MenuText.HIDE_STUDIES(), null);
		//currentCtrl.append(MenuText.MAIN_MENU_VIEW(), null);
		
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		
		currentCtrl.setSelectedIndex(0,getSingleQtnEditSetting(settings));
		currentCtrl.setSelectedIndex(1,getQtnNumberingSetting(settings));
		currentCtrl.setSelectedIndex(2,getOkOnRightSetting(settings));
		currentCtrl.setSelectedIndex(3,getIsHideStudiesSettings(settings));
		//currentCtrl.setSelectedIndex(4,getIsMainMenuSettings(settings));
		//currentCtrl.setSelectedIndex(3,Utilities.stringToBoolean(settings.getSetting(KEY_DELETE_DATA_AFTER_UPLOAD),true));
		
		screen.setCommandListener(this);
		((Form)screen).append(currentCtrl);
				
		AbstractView.display.setCurrent(screen);
	}
	
	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		try{
			if(c == DefaultCommands.cmdOk)
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
	 * Processes the OK command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleOkCommand(Displayable d){

		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		settings.setSetting(KEY_SINGLE_QUESTION_EDIT,Utilities.booleanToString((currentCtrl.isSelected(0))));
		settings.setSetting(KEY_QUESTION_NUMBERING,Utilities.booleanToString((currentCtrl.isSelected(1))));
		settings.setSetting(KEY_OK_ON_RIGHT,Utilities.booleanToString((currentCtrl.isSelected(2))));
		settings.setSetting(KEY_HIDE_STUDIES,Utilities.booleanToString((currentCtrl.isSelected(3))));
		//settings.setSetting(KEY_MAIN_MENU,Utilities.booleanToString((currentCtrl.isSelected(4))));
		//settings.setSetting(KEY_DELETE_DATA_AFTER_UPLOAD,Utilities.booleanToString((currentCtrl.isSelected(3))));
		settings.saveSettings();
		
		DefaultCommands.cmdOk = new Command(MenuText.OK(), currentCtrl.isSelected(3) ? Command.CANCEL : Command.OK, 1);
		
		display.setCurrent(getPrevScreen());
	}
	
	/**
	 * Processes the cancel command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleCancelCommand(Displayable d){
		display.setCurrent(getPrevScreen());
	}
	
	private static boolean getSingleQtnEditSetting(Settings settings) {
		String setting = settings.getSetting(KEY_SINGLE_QUESTION_EDIT);
		if (setting == null) {
			return FormsConstants.SINGLE_QUESTION_EDIT;
		}
		return Utilities.stringToBoolean(setting);
	}
	
	public static boolean isSingleQtnEdit() {
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		return getSingleQtnEditSetting(settings);
	}
	
	public static boolean getQtnNumberingSetting(Settings settings) {
		String setting = settings.getSetting(KEY_QUESTION_NUMBERING);
		if (setting == null) {
			return FormsConstants.QUESTION_NUMBERING;
		}
		return Utilities.stringToBoolean(setting);
	}
	
	public static boolean isQtnNumbering() {
		Settings settings = new Settings(GeneralSettings.STORAGE_NAME_SETTINGS,true);
		return getQtnNumberingSetting(settings);
	}
	
	public static boolean getOkOnRightSetting(Settings settings) {
		String setting = settings.getSetting(KEY_OK_ON_RIGHT);
		if (setting == null) {
			return FormsConstants.OK_ON_RIGHT;
		}
		return Utilities.stringToBoolean(setting);
	}
	
	public static boolean isOkOnRight(){
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		return getOkOnRightSetting(settings);
	}
	
	public static boolean getUseStudyNumericIdSetting(Settings settings) {
		String setting = settings.getSetting(KEY_USE_STUDY_NUMERIC_ID);
		if (setting == null) {
			return FormsConstants.USE_STUDY_NUMERIC_ID;
		}
		return Utilities.stringToBoolean(setting);
	}
	
	public static boolean isUseStudyNumericId(){
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		return getUseStudyNumericIdSetting(settings);
	}
	
	/*public static boolean deleteDataAfterUpload(){
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		return Utilities.stringToBoolean(settings.getSetting(KEY_DELETE_DATA_AFTER_UPLOAD),true);
	}
	
	public static void setDeleteDataAfterUpload(boolean delete){
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		settings.setSetting(KEY_DELETE_DATA_AFTER_UPLOAD,Utilities.booleanToString(delete));
	}*/
	
	private static boolean getIsHideStudiesSettings(Settings settings) {
		String setting = settings.getSetting(KEY_HIDE_STUDIES);
		if (setting == null) {
			return FormsConstants.IS_HIDE_STUDIES;
		}
		return Utilities.stringToBoolean(setting);
	}	
	
	public static boolean isHideStudies(){
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		return getIsHideStudiesSettings(settings);
	}
	
	private static boolean getIsMainMenuSettings(Settings settings) {
		String setting = settings.getSetting(KEY_MAIN_MENU);
		if (setting == null) {
			return FormsConstants.MAIN_MENU;
		}
		return Utilities.stringToBoolean(setting);
	}	
	
	public static boolean isMainMenu(){
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		return getIsMainMenuSettings(settings);
	}	
}
