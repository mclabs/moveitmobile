package org.openxdata.db.util;

import java.util.Vector;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreNotOpenException;

//TODO Exceptions in this class should be propagated to the user in some way.

/**
 * Handles storage and retrieval of objects of various types to RMS.
 * As for now, a data storage of a given name corresponds to a table
 * in the database world. The table fields would be the object fields.
 * 
 * @author Daniel Kayiwa
 *
 */
public class RMSStorage implements Storage{

	/** A reference to the RecordStore. */
	private RecordStore recStore;

	/** The name of this storage. 
	 * This name should be unique for this type of objects throught the midlet.
	 * For now, this is the name of the package and class of object type stored.
	 */
	private String name;

	/** Reference to the event listener. */
	private StorageListener eventListener;

	/** Flag to keep track of whether data store is open or closed. */
	//private boolean open;

	/** 
	 * Constructs a data store with a given name. 
	 * 
	 * @param name - the name of this data store.
	 * @param eventListener - the event listener.
	 */
	public RMSStorage(String name,StorageListener eventListener){
		this.name = name;
		this.eventListener = eventListener;
	}

	/*public boolean isOpen() {
		return open;
	}*/

	/*private void setOpen(boolean open) {
		this.open = open;
	}*/

	/**
	 * Opens the data storage.
	 * 
	 * @return - true if successfully opened, else false.
	 */
	private boolean open(){
		try{
			recStore = RecordStore.openRecordStore(name, true);
			//setOpen(true);
			return true;
		}
		catch(Exception e){
			eventListener.errorOccured("Exception: ", e);
		}

		return false;
	}

	/**
	 * Closes the data storage.
	 * 
	 * @return - true if successfully closed, else false.
	 */
	private boolean close(){
		try{
			recStore.closeRecordStore();
			//setOpen(false);
			return true;
		}
		catch(Exception e){
			eventListener.errorOccured("Exception: ", e);
		}

		return false;
	}

	public int getNumRecords() {
		if (open()){
			try {
				return recStore.getNumRecords();
			} catch (RecordStoreNotOpenException e) {
				eventListener.errorOccured("Exception: ", e);
			} finally{
				close();
			}
		}

		return 0;
	}

	/** Deletes all records from the data store. */
	public boolean delete(){
		try{
			RecordStore.deleteRecordStore(name);
			return true;
		}
		catch(Exception e){ //TODO dont we need to propage this back to the user?
		}
		return false;
	}

	/**
	 * Deletes a record from the data store.
	 * 
	 * @param recId - the numeric identifier of the record to be deleted.
	 */
	public boolean delete(int recId){
		boolean ret = false;

		try{
			open();
			this.recStore.deleteRecord(recId);
			ret = true;
		}
		catch(Exception e){
			eventListener.errorOccured("Exception: ", e);
		}
		finally{
			close();
		}

		return ret;
	}

	/** 
	 * Reads a list of objects of a given class from persistent storage.
	 * 
	 * @param cls - the class of the object to be retrieved.
	 * @return - the list of objects retrieved.
	 */
	public Vector read(Class cls){
		try{

			Vector list = null;
			if(open()){	
				if(recStore.getNumRecords() > 0)
					list = new Vector();

				RecordEnumeration recEnum = recStore.enumerateRecords(null, null, true);
				while(recEnum.hasNextElement()){
					int id = recEnum.nextRecordId();
					Object obj = Serializer.deserialize(recStore.getRecord(id),cls);
					if(obj instanceof Record)
						((Record)obj).setRecordId(id);
					list.addElement(obj);
				}
			}

			return list;
		}
		catch(Exception e){
			eventListener.errorOccured("Exception: ", e);
		}
		finally{
			close();
		}

		return null;
	}

	/** 
	 * Reads an object from persistent store 
	 * using its numeric unique identifier and class.
	 * 
	 * @param id - the unique identifier of the object.
	 * @param cls - the class of the object.
	 * @return
	 */
	public Object read(int id,Class cls){
		try{
			open();
			Object obj = Serializer.deserialize(recStore.getRecord(id), cls);
			if(obj instanceof Record)
				((Record)obj).setRecordId(id);
			return obj;
		}
		catch(Exception e){
			eventListener.errorOccured("Exception: ", e);
		}
		finally{
			close();
		}
		return null;
	}

	/** 
	 * Saves a persistent object to storage. 
	 * A peristent object is one which implements the Persistent interface.
	 * 
	 * @param obj - the object to save.
	 * @return - the unique identifier of the saved object. 
	 * This identifier can be used to later on retrieve this particular object form persistent storage.
	 */
	public int addNew(Persistent obj){

		try{
			open();			
			byte[] record = Serializer.serialize(obj);
			return this.recStore.addRecord(record, 0, record.length);
		}
		catch(Exception e){
			eventListener.errorOccured("Exception: ", e);
		}
		finally{
			close();
		}

		return 0;
	}

	/** 
	 * Updates an existing persistent object in storage. 
	 * A peristent object is one which implements the Persistent interface.
	 * 
	 * @param id - the recordid of the record to update.
	 * @param obj - the object to save.
	 */
	public boolean update(int id, Persistent obj){
		boolean ret = false;

		try{
			open();
			byte[] record = Serializer.serialize(obj);
			this.recStore.setRecord(id,record, 0, record.length);
			ret = true;
		}
		catch(Exception e){
			eventListener.errorOccured("Exception: ", e);
		}
		finally{
			close();
		}

		return ret;
	}


	/** 
	 * Saves a list of persistent objects to storage. 
	 * A peristent object is one which implements the Persistent interface.
	 * 
	 * @param persistentObjects - the list of objects to save.
	 * @return - the unique identifiers of the saved object. 
	 * These identifiers can be used to later on retrieve these particular objects form persistent storage.
	 */
	public Vector addNew(Vector persistentObjects){

		Vector ret = new Vector();
		for(int i=0; i<persistentObjects.size(); i++)
			ret.addElement(new Integer(addNew((Persistent)persistentObjects.elementAt(i))));

		return ret;
	}

	public boolean save(Record rec){
		if(rec.isNew()){
			int id = addNew(rec);
			if(id > 0)
				rec.setRecordId(id);
			return id > 0;
		}
		else
			return update(rec.getRecordId(), rec);
	}

	public boolean delete(Record rec){
		return delete(rec.getRecordId());
	}

	public Persistent readFirst(Class cls){
		try{
			Persistent persistent = null;
			if(open()){	
				int recId = recStore.getNextRecordID();
				if(recId > 0)
					persistent = Serializer.deserialize(recStore.getRecord(recId),cls);
			}

			return persistent;
		}
		catch(Exception e){
			eventListener.errorOccured("Exception: ", e);
		}
		finally{
			close();
		}

		return null;
	}
}
