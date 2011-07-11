package org.openxdata.openclinica;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;


/**
 * 
 * @author daniel
 *
 */
public class StudyEvent implements Persistent {

	private int eventId;

	/** The name of the event. */
	private String name;
	
	private String oid;

	/** The list of crfs in the event. */
	private Vector crfs; //CrfDef


	public StudyEvent(){

	}

	public int getEventId() {
		return eventId;
	}


	public void setEventId(int eventId) {
		this.eventId = eventId;
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


	public Vector getCrfs() {
		return crfs;
	}


	public void setCrfs(Vector crfs) {
		this.crfs = crfs;
	}


	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(eventId);
		dos.writeUTF(oid);
		dos.writeUTF(name);

		if(crfs == null)
			dos.writeByte(0);
		else{
			int size = crfs.size();
			dos.writeByte(size);
			for(int index = 0; index < size; index++)
				((CrfDef)crfs.elementAt(index)).write(dos);
		}
	}


	public void read(DataInputStream dis) throws IOException {
		eventId = dis.readInt();
		oid = dis.readUTF();
		name = dis.readUTF();

		crfs = new Vector();
		int size = dis.readByte();
		for(int index = 0; index < size; index++){
			CrfDef crf = new CrfDef();
			crf.read(dis);
			crfs.addElement(crf);
		}
	}
}
