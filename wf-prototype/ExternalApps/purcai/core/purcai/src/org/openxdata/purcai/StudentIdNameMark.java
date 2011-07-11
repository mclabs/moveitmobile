package org.openxdata.purcai;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StudentIdNameMark extends StudentIdMark{

	private String name;
	

	public StudentIdNameMark(){
		super();
	}
	
	public StudentIdNameMark(int studentId, String name, byte mark){
		super(studentId,mark);
		setName(name);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {		
		return getName() + super.toString();
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		super.read(dis);
		setName(dis.readUTF());
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		super.write(dos);
		dos.writeUTF(getName());
	}
}

