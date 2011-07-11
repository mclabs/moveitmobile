package org.openxdata.db.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;



/**
 * Helper class to write and read collection to and from streams. This class also
 * writes the built in types taking care of nulls if any.
 * 
 * @author Daniel Kayiwa
 *
 */
public class PersistentHelper {
	
	/**
	 * Writes a string to the stream.
	 * 
	 * @param dos - the stream for writing.
	 * @param data - the string to write.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeUTF(DataOutputStream dos,String data) throws IOException{
		if(data != null){
			dos.writeBoolean(true);
			dos.writeUTF(data);
		}
		else
			dos.writeBoolean(false);
	}
	
	/**
	 * Writes an Integer to the stream.
	 * 
	 * @param dos - the stream for writing.
	 * @param data - the Interger to write.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeInteger(DataOutputStream dos,Integer data) throws IOException{
		if(data != null){
			dos.writeBoolean(true);
			dos.writeInt(data.intValue());
		}
		else
			dos.writeBoolean(false);
	} 
	
	/**
	 * Writes a Date to a stream.
	 * 
	 * @param dos - the stream to write to.
	 * @param data - the Date to write.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeDate(DataOutputStream dos,Date data) throws IOException{
		if(data != null){
			dos.writeBoolean(true);
			dos.writeLong(data.getTime());
		}
		else
			dos.writeBoolean(false);
	} 
	
	/**
	 * Writes a boolean to a stream.
	 * 
	 * @param dos - the stream to write to.
	 * @param data - the boolean to write.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeBoolean(DataOutputStream dos,Boolean data) throws IOException{
		if(data != null){
			dos.writeBoolean(true);
			dos.writeBoolean(data.booleanValue());
		}
		else
			dos.writeBoolean(false);
	} 
	
	/**
	 * Reads a string from a stream.
	 * 
	 * @param dis - the stream to read from.
	 * @return - the read string or null if none.
	 * @throws IOException - thrown when a problem occurs during the reading from stream.
	 */
	public static String readUTF(DataInputStream dis) throws IOException {
		if(dis.readBoolean())
			return dis.readUTF();
		return null;
	}
	
	/**
	 * Reads an Integer from a stream.
	 * 
	 * @param dis - the stream to read from.
	 * @return - the read Integer or null of none.
	 * @throws IOException - thrown when a problem occurs during the reading from stream.
	 */
	public static Integer readInteger(DataInputStream dis) throws IOException {
		if(dis.readBoolean())
			return new Integer(dis.readInt());
		return null;
	}
	
	/**
	 * Reads a Date from a stream.
	 * 
	 * @param dis - the stream to read from.
	 * @return - the read Date or null if none.
	 * @throws IOException - thrown when a problem occurs during the reading from stream.
	 */
	public static Date readDate(DataInputStream dis) throws IOException {
		if(dis.readBoolean())
			return new Date(dis.readLong());
		return null;
	}
	
	/**
	 * Reads a boolean from a stream.
	 * 
	 * @param dis - the stream to read from.
	 * @return - the read boolean or null if none.
	 * @throws IOException - thrown when a problem occurs during the reading from stream.
	 */
	public static Boolean readBoolean(DataInputStream dis) throws IOException {
		if(dis.readBoolean())
			return new Boolean(dis.readBoolean());
		return null;
	}
	
	/**
	 * Writes a small vector (byte size) of Persistent objects to a stream.
	 * 
	 * @param persistentVector - the vector of persistent objects.
	 * @param dos - the stream to write to.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void write(Vector persistentVector, DataOutputStream dos) throws IOException {	
		if(persistentVector != null){
			dos.writeByte(persistentVector.size());
			for(int i=0; i<persistentVector.size(); i++ ){
				((Persistent)persistentVector.elementAt(i)).write(dos);
			}
		}
		else
			dos.writeByte(0);
	}
	
	/**
	 * Writes a big vector (of int size) of persistent objects from a stream.
	 * 
	 * @param persistentVector
	 * @param dos
	 * @throws IOException
	 */
	public static void writeBig(Vector persistentVector, DataOutputStream dos) throws IOException {	
		if(persistentVector != null){
			dos.writeInt(persistentVector.size());
			for(int i=0; i<persistentVector.size(); i++ ){
				((Persistent)persistentVector.elementAt(i)).write(dos);
			}
		}
		else
			dos.writeInt(0);
	}
	
	public static void write(Vector persistentVector, DataOutputStream dos, int len) throws IOException {	
		if(persistentVector != null){
			dos.writeInt(persistentVector.size());
			for(int i=0; i<persistentVector.size(); i++ ){
				((Persistent)persistentVector.elementAt(i)).write(dos);
			}
		}
		else
			dos.writeInt(0);
	}
	
	public static void writeIntegers(Vector intVector, DataOutputStream dos) throws IOException {	
		if(intVector != null){
			dos.writeByte(intVector.size());
			for(int i=0; i<intVector.size(); i++ )
				dos.writeInt(((Integer)intVector.elementAt(i)).intValue());
		}
		else
			dos.writeByte(0);
	}
	
