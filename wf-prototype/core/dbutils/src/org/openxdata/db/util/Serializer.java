package org.openxdata.db.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


/**
 * 
 * Converts a persistent object to an array of bytes and vice vasa.
 * 
 * @author Daniel Kayiwa
 *
 */
public class Serializer {

	/**
	 * Converts a peristent object to an array of bytes.
	 * 
	 * @param perst the persistent object
	 * @return an array of bytes
	 * @throws IOException thrown when a problem occurs during the conversion.
	 */
	public static byte[] serialize(Persistent perst) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		perst.write(dos);
		return baos.toByteArray();
	}

	/**
	 * Converts an array of bytes to a persistent object of a given concrete class.
	 * 
	 * @param data the byte array.
	 * @param cls the concrete class implementing the persistent interface.
	 * @return
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static Persistent deserialize(byte[] data, Class cls) throws IOException,IllegalAccessException,InstantiationException {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
		Persistent perst = (Persistent) cls.newInstance();
		perst.read(dis);
		return perst;
	}
}
