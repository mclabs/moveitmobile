package org.openxdata.forms;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.openxdata.model.OpenXdataConstants;
import org.openxdata.model.QuestionData;
import org.openxdata.model.RepeatQtnsData;
import org.openxdata.mvc.AbstractView;
import org.openxdata.mvc.CommandAction;
import org.openxdata.mvc.Controller;
import org.openxdata.util.AlertMessageListener;
import org.openxdata.util.DefaultCommands;


/**
 * Displays one row of a repeat question. As in all the questions in one row.
 * 
 * @author daniel
 *
 */
public class RptQtnsDataView extends AbstractView implements AlertMessageListener{

	//private AlertMessage alertMsg;
	private Controller controller;
	private RepeatQtnsData rptQtnsData;
	
	//for managing state
	private int currentQuestionIndex = OpenXdataConstants.NO_SELECTION;
	private QuestionData currentQuestion = null;
	
	public RptQtnsDataView(){
		
	}
	
	/**
	 * Shows a list of questions in a row of a repeating set.
	 * 
	 * @param rptQtnsData
	 * @param controller
	 */
	public void showQtnData(RepeatQtnsData rptQtnsData,Controller controller){
		try{
			if(this.rptQtnsData != rptQtnsData)
				currentQuestionIndex = 0;
			else if(currentQuestionIndex < rptQtnsData.size() - 1){
				++currentQuestionIndex;
				
				if(currentQuestionIndex < rptQtnsData.size() - 1)
					++currentQuestionIndex;
			}
			
			this.rptQtnsData = rptQtnsData;
			this.controller = controller;
		
			if(rptQtnsData.getDef().getQuestions() == null)
				return;

			screen = new List(rptQtnsData.getDef().getText(), Choice.IMPLICIT );
			((List)screen).setFitPolicy(List.TEXT_WRAP_ON);
			//alertMsg = new AlertMessage(display, title, screen, this);
				
			QuestionData data;
			for(int i=0; i<rptQtnsData.size(); i++){
				data = rptQtnsData.getQuestion(i);
				((List)screen).append(data.toString(), null);
			}

			screen.setCommandListener(this);
			screen.addCommand(DefaultCommands.cmdCancel);
			if(rptQtnsData.isAnswered())
				screen.addCommand(DefaultCommands.cmdOk);

			((List)screen).setSelectedIndex(currentQuestionIndex,true); //should have atleast one question
			
			display.setCurrent(screen);
		}
		catch(Exception e){
			//e.printStackTrace();
		}
	}
	
	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		try{
			if(c == DefaultCommands.cmdSelect)
				handleListSelectCommand(c,d);
			else if(c == DefaultCommands.cmdOk)
				controller.execute(this, CommandAction.OK, rptQtnsData);
				//controller.onViewClosed(this, rptQtnsData, false);
			else if(c == DefaultCommands.cmdCancel)
				controller.execute(this, CommandAction.CANCEL, null);
				//controller.onViewClosed(this, null, true);
		}
		catch(Exception e){
			//alertMsg.showError(e.getMessage());
			//e.printStackTrace();
		}
	}
	
	/**
	 * Processes the list selection command event. This is the command that the user
	 * invokes to start editing of a question.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	public void handleListSelectCommand(Command c,Displayable d){
		//save the user state for more friendliness
		currentQuestionIndex = ((List)d).getSelectedIndex();
		currentQuestion = rptQtnsData.getQuestion(currentQuestionIndex);
		
		//Tell the controller that we wanna edit this question.
		if(currentQuestion.getDef().isEnabled()){
			--currentQuestionIndex; //TODO This is just a temporary fix for some wiered behaviour
			controller.execute(this, CommandAction.EDIT, currentQuestion);
		}
	}
	
	/**
	 * If in cancel mode, user is sure wants to cancel saving changed (discard form data)
	 */
	public void onAlertMessage(byte msg){		
		display.setCurrent(screen);
	}
}
