package org.openxdata.workflow.mobile.util;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.openxdata.util.AlertMessageListener;

public class AlertMsgListenerImpl implements AlertMessageListener{

	private final Display display;
	private final Displayable displayable;

	public AlertMsgListenerImpl(Display display, Displayable displayable){
		this.display = display;
		this.displayable = displayable;
		
	}
	
	public void onAlertMessage(byte msg) {
		display.setCurrent(displayable);
	}

}
