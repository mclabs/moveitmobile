package org.openxdata.purcai;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.db.util.Persistent;

public class StudentIdMark implements Persistent{

	//Assuming we shall not have -1 marks.
	public static final byte NULL_MARK = -1;

	private int studentId;

	//Just for optimization, am assuming marks will be less than 127.
	private byte mark;

	public StudentIdMark() {
		super();
	}

	public StudentIdMark(int studentId, byte mark) {
		this.studentId = studentId;
		this.mark = mark;
	}

	public StudentIdMark(StudentIdNameMark stidentMark) {
		this.studentId = stidentMark.getStudentId();
		this.mark = stidentMark.getMark();
	}

	public byte getMark() {
		return mark;
	}

	public void setMark(byte mark) {
		this.mark = mark;
	}

	public int getStudentId() {
		return studentId;
	}

	public void setStudentId(int studentId) {
		this.studentId = studentId;
	}

	public String getDisplayMark(){
		if(getMark() != NULL_MARK)
			return String.valueOf(getMark());
		else
			return "";
	}

	public void clearMark(){
		setMark(NULL_MARK);
	}

	public String toString() {
		String val = "";

		if(getMark() != NULL_MARK)
			val = "{" + getMark() + "} " + val;

		return val;
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setStudentId(dis.readInt());
		setMark(dis.readByte());
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(getStudentId());
		dos.writeByte(getMark());
	}
}
