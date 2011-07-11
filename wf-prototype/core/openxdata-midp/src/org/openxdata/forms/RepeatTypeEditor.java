package org.openxdata.forms;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;

import org.openxdata.model.QuestionData;
import org.openxdata.model.RepeatQtnsData;
import org.openxdata.model.RepeatQtnsDataList;
import org.openxdata.model.RepeatQtnsDef;
import org.openxdata.model.ValidationRule;
import org.openxdata.mvc.AbstractView;
import org.openxdata.mvc.CommandAction;
import org.openxdata.mvc.Controller;
import org.openxdata.mvc.View;
import org.openxdata.util.DefaultCommands;


/**
 * This serves as the controller for repeat questions.
 * 
 * @author daniel
 *
 */
public class RepeatTypeEditor extends AbstractView implements TypeEditor, TypeEditorListener , Controller{
	
	private QuestionData questionData;
	private RepeatQtnsDataList rptQtnsDataList;
	private RepeatQtnsDef rptQtnsDef;
	private RepeatQtnsData rptQtnsData; //the current one.
	private int pos; //question position pn the form.
	private int count; //total number of questions on the form.
	
	private RptQtnsDataListView dataListView = new RptQtnsDataListView();
	private RptQtnsDataView dataView = new RptQtnsDataView();
	
	private TypeEditor typeEditor = new DefaultTypeEditor();
	private ValidationRule validationRule;
	
	
	//public RepeatTypeEditor(){
		
	//}

	public void startEdit(QuestionData data, ValidationRule validationRule, boolean singleQtnEdit,int pos, int count, TypeEditorListener listener){
		questionData = data;
		this.validationRule = validationRule;
		this.pos = pos;
		this.count = count;
		
		rptQtnsDef = questionData.getDef().getRepeatQtnsDef();
		
		if(questionData.getAnswer() != null)
			rptQtnsDataList = new RepeatQtnsDataList((RepeatQtnsDataList)questionData.getAnswer());
		else{
			rptQtnsDataList = new RepeatQtnsDataList();
			questionData.setAnswer(rptQtnsDataList);
		}

		showQtnsData(validationRule);
		
		typeEditor.setController(this);
	}
	
	private void showQtnsData(ValidationRule validationRule){
		dataListView.showQtnDataList(rptQtnsDef,rptQtnsDataList,this,validationRule);
	}
	
	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		try{
			if(c == DefaultCommands.cmdOk || c == DefaultCommands.cmdNext)
				getOpenXdataController().endEdit(true, questionData, c);
			else if(c == DefaultCommands.cmdCancel || c == DefaultCommands.cmdPrev)
				getOpenXdataController().endEdit(false, questionData, c);
			else
				getOpenXdataController().endEdit(false, questionData, c);
		}
		catch(Exception e){
			//alertMsg.showError(e.getMessage());
			//e.printStackTrace();
		}
	}
	
	/**
	 * Processes the new command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	/*public void handleNewCommand(RepeatQtnsData rptQtnsData){
		dataView.showQtnData(rptQtnsData, this);
	}*/
	
	private OpenXdataController getOpenXdataController(){
		return (OpenXdataController)controller;
	}
	
	public void endEdit(boolean save, QuestionData data, Command cmd){
		rptQtnsData.setQuestionDataById(data);
		dataView.showQtnData(rptQtnsData, this);
	}
	
	public void execute(View view, Object commandAction, Object data){
		
		if(view == dataListView){
			if(commandAction == CommandAction.NEW){
				rptQtnsData = new RepeatQtnsData((byte)(rptQtnsDataList.size()+1),rptQtnsDef);
				dataView.showQtnData(rptQtnsData, this);
			}
			else if(commandAction == CommandAction.EDIT){
				rptQtnsData = new RepeatQtnsData((RepeatQtnsData)data);
				dataView.showQtnData(rptQtnsData, this);
			}
			else if(commandAction == CommandAction.OK){
				questionData.setAnswer(rptQtnsDataList);
				getOpenXdataController().endEdit(false, questionData, null);
			}
			else if(commandAction == CommandAction.CANCEL)
				getOpenXdataController().endEdit(false, null, null);
		}
		else if(view == dataView){
			if(commandAction == CommandAction.EDIT)
				typeEditor.startEdit(new QuestionData((QuestionData)data),null, false,pos,count,this);
			else if(commandAction == CommandAction.OK)
				rptQtnsDataList.setRepeatQtnsDataById((RepeatQtnsData)data);
			
			if(commandAction == CommandAction.OK || commandAction == CommandAction.CANCEL)
				showQtnsData(validationRule);
		}
	}
}
