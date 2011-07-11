package org.openxdata.openclinica.db;

import java.util.Vector;

import org.openxdata.db.util.Record;
import org.openxdata.db.util.Storage;
import org.openxdata.db.util.StorageFactory;
import org.openxdata.db.util.StorageListener;
import org.openxdata.model.FormDef;
import org.openxdata.model.OpenXdataConstants;
import org.openxdata.model.StudyDef;
import org.openxdata.openclinica.StudyEvent;
import org.openxdata.openclinica.StudyEventList;
import org.openxdata.openclinica.Subject;
import org.openxdata.openclinica.SubjectData;
import org.openxdata.openclinica.SubjectForm;
import org.openxdata.openclinica.SubjectList;


/**
 * 
 * @author daniel
 *
 */
public class OpenclinicaDataStorage {
	
	/** The unique identifier for storage of subjects. */
	private static final String SUBJECT_STORAGE_NAME = "org.openclinica.Subject";
	
	/** The unique identifier for storage of study events. */
	private static final String STUDY_EVENT_STORAGE_NAME = "org.openclinica.StudyEvent";
	
	/** The unique identifier for storage of subject form record mappings. */
	private static final String SUBJECT_FORM_STORAGE_NAME = "org.openclinica.SubjectForm";

	public static StorageListener storageListener;
	
	
	/**
	 * Saves a subject form mapping.
	 * 
	 * @param formDefId - the form definition identifier.
	 * @param subjectForm - the subject form mapping data
	 */
	public static void saveSubjectForm(int formDefId, SubjectForm subjectForm){
		Storage store = StorageFactory.getStorage(getSubjectFormStorageName(formDefId),storageListener);
		store.save(subjectForm); //These records are not edited, they are only saved new and deleted.
	}
	
	/**
	 * Saves a subject.
	 * 
	 * @param subject - the subject to be saved.
	 */
	public static void saveSubject(Subject subject, int studyId){
		Storage store = StorageFactory.getStorage(getSubjectStorageName(studyId),storageListener);
		store.save((Record)subject);
	}
	
	/**
	 * Saves subjects.
	 * 
	 * @param subjects - the list of subjects to be saved.
	 */
	/*public static void saveSubjects(SubjectList subjects){
		Storage store = StorageFactory.getStorage(getSubjectStorageName(),null);
		store.delete();
		for(int i=0; i<subjects.size(); i++)
			store.save(subjects.getSubject(i));
	}*/
	
	/**
	 * Saves subject data. All existing data is first deleted before
	 * the new one is saved.
	 * 
	 * @param subjectData - the subject data to be saved.
	 */
	public static void saveSubjectData(SubjectData subjectData, int studyId){
		if(subjectData == null)
			return;
		
		Storage store = null;
		
		SubjectList subjects = subjectData.getSubjects();
		if(subjects != null && subjects.size() > 0){
			store = StorageFactory.getStorage(getSubjectStorageName(studyId),storageListener);
			store.delete();
			for(int i=0; i<subjects.size(); i++)
				store.save(subjects.getSubject(i));
		}
	}

	
	/**
	 * Gets a list of subjects.
	 * 
	 * @return - a list of subjects.
	 */
	public static Vector getSubjects(int studyId){
		Storage store = StorageFactory.getStorage(getSubjectStorageName(studyId),storageListener);
		return store.read(new Subject().getClass());
	}
	
	/**
	 * Gets a list of subjects matching a name and or a subject identifier parameters.
	 * Subjects whose names and identifier contain the search parameters
	 * will be returned. In otherwards, not an exact match.
	 * If a parameter is empty or null, all subjects are considered to match that parameter.
	 * For instance, if you pass null or empty name and identifier, all subjects
	 * will be returned.
	 * 
	 * @param name - the name to search for
	 * @param identifier - the identifier
	 * @return
	 */
	public static Vector getSubjects(String personId,String studySubjectId, int studyId){
		Vector subjects  = getSubjects(studyId);

		Vector matchedSubjects = new Vector();
		
		if(subjects != null){
			Subject subject;
			for(int i=0; i<subjects.size(); i++){
				subject = (Subject)subjects.elementAt(i);
				if(doesSubjectMatch(personId,studySubjectId,subject))
					matchedSubjects.addElement(subject);
			}
		}
		
		return matchedSubjects;
	}
	
	/**
	 * Checks whether a subject matches the search parameters.
	 * 
	 * @param name - the name parameters.
	 * @param identifier - the identifier parameter.
	 * @param subject - the subject.
	 * @return - true if the subject matches, else false.
	 */
	private static boolean doesSubjectMatch( String personId,String studySubjectId, Subject subject){
		boolean studySubjectIdMatch = false;
		boolean personIdMatch = false;
		
		if(studySubjectId == null || studySubjectId.equals(""))
			studySubjectIdMatch = true;
		
		if(personId == null || personId.equals(""))
			personIdMatch = true;
		
		if(!personIdMatch)
			personIdMatch = doesPatternMatch(personId,subject.getPersonId());
		
		if(!studySubjectIdMatch)
			studySubjectIdMatch = doesPatternMatch(studySubjectId,subject.getStudySubjectId());
		
		return studySubjectIdMatch && personIdMatch;
	}
	
	private static boolean doesPatternMatch(String searchPattern, String value){
		if(value != null && value.toLowerCase().indexOf(searchPattern.toLowerCase()) != -1)
				return true;
		return false;
	}
	

