package org.openxdata.openmrs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;


/**
 * Holds a list of values for a patient history field. 
 * eg Past ARVSs as a history field can have a list of values
 * for various patient visit dates.
 * 
 * @author daniel
 *
 */
public class MedicalHistoryField implements Persistent{
	
	private String fieldName;
	private Vector values;
	
	
	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Vector getValues() {
		return values;
	}

	public void setValues(Vector values) {
		this.values = values;
	}

	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setFieldName(dis.readUTF());
		setValues(PersistentHelper.read(dis,new MedicalHistoryValue().getClass(),dis.readInt()));
	}
	
	public void write(DataOutputStream dos) throws IOException {
		dos.writeUTF(getFieldName());
		PersistentHelper.write(getValues(), dos,0);
	}
}
