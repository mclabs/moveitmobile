package org.openxdata.mvc;


/**
 * Manages cordination of views within the application. It knows all the views.
 * Views do not know about each other. All they know is the controller and as a result
 * send requests to it. These requests normally require switching of views and its the 
 * controller that knows which view to switch to.
 * In otherwards, views speak to each other through the controller.
 * The controller also passes data between views. It knows which data (Model) to pass to which
 * View and where the data is to stored and retrieved.
 * 
 * @author Daniel Kayiwa
 *
 */
public interface Controller {

	/**
	 * Called to execute a command.
	 * 
	 * @param view the view initiating command execution.
	 * @param commandAction the command action to execute.
	 * @param data the data passed with the command, if any.
	 */
	public void execute(View view, Object commandAction, Object data);
}
