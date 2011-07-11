package org.fcitmuk.communication.sms.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.db.util.Persistent;


/**
 * Data used by the SMS assembler.
 * 
 * @author daniel
 *
 */
public class SmsAssemblerData  implements Persistent{
	
    private byte[] smsData = null;
	private int smsTotalLen = 0;
	private int smsCurrentLen = 0;
	
	public SmsAssemblerData(){
		
	}

	public int getSmsCurrentLen() {
		return smsCurrentLen;
	}

	public void setSmsCurrentLen(int smsCurrentLen) {
		this.smsCurrentLen = smsCurrentLen;
	}

	public byte[] getSmsData() {
		return smsData;
	}

	public void setSmsData(byte[] smsData) {
		this.smsData = smsData;
	}

	public int getSmsTotalLen() {
		return smsTotalLen;
	}

	public void setSmsTotalLen(int smsTotalLen) {
		this.smsTotalLen = smsTotalLen;
	}
	
	/** 
	 * Reads the SMS data object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		if(dis != null){
			setSmsTotalLen(dis.readInt());
			setSmsCurrentLen(dis.readInt());
			
			byte[] data = new byte[getSmsCurrentLen()];
			dis.readFully(data);
			setSmsData(data);
		}
	}

	/** 
	 * Writes the SMS data object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(getSmsTotalLen());
		dos.writeByte(getSmsCurrentLen());
		dos.write(getSmsData());
	}
}
