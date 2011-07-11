package org.openxdata.openmrs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.db.util.AbstractRecord;


/**
 * This class provides a mapping between a patient record
 * and forms collected for a patient. As for the current implementation,
 * a patient can have a maximum of one record for each type of form
 * as identified by the formDefId.
 * 
 * @author Daniel
 *
 */
public class PatientForm extends AbstractRecord{
	private Integer patientId;
	private int formRecordId;
	//private String patientGuid; //This is because new patients have not patientIds yet.

	public PatientForm(){
		super();
	}
	
	public PatientForm(Integer patientId, int formRecordId) {
		this();
		setPatientId(patientId);
		setFormRecordId(formRecordId);
	}
	
	public Integer getPatientId() {
		return patientId;
	}
	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}
	public int getFormRecordId() {
		return formRecordId;
	}
	public void setFormRecordId(int formRecordId) {
		this.formRecordId = formRecordId;
	}

	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setPatientId(new Integer(dis.readInt()));
		setFormRecordId(dis.readInt());
	}

	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(getPatientId().intValue());
		dos.writeInt(getFormRecordId());
	}
	
	
}
