package org.openxdata.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;


/**
 * Definition for repeat sets of questions. Basically this is just a specialized collection
 * of a set of repeating questions, together with with reference to their parent question.
 * 
 * @author daniel
 *
 */
public class RepeatQtnsDef implements Persistent {
	
	/** A list of questions (QuestionDef objects) on a repeat questions row. */
	private Vector questions = new Vector();
	
	/** Reference to the parent question. */
	private QuestionDef qtnDef;
	
	public RepeatQtnsDef() {
		 
	}
	
	/** Copy Constructor. */
	public RepeatQtnsDef(RepeatQtnsDef repeatQtnsDef) {
		//setQtnDef(new QuestionDef(repeatQtnsDef.getQtnDef()));
		setQtnDef(repeatQtnsDef.getQtnDef());
		copyQuestions(repeatQtnsDef.getQuestions());
	}
	
	public RepeatQtnsDef(QuestionDef qtnDef) {
		setQtnDef(qtnDef);
	}
	
	public RepeatQtnsDef(QuestionDef qtnDef,Vector questions) {
		this(qtnDef);
		setQuestions(questions);
	}
	
	public QuestionDef getQtnDef() {
		return qtnDef;
	}

	public void setQtnDef(QuestionDef qtnDef) {
		this.qtnDef = qtnDef;
	}

	public Vector getQuestions() {
		return questions;
	}

	public void addQuestion(QuestionDef qtn){
		qtn.setId((byte)(questions.size()+1));
		questions.addElement(qtn);
	}
	
	public void setQuestions(Vector questions) {
		this.questions = questions;
	}
	
	public String getText(){
		if(qtnDef != null)
			return qtnDef.getText();
		return null;
	}
	
	public QuestionDef getQuestion(byte id){
		for(int i=0; i<getQuestions().size(); i++){
			QuestionDef def = (QuestionDef)getQuestions().elementAt(i);
			if(def.getId() == id)
				return def;
		}
		
		return null;
	}
	
	public QuestionDef getQuestion(String varName){
		for(int i=0; i<questions.size(); i++){
			QuestionDef def = (QuestionDef)questions.elementAt(i);
			if(def.getVariableName().equals(varName))
				return def;
		}
		
		return null;
	}
	
	/** Reads a page definition object from the supplied stream. */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setQuestions(PersistentHelper.read(dis,new QuestionDef().getClass()));
	}

	/** Write the page definition object to the supplied stream. */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.write(getQuestions(), dos);
	}
	
	private void copyQuestions(Vector questions){
		if(questions == null)
			return;
		
		for(int i=0; i<questions.size(); i++)
			this.questions.addElement(new QuestionDef((QuestionDef)questions.elementAt(i)));
	}
}
