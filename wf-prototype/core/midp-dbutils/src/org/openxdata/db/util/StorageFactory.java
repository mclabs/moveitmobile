package org.openxdata.db.util;

import javax.microedition.rms.RecordStore;


/**
 * Factory for creating data storage instance.
 * 
 * @author Daniel Kayiwa
 *
 */
public class StorageFactory {
	
	/** No external creation allowed. */
	private StorageFactory(){
		
	}
	
	/**
	 * Gets an instance of the storage to use. This could be RMS, FileStorage, 
	 * Local Database, or even a live connection to the server. But the caller
	 * is shielded from the implementation details.
	 * 
	 * @param name - the name of storage.
	 * @param eventListener - listener to storage events.
	 * @return - the storage instance.
	 */
	public static Storage getStorage(String name,StorageListener eventListener){
		/*if(storage == null)
			storage = new RMSStorage(name,eventListener);
		return storage;*/
		
		return new RMSStorage(name,eventListener);
	}
	
	
	/**
	 *  Returns all the record store names for this midlet suite.
	 *  
	 * @return a string array of the stores owned by this midlet suite.
	 */
	public static String[] getNames() {
		return RecordStore.listRecordStores();
	}
}
