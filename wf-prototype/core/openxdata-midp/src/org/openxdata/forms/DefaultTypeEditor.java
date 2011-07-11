package org.openxdata.forms;

import java.util.Date;
import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;

import org.openxdata.model.OpenXdataConstants;
import org.openxdata.model.OptionData;
import org.openxdata.model.OptionDef;
import org.openxdata.model.QuestionData;
import org.openxdata.model.QuestionDef;
import org.openxdata.model.ValidationRule;
import org.openxdata.mvc.AbstractView;
import org.openxdata.util.AlertMessage;
import org.openxdata.util.AlertMessageListener;
import org.openxdata.util.DefaultCommands;


/**
 * 
 * @author daniel
 *
 */
public class DefaultTypeEditor extends AbstractView implements TypeEditor,AlertMessageListener,ItemStateListener/*,ItemCommandListener*/ {

	private QuestionData currentQuestion;
	private Item currentCtrl;
	private TypeEditorListener listener;
	private AlertMessage alertMsg;
	private ValidationRule validationRule;

	private static int BOOLEAN_NO_SELECTION_INDEX = 2;

	public void startEdit(QuestionData data, ValidationRule validationRule, boolean singleQtnEdit,int pos,int count,TypeEditorListener listener){
		currentQuestion = data;
		this.validationRule = validationRule;
		this.listener = listener;

		if(title != null)
			screen = new Form("{"+pos+"/"+count+"} "+title);
		else
			screen = new Form("{"+pos+"/"+count+"}");

		alertMsg = new AlertMessage(display,title,screen,this);

		String qtnText = currentQuestion.getText();
		currentCtrl = new TextField(qtnText,"",100,TextField.ANY);
		int index = 0;

		try{
			//Create the appropriate use control basing on data type.
			byte type = currentQuestion.getDef().getType();
			switch(type){
			case QuestionDef.QTN_TYPE_TEXT:
				currentCtrl = new TextField(qtnText,currentQuestion.getTextAnswer(),OpenXdataConstants.MAX_NUM_CHARS,TextField.ANY);
				break;
			case QuestionDef.QTN_TYPE_NUMERIC:
				currentCtrl = new TextField(qtnText,currentQuestion.getTextAnswer(),OpenXdataConstants.MAX_NUM_CHARS,TextField.NUMERIC);
				break;
			case QuestionDef.QTN_TYPE_DECIMAL:
				currentCtrl = new TextField(qtnText,currentQuestion.getTextAnswer(),OpenXdataConstants.MAX_NUM_CHARS,TextField.DECIMAL);
				break;
			case QuestionDef.QTN_TYPE_DATE:
				currentCtrl = new DateField(qtnText,DateField.DATE,OpenXdataConstants.DEFAULT_TIME_ZONE);
				((DateField)currentCtrl).setDate((Date)currentQuestion.getAnswer());
				break;
			case QuestionDef.QTN_TYPE_TIME:
				currentCtrl = new DateField(qtnText,DateField.TIME,OpenXdataConstants.DEFAULT_TIME_ZONE);
				((DateField)currentCtrl).setDate((Date)currentQuestion.getAnswer());
				break;
			case QuestionDef.QTN_TYPE_DATE_TIME:
				currentCtrl = new DateField(qtnText,DateField.DATE_TIME,OpenXdataConstants.DEFAULT_TIME_ZONE);
				((DateField)currentCtrl).setDate((Date)currentQuestion.getAnswer());
				break;
			case QuestionDef.QTN_TYPE_BOOLEAN:
				currentCtrl = new ChoiceGroup(qtnText,Choice.EXCLUSIVE);
				((ChoiceGroup)currentCtrl).append(QuestionData.TRUE_DISPLAY_VALUE, null);
				((ChoiceGroup)currentCtrl).append(QuestionData.FALSE_DISPLAY_VALUE, null);
				((ChoiceGroup)currentCtrl).append(QuestionData.NO_SELECTION_VALUE, null);
				((ChoiceGroup)currentCtrl).setFitPolicy(ChoiceGroup.TEXT_WRAP_ON);
				
				if(currentQuestion.getAnswer() != null)
					index = ((Boolean)currentQuestion.getAnswer()).booleanValue() ? 0 : 1;
				else
					index = BOOLEAN_NO_SELECTION_INDEX;

				((ChoiceGroup)currentCtrl).setSelectedIndex(index, true);
				break;
			case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
			case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
				currentCtrl = new ChoiceGroup(qtnText,Choice.EXCLUSIVE);
				((ChoiceGroup)currentCtrl).setFitPolicy(ChoiceGroup.TEXT_WRAP_ON);
				
				Vector options = currentQuestion.getDef().getOptions();
				if(options == null)
					options = new Vector();
				if(options != null){
					for(int i=0; i<options.size(); i++)
						((ChoiceGroup)currentCtrl).append(options.elementAt(i).toString(), null);

					((ChoiceGroup)currentCtrl).append(QuestionData.NO_SELECTION_VALUE, null);

					if(currentQuestion.getAnswer() != null)
						index = Integer.parseInt(currentQuestion.getOptionAnswerIndices().toString());
					else if(options.size() > 0)
						index = options.size();

					((ChoiceGroup)currentCtrl).setSelectedIndex(index, true);
				}
				break;
			case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
				currentCtrl = new ChoiceGroup(qtnText,Choice.MULTIPLE);
				((ChoiceGroup)currentCtrl).setFitPolicy(ChoiceGroup.TEXT_WRAP_ON);
				
				options = currentQuestion.getDef().getOptions();
				if(options != null){
					for(int i=0; i<options.size(); i++)
						((ChoiceGroup)currentCtrl).append(options.elementAt(i).toString(), null);

					if(currentQuestion.getOptionAnswerIndices() != null){
						for(int i=0; i<((Vector)currentQuestion.getOptionAnswerIndices()).size(); i++)
							((ChoiceGroup)currentCtrl).setSelectedIndex(((Byte)((Vector)currentQuestion.getOptionAnswerIndices()).elementAt(i)).byteValue(), true);
					}
				}
				break;
			}
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}

		//Set the ticker as help or hint text.
		String helpText = currentQuestion.getDef().getHelpText();
		if(helpText != null && helpText.length() > 0){
			Ticker t = new Ticker(helpText);
			screen.setTicker(t);
		}	
		
		((Form)screen).append(currentCtrl);

		if(singleQtnEdit){
			/*screen.addCommand(DefaultCommands.cmdNext);
			screen.addCommand(DefaultCommands.cmdPrev);
			screen.addCommand(DefaultCommands.cmdBackParent);*/

			boolean isFirst = (pos == 1);
			boolean isLast = (pos == count);

			if(!isLast)
				screen.addCommand(DefaultCommands.cmdNext);

			if(!isFirst){
				screen.addCommand(DefaultCommands.cmdPrev);
				screen.addCommand(DefaultCommands.cmdFirst);
			}

			if(!isLast)
				screen.addCommand(DefaultCommands.cmdLast);

			screen.addCommand(DefaultCommands.cmdBackParent);

		}
		else{
			screen.addCommand(DefaultCommands.cmdOk);
			screen.addCommand(DefaultCommands.cmdCancel);
		}

		//screen.addCommand(DefaultCommands.cmdBackParent);

		((Form)screen).setItemStateListener(this);
		//currentCtrl.setDefaultCommand(DefaultCommands.cmdOk);
		//currentCtrl.setItemCommandListener(this);
		//((Form)screen).a

		screen.setCommandListener(this);
		AbstractView.display.setCurrent(screen);

	}

	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		try{
			if(c == DefaultCommands.cmdOk || c == DefaultCommands.cmdNext ||
					c == DefaultCommands.cmdPrev || c == DefaultCommands.cmdFirst || 
					c == DefaultCommands.cmdLast || c == DefaultCommands.cmdBackParent){

				Object answer = currentQuestion.getAnswer();
				Object optionAnswerIndices = currentQuestion.getOptionAnswerIndices();
				updateQuestionData();
				if(/*currentQuestion.isAnswered() &&*/ validationRule != null && !validationRule.isValid()){

					if(!((c == DefaultCommands.cmdPrev || c == DefaultCommands.cmdFirst || c == DefaultCommands.cmdBackParent) &&
							(currentQuestion.getAnswer() == null || currentQuestion.getAnswer().toString().trim().length() == 0))){

						currentQuestion.setAnswer(answer);
						currentQuestion.setOptionAnswerIndices(optionAnswerIndices);
						alertMsg.show(validationRule.getErrorMessage());
						return;
					}
				}
			}

			//if(listener != null) //Should never be null
			listener.endEdit((c == DefaultCommands.cmdOk || c == DefaultCommands.cmdNext), currentQuestion, c);
		}
		catch(Exception e){
			alertMsg.showError(e.getMessage());
			e.printStackTrace();
		}
	}

	private void updateQuestionData(){

		int index = 0;

		switch(currentQuestion.getDef().getType()){
		case QuestionDef.QTN_TYPE_TEXT:
		case QuestionDef.QTN_TYPE_NUMERIC:
		case QuestionDef.QTN_TYPE_DECIMAL:
			currentQuestion.setAnswer(((TextField)currentCtrl).getString());
			break;
		case QuestionDef.QTN_TYPE_DATE:
		case QuestionDef.QTN_TYPE_TIME:
		case QuestionDef.QTN_TYPE_DATE_TIME:
			currentQuestion.setAnswer(((DateField)currentCtrl).getDate());
			break;
		case QuestionDef.QTN_TYPE_BOOLEAN:
			ChoiceGroup ctrl = (ChoiceGroup)currentCtrl;
			index = ctrl.getSelectedIndex();
			if(index == BOOLEAN_NO_SELECTION_INDEX){
				currentQuestion.setAnswer(null);
				currentQuestion.setOptionAnswerIndices(null);
			}
			else{
				currentQuestion.setAnswer(ctrl.getSelectedIndex() == 0 ? new Boolean(true) : new Boolean(false));
				currentQuestion.setOptionAnswerIndices(toByte(index));
			}
			break;
		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
			ctrl = (ChoiceGroup)currentCtrl;
			index = ctrl.getSelectedIndex();

			Vector options = currentQuestion.getDef().getOptions();
			if(options != null){
				if(index == options.size()){
					currentQuestion.setAnswer(null);
					currentQuestion.setOptionAnswerIndices(null);
				}
				else{
					currentQuestion.setAnswer(new OptionData((OptionDef)options.elementAt(index)));
					currentQuestion.setOptionAnswerIndices(toByte(index));
				}
			}
			break;
		case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
			ctrl = (ChoiceGroup)currentCtrl;
			Vector optionAnswers  = new Vector();
			Vector optionAnswerIndices  = new Vector();
			options = currentQuestion.getDef().getOptions();
			if(options != null){
				for(int i=0; i<options.size(); i++){
					if(ctrl.isSelected(i)){
						optionAnswers.addElement(new OptionData((OptionDef)options.elementAt(i)));
						optionAnswerIndices.addElement(toByte(i));
					}
				}

				currentQuestion.setAnswer(optionAnswers);
				currentQuestion.setOptionAnswerIndices(optionAnswerIndices);
			}
			break;
		}

		if(currentQuestion.getAnswer() == null){
			if(currentQuestion.getDef().getDefaultValue() != null && currentQuestion.getDef().getDefaultValue().trim().length() > 0)
				currentQuestion.setAnswer(currentQuestion.getDef().getDefaultValue());
		}
	}

	public static Byte toByte(int val){
		return new Byte(Byte.parseByte(String.valueOf(val)));
	}

	public void onAlertMessage(byte msg){
		show();
	}


	public void itemStateChanged(Item src){
		byte type = currentQuestion.getDef().getType();
		if(src == currentCtrl && 
				(type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || type == QuestionDef.QTN_TYPE_BOOLEAN
						|| type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC))
		{
			commandAction(DefaultCommands.cmdOk,(Displayable)null);
		}
	}
}
