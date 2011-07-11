package org.openxdata.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;


/** 
 * This is the question definition properties.
 * 
 * @author Daniel Kayiwa
 *
 */
public class QuestionDef implements Persistent{
	/** The prompt text. The text the user sees. */
	private String text = OpenXdataConstants.EMPTY_STRING;

	/** The help text. */
	private String helpText = OpenXdataConstants.EMPTY_STRING;

	/** The type of question. eg Numeric,Date,Text etc. */
	private byte type = QTN_TYPE_TEXT;

	/** The value supplied as answer if the user has not supplied one. */
	private String defaultValue;

	//TODO For a smaller payload, may need to combine (mandatory,visible,enabled,locked) 
	//into bit fields forming one byte. This would be a saving of 3 bytes per question.
	/** A flag to tell whether the question is to be answered or is optional. */
	private boolean mandatory = false;

	/** A flag to tell whether the question should be shown or not. */
	private boolean visible = true;

	/** A flag to tell whether the question should be enabled or disabled. */
	private boolean enabled = true;

	/** A flag to tell whether a question is to be locked or not. A locked question 
	 * is one which is visible, enabled, but cannot be edited.
	 */
	private boolean locked = false;

	//TODO May not need to serialize this property for smaller pay load. Then we would just rely on the id.
	/** The text indentifier of the question. This is used by the users of the questionaire 
	 * but in code we use the dynamically generated numeric id for speed. 
	 */
	private String variableName = OpenXdataConstants.EMPTY_STRING;

	/** The allowed set of values (OptionDef) for an answer of the question. 
	 * This also holds repeat sets of questions (RepeatQtnsDef) for the QTN_TYPE_REPEAT.
	 * This is an optimization aspect to prevent storing these guys diffently as 
	 * they can't both happen at the same time. The internal storage implementation of these
	 * repeats is hidden from the user by means of getRepeatQtnsDef() and setRepeatQtnsDef().
	 */
	private Object options;

	/** The numeric identifier of a question. When a form definition is being built, each question is 
	 * given a unique (on a form) id starting from 1 up to 127. The assumption is that one will never need to have
	 * a form with more than 127 questions for a mobile device (It would be too big).
	 */
	private byte id = OpenXdataConstants.NULL_ID;

	public static final byte QTN_TYPE_NULL = 0;
	
	/** Text question type. */
	public static final byte QTN_TYPE_TEXT = 1;

	/** Numeric question type. These are numbers without decimal points*/
	public static final byte QTN_TYPE_NUMERIC = 2;

	/** Decimal question type. These are numbers with decimals */
	public static final byte QTN_TYPE_DECIMAL = 3;

	/** Date question type. This has only date component without time. */
	public static final byte QTN_TYPE_DATE = 4;

	/** Time question type. This has only time element without date*/
	public static final byte QTN_TYPE_TIME = 5;

	/** This is a question with alist of options where not more than one option can be selected at a time. */
	public static final byte QTN_TYPE_LIST_EXCLUSIVE = 6;

	/** This is a question with alist of options where more than one option can be selected at a time. */
	public static final byte QTN_TYPE_LIST_MULTIPLE = 7;

	/** Date and Time question type. This has both the date and time components*/
	public static final byte QTN_TYPE_DATE_TIME = 8;

	/** Question with true and false answers. */
	public static final byte QTN_TYPE_BOOLEAN = 9;

	/** Question with repeat sets of questions. */
	public static final byte QTN_TYPE_REPEAT = 10;

	/** Question with image. */
	public static final byte QTN_TYPE_IMAGE = 11;
	
	public static final byte QTN_TYPE_VIDEO = 12;
	
	public static final byte QTN_TYPE_AUDIO = 13;
	
	public static final byte QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC = 14;
	
	public static final byte QTN_TYPE_GPS = 15;
	

	/** This constructor is used mainly during deserialization. */
	public QuestionDef(){

	}

	/** The copy constructor. */
	public QuestionDef(QuestionDef questionDef){
		setId(questionDef.getId());
		setText(questionDef.getText());
		setHelpText(questionDef.getHelpText());
		setType(questionDef.getType());
		setDefaultValue(questionDef.getDefaultValue());
		setVisible(questionDef.isVisible());
		setEnabled(questionDef.isEnabled());
		setLocked(questionDef.isLocked());
		setMandatory(questionDef.isMandatory());
		setVariableName(questionDef.getVariableName());

		if(getType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || getType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE || getType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC)
			this.options = copyQuestionOptions(questionDef.getOptions());
		else if(getType() == QuestionDef.QTN_TYPE_REPEAT)
			this.options = new RepeatQtnsDef(questionDef.getRepeatQtnsDef());
	}

