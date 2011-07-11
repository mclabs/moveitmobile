package org.openxdata.openmrs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.openxdata.db.util.Persistent;


/**
 * Holds a value for a patient history field on a given visit date.
 * 
 * @author daniel
 *
 */
public class MedicalHistoryValue implements Persistent{
	
	private String value;
	private Date valueDate;

	public String getValue() {
		return value;
	}

	private void setValue(String value) {
		this.value = value;
	}

	public Date getValueDate() {
		return valueDate;
	}

	private void setValueDate(Date valueDate) {
		this.valueDate = valueDate;
	}

	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setValue(dis.readUTF());
		setValueDate(new Date(dis.readLong()));
	}
	
	public void write(DataOutputStream dos) throws IOException {
		dos.writeUTF(getValue());
		dos.writeLong(getValueDate().getTime());
	}
}
