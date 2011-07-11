package org.openxdata.openclinica;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;


/**
 * This class holds a collection of subjects.
 * 
 * @author Daniel
 *
 */
public class SubjectList implements Persistent{

	/** Collection of subjects. */
	private Vector subjects;

	/** Constructs a new subject collection. */
	public SubjectList(){
		super();
	}

	public SubjectList(Vector subjects){
		this();
		setSubjects(subjects);
	}

	public Vector getSubjects() {
		return subjects;
	}

	public void setSubjects(Vector subjects) {
		this.subjects = subjects;
	}

	public void addSubject(Subject subject){
		if(subjects == null)
			subjects = new Vector();
		subjects.addElement(subject);
	}

	public void addSubjects(Vector subjectList){
		if(subjectList != null){
			if(subjects == null)
				subjects = subjectList;
			else{
				for(int i=0; i<subjectList.size(); i++ )
					this.subjects.addElement(subjectList.elementAt(i));
			}
		}
	}

	public int size(){
		if(getSubjects() == null)
			return 0;
		return getSubjects().size();
	}

	public Subject getSubject(int index){
		return (Subject)getSubjects().elementAt(index);
	}

	/** 
	 * Reads the subject collection object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setSubjects(PersistentHelper.read(dis,new Subject().getClass(),dis.readInt()));
	}

	/** 
	 * Writes the subject collection object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.write(getSubjects(), dos,0);
	}
}
