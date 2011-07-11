package org.openxdata.forms;

import org.openxdata.model.QuestionData;
import org.openxdata.model.ValidationRule;
import org.openxdata.mvc.View;

/**
 * This interface lets users create new data types and set classes for editing these extra types.
 * 
 * @author Daniel Kayiwa
 *
 */
public interface TypeEditor extends View {
	
	/**
	 * Methods called to start editing question data.
	 * 
	 * @param data the data to edit.
	 * 
	 * @param singleQtnEdit flag set to true if editing one question per screen, else false.
	 * 						This just lets the editor know if to diplay the single question
	 * 						edit mode three extra commands (Next,Previous,BackToParent)
	 * 						or else, will only display the two standard editing 
	 * 						commands (Ok,Cancel). Next and Previous commands save changes for
	 * 						the current question before going to the next or previous
	 * 						questions respectively. BackToParent does not save changes in the
	 * 						current question but instead just takes the user to the 
	 * 						parent or previous view. Ok saves the current question before going 
	 * 						to the previous view. Cancel does not save the current question 
	 * 						changes and instead just switches to the previous view.
	 * 
	 * @param listener the listener to type editing events. Eg when editing stops.
	 */
	public void startEdit(QuestionData data, ValidationRule validationRule, boolean singleQtnEdit,int pos, int count,TypeEditorListener listener);
}
