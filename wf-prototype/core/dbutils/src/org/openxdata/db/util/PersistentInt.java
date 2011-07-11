package org.openxdata.db.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Helper class to serialize ints as persistent objects.
 * 
 * @author Daniel Kayiwa
 *
 */
public class PersistentInt implements Persistent{

	private int value;
	
	public PersistentInt(){
		
	}
	
	public PersistentInt(int value){
		setValue(value);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	/**
	 * @see org.openxdata.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setValue(dis.readInt());
	}

	/**
	 * @see org.openxdata.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(getValue());
	}
}
