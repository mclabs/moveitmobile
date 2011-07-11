package org.openxdata.openmrs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.db.util.Persistent;

/**
 * This class encapsulates a patient database field
 * 
 * @author Daniel
 *
 */
public class PatientField implements Persistent {

	private int id;
	private String name;
	
	public PatientField(){
		
	}

	public PatientField(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
	
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setId(dis.readInt());
		setName(dis.readUTF());
	}
	
	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(getId());
		dos.writeUTF(getName());
	}
}
