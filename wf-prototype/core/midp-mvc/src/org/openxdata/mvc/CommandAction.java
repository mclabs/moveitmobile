package org.openxdata.mvc;


/**
 * Command actions executed in the application.
 * The values are intensionally made of type Byte instead of byte
 * to allow more flexibility in the controller execute method
 * command action parameter which is made data type object to 
 * allow user pass arbitrary objects. Eg one may want to
 * dirrectly pass the actual Command object for easiness.
 * 
 * @author daniel
 *
 */
public class CommandAction {
	
	/** Command representing an uninitialized state. */
	//public static final Byte NONE = new Byte((byte)0);
	
	/** Command to accept and action. */
	public static final Byte OK = new Byte((byte)1);
	
	/** Command to cancel and action. */
	public static final Byte CANCEL = new Byte((byte)2);
	
	/** Command to create a new item. */
	public static final Byte NEW = new Byte((byte)3);
	
	/** Command to edit an item. */
	public static final Byte EDIT = new Byte((byte)4);
	
	/** Command to save an item. */
	//public static final Byte SAVE = new Byte((byte)5);
	
	/** Command to delete an item. */
	//public static final Byte DELETE = new Byte((byte)6);
	
	/** Command to move to the next item. */
	//public static final Byte NEXT = new Byte((byte)7);
	
	/** Command to move to the previous item. */
	//public static final Byte PREVIOUS = new Byte((byte)8);
	
	/** Command to move to the previous screen. */
	//public static final Byte BACK = new Byte((byte)9);
	
	/** Command to close the midlet. */
	//public static final Byte EXIT = new Byte((byte)10);
}
