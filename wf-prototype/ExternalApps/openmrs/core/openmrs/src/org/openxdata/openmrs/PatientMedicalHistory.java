package org.openxdata.openmrs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;


/**
 * 
 * Holds the medical history of a patient.
 * 
 * @author daniel
 *
 */
public class PatientMedicalHistory implements Persistent{

	private int patientId;
	private Vector history;
	
	
	public Vector getHistory() {
		return history;
	}

	public void setHistory(Vector history) {
		this.history = history;
	}

	public int getPatientId() {
		return patientId;
	}

	public void setPatientId(int patientId) {
		this.patientId = patientId;
	}
	
	public MedicalHistoryField getFieldAt(int index){
		return (MedicalHistoryField)history.elementAt(index);
	}

	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setPatientId(dis.readInt());
		setHistory(PersistentHelper.read(dis,new MedicalHistoryField().getClass(),dis.readInt()));
	}
	
	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(getPatientId());
		PersistentHelper.write(getHistory(), dos,0);
	}
}
