package org.openxdata.openclinica;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.db.util.Persistent;


/**
 * This class encapsulates all data about subjects. Can serve as the subject in-memory database.
 * This class is the one serialized from the subject server to the device.
 * 
 * @author Daniel
 *
 */
public class SubjectData implements Persistent{

	/** The list of subjects. */
	private SubjectList subjects = new SubjectList();

	public SubjectData(){

	}

	public SubjectData(SubjectList subjects) {
		super();
		this.subjects = subjects;
	}

	public SubjectList getSubjects() {
		return subjects;
	}

	public void setSubjects(SubjectList subjects) {
		this.subjects = subjects;
	}

	/** 
	 * Reads the subject data object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		subjects.read(dis);
	}

	/** 
	 * Writes the subject data object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		subjects.write(dos);
	}
}