	/**
	 * Constructs a new question definition object from the supplied parameters.
	 * For String type parameters, they should NOT be NULL. They should instead be empty,
	 * for the cases of missing values.
	 * 
	 * @param id
	 * @param text
	 * @param helpText - The hint or help text. Should NOT be NULL.
	 * @param mandatory
	 * @param type
	 * @param defaultValue
	 * @param visible
	 * @param enabled
	 * @param locked
	 * @param variableName
	 * @param options
	 */
	public QuestionDef(byte id,String text, String helpText, boolean mandatory, byte type, String defaultValue, boolean visible, boolean enabled, boolean locked, String variableName, Object options) {
		this();
		setId(id);
		setText(text);
		setHelpText(helpText);
		setType(type);
		setDefaultValue(defaultValue);
		setVisible(visible);
		setEnabled(enabled);
		setLocked(locked);
		setMandatory(mandatory);		
		setVariableName(variableName);
		setOptions(options);
	}

	public byte getId() {
		return id;
	}

	public void setId(byte id) {
		this.id = id;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		if(defaultValue != null && defaultValue.trim().length() > 0)
			this.defaultValue = defaultValue;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getHelpText() {
		return helpText;
	}

	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public Vector getOptions() {
		//if(!(type == QTN_TYPE_LIST_EXCLUSIVE || type == QTN_TYPE_LIST_MULTIPLE))
		//	throw new Exception("Invalid Operation");
		return (Vector)options;
	}

	public void setOptions(Object options) {
		this.options = options;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {	
		this.text = text;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void addOption(OptionDef optionDef){
		if(options == null)
			options = new Vector();
		((Vector)options).addElement(optionDef);
	}

	/**
	 * Reads the object from stream.
	 */
	public void read(DataInputStream dis) throws IOException, IllegalAccessException, InstantiationException{
		setId(dis.readByte());

		setText(dis.readUTF());
		setHelpText(dis.readUTF());
		setType(dis.readByte());

		setDefaultValue(PersistentHelper.readUTF(dis));

		/*setVisible(dis.readBoolean());
			setEnabled(dis.readBoolean());
			setLocked(dis.readBoolean());
			setMandatory(dis.readBoolean());*/

		//Intensionally done this way to provide some optimizations.
		byte val = dis.readByte();
		setVisible((val & OpenXdataConstants.BIT_FLAG1) != 0);
		setEnabled((val & OpenXdataConstants.BIT_FLAG2) != 0);
		setLocked((val & OpenXdataConstants.BIT_FLAG3) != 0);
		setMandatory((val & OpenXdataConstants.BIT_FLAG4) != 0);

		setVariableName(dis.readUTF());

		if(getType() != QuestionDef.QTN_TYPE_REPEAT)
			setOptions(PersistentHelper.read(dis,new OptionDef().getClass()));
		else{
			options = new RepeatQtnsDef();
			((RepeatQtnsDef)options).read(dis);
			((RepeatQtnsDef)options).setQtnDef(this);
		}
	}

	/**
	 * Write the object to stream.
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(getId());

		dos.writeUTF(getText());
		dos.writeUTF(getHelpText());
		dos.writeByte(getType());

		PersistentHelper.writeUTF(dos, getDefaultValue());

		/*dos.writeBoolean(isVisible());
		dos.writeBoolean(isEnabled());
		dos.writeBoolean(isLocked());
		dos.writeBoolean(isMandatory());*/

		//Intensionally done this way to provide some optimizations.
		byte val = 0;
		if(isVisible())
			val |= OpenXdataConstants.BIT_FLAG1;
		if(isEnabled())
			val |= OpenXdataConstants.BIT_FLAG2;
		if(isLocked())
			val |= OpenXdataConstants.BIT_FLAG3;
		if(isMandatory())
			val |= OpenXdataConstants.BIT_FLAG4;
		dos.writeByte(val);

		dos.writeUTF(getVariableName());

		if(getType() != QuestionDef.QTN_TYPE_REPEAT)
			PersistentHelper.write(getOptions(), dos);
		else
			((RepeatQtnsDef)options).write(dos);
	}

	public RepeatQtnsDef getRepeatQtnsDef(){
		return (RepeatQtnsDef)options;
	}

	public void setRepeatQtnsDef(RepeatQtnsDef repeatQtnsDef){
		options = repeatQtnsDef;
	}

	public String toString() {
		return getText();
	}

	/*private void copyQuestionOptions(Vector options){
		if(options == null)
			return;

		this.options = new Vector();
		for(byte i=0; i<options.size(); i++)
			((Vector)this.options).addElement(new OptionDef((OptionDef)options.elementAt(i)));
	}*/
	
	public static Vector copyQuestionOptions(Vector options){
		if(options == null)
			return null;
		Vector copy = new Vector();
		for(byte i=0; i<options.size(); i++)
			copy.addElement(new OptionDef((OptionDef)options.elementAt(i)));
		return copy;
	}

	public void addRepeatQtnsDef(QuestionDef qtn){
		if(options == null)
			options = new RepeatQtnsDef(qtn);
		((RepeatQtnsDef)options).addQuestion(qtn);
	}
	
	public OptionDef getOptionWithValue(Object value){
		if(options == null || value == null)
			return null;

		Vector list = (Vector)options;
		for(int i=0; i<list.size(); i++){
			OptionDef optionDef = (OptionDef)list.elementAt(i);
			if(optionDef.getVariableName().equals(value))
				return optionDef;
		}
		return null;
	}
}

