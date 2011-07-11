package org.openxdata.openmrs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.db.util.Persistent;


/**
 * This class encapsulates all data about patients. Can serve as the patient in-memory database.
 * This class is the one serialized from the patient server to the device.
 * 
 * @author Daniel
 *
 */
public class PatientData implements Persistent{

	/** The list of patients. */
	private PatientList patients = new PatientList();

	/** The list of patient database fields. */
	private PatientFieldList fields = new PatientFieldList();

	/** The list of patient database field values. */
	private PatientFieldValueList fieldValues = new PatientFieldValueList();
	
	/** The patient medical history. */
	private MedicalHistoryList medicalHistory = new MedicalHistoryList();

	
	public PatientData(){

	}

	public PatientData(PatientList patients, PatientFieldList fields, PatientFieldValueList fieldValues,MedicalHistoryList medicalHistory) {
		this.patients = patients;
		this.fields = fields;
		this.fieldValues = fieldValues;
		this.medicalHistory = medicalHistory;
	}

	public PatientFieldList getFields() {
		return fields;
	}

	public void setFields(PatientFieldList fields) {
		this.fields = fields;
	}

	public PatientFieldValueList getFieldValues() {
		return fieldValues;
	}

	public void setFieldValues(PatientFieldValueList fieldValues) {
		this.fieldValues = fieldValues;
	}

	public PatientList getPatients() {
		return patients;
	}

	public void setPatients(PatientList patients) {
		this.patients = patients;
	}

	public MedicalHistoryList getHistory() {
		return medicalHistory;
	}

	public void setHistory(MedicalHistoryList medicalHistory) {
		this.medicalHistory = medicalHistory;
	}

	/** 
	 * Reads the patient data object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		patients.read(dis);
		fields.read(dis);
		fieldValues.read(dis);
		medicalHistory.read(dis);
	}

	/** 
	 * Writes the patient data object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		patients.write(dos);
		fields.write(dos);
		fieldValues.write(dos);
		medicalHistory.write(dos);
	}
}
