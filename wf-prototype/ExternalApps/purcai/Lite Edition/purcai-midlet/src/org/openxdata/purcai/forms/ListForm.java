package org.openxdata.purcai.forms;

import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.openxdata.mvc.AbstractView;
import org.openxdata.purcai.NameValue;
import org.openxdata.util.DefaultCommands;


public class ListForm extends AbstractView{

	private Vector list;
	//private AlertMessage alertMsg;
	
	public void commandAction(Command c, Displayable d){
		try{
			if(c == List.SELECT_COMMAND || c == DefaultCommands.cmdOk)
				handleOkCommand(DefaultCommands.cmdOk,d);
			else if(c == DefaultCommands.cmdBack || c == DefaultCommands.cmdMainMenu)
				handleCancelCommand(c,d);
		}
		catch(Exception e){
			//alertMsg.showError(e.getMessage());
			e.printStackTrace();
		}

	}
	
	public void showData(Vector list,int defaultSelectId,boolean showMainMenuCmd){
		this.list = list;
		
		screen = new List(getTitle(), Choice.IMPLICIT);
		//alertMsg = new AlertMessage(getDisplay(),getTitle(),screen,this);
		
		NameValue val;
		int selectedIndex = 0;
		if(list != null){
			for(int i=0; i<list.size(); i++){
				val = ((NameValue)list.elementAt(i));
				if(val.getId() == defaultSelectId)
					selectedIndex = i;
				((List)screen).append(val.getName(), null);
			}
			
			if(list.size() > 0)
				((List)screen).setSelectedIndex(selectedIndex, true);
		}
		
		screen.setCommandListener(this);
		screen.addCommand(DefaultCommands.cmdOk);
		screen.addCommand(DefaultCommands.cmdBack);
		if(showMainMenuCmd)
			screen.addCommand(DefaultCommands.cmdMainMenu);
		getDisplay().setCurrent(screen);
	}
	
	public void handleOkCommand(Command c,Displayable d){
		if(list == null || list.size() == 0)
			handleCancelCommand(c,d);
		else
			getController().execute(this,c, list.elementAt(((List)screen).getSelectedIndex()));
	}
	
	public void handleCancelCommand(Command c,Displayable d){
		getController().execute(this,(c == DefaultCommands.cmdBack) ? DefaultCommands.cmdCancel : c, new NameValue());
	}
}
