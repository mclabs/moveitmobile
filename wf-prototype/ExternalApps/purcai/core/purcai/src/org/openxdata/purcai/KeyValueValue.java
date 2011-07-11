package org.openxdata.purcai;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.db.util.Persistent;


/**
 * Encapsulates id to id mappings and associates them with a key.
 * 
 * @author Daniel
 *
 */
public class KeyValueValue implements Persistent{
	private int id;
	private int id1;
	private int id2;

	public KeyValueValue(){

	}

	public KeyValueValue(int id,int id1, int id2) {
		super();
		this.id = id;
		this.id1 = id1;
		this.id2 = id2;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getId1() {
		return id1;
	}
	public void setId1(int id1) {
		this.id1 = id1;
	}
	public int getId2() {
		return id2;
	}
	public void setId2(int id2) {
		this.id2 = id2;
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setId(dis.readInt());
		setId1(dis.readInt());
		setId2(dis.readInt());
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(getId());
		dos.writeInt(getId1());
		dos.writeInt(getId2());
	}

	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + id;
		result = PRIME * result + id1;
		result = PRIME * result + id2;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final KeyValueValue other = (KeyValueValue) obj;
		if (id != other.id)
			return false;
		if (id1 != other.id1)
			return false;
		if (id2 != other.id2)
			return false;
		return true;
	}
}
