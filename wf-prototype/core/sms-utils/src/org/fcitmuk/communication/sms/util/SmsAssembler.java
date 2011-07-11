package org.fcitmuk.communication.sms.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Hashtable;



/**
 * Assembles concatenated binary SMS's into one byte stream.
 * 
 * @author daniel
 *
 */
public class SmsAssembler {

	Hashtable assemblerDatas;
	
	public SmsAssembler(){
		assemblerDatas = new Hashtable();
	}
	
	/**
	 * Reads the first four bytes of the first SMS to tell the size of the entire SMS set.
	 * 
	 * @param payload the bytes in the first (in set) incoming sms.
	 * @return the total number of bytes in all the concatenated sms es.
	 */
	private static int getSize(byte[] payload){
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(payload));
		try{
			return dis.readInt();
		}catch(IOException e){return -1;}
	}
	
	/**
	 * Assembles concatenated SMS es into one byte stream.
	 * 
	 * @param senderAddress the phone number sending the sms to assemble.
	 * @param payload the byte stream in the incoming sms.
	 * @return true if the entire message set has been sent, else false.
	 */
	public boolean assembleMessage(String senderAddress, byte[] payload){
		SmsAssemblerData data = (SmsAssemblerData)assemblerDatas.get(senderAddress);
		if(data == null){
			data = new SmsAssemblerData();
			assemblerDatas.put(senderAddress, data);
		}
		
		return assembleMessage(data,payload);
	}
	
	/**
	 * Assembles concatenated SMS es into one byte stream.
	 * 
	 * @param assemblerData previously stored assembler data if any.
	 * @param payload the byte stream in the incoming sms.
	 * @return true if the entire message set has been sent, else false.
	 */
	public static boolean assembleMessage(SmsAssemblerData assemblerData, byte[] payload){
		byte[] smsdata = assemblerData.getSmsData();
		int smsTotalLen = assemblerData.getSmsTotalLen();
		int smsCurrentLen = assemblerData.getSmsCurrentLen();

		if(smsdata == null){ //first send, this will be null
			smsTotalLen = getSize(payload); //read off first four bytes for total length.
			System.out.println("smsTotalLen="+smsTotalLen);
			smsdata = new byte[smsTotalLen]; //create storage space.
			System.out.println("payload.length="+payload.length);
			smsCurrentLen = payload.length - 4; //actual data is minus the four length bytes.
			System.arraycopy(payload,4,smsdata,0,smsCurrentLen);
			
			assemblerData.setSmsTotalLen(smsTotalLen);
		}
		else{ //this should be a send after the first one
			System.arraycopy(payload,0,smsdata,smsCurrentLen,payload.length);
			smsCurrentLen = (smsCurrentLen + payload.length);
		}	
		
		assemblerData.setSmsData(smsdata);
		assemblerData.setSmsCurrentLen(smsCurrentLen);
				
		if(smsCurrentLen == smsTotalLen){ //if all bytes have been read.
			//DataInputStream dis = new DataInputStream(new ByteArrayInputStream(smsdata));
			//dis = this.getDecompressedStream(dis);
			
			/*assemblerData.setSmsData(null);
			assemblerData.setSmsTotalLen(0);
			assemblerData.setSmsCurrentLen(0);*/
			
			return true; //tell caller that all data is processed
		}
		else
			System.out.println("Received " + smsCurrentLen + " of " + smsTotalLen + " bytes. " + (smsTotalLen-smsCurrentLen) + " left.");

		return false; //tell caller that not all data is processed
	}
	
	/**
	 * Gets the entire byte stream for the concatenated (or not) sms es.
	 * 
	 * @return the byte stream.
	 */
	public byte[] getPayloadData(String senderAddress){
		SmsAssemblerData data = ((SmsAssemblerData)assemblerDatas.get(senderAddress));
		assemblerDatas.remove(data);
		return data.getSmsData();
	}
}
