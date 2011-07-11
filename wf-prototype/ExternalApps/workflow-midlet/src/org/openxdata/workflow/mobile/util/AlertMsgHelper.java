package org.openxdata.workflow.mobile.util;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.openxdata.util.AlertMessage;

public class AlertMsgHelper {

	public static void showMsg(Display display,Displayable disp, String errorMessage){
		AlertMessage msg = getAlertMessage(display, disp);
		msg.setTitle("Information");
		msg.show(errorMessage, AlertType.INFO);
		System.gc();
	}
	
	public static void showError(Display display,Displayable disp, String errorMessage){
		AlertMessage alertMessage = getAlertMessage(display, disp);
		alertMessage.setTitle("Error");
		alertMessage.showError(errorMessage);
		System.gc();
	}

	private static AlertMessage getAlertMessage(Display display,Displayable disp) {
		AlertMsgListenerImpl msgListener = new AlertMsgListenerImpl(display, disp);
		AlertMessage msg = new AlertMessage(display, null, disp, msgListener );
		return msg;
	}
	
}
