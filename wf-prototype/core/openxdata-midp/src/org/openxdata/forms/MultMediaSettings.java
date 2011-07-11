package org.openxdata.forms;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

import org.openxdata.db.util.Settings;
import org.openxdata.mvc.AbstractView;
import org.openxdata.util.DefaultCommands;
import org.openxdata.util.MenuText;


/**
 * 
 * @author daniel
 *
 */
public class MultMediaSettings extends AbstractView{

	private static final String KEY_PICTURE_FORMAT = "PICTURE_FORMAT ";
	public static final String KEY_PICTURE_WIDTH = "PICTURE_WIDTH";
	public static final String KEY_PICTURE_HEIGHT = "PICTURE_HEIGHT";
	public static final String KEY_VIDEO_FORMAT = "KEY_VIDEO_FORMAT";
	public static final String KEY_AUDIO_FORMAT = "KEY_AUDIO_FORMAT";
	public static final String STORAGE_NAME_SETTINGS = "fcitmuk.MultMediaSettings";

	public void display(Display display, Displayable prevScreen){

		setDisplay(display);
		setPrevScreen(prevScreen);

		screen = new Form(MenuText.MULTIMEDIA());

		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);

		TextField txtField = new TextField(MenuText.PICTURE_FORMAT(),settings.getSetting(KEY_PICTURE_FORMAT),20,TextField.ANY);
		((Form)screen).append(txtField);

		txtField = new TextField(MenuText.PICTURE_WIDTH(),settings.getSetting(KEY_PICTURE_WIDTH),10,TextField.NUMERIC);
		((Form)screen).append(txtField);

		txtField = new TextField(MenuText.PICTURE_HEIGHT(),settings.getSetting(KEY_PICTURE_HEIGHT),10,TextField.NUMERIC);
		((Form)screen).append(txtField);

		txtField = new TextField(MenuText.VIDEO_FORMAT(),settings.getSetting(KEY_VIDEO_FORMAT),20,TextField.ANY);
		((Form)screen).append(txtField);

		txtField = new TextField(MenuText.AUDIO_FORMAT(),settings.getSetting(KEY_AUDIO_FORMAT),20,TextField.ANY);
		((Form)screen).append(txtField);

		try{
			txtField = new TextField(MenuText.ENCODINGS(),System.getProperty("video.snapshot.encodings"),500,TextField.ANY);
			((Form)screen).append(txtField);
		}catch(Exception ex){}

		//Audio Rate
		//Audio Bits

		screen.addCommand(DefaultCommands.cmdCancel);
		screen.addCommand(DefaultCommands.cmdOk);
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
		settings.setSetting(KEY_PICTURE_FORMAT,((TextField)((Form)d).get(0)).getString());
		settings.setSetting(KEY_PICTURE_WIDTH,((TextField)((Form)d).get(1)).getString());
		settings.setSetting(KEY_PICTURE_HEIGHT,((TextField)((Form)d).get(2)).getString());
		settings.setSetting(KEY_VIDEO_FORMAT,((TextField)((Form)d).get(3)).getString());
		settings.setSetting(KEY_AUDIO_FORMAT,((TextField)((Form)d).get(4)).getString());
		settings.saveSettings();

		display.setCurrent(getPrevScreen());
	}

	/**
	 * Processes the cancel command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	/*private void handleCancelCommand(Displayable d){
		display.setCurrent(getPrevScreen());
	}*/

	public static String getPictureParameters(){
		String format = null;

		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		String s = settings.getSetting(KEY_PICTURE_FORMAT);
		if(s == null || s.trim().length() == 0)
			return null;
		format = "encoding="+s;

		s = settings.getSetting(KEY_PICTURE_WIDTH);
		if(s == null || s.trim().length() == 0)
			return null;
		format += "&width="+s;

		s = settings.getSetting(KEY_PICTURE_HEIGHT);
		if(s == null || s.trim().length() == 0)
			return null;
		format += "&height="+s;

		return format;
	}

	public static String getAudioFormat(){
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		return settings.getSetting(KEY_AUDIO_FORMAT,"x-wav");
	}

	public static String getVideoFormat(){
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		return settings.getSetting(KEY_VIDEO_FORMAT,"mpeg");
	}
}
