package org.openxdata.purcai.forms;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;

import org.openxdata.mvc.AbstractView;
import org.openxdata.mvc.Controller;
import org.openxdata.purcai.StudentIdMark;
import org.openxdata.purcai.StudentIdNameMark;
import org.openxdata.util.AlertMessage;
import org.openxdata.util.AlertMessageListener;
import org.openxdata.util.DefaultCommands;

public class MarkEditForm  extends AbstractView implements AlertMessageListener{

	private StudentIdNameMark studentMark;
	private AlertMessage alertMsg;
	Item currentCtrl;
	byte outOf;
	boolean allowEmpty = false;
	
	public MarkEditForm(String title,Display display,Displayable prevScreen,Controller controller){
		super(title,display,prevScreen,controller);
	}
	
	public void editMark(StudentIdNameMark studentMark,byte outOf,String helpText,boolean allowEmpty,boolean singleQtnEdit, int pos, int count){
		this.studentMark = studentMark;
		this.outOf = outOf;
		this.allowEmpty = allowEmpty;
		
		if(pos > 0)
			screen = new Form("{"+pos+"/"+count+"} "+title);
		else
			screen = new Form(title);
		
		alertMsg = new AlertMessage(display,title,screen,this);
		
		currentCtrl = new TextField(studentMark.getName(),studentMark.getDisplayMark(),3,TextField.DECIMAL);
		
		if(helpText != null && helpText.length() > 0){
			Ticker t = new Ticker(helpText);
			screen.setTicker(t);
		}

		((Form)screen).append(currentCtrl);
		
		boolean isFirst = (pos == 0);
		boolean isLast = (pos == count);
		
		if(singleQtnEdit){
			
			if(!isLast)
				screen.addCommand(DefaultCommands.cmdNext);
			
			if(!isFirst){
				screen.addCommand(DefaultCommands.cmdPrev);
				screen.addCommand(DefaultCommands.cmdFirst);
			}
			
			if(!isLast)
				screen.addCommand(DefaultCommands.cmdLast);
			
			screen.addCommand(DefaultCommands.cmdBackParent);
		}
		else{
			screen.addCommand(DefaultCommands.cmdOk);
			screen.addCommand(DefaultCommands.cmdCancel);
		}
		
		screen.setCommandListener(this);
		display.setCurrent(screen);
	}
	
	public void commandAction(Command c, Displayable d){
		if(c == DefaultCommands.cmdOk || c == DefaultCommands.cmdNext || 
				c == DefaultCommands.cmdBackParent || c == DefaultCommands.cmdPrev ||
				c == DefaultCommands.cmdFirst || c == DefaultCommands.cmdLast)
			handleOkCommand(d,c);
		else if(c == DefaultCommands.cmdCancel /*|| c == DefaultCommands.cmdBackParent*/)
			handleCancelCommand(d,c);
		else
			getController().execute(this, DefaultCommands.cmdPrev, c);
	}
	
	public void handleOkCommand(Displayable d, Command cmd){
		try{
			String val = ((TextField)currentCtrl).getString();
			if(val != null && val.trim().length() > 0){
				byte mark = Byte.parseByte(val);
				if(mark <= outOf){
					studentMark.setMark(mark);
					getController().execute(this, cmd, studentMark);					
					return;
				}
			}
			else if(val != null && val.trim().length() == 0 && allowEmpty){
				studentMark.setMark(StudentIdMark.NULL_MARK);
				getController().execute(this, cmd, studentMark);					
				return;
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		alertMsg.show("Please enter a value less than or equal to " + outOf + " or click Cancel:");
	}
	
	public void handleCancelCommand(Displayable d, Command cmd){
		getController().execute(this, DefaultCommands.cmdCancel,new StudentIdNameMark());
	}
	
	public void onAlertMessage(byte msg){
		show();
	}
}
