package org.openxdata.forms;

import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.openxdata.model.FormData;
import org.openxdata.model.FormDef;
import org.openxdata.model.OpenXdataConstants;
import org.openxdata.model.PageData;
import org.openxdata.model.QuestionData;
import org.openxdata.model.QuestionDef;
import org.openxdata.model.StudyDef;
import org.openxdata.model.ValidationRule;
import org.openxdata.mvc.AbstractView;
import org.openxdata.util.AlertMessage;
import org.openxdata.util.AlertMessageListener;
import org.openxdata.util.DefaultCommands;
import org.openxdata.util.MenuText;

/**
 * Displays a form. This means displaying the list of questions on a form.
 * This view may also optionally like to display the answers of the questions if any.
 * It is up to the view to either display the questions either one at a time
 * or all at the same time. The view does not know how to edit a question (as in doesnt know
 * how to edit images, dates, numbers, text etc). All it does is provide the user with
 * a way of starting editing of a question. This could be by a button, voice or any other
 * way depending on its implementation. The view also doesnt know where and how to save a form.
 * All it does is delegate to the controller and pass it the modified model.
 * It is the view which decides whether to group questions in pages or any other format.
 * It should allow the user to:
 * 1. Browse through the questions.
 * 2. Edit a question, 
 * 3. Save the form.
 * 4. Close the form.
 * 
 * @author Daniel
 *
 */
public class FormView extends AbstractView implements AlertMessageListener {

	/** Command for displaying the next page. */
	private Command cmdNext = new Command(MenuText.NEXT_PAGE(),Command.SCREEN,1);

	/** Command for displaying the previous page. */
	private Command cmdPrev = new Command(MenuText.PREVIOUS_PAGE(),Command.SCREEN,2);

	private FormData formData;
	private PageData currentPage; //TODO is this really necessary????
	private AlertMessage alertMsg;
	private FormListener listener;

	//for managing state
	private int currentPageIndex = OpenXdataConstants.NO_SELECTION;
	private int currentQuestionIndex = OpenXdataConstants.NO_SELECTION;
	private QuestionData currentQuestion = null;

	private final byte CA_NONE = 0;
	private final byte CA_CONFIRM_CANCEL = 1;
	private final byte CA_CONFIRM_DELETE = 2;
	private final byte CA_ERROR = 3;
	//private final byte CA_NO_VISIBLE_QTNS = 4;

	private byte currentAction = CA_NONE;

	private boolean dirty = false;


	/** Keeps a mapping of displayed questions (in a page) to their indices in the list control.
	 *  We were originally using the questions collection of the page in formdata which
	 *  did not work as their indices get out of sync with those of the List control
	 *  because of invisible questions not being put in the list.
	 */
	private Vector displayedQuestions;

	/**
	 * Called by the controller after an editing operation.
	 * 
	 * @param saved - true when the edit was commited, else false.
	 */
	public void onEndEdit(boolean saved, Command cmd){

		if(!dirty && saved)
			dirty = true;

		display.setCurrent(screen);

		if(cmd == DefaultCommands.cmdBackParent){
			if(currentQuestionIndex > 0)
				currentQuestionIndex -= 1;
			else
				currentQuestionIndex = displayedQuestions.size() - 1;
		}
		else if(cmd == DefaultCommands.cmdPrev){
			if(currentQuestionIndex > 1)
				currentQuestionIndex -= 2;
			else{
				currentQuestionIndex -= 1; // displayedQuestions.size() - 1;	
				cmd = DefaultCommands.cmdFirst;
			}
		}
		else if(cmd == DefaultCommands.cmdFirst)
			currentQuestionIndex = 0;
		else if(cmd == DefaultCommands.cmdLast)
			currentQuestionIndex = displayedQuestions.size() - 1;

		if((currentQuestionIndex == 0 && (cmd == DefaultCommands.cmdFirst))||
				(currentQuestionIndex == displayedQuestions.size() - 1 && (cmd == DefaultCommands.cmdBackParent))){
			currentQuestionIndex = 0;
			currentQuestion = null;
		}
		else
			currentQuestion  = (QuestionData)displayedQuestions.elementAt(currentQuestionIndex);
		
		showPage(this.currentPageIndex,new Integer(currentQuestionIndex));

		if(cmd != DefaultCommands.cmdBackParent && GeneralSettings.isSingleQtnEdit())
		{
			//if we are on the last question.
			if(currentQuestionIndex == displayedQuestions.size()){
				//if no on the last page
				if(currentPageIndex < formData.getPages().size()){
					currentPageIndex++;
					if(currentPageIndex == formData.getPages().size()){
						currentPageIndex = 0;
						currentQuestionIndex = 0;
					}
					showPage(currentPageIndex,new Integer(0));
				}
			}

			handleListSelectCommand(screen);
		}
	}

