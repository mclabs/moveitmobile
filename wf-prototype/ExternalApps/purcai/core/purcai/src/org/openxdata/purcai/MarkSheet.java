package org.openxdata.purcai;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.PersistentHelper;


/**
 * Mark sheet as sent from server to mobile device.
 * This is different from the one sent back to server in that it
 * also has the student name. 
 * 
 * @author daniel
 *
 */
public class MarkSheet extends MarkSheetData{

	public MarkSheet(){
		super();
	}

	public MarkSheet(MarkSheetHeader header,Vector studentMarks){
		super(header,studentMarks);
	}

	public void clearMarks(){
		for(int i=0; i<getStudentMarks().size(); i++)
			((StudentIdNameMark)getStudentMarks().elementAt(i)).clearMark();

		getHeader().setOutOf(StudentIdNameMark.NULL_MARK);
	}

	public StudentIdNameMark getStudentMark(int i){
		return (StudentIdNameMark)getStudentMarks().elementAt(i);
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setHeader(new MarkSheetHeader());
		getHeader().read(dis);
		setStudentMarks(PersistentHelper.readBig(dis, new StudentIdNameMark().getClass()));
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		getHeader().write(dos);
		PersistentHelper.writeBig(getStudentMarks(), dos);
	}

	public boolean hasMarks(){
		return getHeader().getOutOf() != StudentIdMark.NULL_MARK;
	}
}
