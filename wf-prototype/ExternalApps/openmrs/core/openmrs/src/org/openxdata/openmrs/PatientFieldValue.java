package org.openxdata.openmrs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.db.util.Persistent;


/**
 * This class encapsulates a patient database field value.
 * 
 * @author Daniel
 *
 */
public class PatientFieldValue implements Persistent{

	private int fieldId;
	private int patientId;
	private Object value;
	
	public PatientFieldValue(){
		
	}

	public PatientFieldValue(int fieldId, int patientId, Object value) {
		super();
		this.fieldId = fieldId;
		this.patientId = patientId;
		this.value = value;
	}
	
	public int getFieldId() {
		return fieldId;
	}

	public void setFieldId(int fieldId) {
		this.fieldId = fieldId;
	}

	public int getPatientId() {
		return patientId;
	}

	public void setPatientId(int patientId) {
		this.patientId = patientId;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setFieldId(dis.readInt());
		setPatientId(dis.readInt());
		setValue(dis.readUTF());
	}
	
	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(getFieldId());
		dos.writeInt(getPatientId());
		dos.writeUTF(getValue().toString());
	}
}
