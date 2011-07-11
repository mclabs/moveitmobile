package org.openxdata.workflow.mobile.command;

import java.util.Hashtable;

import javax.microedition.lcdui.Command;

public class ActionDispatcher {

	private Hashtable listeners = new Hashtable(3);

	public void fireAction(Command cmd, Viewable disp, Object obj) {
		Object handler = listeners.get(cmd);
		if (handler != null) {
			ActionListener listener = (ActionListener) handler;
			listener.handle(cmd, disp, obj);
		}
	}

	public void registerListener(Command cmd, ActionListener listener) {
		listeners.put(cmd, listener);
	}

	public void deregister(Command cmd) {
		listeners.remove(cmd);
	}

	public void clear() {
		listeners.clear();
	}

}
