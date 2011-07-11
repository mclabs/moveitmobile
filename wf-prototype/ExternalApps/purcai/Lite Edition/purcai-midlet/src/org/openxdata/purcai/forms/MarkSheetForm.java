package org.openxdata.purcai.forms;

import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.openxdata.db.util.Settings;
import org.openxdata.forms.GeneralSettings;
import org.openxdata.mvc.AbstractView;
import org.openxdata.mvc.Controller;
import org.openxdata.mvc.View;
import org.openxdata.purcai.MarkSheet;
import org.openxdata.purcai.StudentIdNameMark;
import org.openxdata.util.AlertMessage;
import org.openxdata.util.AlertMessageListener;
import org.openxdata.util.DefaultCommands;
import org.openxdata.util.Utilities;

public class MarkSheetForm  extends AbstractView implements AlertMessageListener,Controller{

	private AlertMessage alertMsg;
	private MarkSheet markSheet;
	private MarkEditForm markEditor;
	private boolean singleQtnEdit;
	private boolean confirmCancelMode = false;
	private boolean dirty = false;

	public MarkSheetForm(String title,Display display,Displayable prevScreen,Controller controller){
		super(title,display,prevScreen,controller);
		dirty = false;
	}

	public void show(MarkSheet markSheet,boolean singleQtnEdit){
		this.markSheet = markSheet;
		this.singleQtnEdit = singleQtnEdit;		

		screen = new List(getTitle(), Choice.IMPLICIT);

		alertMsg = new AlertMessage(getDisplay(),getTitle(),screen,this);
		markEditor = new MarkEditForm(getTitle(),getDisplay(),screen,this);

		Settings settings = new Settings(GeneralSettings.STORAGE_NAME_SETTINGS,true);
		boolean numbering = Utilities.stringToBoolean(settings.getSetting(GeneralSettings.KEY_QUESTION_NUMBERING));

		Vector list = markSheet.getStudentMarks();
		if(list != null){
			for(int i=0; i<list.size(); i++)
				((List)screen).append((numbering ? String.valueOf(i+1)+" " : "") + ((StudentIdNameMark)list.elementAt(i)).toString(), null);
		}

		screen.setCommandListener(this);
		screen.addCommand(DefaultCommands.cmdSave);
		screen.addCommand(DefaultCommands.cmdCancel);
		getDisplay().setCurrent(screen);

		//if(singleQtnEdit)
		//	handleEditCommand(null);
	}

	public void commandAction(Command c, Displayable d){
		try{
			if(c == List.SELECT_COMMAND)
				handleEditCommand(d);
			else if(c == DefaultCommands.cmdSave)
				handleSaveCommand(d);
			else if(c == DefaultCommands.cmdCancel)
				handleCancelCommand(d);
		}
		catch(Exception e){
			alertMsg.showError(e.getMessage());
			e.printStackTrace();
		}

	}

	private int getSelectedIndex(){
		return ((List)getScreen()).getSelectedIndex();
	}

	public void handleEditCommand(Displayable d){
		int count = markSheet.getStudentMarks().size();
		int index = getSelectedIndex();
		markEditor.editMark(markSheet.getStudentMark(index),markSheet.getHeader().getOutOf(), "Please enter a mark less than 100",true,singleQtnEdit, index+1, count);
	}

	public void handleSaveCommand(Displayable d){
		getController().execute(this,DefaultCommands.cmdOk, markSheet);
	}

	public void handleCancelCommand(Displayable d){
		confirmCancelMode = true;
		if(dirty)
			alertMsg.showConfirm("Do you want to close this mark sheet without saving any changes you may have made?");
		else
			onAlertMessage(AlertMessageListener.MSG_OK);
	}

	public void onAlertMessage(byte msg){
		if(msg == AlertMessageListener.MSG_OK){
			if(confirmCancelMode){
				confirmCancelMode = false;
				getController().execute(this, DefaultCommands.cmdCancel,new MarkSheet());
			}
			else
				display.setCurrent(screen);
		}
		else{
			if(confirmCancelMode){
				confirmCancelMode = false;
				show();
			}
		}
	}

	public void execute(View view, Object command, Object data){

		if(!dirty && command != DefaultCommands.cmdCancel)
			dirty = true;

		/*if(data == DefaultCommands.cmdPrev){
			((List)screen).setSelectedIndex(((List)screen).getSelectedIndex()-1, true);
			handleEditCommand(screen);
		}
		else*/{
			int index = getSelectedIndex();
			show(markSheet,singleQtnEdit);

			if(command == DefaultCommands.cmdPrev)
				index--;
			else if(command == DefaultCommands.cmdFirst)
				index = 0;
			else if(command  == DefaultCommands.cmdLast)
				index = markSheet.getStudentMarks().size() - 1;
			else if(index < (markSheet.getStudentMarks().size()-1) && !(command == DefaultCommands.cmdCancel || command == DefaultCommands.cmdBackParent))
				index++;
			((List)screen).setSelectedIndex(index, true);

			if(singleQtnEdit && !(command == DefaultCommands.cmdCancel || command == DefaultCommands.cmdBackParent))
				handleEditCommand(screen);
		}
	}
}
