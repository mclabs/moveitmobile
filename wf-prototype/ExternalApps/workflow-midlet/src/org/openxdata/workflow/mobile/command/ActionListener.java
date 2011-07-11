package org.openxdata.workflow.mobile.command;

import javax.microedition.lcdui.Command;

public interface ActionListener {
	
	public boolean handle(Command cmd, Viewable disp, Object obj);

}
