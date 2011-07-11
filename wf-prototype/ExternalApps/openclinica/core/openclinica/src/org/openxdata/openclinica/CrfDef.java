package org.openxdata.openclinica;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.db.util.Persistent;


public class CrfDef implements Persistent {

	private int crfId;
	private String oid;
	private String name;
	
	
	public CrfDef(){
		
	}

	public int getCrfId() {
		return crfId;
	}

	public void setCrfId(int crfId) {
		this.crfId = crfId;
	}
	
	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(crfId);
		dos.writeUTF(oid);
		dos.writeUTF(name);
	}
	
	public void read(DataInputStream dis) throws IOException {
		crfId = dis.readInt();
		oid = dis.readUTF();
		name = dis.readUTF();
	}
}
