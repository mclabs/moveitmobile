package org.openxdata.openclinica;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import org.openxdata.db.util.AbstractRecord;
import org.openxdata.db.util.PersistentHelper;

/**
 * 
 * @author Daniel
 *
 */
public class Subject extends AbstractRecord{

	private Integer subjectId; //patientid is made Integer instead of int because new patients dont have it.
	private String studySubjectId;
	private String personId;
	private String secondaryId;
	private String oid;
	private String gender;
	private Date birthDate;
	//private Date enrollmentDate;
	private Vector events; //List<SubjectEvent>
	
	/** true if the subject has just been created on the device before being saved on the server? **/
	boolean isNewSubject;
	
	
	public Subject(){
		super();
	}
	
	public Integer getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Integer subjectId) {
		this.subjectId = subjectId;
	}


	public Vector getEvents() {
		return events;
	}


	public void setEvents(Vector events) {
		this.events = events;
	}


	public String getStudySubjectId() {
		return studySubjectId;
	}


	public void setStudySubjectId(String studySubjectId) {
		this.studySubjectId = studySubjectId;
	}


	public String getPersonId() {
		return personId;
	}


	public void setPersonId(String personId) {
		this.personId = personId;
	}


	public String getSecondaryId() {
		return secondaryId;
	}


	public void setSecondaryId(String secondaryId) {
		this.secondaryId = secondaryId;
	}


	public String getGender() {
		return gender;
	}


	public void setGender(String gender) {
		this.gender = gender;
	}


	public Date getBirthDate() {
		return birthDate;
	}


	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public boolean isNewSubject() {
		return isNewSubject;
	}

	public void setNewSubject(boolean isNewSubject) {
		this.isNewSubject = isNewSubject;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	/**
	 * Gets the subject whole name which is a concatenation of the
	 * given, middle and family names.
	 * 
	 * @return
	 */
	public String getName(){
		String s="";

		if(getPersonId() != null && getPersonId().length() != 0)
			s += " " + getPersonId();
		
		if(getStudySubjectId() != null && getStudySubjectId().length() != 0)
			s += " " + getStudySubjectId();

		if(getSecondaryId() != null && getSecondaryId().length() != 0)
			s += " " + getSecondaryId();

		return s;
	}
	
	public String toString() {
		String s = "";

		if(getPersonId() != null && getPersonId().length() != 0)
			s += " " + getPersonId();
		
		if(getStudySubjectId() != null && getStudySubjectId().length() != 0)
			s += " " + getStudySubjectId();

		/*if(getSecondaryId() != null && getSecondaryId().length() != 0)
			s += " " + getSecondaryId();*/

		if(isNewSubject())
			s += " (NEW)";

		if(s == null)
			s = "NAMELESS SubjectId="+getSubjectId();
		return s;
	}

	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		
		subjectId = PersistentHelper.readInteger(dis);
		
		studySubjectId = PersistentHelper.readUTF(dis);
		personId = PersistentHelper.readUTF(dis);
		secondaryId = PersistentHelper.readUTF(dis);
		oid = PersistentHelper.readUTF(dis);
		gender = PersistentHelper.readUTF(dis);
		birthDate = PersistentHelper.readDate(dis);
		
		isNewSubject = dis.readBoolean();
		
		//enrollmentDate = new Date(dis.readLong());
		
		events = new Vector();
		int size = dis.readByte();
		for(int index = 0; index < size; index++){
			SubjectEvent event = new SubjectEvent();
			event.read(dis);
			events.addElement(event);
		}
	}

	public void write(DataOutputStream dos) throws IOException {
		
		PersistentHelper.writeInteger(dos, subjectId);
		
		PersistentHelper.writeUTF(dos,studySubjectId);
		PersistentHelper.writeUTF(dos,personId);
		PersistentHelper.writeUTF(dos,secondaryId);
		PersistentHelper.writeUTF(dos,oid);
		PersistentHelper.writeUTF(dos,gender);
		PersistentHelper.writeDate(dos,birthDate);
		
		dos.writeBoolean(isNewSubject);
		
		//dos.writeLong(enrollmentDate.getTime());
		
		if(events == null)
			dos.writeByte(0);
		else{
			int size = events.size();
			dos.writeByte(size);
			for(int index = 0; index < size; index++)
				((SubjectEvent)events.elementAt(index)).write(dos);
		}
	}
}
