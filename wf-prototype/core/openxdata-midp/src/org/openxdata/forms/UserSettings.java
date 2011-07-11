package org.openxdata.forms;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.openxdata.communication.TransportLayer;
import org.openxdata.db.util.Settings;
import org.openxdata.mvc.AbstractView;
import org.openxdata.util.DefaultCommands;
import org.openxdata.util.MenuText;


/**
 * 
 * @author daniel
 *
 */
public class UserSettings extends AbstractView {

	private static final String KEY_LAST_SELECTED_SETTING = "KEY_LAST_SELECTED_SETTING";
	private static final String STORAGE_NAME_SETTINGS = "fcitmuk.UserSettings";
	
	private TransportLayer transportLayer;
	private String userName;
	private String password;
	
	public void display(Display display, Displayable prevScreen, TransportLayer transportLayer,String userName, String password){
		
		this.userName = userName;
		this.password = password;
		
		setDisplay(display);
		setPrevScreen(prevScreen);
		this.transportLayer = transportLayer;
		
		screen = new List(MenuText.SETTINGS(), Choice.IMPLICIT);
		((List)screen).setFitPolicy(List.TEXT_WRAP_ON);
			
		screen.addCommand(DefaultCommands.cmdSel);
		screen.addCommand(DefaultCommands.cmdBack);
		
		((List)screen).append(MenuText.GENERAL(), null);
		((List)screen).append(MenuText.DATE_FORMAT(), null);
		((List)screen).append(MenuText.MULTIMEDIA(), null);
		((List)screen).append(MenuText.LANGUAGE(), null);
		((List)screen).append(MenuText.CONNECTION(), null);
		
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		String val = settings.getSetting(KEY_LAST_SELECTED_SETTING);
		if(val != null)
			((List)screen).setSelectedIndex(Integer.parseInt(val),true);
		
		screen.setCommandListener(this);
				
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
			if(c == DefaultCommands.cmdSel || c == List.SELECT_COMMAND)
				handleOkCommand(d);
			else if(c == DefaultCommands.cmdBack)
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
		int index = ((List)getScreen()).getSelectedIndex();
		if(index == 0)
			new GeneralSettings().display(display, screen);
		else if(index == 1)
			new DateSettings().display(display, screen);
		else if(index == 2)
			new MultMediaSettings().display(display, screen);
		else if(index == 3)
			new LanguageSettings().display(display, screen);
		else{
			transportLayer.setCurrentAlert(null);
			transportLayer.getUserSettings(display, screen,userName,password);
		}
		
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		settings.setSetting(KEY_LAST_SELECTED_SETTING, String.valueOf(((List)getScreen()).getSelectedIndex()));
		settings.saveSettings();
	}
	
	/**
	 * Processes the cancel command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleCancelCommand(Displayable d){
		display.setCurrent(getPrevScreen());
	}
}
