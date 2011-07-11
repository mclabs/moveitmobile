package org.openxdata.db;

import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.Record;
import org.openxdata.db.util.Settings;
import org.openxdata.db.util.Storage;
import org.openxdata.db.util.StorageFactory;
import org.openxdata.db.util.StorageListener;
import org.openxdata.model.FormData;
import org.openxdata.model.LanguageList;
import org.openxdata.model.MenuTextList;
import org.openxdata.model.StudyDef;
import org.openxdata.model.StudyDefList;
import org.openxdata.model.UserList;

/**
 * Handles data storage operations for epihandy.
 * 
 * @author Daniel Kayiwa
 *
 */
public class OpenXdataDataStorage {

	/** The unique identifier for storage of studies. */
	private static final String IDENTIFIER_STUDY_LIST_STORAGE = "StudyDefList";
	
	/** The unique identifier for storage of studies. */
	private static final String IDENTIFIER_STUDY_STORAGE = "StudyDef";
	
	/** The unique identifier for storage of form definitions of a particular study. */
	//private static final String IDENTIFIER_FORM_DEF_STORAGE = "FormDef";
	
	/** The unique identifier for storage of form data of a particular study. */
	private static final String IDENTIFIER_FORM_DATA_STORAGE = "FormData";
	
	private static final String IDENTIFIER_SETTINGS_STORAGE = "Settings";
	
	private static final String IDENTIFIER_USER_STORAGE = "User";
	
	private static final String IDENTIFIER_LANGUAGES_STORAGE = "Languages";
	
	private static final String IDENTIFIER_MENU_TEXT_STORAGE = "MenuText";
	
	private static final String STORAGE_NAME_SEPARATOR = ".";
	
