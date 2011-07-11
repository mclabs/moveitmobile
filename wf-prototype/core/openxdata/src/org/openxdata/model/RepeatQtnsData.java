package org.openxdata.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;


/**
 * Data collected for repeat sets of questions.
 * This represents question data (list of QuestionData objects) for a single repeating row.
 * 
 * @author daniel
 *
 */
public class RepeatQtnsData implements Persistent {

	/** A list of repeat question data (QuestionData). */
	private Vector questions = new Vector();;
	
	/** A reference to the Definition for repeat sets of questions we have. */
	private RepeatQtnsDef def;
	
	/** Unique identifier of repeat questions data. 
	 * This value does not have to be stored.
	 * Only used for identintification at runtime.
	 * */
	private byte id;
	
	
	public RepeatQtnsData(){
		
	}
	
	/** Copy constructor. */
	public RepeatQtnsData(RepeatQtnsData data){
		setId(data.getId());
		def = data.getDef();
		
		Vector qtns = data.getQuestions();
		for(int i=0; i<qtns.size(); i++)
			questions.addElement(new QuestionData((QuestionData)qtns.elementAt(i)));		
	}

	public RepeatQtnsData(byte id,RepeatQtnsDef def) {
		setId(id);
		setDef(def);
	}
	
	public RepeatQtnsData(byte id, Vector questions, RepeatQtnsDef def) {
		setId(id);
		setQuestions(questions);
		setDef(def);
	}
	
	public byte getId() {
		return id;
	}
	
	public void setId(byte id) {
		this.id = id;
	}
	
	public Vector getQuestions() {
		return questions;
	}

	public void setQuestions(Vector questions) {
		this.questions = questions;
	}

	public RepeatQtnsDef getDef() {
		return def;
	}

	public void setDef(RepeatQtnsDef def) {
		this.def = def;
		updateQuestionData();
		
	}
	
	public int size(){
		return questions.size();
	}
	
	private void updateQuestionData(){
		if(questions.size() == 0)
			createQuestionData();
		
		for(int j=0; j<questions.size(); j++){
			QuestionData qtnData = (QuestionData)questions.elementAt(j);
			QuestionDef qtnDef = def.getQuestion(qtnData.getId());
			qtnData.setDef(qtnDef);
			if(qtnData.getAnswer() != null && qtnDef.getType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE)
				((OptionData)qtnData.getAnswer()).setDef((OptionDef)qtnDef.getOptions().elementAt(Integer.parseInt(qtnData.getOptionAnswerIndices().toString())));
			else if(qtnData.getAnswer() != null && qtnDef.getType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
				Vector answers = (Vector)qtnData.getAnswer();
				for(byte k=0; k<answers.size(); k++){
					OptionData option = (OptionData)answers.elementAt(k);
					option.setDef((OptionDef)qtnDef.getOptions().elementAt(((Byte)((Vector)qtnData.getOptionAnswerIndices()).elementAt(k)).byteValue()));
				}
			}
			//As for now, we do not support nested repeats.
			/*else if(qtnData.getAnswer() != null && qtnDef.getType() == QuestionDef.QTN_TYPE_REPEAT){
				RepeatQtnsDataList answer = (RepeatQtnsDataList)qtnData.getAnswer();
				for(byte k=0; k<answer.size(); k++){
					RepeatQtnsData data = answer.getRepeatQtnsData(k);
					data.setDef(qtnDef.getRepeatQtnsDef());
				}
			}*/
		}	
	}
	
	/** Creates question data from their corresponding definitions. */
	private void createQuestionData(){
		Vector qtns = def.getQuestions();

		if(qtns == null)
			return;
		
		for(int i=0; i<qtns.size(); i++)
			questions.addElement(new QuestionData((QuestionDef)qtns.elementAt(i)));
	}
	
	public QuestionData getQuestion(int index){
		return (QuestionData)questions.elementAt(index);
	}
	
	public void setQuestionDataById(QuestionData questionData){
		QuestionData data;
		for(int i=0; i<questions.size(); i++){
			data = (QuestionData)questions.elementAt(i);
			if(data.getId() == questionData.getId()){
				questions.setElementAt(questionData, i);
				return;
			}
		}
	}
	
	public void addQuestion(QuestionData questionData){
		questions.addElement(questionData);
	}
	
	public String toString() {
		String val = "";
		if(questions != null && questions.size() > 0){
			for(int i=0; i<questions.size(); i++){
				QuestionData data = (QuestionData)questions.elementAt(i);
				if(data.getTextAnswer() != null && data.getTextAnswer().length() > 0){
					if(val.trim().length() > 0)
						val += ",";
					val += data.getTextAnswer();
				}
			}
		}
		return val;
	}
	
	/**
	 * Checks if if a repeat questions row has been answered.
	 * 
	 * @return
	 */
	public boolean isAnswered(){
		for(int i=0; i<questions.size(); i++){
			if(((QuestionData)questions.elementAt(i)).isAnswered())
				return true; 
		}
		return false; //Not even one question answered.
	}
	
	/** Reads a page definition object from the supplied stream. */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setQuestions(PersistentHelper.read(dis,new QuestionData().getClass()));
	}

	/** Write the page definition object to the supplied stream. */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.write(getQuestions(), dos);
	}
}
