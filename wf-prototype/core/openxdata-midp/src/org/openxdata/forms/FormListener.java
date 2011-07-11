package org.openxdata.forms;

import java.util.Vector;

import org.openxdata.model.FormData;
import org.openxdata.model.FormDef;
import org.openxdata.model.QuestionData;


/** 
 * List of form events.
 * 
 * @author Daniel Kayiwa.
 *
 */
public interface FormListener {	
	
	/** 
	 * Called after the cancel button of a form has been clicked but before 
	 * the cancel command is called.
	 * 
	 * @param data - Data in the form to be canceled. 
	 * @param return - A flag which you can set to prevent or allow the user go ahead and cancel the form.
	 */
	public boolean beforeFormCancelled(FormData data);
	
	/** 
	 * Called after a form has been cancelled.
	 * 
	 * @param data
	 */
	public void afterFormCancelled(FormData data);
	
	/** 
	 * Called after the use has clicked the save button but before the form is saved
	 * 
	 * @param data - the data being saved.
	 * @param isNew - true if saving a new form, else false if this is an existing form which has just been edited.
	 * @return - set to true if you want to go ahead and save, else set to false.
	 */
	public boolean beforeFormSaved(FormData data, boolean isNew); 
	
	/** 
	 * Called after a form is saved.
	 * 
	 * @param data - the form data to save
	 * @param isNew - true if this is a new form, else false.
	 */
	public void afterFormSaved(FormData data, boolean isNew);
	
	
	/**
	 * You may want to say prepopulate values before a form is displayed.
	 * You may even do some custom processing and decide whether to display the form or not.
	 * 
	 * @param data the data of the form about to be displayed.
	 * @return true if you want the form to be displayed, else false.
	 */
	public boolean beforeFormDisplay(FormData data);
	//public boolean afterFormDisplay(FormData data); //is this event usefull?
	
	public boolean beforeQuestionEdit(QuestionData data);
	public boolean afterQuestionEdit(QuestionData data);
	
	//public boolean beforeRuleFire(SkipRule rule,QuestionData data);
	//public boolean afterRuleFire(SkipRule rule,QuestionData data);
	
	public boolean beforeFormDataListDisplay(FormDef formDef);
	
	
	public boolean beforeFormDelete(FormData data);
	public void afterFormDelete(FormData data);
	
	/**
	 * Called before displaying a list of form definitions.
	 * This can be used to say filter out forms you may not want to display to the user.
	 * Eg a new patient form which you automatically open when one is creating a new patient.
	 * 
	 * @param formDefList - a list of form definitions (FormDef)
	 * @return true if you want the list to be displayed, else false.
	 */
	public boolean beforeFormDefListDisplay(Vector formDefList);
}
