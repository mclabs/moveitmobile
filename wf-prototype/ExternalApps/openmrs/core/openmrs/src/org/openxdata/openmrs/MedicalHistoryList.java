package org.openxdata.openmrs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;


/**
 * 
 * Holds a list of patients medical history.
 * 
 * @author daniel
 *
 */
public class MedicalHistoryList implements Persistent{

	private Vector history = new Vector();

	public MedicalHistoryList(){

	}

	public MedicalHistoryList(Vector history) {
		this.history = history;
	}

	public Vector getHistory() {
		return history;
	}

	public void setHistory(Vector history) {
		this.history = history;
	}

	public void addHistory(PatientMedicalHistory history){
		this.history.addElement(history);
	}

	public void addPatientFields(Vector historyList){
		if(historyList != null){
			for(int i=0; i<historyList.size(); i++ )
				this.history.addElement(historyList.elementAt(i));
		}
	}

	public int size(){
		return history.size();
	}

	public PatientMedicalHistory getHistory(int index){
		return (PatientMedicalHistory)getHistory().elementAt(index);
	}

	public void remove(PatientMedicalHistory history){
		getHistory().removeElement(history);
	}


	/*public Object getPatintFiledValue(int fieldId,Integer patientId){
		for(int i=0; i<size(); i++){
			PatientFieldValue fieldVal = getValue(i);
			if(fieldVal.getFieldId() == fieldId && fieldVal.getPatientId() == patientId.intValue())
				return fieldVal.getValue(); 
		}
		return null;
	}*/

	/** 
	 * Reads the patient medical history collection object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setHistory(PersistentHelper.read(dis,new PatientMedicalHistory().getClass(),dis.readInt()));
	}

	/** 
	 * Writes the patient medical history collection object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.write(getHistory(), dos,0);
	}
}
