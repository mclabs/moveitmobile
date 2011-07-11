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
 * 
 * @author daniel
 *
 */
public class DynamicOptionDef  implements Persistent {

	/** The question whose values are determined by or dependent on the answer of another question. **/
	private byte questionId;

	/** A map between each parent option and a list of possible options for the dependant question. */
	private Hashtable/*<Byte,List<OptionDef>>*/ parentToChildOptions = new Hashtable();;


	public DynamicOptionDef(){

	}
	
	/** The copy constructor  */
	public DynamicOptionDef(DynamicOptionDef dynamicOptionDef) {  
		setQuestionId(dynamicOptionDef.getQuestionId());
		copyQuestionOptions(dynamicOptionDef.getParentToChildOptions());
	}

	public Hashtable getParentToChildOptions() {
		return parentToChildOptions;
	}


	public void setParentToChildOptions(Hashtable parentToChildOptions) {
		this.parentToChildOptions = parentToChildOptions;
	}


	public byte getQuestionId() {
		return questionId;
	}

	public void setQuestionId(byte questionId) {
		this.questionId = questionId;
	}
	
	private void copyQuestionOptions(Hashtable parentToChildOptions){
		if(parentToChildOptions == null)
			return;
		
		Enumeration keys = parentToChildOptions.keys();
		Byte key;
		while(keys.hasMoreElements()){
			key = (Byte)keys.nextElement();
			this.parentToChildOptions.put(key, QuestionDef.copyQuestionOptions((Vector)parentToChildOptions.get(key)));
		}
	}
	
	public Vector getOptionList(byte optionId){
		return (Vector)parentToChildOptions.get(new Byte(optionId));
	}
	
	public void read(DataInputStream dis) throws IOException , InstantiationException, IllegalAccessException {
		setQuestionId(dis.readByte());
		
		byte len = dis.readByte();
		if(len == 0)
			return;
		
		for(byte i=0; i<len; i++ )
			parentToChildOptions.put(new Byte(dis.readByte()), PersistentHelper.read(dis, new OptionDef().getClass()));
	}

	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(getQuestionId());

		if(parentToChildOptions != null){
			dos.writeByte(parentToChildOptions.size());
			Enumeration keys = parentToChildOptions.keys();
			Byte key;
			while(keys.hasMoreElements()){
				key = (Byte)keys.nextElement();
				dos.writeByte(key.byteValue());
				PersistentHelper.write((Vector)parentToChildOptions.get(key), dos);
			}
		}
		else
			dos.writeByte(0);
	}
}
