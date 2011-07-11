package org.openxdata.forms;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.openxdata.model.OpenXdataConstants;
import org.openxdata.model.RepeatQtnsData;
import org.openxdata.model.RepeatQtnsDataList;
import org.openxdata.model.RepeatQtnsDef;
import org.openxdata.model.ValidationRule;
import org.openxdata.mvc.AbstractView;
import org.openxdata.mvc.CommandAction;
import org.openxdata.util.AlertMessage;
import org.openxdata.util.AlertMessageListener;
import org.openxdata.util.DefaultCommands;


/**
 * Displays rows of data collected for repeat questions.
 * 
 * @author daniel
 *
 */
public class RptQtnsDataListView  extends AbstractView implements AlertMessageListener {

	//private RepeatQtnsDef rptQtnsDef;
	private RepeatQtnsDataList rptQtnsDataList;
	//private boolean confirmCancelMode = false;
	private AlertMessage alertMsg;
	private boolean deleting = false;

	private RepeatTypeEditor controller;

//	for managing state
	private int currentQuestionIndex = OpenXdataConstants.NO_SELECTION;


	public void showQtnDataList(RepeatQtnsDef rptQtnsDef,RepeatQtnsDataList rptQtnsDataLst,RepeatTypeEditor controller, ValidationRule validationRule){
		try{
			if(this.rptQtnsDataList != rptQtnsDataLst)
				currentQuestionIndex = 0;
			/*else if(rptQtnsDataList != null && rptQtnsDataLst.size() > rptQtnsDataList.size())
				currentQuestionIndex = rptQtnsDataList.size();

			}*/

			//this.rptQtnsDef = rptQtnsDef;
			this.rptQtnsDataList = rptQtnsDataLst;
			this.controller = controller;

			screen = new List(rptQtnsDef.getText()+ " - Data List - " + title, Choice.IMPLICIT );
			((List)screen).setFitPolicy(List.TEXT_WRAP_ON);
			
			alertMsg = new AlertMessage(display, title, screen, this);

			//rptQtnsDataList should never be null.
			for(int i=0; i<rptQtnsDataList.size(); i++){				
				RepeatQtnsData data = rptQtnsDataList.getRepeatQtnsData(i);
				//data.setDef(rptQtnsDef);

				//data.buildDataDescription();
				((List)screen).append(data.toString(), null);
			}

			screen.setCommandListener(this);
			screen.addCommand(DefaultCommands.cmdCancel);
			screen.addCommand(DefaultCommands.cmdNew);

			if(rptQtnsDataList.size() > 0){
				screen.addCommand(DefaultCommands.cmdOk); //No saving if we have no items.
				screen.addCommand(DefaultCommands.cmdDelete);
			}

			if(rptQtnsDataList.size() > 0) //may not have data yet.
				((List)screen).setSelectedIndex(currentQuestionIndex,true);

			display.setCurrent(screen);
		}
		catch(Exception e){
			alertMsg.show(e.getMessage());
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
			if(c == List.SELECT_COMMAND)
				handleSelectCommand(d);
			else if(c == DefaultCommands.cmdOk)
				controller.execute(this, CommandAction.OK, rptQtnsDataList);
			else if(c == DefaultCommands.cmdCancel)
				controller.execute(this, CommandAction.CANCEL, null);
			else if(c == DefaultCommands.cmdNew)
				controller.execute(this, CommandAction.NEW, null);
			else if(c == DefaultCommands.cmdDelete)
				handleDeleteCommand(d);
		}
		catch(Exception e){
			alertMsg.showError(e.getMessage());
			//e.printStackTrace();
		}
	}

	/**
	 * Processes the cancel command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	/*private void handleCancelCommand(Displayable d){
		//controller.onViewClosed(this, null, false);
		controller.execute(this, CommandAction.CANCEL, null);
	}

	/**
	 * Processes the new command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	/*private void handleNewCommand(Displayable d){
		//controller.handleNewCommand(new RepeatQtnsData((byte)(rptQtnsDataList.size()+1),rptQtnsDef));
		controller.execute(this, CommandAction.NEW, null);
	}

	/**
	 * Processes the OK command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	/*private void handleOkCommand(Displayable d){
		//controller.onViewClosed(this, rptQtnsDataList, true);
		controller.execute(this, CommandAction.OK, rptQtnsDataList);
		//getEpihandyController().showRepeatQtnsRow(rptQtnsDataList.getRepeatQtnsData(((List)d).getSelectedIndex()));
	}

	/**
	 * Processes the list select command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleSelectCommand(Displayable d){
		//controller.onViewClosed(this, rptQtnsDataList, true);
		currentQuestionIndex = ((List)screen).getSelectedIndex();
		controller.execute(this, CommandAction.EDIT, rptQtnsDataList.getRepeatQtnsData(currentQuestionIndex));
		//getEpihandyController().showRepeatQtnsRow(rptQtnsDataList.getRepeatQtnsData(((List)d).getSelectedIndex()));
	}

	/**
	 * Processes the delete command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleDeleteCommand(Displayable d){
		RepeatQtnsData rptQtnsData = this.rptQtnsDataList.getRepeatQtnsData(((List)screen).getSelectedIndex());
		alertMsg.showConfirm("Do u really wanna delete record: " + rptQtnsData.toString());
		deleting = true;
	}

	/**
	 * If in cancel mode, user is sure wants to cancel saving changed (discard form data)
	 */
	public void onAlertMessage(byte msg){
		if(msg == AlertMessageListener.MSG_OK){
			if(deleting){
				deleting = false;
				int index  = ((List)screen).getSelectedIndex();
				rptQtnsDataList.removeRepeatQtnsData(index);
				((List)screen).delete(index);
			}
		}
		
		show();
	}
}
