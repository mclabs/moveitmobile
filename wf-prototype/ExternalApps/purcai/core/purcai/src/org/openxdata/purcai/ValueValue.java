package org.openxdata.purcai;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.db.util.Persistent;


/**
 * Encapsulates id to id mappings
 * 
 * @author Daniel
 *
 */
public class ValueValue implements Persistent{

	private int id1;
	private int id2;

	public ValueValue(){

	}

	public ValueValue(int id1, int id2) {
		super();
		this.id1 = id1;
		this.id2 = id2;
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
		setId1(dis.readInt());
		setId2(dis.readInt());
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(getId1());
		dos.writeInt(getId2());
	}
}
