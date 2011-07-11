package org.openxdata.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.openxdata.db.util.Persistent;


/**
 * 
 * @author daniel
 *
 */
public class MenuTextList  implements Persistent{
	
	private Hashtable hashtable = new Hashtable();
	
	public int size(){
		return hashtable.size();
	}
	
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		hashtable = new Hashtable();
		
		short len = dis.readShort();
		if(len == 0)
			return;
		
		for(short i=0; i<len; i++ )
			hashtable.put(new Short(dis.readShort()), dis.readUTF());
	}

	public void write(DataOutputStream dos) throws IOException {
		if(hashtable != null){
			dos.writeShort(hashtable.size());
			Enumeration keys = hashtable.keys();
			Short key;
			while(keys.hasMoreElements()){
				key  = (Short)keys.nextElement();
				dos.writeShort(key.shortValue());
				dos.writeUTF((String)hashtable.get(key));
			}
		}
		else
			dos.writeShort(0);
	}
	
	public String getText(Short key){
		return (String)hashtable.get(key);
	}
}
