package org.openxdata.openmrs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.model.RequestHeader;


/**
 * 
 * @author daniel
 *
 */
public class PatientDownloadRequestHeader extends RequestHeader {

	public static byte FILTER_TYPE_COHORT = 1;
	public static byte FILTER_TYPE_NAME_AND_IDENTIFIER = 2;
	
	private byte filterType = FILTER_TYPE_COHORT;
	
	private int cohortId;
	private String name;
	private String identifier;

	public PatientDownloadRequestHeader(){
		super();
	}
	
	public PatientDownloadRequestHeader(byte filterType){
		super();
		this.filterType = filterType;
	}

	public int getCohortId() {
		return cohortId;
	}

	public void setCohortId(int cohortId) {
		this.cohortId = cohortId;
	}

	public byte getFilterType() {
		return filterType;
	}

	public void setFilterType(byte filterType) {
		this.filterType = filterType;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException{
		super.write(dos);
		//dos.writeByte(filterType); //Not written as the opposite end knows from action type
		
		if(filterType == FILTER_TYPE_COHORT)
			dos.writeInt(getCohortId());
		else{
			if(name == null)
				name = "";
			if(identifier == null)
				identifier = "";
			dos.writeUTF(name);
			dos.writeUTF(identifier);
			//PersistentHelper.writeUTF(dos,name);
			//PersistentHelper.writeUTF(dos,identifier);
		}
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException,InstantiationException,IllegalAccessException{
		super.read(dis);
		//setFilterType(dis.readByte()); //Not read as the opposite end knows from action type
		
		if(filterType == FILTER_TYPE_COHORT)
			setCohortId(dis.readInt());
		else{
			setName(dis.readUTF());
			setIdentifier(dis.readUTF());
			//setName(PersistentHelper.readUTF(dis));
			//setIdentifier(PersistentHelper.readUTF(dis));
		}
	}
}
