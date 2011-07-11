package org.openxdata.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;

/**
 * Definition of a form. This has some meta data about the form definition and  
 * a collection of pages together with question branching or skipping skipRules.
 * A form is sent as defined in one language. For instance, those using
 * Swahili would get forms in that language, etc. We don't support runtime
 * changing of a form language in order to have a more efficient implementation
 * as a trade off for more flexibility which may not be used most of the times.
 * 
 * @author Daniel Kayiwa
 *
 */
public class FormDef implements Persistent{

	/** A collection of page definitions (PageDef objects). */
	private Vector pages = new Vector();

	//TODO May not need to serialize this property for smaller pay load. Then we may just rely on the id.
	//afterall it is not even guaranteed to be unique.
	/** The string unique identifier of the form definition. */
	private String variableName = OpenXdataConstants.EMPTY_STRING;

	/** The display name of the form. */
	private String name = OpenXdataConstants.EMPTY_STRING;

	/** The numeric unique identifier of the form definition. */
	private int id = OpenXdataConstants.NULL_ID;
	
	/** The key of the form. */
	private String formKey = OpenXdataConstants.EMPTY_STRING;

	/** The collection of skipRules (SkipRule objects) for this form. */
	private Vector skipRules;

	/** The collection of valifationRules (ValidationRule objects) for this form. */
	private Vector validationRules;

	/** A string constistig for form fields that describe its data. eg description-template="${/data/question1}$ Market" */
	private String descriptionTemplate =  OpenXdataConstants.EMPTY_STRING;

	/** A mapping of dynamic lists keyed by the id of the question whose values
	 *  determine possible values of another question as specified in the DynamicOptionDef object.
	 */
	private Hashtable/*<Byte,DynamicOptionDef>*/ dynamicOptions;


	/** Constructs a form definition object. */
	public FormDef() {

	}

	public FormDef(FormDef formDef) {
		setId(formDef.getId());
		setName(formDef.getName());
		setFormKey(formDef.getFormKey());

		//I just don't think we need this in addition to the id
		setVariableName(formDef.getVariableName());

		setDescriptionTemplate(formDef.getDescriptionTemplate());
		copyPages(formDef.getPages());
		copySkipRules(formDef.getSkipRules());
		copyValidationRules(formDef.getValidationRules());
		copyDynamicOptions(formDef.getDynamicOptions());
	}

	/**
	 * Constructs a form definition object from these parameters.
	 * 
	 * @param name - the numeric unique identifier of the form definition.
	 * @param name - the display name of the form.
	 * @param variableName - the string unique identifier of the form definition.
	 * @param pages - collection of page definitions.
	 * @param skipRules - collection of branching skipRules.
	 */
	/*public FormDef(int id, String name, String variableName,Vector pages, Vector skipRules, Vector validationRules, String descTemplate) {
		setId(id);
		setName(name);

		//I just don't think we need this in addition to the id
		setVariableName(variableName);

		setPages(pages);
		setSkipRules(skipRules);
		setValidationRules(validationRules);
		setDescriptionTemplate((descTemplate == null) ? EpihandyConstants.EMPTY_STRING : descTemplate);
	}*/

	public Vector getPages() {
		return pages;
	}

