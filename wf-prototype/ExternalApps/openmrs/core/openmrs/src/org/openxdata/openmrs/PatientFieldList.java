package org.openxdata.openmrs;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;


/**
 * This class encapsulates a list of patient database fields.
 * 
 * @author Daniel
 *
 */
public class PatientFieldList implements Persistent{

	private Vector fields = new Vector();

	public PatientFieldList(){

	}

	public PatientFieldList(Vector patientFields) {
		this.fields = patientFields;
	}

	public Vector getFields() {
		return fields;
	}

	public void setFields(Vector fields) {
		this.fields = fields;
	}

	public void addField(PatientField field){
		fields.addElement(field);
	}

	public void addPatientFields(Vector fieldList){
		if(fieldList != null){
			for(int i=0; i<fieldList.size(); i++ )
				this.fields.addElement(fieldList.elementAt(i));
		}
	}

	public int size(){
		return fields.size();
	}

	public PatientField getField(int index){
		return (PatientField)fields.elementAt(index);
	}

	/** 
	 * Reads the patient field collection object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setFields(PersistentHelper.read(dis,new PatientField().getClass()));
	}

	/** 
	 * Writes the patient field collection object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.write(getFields(), dos);
	}
}
