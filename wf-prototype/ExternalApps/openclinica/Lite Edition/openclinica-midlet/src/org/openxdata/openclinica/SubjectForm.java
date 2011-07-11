package org.openxdata.openclinica;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.db.util.AbstractRecord;


/**
 * This class provides a mapping between a subject record
 * and forms collected for a subject. As for the current implementation,
 * a subject can have a maximum of one record for each type of form
 * as identified by the formDefId and for a particular event.
 * 
 * @author Daniel
 *
 */
public class SubjectForm extends AbstractRecord{
	private Integer subjectId;
	private int formRecordId;
	private int eventId;
	
	//private String subjectGuid; //This is because new subjects have not subjectIds yet.

	public SubjectForm(){
		super();
	}
	
	public SubjectForm(Integer subjectId, int formRecordId, int eventId) {
		this();
		setSubjectId(subjectId);
		setFormRecordId(formRecordId);
		setEventId(eventId);
	}
	
	public Integer getSubjectId() {
		return subjectId;
	}
	public void setSubjectId(Integer subjectId) {
		this.subjectId = subjectId;
	}
	public int getFormRecordId() {
		return formRecordId;
	}
	public void setFormRecordId(int formRecordId) {
		this.formRecordId = formRecordId;
	}
	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setSubjectId(new Integer(dis.readInt()));
		setFormRecordId(dis.readInt());
		setEventId(dis.readInt());
	}

	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(getSubjectId().intValue());
		dos.writeInt(getFormRecordId());
		dos.writeInt(getEventId());
	}
}
