package org.openxdata.openmrs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.openxdata.db.util.AbstractRecord;
import org.openxdata.db.util.PersistentHelper;

/**
 * 
 * @author Daniel
 *
 */
public class Patient extends AbstractRecord{
	Integer patientId; //patientid is made Integer instead of int because new patients dont have it.
	String prefix;
	String familyName;
	String middleName;
	String givenName;
	String gender;
	Date birthDate;
	String patientIdentifier;
	
	/** true if the patient has just been created on the device before being saved on the server? **/
	boolean isNewPatient;
	
	
	public Patient(){
		super();
	}
	
	public Patient(Integer patientId, String prefix, String familyName, String middleName, String givenName, String gender, Date birthDate, String patientIdentifier, boolean isNewPatient) {
		this();
		setPatientId(patientId);
		setPrefix(prefix);
		setFamilyName(familyName);
		setMiddleName(middleName);
		setGivenName(givenName);
		setGender(gender);
		setBirthDate(birthDate);
		setPatientIdentifier(patientIdentifier);
		setNewPatient(isNewPatient);
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public boolean isNewPatient() {
		return isNewPatient;
	}

	public void setNewPatient(boolean isNewPatient) {
		this.isNewPatient = isNewPatient;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public Integer getPatientId() {
		//For now, new patients have negative ids. assuming server does not assign negatives
		//We use recordid value because is takes care of generating new ids for us.
		if(isNewPatient())
			return new Integer(-getRecordId());
		
		return patientId;
	}

	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}

	public String getPatientIdentifier() {
		return patientIdentifier;
	}

	public void setPatientIdentifier(String patientIdentifier) {
		this.patientIdentifier = patientIdentifier;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * Gets the patient whole name which is a concatenation of the
	 * given, middle and family names.
	 * 
	 * @return
	 */
	public String getName(){
		String s="";

		if(getGivenName() != null && getGivenName().length() != 0)
			s += " " + getGivenName();
		
		if(getMiddleName() != null && getMiddleName().length() != 0)
			s += " " + getMiddleName();

		if(getFamilyName() != null && getFamilyName().length() != 0)
			s += " " + getFamilyName();

		return s;
	}
	
	public String toString() {
		String s;

		if(getPrefix() != null && getPrefix().length() != 0)
			s = getPatientIdentifier() + " " + getPrefix();
		else
			s = getPatientIdentifier();

		if(getGivenName() != null && getGivenName().length() != 0)
			s += " " + getGivenName();
		
		if(getMiddleName() != null && getMiddleName().length() != 0)
			s += " " + getMiddleName();

		if(getFamilyName() != null && getFamilyName().length() != 0)
			s += " " + getFamilyName();

		if(isNewPatient())
			s += " (NEW)";

		if(s == null)
			s = "NAMELESS PatientId="+getPatientId();
		return s;
	}

	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setPatientId(PersistentHelper.readInteger(dis));
		setPrefix(PersistentHelper.readUTF(dis));
		setFamilyName(PersistentHelper.readUTF(dis));
		setMiddleName(PersistentHelper.readUTF(dis));
		setGivenName(PersistentHelper.readUTF(dis));
		setGender(PersistentHelper.readUTF(dis));
		setBirthDate(PersistentHelper.readDate(dis));
		setPatientIdentifier(PersistentHelper.readUTF(dis));
		setNewPatient(dis.readBoolean());
	}

	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.writeInteger(dos,getPatientId());
		PersistentHelper.writeUTF(dos, getPrefix());
		PersistentHelper.writeUTF(dos, getFamilyName());
		PersistentHelper.writeUTF(dos, getMiddleName());
		PersistentHelper.writeUTF(dos, getGivenName());
		PersistentHelper.writeUTF(dos, getGender());
		PersistentHelper.writeDate(dos, getBirthDate());
		PersistentHelper.writeUTF(dos, getPatientIdentifier());
		dos.writeBoolean(isNewPatient());
	}

}
