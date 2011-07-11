package org.openxdata.purcai;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.db.util.Persistent;

public class MarkSheetHeader implements Persistent{

	private int classStrmId;
	private int subjectPaperId;
	private int testTypeId;
	private byte outOf;

	public MarkSheetHeader(){
		super();
	}

	public MarkSheetHeader(int classStrmId, int subjectPaperId, int testTypeId){
		setClassStrmId(classStrmId);
		setSubjectPaperId(subjectPaperId);
		setTestTypeId(testTypeId);
	}

	public MarkSheetHeader(MarkSheetHeader header){
		setClassStrmId(header.getClassStrmId());
		setSubjectPaperId(header.getSubjectPaperId());
		setTestTypeId(header.getTestTypeId());
		setOutOf(header.getOutOf());
	}

	public MarkSheetHeader(int classStrmId, int subjectPaperId, int testTypeId,byte outOf){
		setClassStrmId(classStrmId);
		setSubjectPaperId(subjectPaperId);
		setTestTypeId(testTypeId);
		setOutOf(outOf);
	}

	public int getClassStrmId() {
		return classStrmId;
	}
	public void setClassStrmId(int classStrmId) {
		this.classStrmId = classStrmId;
	}
	public int getSubjectPaperId() {
		return subjectPaperId;
	}
	public void setSubjectPaperId(int subjectPaperId) {
		this.subjectPaperId = subjectPaperId;
	}
	public int getTestTypeId() {
		return testTypeId;
	}
	public void setTestTypeId(int testTypeId) {
		this.testTypeId = testTypeId;
	} 

	public byte getOutOf() {
		return outOf;
	}

	public void setOutOf(byte outOf) {
		this.outOf = outOf;
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setClassStrmId(dis.readInt());
		setSubjectPaperId(dis.readInt());
		setTestTypeId(dis.readInt());
		setOutOf(dis.readByte());
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(getClassStrmId());
		dos.writeInt(getSubjectPaperId());
		dos.writeInt(getTestTypeId());
		dos.writeByte(getOutOf());
	}
}
