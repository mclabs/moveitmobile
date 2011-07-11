package org.openxdata.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;

/**
 * Does data validations eg value should be in range (1,90) etc
 * 
 * @author daniel
 *
 */
public class ValidationRule implements Persistent {
	
	/** The unique identifier of the question referenced by this condition. */
	private int questionId;
	
	/** A list of conditions (Condition object) to be tested for a rule. 
	 * E.g. age is greater than 4. etc
	 */
	private Vector conditions = new Vector();
	
	
	/** The validation rule name. */
	private String errorMessage;
	
	/** Operator for combining more than one condition. (And, Or) only these two for now. */
	private int conditionsOperator = OpenXdataConstants.CONDITIONS_OPERATOR_NULL;
	
	private FormData formData;
	
	
	public ValidationRule(){
		
	}
	
	/** Copy constructor. */
	public ValidationRule(ValidationRule validationRule){
		setQuestionId(validationRule.getQuestionId());
		setErrorMessage(validationRule.getErrorMessage());
		setConditionsOperator(validationRule.getConditionsOperator());
		copyConditions(validationRule.getConditions());
	}
	
	/** Construct a Rule object from parameters. 
	 * 
	 * @param ruleId 
	 * @param conditions 
	 * @param action
	 * @param actionTargets
	 */
	public ValidationRule(int questionId, Vector conditions , String errorMessage) {
		setQuestionId(questionId);
		setConditions(conditions);
		setErrorMessage(errorMessage);
	}

	public Vector getConditions() {
		return conditions;
	}

	public void setConditions(Vector conditions) {
		this.conditions = conditions;
	}

	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}
	
	public int getConditionsOperator() {
		return conditionsOperator;
	}

	public void setConditionsOperator(int conditionsOperator) {
		this.conditionsOperator = conditionsOperator;
	}
	
	public Condition getConditionAt(int index) {
		return (Condition)conditions.elementAt(index);
	}
	
	public int getConditionCount() {
		return conditions.size();
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void addCondition(Condition condition){
		conditions.addElement(condition);
	}
	
	public boolean containsCondition(Condition condition){
		return conditions.contains(condition);
	}
	
	/** 
	 * Checks conditions of a rule and executes the corresponding actions
	 * 
	 * @param data
	 */
	public boolean isValid(){
		boolean trueFound = false, falseFound = false;
		
		for(int i=0; i<conditions.size(); i++){
			Condition condition = (Condition)this.conditions.elementAt(i);
			if(condition.isTrue(formData,true))
				trueFound = true;
			else
				falseFound = true;
		}
		
		if(conditions.size() == 1 || getConditionsOperator() == OpenXdataConstants.CONDITIONS_OPERATOR_AND)
			return !falseFound;
		else if(getConditionsOperator() == OpenXdataConstants.CONDITIONS_OPERATOR_OR)
			return trueFound;
		
		return false;
	}
	
	private void copyConditions(Vector conditions){
		this.conditions = new Vector();
		for(int i=0; i<conditions.size(); i++)
			this.conditions.addElement(new Condition((Condition)conditions.elementAt(i)));
	}
	
	/**
	 * @see org.openxdata.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setQuestionId(dis.readByte());
		setConditions(PersistentHelper.read(dis,new Condition().getClass()));
		setErrorMessage(dis.readUTF());
		setConditionsOperator(dis.readByte());

	}

	/**
	 * @see org.openxdata.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(getQuestionId());
		PersistentHelper.write(getConditions(), dos);
		dos.writeUTF(getErrorMessage());
		dos.writeByte(getConditionsOperator());
	}

	public FormData getFormData() {
		return formData;
	}

	public void setFormData(FormData formData) {
		this.formData = formData;
	}
}
