package org.openxdata.openclinica;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.model.RequestHeader;


/**
 * 
 * @author daniel
 *
 */
public class StudyRequestHeader extends RequestHeader {

	/** The study id. */
	private int studyId;
	
	
	public StudyRequestHeader(){
		super();
	}
	
	public int getStudyId() {
		return studyId;
	}

	public void setStudyId(int studyId) {
		this.studyId = studyId;
	}

	public void write(DataOutputStream dos) throws IOException{
		super.write(dos);
		dos.writeInt(getStudyId());
	}
	
	public void read(DataInputStream dis) throws IOException,InstantiationException,IllegalAccessException{
		super.read(dis);
		setStudyId(dis.readInt());
	}
}
