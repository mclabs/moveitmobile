package org.openxdata.forms;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

import org.openxdata.db.util.Settings;
import org.openxdata.model.QuestionData;
import org.openxdata.mvc.AbstractView;
import org.openxdata.util.DefaultCommands;
import org.openxdata.util.MenuText;


/**
 * 
 * @author daniel
 *
 */
public class DateSettings  extends AbstractView{

	private static final String KEY_DATE_FORMAT = "DATE_FORMAT ";
	public static final String STORAGE_NAME_SETTINGS = GeneralSettings.STORAGE_NAME_SETTINGS;
	private ChoiceGroup currentCtrl;
	
	public void display(Display display, Displayable prevScreen){

		setDisplay(display);
		setPrevScreen(prevScreen);
		
		screen = new Form(MenuText.DATE_FORMAT());
		currentCtrl = new ChoiceGroup(MenuText.DATE_FORMAT(),Choice.EXCLUSIVE);
			
		screen.addCommand(DefaultCommands.cmdOk);
		screen.addCommand(DefaultCommands.cmdCancel);
		
		currentCtrl.append(MenuText.DAY_FIRST(), null);
		currentCtrl.append(MenuText.MONTH_FIRST(), null);
		currentCtrl.append(MenuText.YEAR_FIRST(), null);
		
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		currentCtrl.setSelectedIndex(Integer.parseInt(settings.getSetting(KEY_DATE_FORMAT,"0")),true);

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
				display.setCurrent(getPrevScreen());
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
		settings.setSetting(KEY_DATE_FORMAT,String.valueOf(currentCtrl.getSelectedIndex()));
		settings.saveSettings();
		QuestionData.dateDisplayFormat = (byte)currentCtrl.getSelectedIndex();
		display.setCurrent(getPrevScreen());
	}

	public static byte getDateFormat(){
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		return Byte.parseByte(settings.getSetting(KEY_DATE_FORMAT,"0"));
	}
}
