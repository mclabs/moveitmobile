package org.openxdata.purcai;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;


public class ValueValueList implements Persistent {
	/** Collection of values. */
	private Vector values;
	
	public ValueValueList(){
		super();
	}
	
	public ValueValueList(Vector values){
		this();
		setValues(values);
	}
	
	public Vector getValues() {
		return values;
	}

	public void setValues(Vector values) {
		this.values = values;
	}
	
	public void addValue(ValueValue value){
		if(values == null)
			values = new Vector();
		values.addElement(value);
	}
	
	public void addValues(Vector valueList){
		if(valueList != null){
			if(values == null)
				values = valueList;
			else{
				for(int i=0; i<valueList.size(); i++ )
					this.values.addElement(valueList.elementAt(i));
			}
		}
	}
	
	public int size(){
		if(getValues() == null)
			return 0;
		return getValues().size();
	}
	
	public ValueValue getValue(int index){
		return (ValueValue)getValues().elementAt(index);
	}
	
	/** 
	 * Reads the patient collection object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setValues(PersistentHelper.read(dis,new ValueValue().getClass(),dis.readInt()));
	}

	/** 
	 * Writes the patient collection object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.write(getValues(), dos,0);
	}
}