	/**
	 * Deletes a subject.
	 * When you delete a subject, all forms collected about them are deleted
	 * including the form that created this subject.
	 * Is this really necessary functionality?
	 * 
	 * @param data - the subject to be deleted.
	 */
	public static void deleteSubject(Subject subject, int studyId){
		Storage store = StorageFactory.getStorage(getSubjectStorageName(studyId),storageListener);
		store.delete(subject);
	}
	
	/**
	 * Delete all subjects from storage.
	 *
	 */
	public static void deleteSubjects(int studyId){
		Storage store = StorageFactory.getStorage(getSubjectStorageName(studyId),storageListener);
		store.delete();
	}
	
	/**
	 * Delete all study events from storage.
	 *
	 */
	/*public static void deleteStudyEvents(){
		Storage store = StorageFactory.getStorage(getStudyEventStorageName(),null);
		store.delete();
	}*/
	
	public static void saveStudyEvents(int studyId, StudyEventList studyEvents){
		//TODO May need to delete these events when the study is removed from the server.
		Storage store = StorageFactory.getStorage(getStudyEventStorageName(studyId),storageListener);
		store.delete();
		store.addNew(studyEvents);
	}
	
	public static StudyEventList getStudyEvents(int studyId){
		//TODO May need to delete these events when the study is removed from the server.
		StudyEventList studyEvents = null;
		Vector vect = StorageFactory.getStorage(getStudyEventStorageName(studyId),storageListener).read(new StudyEventList().getClass());
		if(vect != null && vect.size() > 0)
			studyEvents = (StudyEventList)vect.elementAt(0); //We can only have one per storage.
		
		return studyEvents;
	}
	
	public static StudyEvent getStudyEvent(int studyId, int eventId){
		StudyEventList studyEvents = getStudyEvents(studyId);
		if(studyEvents == null)
			return null;
		
		for(int index = 0; index < studyEvents.size(); index++){
			StudyEvent event = studyEvents.getEvent(index);
			if(event.getEventId() == eventId)
				return event;
		}
		
		return null;
	}
	
	public static SubjectForm getSubjectForm(Integer subjectId, int formDefId, int eventId){
		Storage store = StorageFactory.getStorage(getSubjectFormStorageName(formDefId),storageListener);
		Vector vect = store.read(new SubjectForm().getClass());
		if(vect != null && vect.size() > 0){
			SubjectForm subjectForm = null;
			for(int i=0; i<vect.size(); i++){
				subjectForm = (SubjectForm)vect.elementAt(i); 
				if(subjectId.equals(subjectForm.getSubjectId()) && eventId == subjectForm.getEventId())
					return subjectForm;
			}
		}
		return null;
	}
	
	public static void deleteSubjectForm(Integer subjectId, int formDefId, int eventId){
		SubjectForm subjectForm = getSubjectForm(subjectId,formDefId,eventId);
		if(subjectForm != null)
			StorageFactory.getStorage(getSubjectFormStorageName(formDefId),storageListener).delete(subjectForm);
	}
	
	public static void deleteSubjectForms(Vector studies){
		for(int i=0; i<studies.size(); i++){
			StudyDef studyDef = (StudyDef)studies.elementAt(i);
			deleteSubjectForms(studyDef);
		}
	}
	
	public static void deleteSubjectForms(StudyDef studyDef){
		for(byte i=0; i<studyDef.getForms().size(); i++){
			FormDef formDef = studyDef.getFormAt(i);
			Storage store = StorageFactory.getStorage(getSubjectFormStorageName(formDef.getId()),storageListener);
			store.delete();
		}
	}
	
	/**
	 * Gets the recordId of a form entered for a subject.
	 * @param subjectId
	 * @param formDefId
	 * @return
	 */
	public static int getSubjectFormRecordId(Integer subjectId, int formDefId, int eventId){
		SubjectForm subjectForm = getSubjectForm(subjectId,formDefId,eventId);
		if(subjectForm != null)
			return subjectForm.getFormRecordId();
		return OpenXdataConstants.NULL_ID;
	}
	
	public static Vector getSubjectForms(int formDefId){
		Storage store = StorageFactory.getStorage(getSubjectFormStorageName(formDefId),storageListener);
		return store.read(new SubjectForm().getClass());
	}

	/**
	 * Gets the name of the storage for subjects.
	 * 
	 * @return - the storage name
	 */
	private static  String getSubjectStorageName(int studyId){
		return SUBJECT_STORAGE_NAME + "." + studyId;
	}
	
	
	/**
	 * Gets the name of the storage for study events.
	 * 
	 * @return - the storage name
	 */
	private static  String getStudyEventStorageName(int studyId){
		return STUDY_EVENT_STORAGE_NAME + "." + studyId;
	}
	
	
	/**
	 * Gets the name of the storage for subject form record mappings.
	 * For performance, subject form record mappings are stored separately
	 * for each form type. This will not only reduce the size per record
	 * but will also incread the search speed as less records will have
	 * to be searched through when locating a subject form record mapping.
	 * As a result of this, each subject will have one record in this storage.
	 * 
	 * @param - formDefId - the form definition identifier.
	 * @return - the storage name
	 */
	private static  String getSubjectFormStorageName(int formDefId){
		return SUBJECT_FORM_STORAGE_NAME + "." + formDefId;
	}
}