	/**
	 * Gets the index of the next question for editing.
	 * Gets the next visible and enabled question, else stays at the current question.
	 * 
	 * @param answered - set to true if you want to skip questions with answers already.
	 * 
	 * @return - the next question display index.
	 */
	private int getNextQuestionIndex(boolean notAnswered){
		int index = currentQuestionIndex;//+1;

		while(index < displayedQuestions.size()){
			QuestionData qtn = (QuestionData)displayedQuestions.elementAt(index);
			QuestionDef def = qtn.getDef();
			if (def.isVisible() && def.isEnabled()) {
				if(notAnswered){
					if (!qtn.isAnswered()){
						currentQuestionIndex = index;
						break;
					}
				}
				else {
					if (currentQuestion == null) {
						currentQuestion = qtn;
						currentQuestionIndex = index;
						break;
					} else if(currentQuestion.getId() == qtn.getId()) {
						do {
							currentQuestionIndex = ++index;
							if (currentQuestionIndex < displayedQuestions.size()) {
								currentQuestion = (QuestionData)displayedQuestions.elementAt(currentQuestionIndex);
							} else {
								break; // if we are at the end, stop looking
							}
						} while (!currentQuestion.getDef().isEnabled()); 
						break;
					}
				}
			}
			index++;
		}
		return currentQuestionIndex;
	}

	public void showForm(FormData data,FormListener listener, boolean allowDelete){
		try{
			this.formData = new FormData(data);
			this.listener  = listener;

			currentPageIndex = 0;
			currentQuestionIndex = 0;	
			currentQuestion = null;
			dirty = false;

			//create here such that the show page can use.
			screen = new List(this.formData.getDef().getName() + " - " + title, Choice.IMPLICIT);
			((List)screen).setFitPolicy(List.TEXT_WRAP_ON);
			
			alertMsg = new AlertMessage(display,title,screen,this);

			showPage(currentPageIndex,new Integer(currentQuestionIndex));

			//commands are added here because the show page can remove all of them for ease
			//of current implementation.
			if(displayedQuestions.size() > 0){
				screen.setCommandListener(this);
				screen.addCommand(DefaultCommands.cmdSave);
				if(allowDelete)
					screen.addCommand(DefaultCommands.cmdDelete);
				screen.addCommand(DefaultCommands.cmdCancel);

				display.setCurrent(screen);
			}
		}
		catch(Exception e){
			String s = MenuText.FORM_DISPLAY_PROBLEM();
			if(e.getMessage() != null && e.getMessage().trim().length() > 0)
				s = e.getMessage();
			currentAction = CA_ERROR;
			alertMsg.showError(s);
			
			e.printStackTrace();
		}
	}

	/**
	 * Shows a particular page.
	 * 
	 * @param pageIndex - the index of the page.
	 * @param currentQuestionIndex - the index of the question to preselect.
	 */
	private void showPage(int pageIndex,Integer currentQuestionIndex){
		currentPageIndex = pageIndex;
		((List)screen).deleteAll();

		Vector pages = formData.getPages();
		if (pageIndex >= 0 && pageIndex < pages.size()) {
			currentPage = ((PageData)pages.elementAt(pageIndex));
			displayedQuestions = new Vector();
			
			boolean useQtnNumbering = GeneralSettings.isQtnNumbering();
			int qtnNumberCount = (useQtnNumbering ? previousQuestionCount(pages, pageIndex) : 0);
			
			Vector qns = currentPage.getQuestions();
			QuestionData qn; 
			for (int index = 0; index < qns.size(); index++) {
				qn = (QuestionData)qns.elementAt(index);
				boolean qtnRequired = qn.getDef().isMandatory() && !qn.isAnswered();
				boolean qtnDisabled = !qn.getDef().isEnabled(); 
				if (qn.getDef().isVisible()) {
					String questionText = 
						(useQtnNumbering ? String.valueOf(qtnNumberCount+index+1) + " " : "") 
						+ (qtnRequired ? "* " : "")
						+ (qtnDisabled ? "- " : "")
						+ (!qtnRequired && !qtnDisabled ? "  " : "")
						+ (qtnDisabled ? "[" : "")
						+ qn.toString()
					    + (qtnDisabled ? "]" : "");
					
					((List)screen).append(questionText, null);
					displayedQuestions.addElement(qn);
				}
			}
	
			if(pageIndex < pages.size()-1)
				screen.addCommand(cmdNext);
			else
				screen.removeCommand(cmdNext);
	
			if(pageIndex > 0)
				screen.addCommand(cmdPrev);
			else
				screen.removeCommand(cmdPrev);
	
			if (displayedQuestions.size() > 0) {	
				selectNextQuestion(currentQuestionIndex);
				screen.setTitle((formData.getDef().getPageCount() > 1 ? currentPage.getDef().getName()+ " - " : "") + formData.getDef().getName() + " - " + title);
			}
		} else {
			alertMsg.showError(MenuText.FORM_DISPLAY_PROBLEM());
		}
	}
	
