package org.openxdata.openmrs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;

/**
 * This class encapsulates a list of patients database field values.
 * 
 * @author Daniel
 *
 */
public class PatientFieldValueList implements Persistent{

	private Vector values = new Vector();

	public PatientFieldValueList(){

	}

	public PatientFieldValueList(Vector values) {
		this.values = values;
	}

	public Vector getValues() {
		return values;
	}

	public void setValues(Vector values) {
		this.values = values;
	}

	public void addValue(PatientFieldValue value){
		values.addElement(value);
	}

	public void addPatientFields(Vector valueList){
		if(valueList != null){
			for(int i=0; i<valueList.size(); i++ )
				this.values.addElement(valueList.elementAt(i));
		}
	}

	public int size(){
		return values.size();
	}

	public PatientFieldValue getValue(int index){
		return (PatientFieldValue)getValues().elementAt(index);
	}

	public void remove(PatientFieldValue value){
		getValues().removeElement(value);
	}

	public Object getPatintFiledValue(int fieldId,Integer patientId){
		for(int i=0; i<size(); i++){
			PatientFieldValue fieldVal = getValue(i);
			if(fieldVal.getFieldId() == fieldId && fieldVal.getPatientId() == patientId.intValue())
				return fieldVal.getValue(); 
		}
		return null;
	}

	/** 
	 * Reads the patient field value collection object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setValues(PersistentHelper.read(dis,new PatientFieldValue().getClass(),dis.readInt()));
	}

	/** 
	 * Writes the patient field value collection object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.write(getValues(), dos,0);
	}
}
