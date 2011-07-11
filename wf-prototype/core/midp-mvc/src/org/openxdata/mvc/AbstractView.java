package org.openxdata.mvc;


import javax.microedition.lcdui.*;


/**
 * Provides common implementation for all views.
 * 
 * @author Daniel
 *
 */
public abstract class AbstractView implements View {
	/** The title of our screen. */	
	protected String title;
	
	/** Rerefence to the current display. */
	public static Display display;
	
	/** The mvc controller. */
	protected Controller controller;
	
	/** Our screen. */
	protected Displayable screen;
	
	/** Screen previously displayed before ours. It is normally this screen to display when ours is closed. */
	protected Displayable prevScreen;
	
	protected AbstractView(){
		
	}
	
	protected AbstractView(String title,Display display,Displayable prevScreen,Controller controller){
		setTitle(title);
		setDisplay(display);
		setPrevScreen(prevScreen);
		setController(controller);
	}
	
	public String getTitle(){
		return title;
	}
	
	public Display getDisplay(){
		return display;
	}
	
	public Controller getController(){
		return controller;
	}
	
	public Displayable getScreen(){
		return screen;
	}
	
	public Displayable getPrevScreen(){
		return prevScreen;
	}

	public void setTitle(String t){
		title = t;
	}
	
	public void setDisplay(Display d){
		display = d;
	}
	
	public void setPrevScreen(Displayable prevScreen){
		this.prevScreen = prevScreen;
	}
	
	public void setController(Controller controller){
		this.controller = controller;
	}
	
	public void show(){
		display.setCurrent(screen);
	}
	
	public abstract void commandAction(Command c, Displayable s);
}
