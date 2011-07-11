package org.openxdata.purcai;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;

public class KeyValueValueList  implements Persistent{
	/** Collection of values. */
	private Vector values;
	
	public KeyValueValueList(){
		super();
	}
	
	public KeyValueValueList(Vector values){
		setValues(values);
	}
	
	public Vector getValues() {
		return values;
	}

	public void setValues(Vector values) {
		this.values = values;
	}
	
	public void addValue(KeyValueValue value){
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
	
	public KeyValueValue getValue(int index){
		return (KeyValueValue)getValues().elementAt(index);
	}
	
	public boolean contains(KeyValueValue value){		
		for(int i=0; i<values.size(); i++){
			if(((KeyValueValue)values.elementAt(i)).equals(value))
				return true;
		}
		
		return false;
	}
	
	public boolean contains(int id, int id1, int id2){	
		KeyValueValue value;
		for(int i=0; i<values.size(); i++){
			value = (KeyValueValue)values.elementAt(i);
			if(value.getId() == id && value.getId1() == id1 && value.getId2() == id2)
				return true;
		}
		
		return false;
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
		setValues(PersistentHelper.read(dis,new KeyValueValue().getClass(),dis.readInt()));
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
