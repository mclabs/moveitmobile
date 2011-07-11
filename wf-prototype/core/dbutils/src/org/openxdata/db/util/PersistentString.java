package org.openxdata.db.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


/**
 * Helper class to serialize strings as persistent objects.
 * 
 * @author Daniel Kayiwa
 *
 */
public class PersistentString implements Persistent {

	private String value;
	
	public PersistentString(){
		
	}
	
	public PersistentString(String value){
		setValue(value);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * @see org.openxdata.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setValue(dis.readUTF());
	}

	/**
	 * @see org.openxdata.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeUTF(getValue());
	}
}
