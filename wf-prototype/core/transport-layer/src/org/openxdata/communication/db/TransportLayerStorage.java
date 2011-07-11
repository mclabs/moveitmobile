package org.openxdata.communication.db;



/**
 * Persistently stores data used for transport layer purposes.
 * 
 * @author daniel
 *
 */
public class TransportLayerStorage {

	/** The unique identifier for storage of studies. */
	/*private static final String IDENTIFIER_SMS_DATA_STORAGE = "SmsAssemblerData";
	
	private static TransportLayerStorage instance;
	
	
	private TransportLayerStorage(){
		
	}
	
	public static TransportLayerStorage getInstance(){
		if(instance == null)
			instance = new TransportLayerStorage();
		
		return instance;
	}
	
	/**
	 * Saves SMS assembler data.
	 * 
	 * @param smsData
	 */
	/*public void saveSmsAssemblerData(SmsAssemblerData data){
		Storage store = StorageFactory.getStorage(IDENTIFIER_SMS_DATA_STORAGE,null);
		store.delete(); //only one is allowed.
		store.addNew(data);
	}*/
	
	/**
	 * Gets previously stored sms assembler data if any.
	 * 
	 * @return - the SmsAssemblerData object.
	 */
	/*public SmsAssemblerData getSmsAssemblerData(){
		Storage store = StorageFactory.getStorage(IDENTIFIER_SMS_DATA_STORAGE,null);
		Vector vect = store.read(new SmsAssemblerData().getClass());
		if(vect != null && vect.size() > 0)
			return (SmsAssemblerData)vect.elementAt(0); //There can only be one record for the sms data object.
		return null;
	}
	
	/**
	 * Deletes SMS assembler data.
	 *
	 */
	/*public void deleteSmsAssemblerData(){
		Storage store = StorageFactory.getStorage(IDENTIFIER_SMS_DATA_STORAGE,null);
		store.delete();
	}*/
}