	private int previousQuestionCount(Vector pages, int pageIndex) {
		int qtnNumberCount = 0;
		for (int i=0; i<pageIndex; i++) {
			PageData pd = (PageData)pages.elementAt(i);
			qtnNumberCount+=pd.getNumberOfQuestions();
		}
		return qtnNumberCount;
	}

	
	/**
	 * Selects the next question to edit.
	 * 
	 * @param currentQuestionIndex - index of the current question to edit
	 */
	private void selectNextQuestion(Integer currentQuestionIndex){
		if(currentQuestionIndex == null)
			((List)screen).setSelectedIndex(0, true);
		else{
			currentQuestionIndex = new Integer(getNextQuestionIndex(false));
			if(currentQuestionIndex != null && currentQuestionIndex.intValue() < ((List)screen).size())
				((List)screen).setSelectedIndex(currentQuestionIndex.intValue(), true);
			else if(currentQuestionIndex != null  && currentQuestionIndex.intValue() == ((List)screen).size()){
				//TODO Restructure this with the above. Added temporarily to prevent jumping to the
				//first question from the last one.
				currentQuestionIndex = new Integer(((List)screen).size() - 1);
				((List)screen).setSelectedIndex(currentQuestionIndex.intValue(), true);
			}
		}
	}

	/** Moves to the next page. */
	private void nextPage(){
		showPage(++this.currentPageIndex,null);
	}

