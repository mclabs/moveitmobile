package org.openxdata.openclinica.forms;

import javax.microedition.lcdui.*;

public class SubjectSearchForm extends Form {

	private TextField txtStudySubjectId;
	private TextField txtPersonId;
	
	public SubjectSearchForm(String title) {
		super(title);	
		txtStudySubjectId= new TextField("Study Subject ID:","",100,TextField.ANY);
		txtPersonId = new TextField("Person ID:","",100,TextField.ANY);
		
		this.append(txtStudySubjectId);
		this.append(txtPersonId);
	}
	
	public String getPersonId(){
		return this.txtPersonId.getString();
	}
	
	public void setPersonId(String personId){
		this.txtPersonId.setString(personId);
	}
	
	public String getStudySubjectId(){
		return this.txtStudySubjectId.getString();
	}
	
	public void setStudySubjectId(String studySubjectId){
		this.txtPersonId.setString(studySubjectId);
	}
}