	public static StorageListener storageListener;
	
	
	/**
	 * Returns whether there are records related to form data.
	 * 
	 * @return true if there are form data records, false otherwise.
	 */
	public static boolean hasFormData() {
		String[] names = StorageFactory.getNames();
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			if (name.startsWith(IDENTIFIER_FORM_DATA_STORAGE)) {
				Storage store = StorageFactory
						.getStorage(name, storageListener);
				if (store.getNumRecords() > 0)
					return true;
			}
		}
		return false;
	}

	
	/**
	 * Saves form data.
	 * 
	 * @param studyId - the numeric unique identifier of the study.
	 * @param data - the data to be saved.
	 */
	public static boolean saveFormData(int studyId, FormData data){
		return StorageFactory.getStorage(getFormDataStorageName(studyId,data.getDefId()),storageListener).save((Record)data);
	}
	
	/*/**
	 * Saves form data.
	 * 
	 * @param studyId - the numeric unique identifier of the study.
	 * @param data - the data to be saved.
	 * @return - the unique numeric indentifier of the saved form.
	 */
	/*public static void updateFormData(int studyId, FormData data){
		Storage store = StorageFactory.getStorage(getFormDataStorageName(studyId,data.getDefId()),storageListener);
		store.update(data.getId(),(Persistent)data);
	}*/
	
	/**
	 * Gets data collected for a form of a particular type (definition).
	 * 
	 * @param studyId - the numeric unique identifier of the study.
	 * @param formDefId - the numeric unique identifier of the form definition
	 * @return - a list of collected data for this form.
	 */
	public static Vector getFormData(int studyId, int formDefId){
		return StorageFactory.getStorage(getFormDataStorageName(studyId,formDefId),storageListener).read(new FormData().getClass());
	}
	
	public static void deleteData(StudyDefList studyList){
		if(studyList == null || studyList.getStudies() == null)
			return;
		
		for(byte i=0; i<studyList.getStudies().size(); i++)
			deleteData(studyList.getStudy(i));
	}
	
	public static void deleteData(StudyDef studyDef){
		if(studyDef == null || studyDef.getForms() == null)
			return;
		
		for(byte i=0; i<studyDef.getForms().size(); i++)
			StorageFactory.getStorage(getFormDataStorageName(studyDef.getId(),studyDef.getFormAt(i).getId()),storageListener).delete();
	}
	
	/**
	 * Gets data collected for a form.
	 * 
	 * @param studyId - the numeric unique identifier of the study.
	 * @param formDefId - the numeric unique identifier of the form definition
	 * @param recordId - the numeric unique identifier of the form data record.
	 * @return - the form data.
	 */
	public static FormData getFormData(int studyId, int formDefId, int recordId){
		return (FormData)StorageFactory.getStorage(getFormDataStorageName(studyId,formDefId),storageListener).read(recordId, new FormData().getClass());
	}
	
	/**
	 * Deletes form data.
	 * 
	 * @param studyId - the numeric unique identifier of the study.
	 * @param data - the data to be deleted.
	 */
	public static void deleteFormData(int studyId, FormData data){
		StorageFactory.getStorage(getFormDataStorageName(studyId,data.getDefId()),storageListener).delete(data);
	}
	
	/**
	 * Saves a study. Or a list of form definitions.
	 * 
	 * @param study - the study to save.
	 * @return
	 */
	public static int saveStudy(StudyDef study){
		Storage store = StorageFactory.getStorage(getStudyStorageName(study.getId()),storageListener);
		
		//studies are stored one by one separately for performance reasons.
		//So each time we save a new one, we have to delete the existing.
		store.delete();
		return store.addNew((Persistent)study);
	}
	
	/**
	 * Loads a study from storage.
	 * 
	 * @param studyId - the unique identifier of the study to load.
	 * @return the study definition.
	 */
	public static StudyDef getStudy(int studyId){
		StudyDef study = null;
		Vector vect = StorageFactory.getStorage(getStudyStorageName(studyId),storageListener).read(new StudyDef().getClass());
		if(vect != null && vect.size() > 0)
			study = (StudyDef)vect.elementAt(0); //We can only have one per storage.
		return study;
	}
	
	/**
	 * Deletes all form definitions in a given study.
	 * 
	 * @param study the study definition object.
	 */
	public static void deleteStudy(StudyDef study){
		StorageFactory.getStorage(getStudyStorageName(study.getId()),storageListener).delete();
	}
	
	/**
	 * Saves a list of studies (as in their names and ids)
	 * 
	 * @param studyList - the study list collection.
	 * @return
	 */
	public static int saveStudyList(StudyDefList studyList){
		Storage store = StorageFactory.getStorage(IDENTIFIER_STUDY_LIST_STORAGE,storageListener);
		
		//We can only have one study list.
		//So each time we save a new one, we have to delete the existing.
		store.delete();

		return store.addNew((Persistent)studyList);
	}
	
	/** Gets the list of studies from persistent storage. */
	public static StudyDefList getStudyList(){
		StudyDefList list = null;
		Vector vect = StorageFactory.getStorage(IDENTIFIER_STUDY_LIST_STORAGE,storageListener).read(new StudyDefList().getClass());
		if(vect != null && vect.size() > 0)
			list = (StudyDefList)vect.elementAt(0); //We can only have one in the whole application.
		return list;
	}
	
	public static Settings getSettings(){
		Settings settings = null;
		Vector vect = StorageFactory.getStorage(IDENTIFIER_SETTINGS_STORAGE,storageListener).read(new Settings().getClass());
		if(vect != null && vect.size() > 0)
			settings = (Settings)vect.elementAt(0); //We can only have one in the whole application.
		return settings;
	}
	
	public static void saveUsers(UserList users){
		Storage store = StorageFactory.getStorage(IDENTIFIER_USER_STORAGE,storageListener);
		store.delete(); //only one list is allowed.
		store.addNew(users);
	}
	
	public static void saveMenuText(MenuTextList MenuTextList){
		Storage store = StorageFactory.getStorage(IDENTIFIER_MENU_TEXT_STORAGE,storageListener);
		store.delete(); //only one list is allowed.
		store.addNew(MenuTextList);
	}
	
	public static void saveLanguages(LanguageList languages){
		Storage store = StorageFactory.getStorage(IDENTIFIER_LANGUAGES_STORAGE,storageListener);
		store.delete(); //only one list is allowed.
		store.addNew(languages);
	}
	
	/**
	 * Gets a list of users.
	 * 
	 * @return - the UserList object.
	 */
	public static UserList getUsers(){
		Vector vect = StorageFactory.getStorage(IDENTIFIER_USER_STORAGE,storageListener).read(new UserList().getClass());
		if(vect != null && vect.size() > 0)
			return (UserList)vect.elementAt(0); //There can only be one record for the users list object.
		return null;
	}
	
	/**
	 * Gets a list of languages.
	 * 
	 * @return - the LanguageList object.
	 */
	public static LanguageList getLanguages(){
		Vector vect = StorageFactory.getStorage(IDENTIFIER_LANGUAGES_STORAGE,storageListener).read(new LanguageList().getClass());
		if(vect != null && vect.size() > 0)
			return (LanguageList)vect.elementAt(0); //There can only be one record for the users list object.
		return null;
	}
	
	/**
	 * Gets a list of menu text.
	 * 
	 * @return - the LanguageList object.
	 */
	public static MenuTextList getMenuText(){
		Vector vect = StorageFactory.getStorage(IDENTIFIER_MENU_TEXT_STORAGE,storageListener).read(new MenuTextList().getClass());
		if(vect != null && vect.size() > 0)
			return (MenuTextList)vect.elementAt(0); //There can only be one record for the users list object.
		return new MenuTextList();
	}
	
	/**
	 * Gets a user with a particular name and password.
	 * 
	 * @param name - the user name.
	 * @param password - the password
	 * @return - the User object.
	 */
	/*public static User getUser(String name, String password){
		UserList users = getUsers();
		User user = null;
		for(int i=0; i<users.size(); i++){
			user = users.getUser(i);
			if(user.getName().equals(name) && user.getPassword().equals(password))
				return user;
		}
		return null;
	}*/
	
	public static int saveSettings(Settings settings){
		Storage store = StorageFactory.getStorage(IDENTIFIER_SETTINGS_STORAGE,storageListener);
		
		//We can only have one study list.
		//So each time we save a new one, we have to delete the existing.
		store.delete();

		return store.addNew((Persistent)settings);
	}
	
	/** Gets the name of the storage for the study. */
	private static String getStudyStorageName(int studyId){
		return IDENTIFIER_STUDY_STORAGE + STORAGE_NAME_SEPARATOR + studyId;
	}
	
	/**
	 * Gets the name of the storage for form definitions of a particular study.
	 * For performance and common usage scenarios, form definitions are stored
	 * per study. As in each study's form definitions are stored separately from
	 * those of another study.
	 * 
	 * @param studyId - the numeric identifier of the study.
	 * @return - the storage name
	 */
	/*private static  String getFormDefStorageName(int studyId){
		return IDENTIFIER_FORM_DEF_STORAGE + STORAGE_NAME_SEPARATOR + studyId;
	}*/
	
	/**
	 * Gets the name of the storage for form data of a particular study 
	 * and of a particular form definition.
	 * For performance and common usage scenarios, form data is stored
	 * per study and per form definition. As in each study's form definition data
	 * is stored separately from that of another form definition in the same study.
	 * 
	 * @param studyId - the numeric identifier of the study where this data belongs.
	 * @param formId - the numeric identifier of the form definition that this data represents.
	 * @return - the storage name
	 */
	private static  String getFormDataStorageName(int studyId, int formId){
		return IDENTIFIER_FORM_DATA_STORAGE + STORAGE_NAME_SEPARATOR + studyId + STORAGE_NAME_SEPARATOR + formId;
	}
}