	/** Moves to the previous page. */
	private void prevPage(){
		showPage(--this.currentPageIndex,null);
	}

	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		try{
			if(c == List.SELECT_COMMAND)
				handleListSelectCommand(d);
			else if(c == DefaultCommands.cmdSave)
				handleSaveCommand(d);
			/*else if(c == DefaultCommands.cmdOk)
				handleOkCommand(d);*/
			else if(c == DefaultCommands.cmdCancel)
				handleCancelCommand(d);
			else if(c == cmdNext)
				nextPage();
			else if(c == cmdPrev)
				prevPage();
			else if(c == DefaultCommands.cmdDelete)
				handleDeleteCommand(d);
			else if(c == DefaultCommands.cmdMainMenu)
				getOpenXdataController().backToMainMenu();			
		}
		catch(Exception e){
			alertMsg.showError(e.getMessage());
			//e.printStackTrace();
		}
	}

	/**
	 * Processes the delete command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleDeleteCommand(Displayable d){
		currentAction = CA_CONFIRM_DELETE;
		alertMsg.showConfirm(MenuText.DATA_DELETE_PROMPT());
	}

	/**
	 * Processes the cancel command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleCancelCommand(Displayable d){
		currentAction = CA_CONFIRM_CANCEL;

		if (dirty) {
			alertMsg.showConfirm(MenuText.FORM_CLOSE_PROMPT());
		} else {
			onAlertMessage(AlertMessageListener.MSG_OK);
		}
	}

	/**
	 * Processes the Save command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleSaveCommand(Displayable d){
		//Check if user entered data correctly.
		if(!formData.isRequiredAnswered()){
			alertMsg.show(MenuText.REQUIRED_PROMPT());
			selectMissingValueQtn();
		}
		else if(!formData.isFormAnswered())
			alertMsg.show(MenuText.ANSWER_MINIMUM_PROMPT());
		else{
			String errMsg = selectInvalidQtn();			
			if(errMsg != null){
				alertMsg.show(errMsg);
				return;
			}

			boolean save = true;
			if(listener != null)
				save = listener.beforeFormSaved(formData,formData.isNew()); //Give the API user a chance to do some custom validations.

			if(save){
				getOpenXdataController().saveForm(formData);

				if(listener != null)
					listener.afterFormSaved(formData,formData.isNew());
			}
		}
	}

	private void handleUploadData(Displayable d){

		//Check if user entered data correctly.
		if(!formData.isRequiredAnswered()){
			alertMsg.show(MenuText.REQUIRED_PROMPT());
			selectMissingValueQtn();
		}else if(!formData.isFormAnswered()){
			alertMsg.show(MenuText.ANSWER_MINIMUM_PROMPT());
			return;		
		}else{
			String errMsg = selectInvalidQtn();			
			if(errMsg != null){
				alertMsg.show(errMsg);
				return;				
			}
			getOpenXdataController().saveForm(formData);
		}

		//Select the current form definition
		int currentFormId = formData.getDef().getId();//current form
		Vector studyList = new Vector();
		FormDef fd = getOpenXdataController().getCurrentStudy().getForm(currentFormId);
		Vector forms = new Vector();
		forms.addElement(fd);
		StudyDef sd = new StudyDef(); //StudyDef sd = getOpenXdataController().getCurrentStudy();
		sd.setId(getOpenXdataController().getCurrentStudy().getId());
		sd.setForms(forms);		
		studyList.addElement(sd);
		
		//Clear the screen
		getOpenXdataController().uploadData(this.getScreen(), studyList);
		
		//Re populate the screen with all the formdef's for the current studydef
		((List)screen).deleteAll();
		
		//Repopulate the screen with all the formdef's for the current studydef
		//When done this seems to make the upload generate duplicates
		/*Vector formList = getOpenXdataController().getCurrentStudy().getForms();
		for (byte i=0; i<formList.size(); i++) {
			forms.addElement(formList.elementAt(i));
			((List)screen).append(((FormDef)formList.elementAt(i)).getName(), null);
		}*/
		
		//Set the appropriate command menu options
		screen.setTitle("Form data uploaded");
		screen.removeCommand(DefaultCommands.cmdSave);
		screen.removeCommand(DefaultCommands.cmdUploadData);
		screen.removeCommand(DefaultCommands.cmdDelete);
		screen.addCommand(DefaultCommands.cmdExit);
		screen.addCommand(DefaultCommands.cmdMainMenu);
		display.setCurrent(screen);
		//getOpenXdataController().showFormDataList(formData.getDef());

	}
	
	private boolean selectMissingValueQtn(byte pageNo){	

		if(pageNo != currentPageIndex)
			showPage(pageNo,new Integer(0));

		for(int i=0; i<displayedQuestions.size(); i++){
			QuestionData qtn = (QuestionData)displayedQuestions.elementAt(i);
			QuestionDef def = qtn.getDef();
			if(def.isMandatory() && !qtn.isAnswered()){
				((List)screen).setSelectedIndex(i, true);
				return true;
			}
		}

		return false;
	}

	private String selectInvalidQtn(byte pageNo){	

		if(pageNo != currentPageIndex)
			showPage(pageNo,new Integer(0));

		for(int i=0; i<displayedQuestions.size(); i++){
			QuestionData qtn = (QuestionData)displayedQuestions.elementAt(i);

			ValidationRule rule = formData.getDef().getValidationRule(qtn.getId());
			if(rule == null)
				continue;

			rule.setFormData(formData);

			if(!rule.isValid()){
				((List)screen).setSelectedIndex(i, true);
				return rule.getErrorMessage();
			}
		}

		return null;
	}

	private void selectMissingValueQtn(){
		Vector pages = formData.getPages();
		for(byte i = 0; i < pages.size(); i++){
			if(selectMissingValueQtn(i))
				break;
		}
	}

	private String selectInvalidQtn(){
		Vector pages = formData.getPages();
		for(byte i = 0; i < pages.size(); i++){
			String errorMsg = selectInvalidQtn(i);
			if(errorMsg != null)
				return errorMsg;
		}
		return null;
	}

	/**
	 * Processes the list selection command event. This is the command that the user
	 * invokes to start editing of a question.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	public void handleListSelectCommand(Displayable d){
		//handleOkCommand(d);
		//save the user state for more friendliness
		currentQuestionIndex = ((List)d).getSelectedIndex();
		currentQuestion = (QuestionData)displayedQuestions.elementAt(currentQuestionIndex);
		if(currentQuestion.getDef().isEnabled()){
			boolean edit = true;
			if(listener != null)
				edit = listener.beforeQuestionEdit(currentQuestion); //give the API user a chance to override this editing.

			if(edit)
				getOpenXdataController().startEdit(currentQuestion,(currentQuestionIndex+1),displayedQuestions.size());
		}
	}

	private OpenXdataController getOpenXdataController(){
		return (OpenXdataController)controller;
	}

	/**
	 * If in cancel mode, user is sure wants to cancel saving changed (discard form data)
	 */
	public void onAlertMessage(byte msg){
		if(msg == AlertMessageListener.MSG_OK){
			if(currentAction == CA_CONFIRM_CANCEL || currentAction == CA_ERROR) {
				getOpenXdataController().handleCancelCommand(this);
			} else if(currentAction == CA_CONFIRM_DELETE) {
				getOpenXdataController().deleteForm(formData,this);
				getOpenXdataController().handleCancelCommand(this);
			} else {
				display.setCurrent(screen);
			}
		}
		else
			show();

		currentAction = CA_NONE;
	}

	public FormData getFormData(){
		return formData;
	}
}
