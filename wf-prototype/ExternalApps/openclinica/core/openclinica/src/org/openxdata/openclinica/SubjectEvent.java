package org.openxdata.openclinica;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.db.util.Persistent;


/**
 * 
 * @author daniel
 *
 */
public class SubjectEvent implements Persistent {

	/** The study event identifier. */
	private int eventId;
	
	/** The location where this event is scheduled for the subject. */
	private String location;
	
	
	public SubjectEvent(){
		
	}
	
	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}


	public String getLocation() {
		return location;
	}


	public void setLocation(String location) {
		this.location = location;
	}


	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(eventId);
		dos.writeUTF(location);
	}
	
	
	public void read(DataInputStream dis) throws IOException {
		eventId = dis.readInt();
		location = dis.readUTF();
	}
}