	/**
	 * Writes a list of bytes a stream.
	 * 
	 * @param byteVector - the Byte vector.
	 * @param dos  - the stream.
	 * @throws IOException
	 */
	public static void writeBytes(Vector byteVector, DataOutputStream dos) throws IOException {	
		if(byteVector != null){
			dos.writeByte(byteVector.size());
			for(int i=0; i<byteVector.size(); i++ )
				dos.writeByte(((Byte)byteVector.elementAt(i)).byteValue());
		}
		else
			dos.writeByte(0);
	}
	
	/**
	 * Reads a small vector (byte size) of persistent objects of a certain class from a stream.
	 * 
	 * @param dis - the stream to read from.
	 * @param cls - the class of the persistent objects contained in the vector.
	 * @return - the Vector of persistent objets or null if none.
	 * @throws IOException - thrown when a problem occurs during the reading from stream.
	 * @throws InstantiationException - thrown when a problem occurs during the peristent object creation.
	 * @throws IllegalAccessException - thrown when a problem occurs when setting values of the persistent object.
	 */
	public static Vector read(DataInputStream dis, Class cls) throws IOException, InstantiationException,IllegalAccessException {
		
		byte len = dis.readByte();
		
		/*if(len == 0)
			return null;*/

		Vector persistentVector = new Vector(len);
		
		for(byte i=0; i<len; i++ ){
			Object obj = (Persistent)cls.newInstance();
			((Persistent)obj).read(dis);
			persistentVector.addElement(obj);
		}
		
		return persistentVector;
	}
	
	/**
	 * Reads a big vector (with int size) of a persistent class from stream.
	 * 
	 * @param dis
	 * @param cls
	 * @return
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static Vector readBig(DataInputStream dis, Class cls) throws IOException, InstantiationException,IllegalAccessException {
		
		int len = dis.readInt();
		/*if(len == 0)
			return null;*/

		Vector persistentVector = new Vector(len);
		
		for(int i=0; i<len; i++ ){
			Object obj = (Persistent)cls.newInstance();
			((Persistent)obj).read(dis);
			persistentVector.addElement(obj);
		}
		
		return persistentVector;
	}
	
	public static Vector read(DataInputStream dis, Class cls, int len) throws IOException, InstantiationException,IllegalAccessException {
		
		/*if(len == 0)
			return null;*/

		Vector persistentVector = new Vector(len);
		
		for(int i=0; i<len; i++ ){
			Object obj = (Persistent)cls.newInstance();
			((Persistent)obj).read(dis);
			persistentVector.addElement(obj);
		}
		
		return persistentVector;
	}
	
	public static Vector readIntegers(DataInputStream dis) throws IOException, InstantiationException,IllegalAccessException {
		
		byte len = dis.readByte();
		/*if(len == 0)
			return null;*/
		
		Vector intVector = new Vector(len);
		
		for(byte i=0; i<len; i++ )
			intVector.addElement(new Integer(dis.readInt()));
		
		return intVector;
	}
	
	/**
	 * Reads a list of bytes from the stream.
	 * 
	 * @param dis
	 * @return
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static Vector readBytes(DataInputStream dis) throws IOException, InstantiationException,IllegalAccessException {
		
		byte len = dis.readByte();
		/*if(len == 0)
			return null;*/
		
		Vector byteVector = new Vector(len);
		
		for(byte i=0; i<len; i++ )
			byteVector.addElement(new Byte(dis.readByte()));
		
		return byteVector;
	}
	
	/**
	 * Write a hashtable of string keys and values to a stream.
	 * 
	 * @param stringHashtable - a hashtable of string keys and values.
	 * @param dos - that stream to write to.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void write(Hashtable stringHashtable, DataOutputStream dos) throws IOException {	
		if(stringHashtable != null){
			dos.writeByte(stringHashtable.size());
			Enumeration keys = stringHashtable.keys();
			String key;
			while(keys.hasMoreElements()){
				key  = (String)keys.nextElement();
				dos.writeUTF(key);
				dos.writeUTF((String)stringHashtable.get(key));
			}
		}
		else
			dos.writeByte(0);
	}
	
	/**
	 * Reads a hashtable of string keys and values from a stream.
	 * 
	 * @param dis - the stream to read from.
	 * @return - the hashtable of string keys and values or null if none.
	 * @throws IOException - thrown when a problem occurs during the reading from stream.
	*/
	public static Hashtable read(DataInputStream dis) throws IOException {
		
		byte len = dis.readByte();
		/*if(len == 0)
			return null;*/
		
		Hashtable stringHashtable = new Hashtable();

		for(byte i=0; i<len; i++ )
			stringHashtable.put(dis.readUTF(), dis.readUTF());
		
		return stringHashtable;
	}
}