	public void setPages(Vector pages) {
		this.pages = pages;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	//I just don't think we need this in addition to the id
	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFormKey() {
		return formKey;
	}

	public void setFormKey(String formKey) {
		this.formKey = formKey;
	}

	public Vector getSkipRules() {
		return skipRules;
	}

	public void setSkipRules(Vector skipRules) {
		this.skipRules = skipRules;
	}

	public Vector getValidationRules() {
		return validationRules;
	}

	public void setValidationRules(Vector validationRules) {
		this.validationRules = validationRules;
	}

	public String getDescriptionTemplate() {
		return descriptionTemplate;
	}

	public void setDescriptionTemplate(String descriptionTemplate) {
		this.descriptionTemplate = descriptionTemplate;
	}

	public Hashtable getDynamicOptions() {
		return dynamicOptions;
	}

	public void setDynamicOptions(Hashtable dynamicOptions) {
		this.dynamicOptions = dynamicOptions;
	}

	public String toString() {
		return getName();
	}

	public ValidationRule getValidationRule(byte questionId){
		for(int i=0; i<validationRules.size(); i++){
			ValidationRule rule = (ValidationRule)validationRules.elementAt(i);
			if(questionId == rule.getQuestionId())
				return rule;
		}

		return null;
	}

	/**
	 * Gets a question identified by a variable name.
	 * 
	 * @param varName - the string identifier of the question. 
	 * @return the question reference.
	 */
	public QuestionDef getQuestion(String varName){
		if(varName == null)
			return null;

		for(byte i=0; i<getPages().size(); i++){
			QuestionDef def = ((PageDef)getPages().elementAt(i)).getQuestion(varName);
			if(def != null)
				return def;
		}

		return null;
	}

	/**
	 * Gets a question identified by an id
	 * 
	 * @param id - the numeric identifier of the question. 
	 * @return the question reference.
	 */
	public QuestionDef getQuestion(byte id){	
		for(byte i=0; i<getPages().size(); i++){
			QuestionDef def = ((PageDef)getPages().elementAt(i)).getQuestion(id);
			if(def != null)
				return def;
		}

		return null;
	}

	/**
	 * Gets a numeric question identifier for a given question variable name.
	 * 
	 * @param varName - the string identifier of the question. 
	 * @return the numeric question identifier.
	 */
	public byte getQuestionId(String varName){
		QuestionDef qtn = getQuestion(varName);
		if(qtn != null)
			return qtn.getId();

		return OpenXdataConstants.NULL_ID;
	}

	public void addQuestion(QuestionDef qtn){
		if(pages.size() == 0){
			PageDef page = new PageDef(/*this.getVariableName()*/"Page1",Byte.parseByte("1"),null);
			pages.addElement(page);
		}

		((PageDef)pages.elementAt(0)).addQuestion(qtn);
	}

	/** 
	 * Reads the form definition object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setId(dis.readInt());
		setFormKey(dis.readUTF());
		setName(dis.readUTF());

		//I just don't think we need this in addition to the id
		setVariableName(dis.readUTF());

		setDescriptionTemplate(dis.readUTF());
		setPages(PersistentHelper.read(dis,new PageDef().getClass()));
		setSkipRules(PersistentHelper.read(dis,new SkipRule().getClass()));
		setValidationRules(PersistentHelper.read(dis,new ValidationRule().getClass()));

		byte len = dis.readByte();
		if(len == 0)
			return;
		dynamicOptions = new Hashtable();
		for(byte i=0; i<len; i++){
			Byte questionId = new Byte(dis.readByte());
			DynamicOptionDef dynamicOptionDef = new DynamicOptionDef();
			dynamicOptionDef.read(dis);
			dynamicOptions.put(questionId, dynamicOptionDef);
		}
	}

	/** 
	 * Writes the form definition object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(getId());
		dos.writeUTF(getFormKey());
		dos.writeUTF(getName());

		//I just don't think we need this in addition to the id
		dos.writeUTF(getVariableName());

		dos.writeUTF(getDescriptionTemplate());
		PersistentHelper.write(getPages(), dos);
		PersistentHelper.write(getSkipRules(), dos);
		PersistentHelper.write(getValidationRules(), dos);

		if(dynamicOptions != null){
			dos.writeByte(dynamicOptions.size());
			Enumeration keys = dynamicOptions.keys();
			Byte key;
			while(keys.hasMoreElements()){
				key  = (Byte)keys.nextElement();
				dos.writeByte(key.byteValue());
				((DynamicOptionDef)dynamicOptions.get(key)).write(dos);
			}
		}
		else
			dos.writeByte(0);
	}

	private void copyPages(Vector pages){
		for(byte i=0; i<pages.size(); i++) //Should have atleast one page is why we are not checking for nulls.
			this.pages.addElement(new PageDef((PageDef)pages.elementAt(i)));
	}

	private void copySkipRules(Vector rules){
		if(rules != null)
		{
			this.skipRules =  new Vector();
			for(byte i=0; i<rules.size(); i++)
				this.skipRules.addElement(new SkipRule((SkipRule)rules.elementAt(i)));
		}
	}

	private void copyValidationRules(Vector rules){
		if(rules != null)
		{
			this.validationRules =  new Vector();
			for(byte i=0; i<rules.size(); i++)
				this.validationRules.addElement(new ValidationRule((ValidationRule)rules.elementAt(i)));
		}
	}

	private void copyDynamicOptions(Hashtable dynamicOptions){
		if(dynamicOptions != null)
		{
			this.dynamicOptions =  new Hashtable();

			Enumeration keys = dynamicOptions.keys();
			Byte key;
			while(keys.hasMoreElements()){
				key = (Byte)keys.nextElement();
				this.dynamicOptions.put(key, new DynamicOptionDef((DynamicOptionDef)dynamicOptions.get(key)));
			}
		}
	}

	public int getPageCount(){
		return pages.size();
	}



	//TODO These could be moved from here to reduce the jar size.
	//reason why they can be moved is they are not used on the device.
	//They are instead used by the parser on the server side.
	public void addPage(){
		pages.addElement(new PageDef("Page"+pages.size(),(byte)pages.size(),null));
	}

	public void moveQuestion2Page(QuestionDef qtn, int pageNo){
		for(int i=0; i<pages.size(); i++){
			PageDef page = (PageDef)pages.elementAt(i);
			if(page.getQuestions().contains(qtn)){
				if(i == pageNo-1)
					return;
				page.getQuestions().removeElement(qtn);
				((PageDef)pages.elementAt(pageNo-1)).addQuestion(qtn);
				return;
			}
		}
	}

	public boolean removeQuestion(QuestionDef qtnDef){
		for(int i=0; i<pages.size(); i++){
			if(((PageDef)pages.elementAt(i)).getQuestions().removeElement(qtnDef))
				return true;
		}
		return false;
	}

	public PageDef getPageAt(int index) {
		return (PageDef)pages.elementAt(index);
	}

	public void setPageName(String name){
		((PageDef)pages.elementAt(pages.size()-1)).setName(name);
	}
	
	public DynamicOptionDef getDynamicOptions(byte questionId){
		if(dynamicOptions == null)
			return null;
		return (DynamicOptionDef)dynamicOptions.get(new Byte(questionId));
	}
}
