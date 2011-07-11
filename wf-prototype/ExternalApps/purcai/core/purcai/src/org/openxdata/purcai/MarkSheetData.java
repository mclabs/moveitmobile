package org.openxdata.purcai;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;


/**
 * Mark sheet as sent from mobile device to server.
 * This is different from the one received from the server in that 
 * it does not have the student names, as a way of reducing payload. 
 * 
 * @author daniel
 *
 */
public class MarkSheetData implements Persistent{
	private Vector studentMarks;
	private MarkSheetHeader header;

	public MarkSheetData(){

	}

	public MarkSheetData(MarkSheetHeader header,Vector studentMarks){
		setHeader(header);
		setStudentMarks(studentMarks);
	}

	public MarkSheetData(MarkSheet markSheet){
		setHeader(new MarkSheetHeader(markSheet.getHeader()));

		Vector markSheetMarks = markSheet.getStudentMarks();
		Vector marks = new Vector();
		for(int i=0; i<markSheetMarks.size(); i++)
			marks.addElement(new StudentIdMark((StudentIdNameMark)markSheetMarks.elementAt(i)));

		setStudentMarks(marks);
	}

	public Vector getStudentMarks() {
		return studentMarks;
	}

	public void setStudentMarks(Vector studentMarks) {
		this.studentMarks = studentMarks;
	}

	public MarkSheetHeader getHeader() {
		return header;
	}

	public void setHeader(MarkSheetHeader header) {
		this.header = header;
	}

	public void clearMarks(){
		for(int i=0; i<studentMarks.size(); i++)
			((StudentIdMark)studentMarks.elementAt(i)).clearMark();

		header.setOutOf(StudentIdMark.NULL_MARK);
	}

	/*public StudentIdMark getStudentMark(int i){
		return (StudentIdMark)studentMarks.elementAt(i);
	}*/

	/**
	 * @see org.fcitmuk.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		header = new MarkSheetHeader();
		header.read(dis);
		setStudentMarks(PersistentHelper.readBig(dis, new StudentIdMark().getClass()));
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		header.write(dos);
		PersistentHelper.writeBig(getStudentMarks(), dos);
	}
}
