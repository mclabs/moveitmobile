package org.openxdata.model;

import java.io.*;

import org.openxdata.db.util.Persistent;


/** 
 * Definition of an answer option or one of the possible answers of a question
 * with a given set of allowed answers..
 * 
 * @author Daniel Kayiwa
 *
 */
public class OptionDef implements Persistent {
	/** The numeric unique identifier of an answer option. */
	private byte id = OpenXdataConstants.NULL_ID;

	/** The display text of the answer option. */
	private String text = OpenXdataConstants.EMPTY_STRING;

	//TODO May not need to serialize this property for smaller pay load. Then we would just rely on the id.
	/** The unique text ientifier of an answer option. */
	private String variableName = OpenXdataConstants.EMPTY_STRING;

	public static final char SEPARATOR_CHAR = ',';

	/** Constructs the answer option definition object where
	 * initialization parameters are not supplied. */
	public OptionDef() {  

	}

	/** The copy constructor  */
	public OptionDef(OptionDef optionDef) {  
		setId(optionDef.getId());
		setText(optionDef.getText());
		setVariableName(optionDef.getVariableName());
	}

	/** Constructs a new option answer definition object from the following parameters.
	 * 
	 * @param id
	 * @param text
	 * @param variableName
	 */
	public OptionDef(byte id,String text, String variableName) {
		this();
		setId(id);
		setText(text);
		setVariableName(variableName);
	}

	public byte getId() {
		return id;
	}

	public void setId(byte id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public String toString() {
		return getText();
	}

	/** Reads the answer option definition from the stream. 
	 * 
	 */
	public void read(DataInputStream dis) throws IOException {
		setId(dis.readByte());
		setText(dis.readUTF());
		setVariableName(dis.readUTF());
	}

	/** Writes the answer option definition to the stream. 
	 * 
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(getId());
		dos.writeUTF(getText());
		dos.writeUTF(getVariableName());
	}
}
