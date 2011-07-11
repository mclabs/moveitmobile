package org.openxdata.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.db.util.Persistent;

/**
 * Contains the header of a connection response.
 * 
 * @author Daniel
 *
 */
public class ResponseHeader implements Persistent{

	/** Problems occured during execution of the request. */
	public static final byte STATUS_ERROR = 0;
	
	/** Request completed successfully. */
	public static final byte STATUS_SUCCESS = 1;
	
	/** User login failed */
	public static final byte STATUS_ACCESS_DENIED = 2;
	
	/** Not permitted to carry out the requested operation. */
	public static final byte STATUS_PERMISSION_DENIED = 3;
	
	private byte status = STATUS_ERROR;
	
	
	public ResponseHeader(){
		
	}
	
	public ResponseHeader(byte status){
		setStatus(status);
	}
	
	/**
	 * @see org.openxdata.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException {
		setStatus(dis.readByte());
	}

	/**
	 * @see org.openxdata.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(getStatus());
	}

	public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}
	
	public boolean isSuccess(){
		return getStatus() == STATUS_SUCCESS;
	}
}
