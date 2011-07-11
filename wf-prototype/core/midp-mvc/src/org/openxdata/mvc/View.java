package org.openxdata.mvc;

import javax.microedition.lcdui.*;

/**
 * The view displays information to the user and allows him to manipulate this information
 * in ways like editing, saving, deleting, closing etc.
 * Because some views are not editable is why the edit functionality is in the subclasses.
 * The view is responsible for managing its state between invocations for increased user
 * friendliness. An example is a form view which may remember the last selected question 
 * and page (for those view implementation that have pages).
 * A view may also do some validations before delegating to the controller.
 * But the view should not know any business rules in the model, so this validation
 * is like asking the model for validity and the see if can close.
 * 
 * @author Daniel Kayiwa
 *
 */
public interface View extends CommandListener{
	
	public Controller getController();
	public Displayable getScreen();
	public Displayable getPrevScreen();
	public String getTitle();
	public Display getDisplay();
	
	public void setController(Controller controller);
	public void setPrevScreen(Displayable prevScreen);
	public void setTitle(String title);
	public void setDisplay(Display title);
	
	public void show();
}
