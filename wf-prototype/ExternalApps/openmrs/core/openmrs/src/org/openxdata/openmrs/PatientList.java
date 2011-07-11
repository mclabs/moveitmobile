package org.openxdata.openmrs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;


/**
 * This class holds a collection of patients.
 * 
 * @author Daniel
 *
 */
public class PatientList implements Persistent{

	/** Collection of patients. */
	private Vector patients = new Vector();;

	/** Constructs a new patient collection. */
	public PatientList(){

	}

	public PatientList(Vector patients){
		setPatients(patients);
	}

	public Vector getPatients() {
		return patients;
	}

	public void setPatients(Vector patients) {
		this.patients = patients;
	}

	public void addPatient(Patient patient){
		patients.addElement(patient);
	}

	public void addPatients(Vector patientList){
		if(patientList != null){
			for(int i=0; i<patientList.size(); i++ )
				this.patients.addElement(patientList.elementAt(i));
		}
	}

	public int size(){
		return patients.size();
	}

	public Patient getPatient(int index){
		return (Patient)patients.elementAt(index);
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
		setPatients(PersistentHelper.read(dis,new Patient().getClass(),dis.readInt()));
	}

	/** 
	 * Writes the patient collection object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.write(getPatients(), dos,0);
	}
}
