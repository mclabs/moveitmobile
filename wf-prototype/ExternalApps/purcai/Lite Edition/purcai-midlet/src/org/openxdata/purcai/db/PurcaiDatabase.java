package org.openxdata.purcai.db;

import java.util.Vector;

import org.openxdata.db.util.Storage;
import org.openxdata.db.util.StorageFactory;
import org.openxdata.purcai.KeyValueValue;
import org.openxdata.purcai.KeyValueValueList;
import org.openxdata.purcai.MarkSheet;
import org.openxdata.purcai.MarkSheetData;
import org.openxdata.purcai.MarkSheetDataList;
import org.openxdata.purcai.TestData;


/**
 * For now we store a duplicate of student names for various marksheets.
 * The result is that we use more space but as a trade off for increased
 * processing speed because we do not want to always look for student
 * names given a studentid which is also harder to implement.
 * 
 * @author Daniel
 *
 */
public class PurcaiDatabase {
	private static final String TBL_TEST_DATA = "test.data";
	private static final String TBL_MARK_SHEET = "mark.sheet";
	private static final String TBL_MARKS = "marks";
	private static final String TBL_MARKS_LIST = "marks.list";
	
	private static String getTestDataTable(){
		return TBL_TEST_DATA;
	}
	
	private static String getMarksListTable(){
		return TBL_MARKS_LIST;
	}
	
	private static String getMarkSheetTable(int classStrmId, int subjectPaperId){
		return TBL_MARK_SHEET + "." + classStrmId + "." + subjectPaperId;
	}
	
	private static String getMarksTable(int classStrmId, int subjectPaperId,int testTypeId){
		return TBL_MARKS + "." + classStrmId + "." + subjectPaperId + "." + testTypeId;
	}
	
	public static void saveTestData(TestData data){
		Storage store = StorageFactory.getStorage(getTestDataTable(),null);
		store.delete();
		store.addNew(data);
	}
	
	public static TestData getTestData(){
		Storage store = StorageFactory.getStorage(getTestDataTable(),null);
		//return (TestData)store.readFirst(new TestData().getClass());
		
		TestData data = null;
		Vector vect = store.read(new TestData().getClass());
		if(vect != null && vect.size() > 0)
			data = (TestData)vect.elementAt(0); //We can only have one in the whole application.
		return data;
	}
	
	/**
	 * Saves a student list, without marks, for a given test.
	 * 
	 * @param classStrmId
	 * @param subjectPaperId
	 * @param markSheet
	 */
	public static void saveMarkSheet(int classStrmId,int subjectPaperId, int testTypeId,MarkSheet markSheet){		
		Storage store = StorageFactory.getStorage(getMarksTable(classStrmId,subjectPaperId,testTypeId),null);
		store.delete();
		store.addNew(markSheet);
		
		store = StorageFactory.getStorage(getMarkSheetTable(classStrmId,subjectPaperId),null);
		store.delete();
		markSheet.clearMarks(); //clear the marks to have a template for other test types usage.
		store.addNew(markSheet);
		
		saveMarksList(classStrmId,subjectPaperId,testTypeId);
	}
	
	public static void saveMarksList(int classStrmId,int subjectPaperId, int testTypeId){	
		KeyValueValueList marksList = null;
		
		Storage store = StorageFactory.getStorage(getMarksListTable(),null);
		Vector vect = store.read(new KeyValueValueList().getClass());
		if(vect != null && vect.size() > 0){
			marksList = (KeyValueValueList)vect.elementAt(0); //We can only have one in the whole application.
			if(marksList.contains(classStrmId, subjectPaperId, testTypeId))
				return; //No need to save this list as it is already done so.
		}
		else
			marksList = new KeyValueValueList();
		
		marksList.addValue(new KeyValueValue(classStrmId,subjectPaperId,testTypeId));
		store.delete();
		store.addNew(marksList);
	}
	
	public static KeyValueValueList getMarksList(){
		KeyValueValueList marksList = null;
		
		Storage store = StorageFactory.getStorage(getMarksListTable(),null);
		Vector vect = store.read(new KeyValueValueList().getClass());
		if(vect != null && vect.size() > 0)
			marksList = (KeyValueValueList)vect.elementAt(0); //We can only have one in the whole application.
		
		return marksList;
	}
	
	/**
	 * Gets a mark sheet
	 * 
	 * @param classStrmId
	 * @param subjectPaperId
	 * @param testTypeId
	 * @return
	 */
	public static MarkSheet getMarkSheet(int classStrmId, int subjectPaperId, int testTypeId){
		MarkSheet markSheet = null;
		
		Storage store = StorageFactory.getStorage(getMarkSheetTable(classStrmId,subjectPaperId),null);

		Vector list = store.read(new MarkSheet().getClass());
		if(list != null && list.size() > 0){
			markSheet = new MarkSheet();
			markSheet = (MarkSheet)list.elementAt(0); //ge blank sheet to fill incase there are not marks saved yet.
			markSheet.getHeader().setTestTypeId(testTypeId); //No fill blank with concrete test type.
			
			store = StorageFactory.getStorage(getMarksTable(classStrmId,subjectPaperId,testTypeId),null);
			list = store.read(new MarkSheet().getClass());
			if(list != null && list.size() > 0)
				markSheet = (MarkSheet)list.elementAt(0);
		}

		return markSheet;
	}
	
	/**
	 * Gets a filled mark sheet.
	 * 
	 * @param classStrmId
	 * @param subjectPaperId
	 * @param testTypeId
	 * @return
	 */
	public static MarkSheetData getMarks(int classStrmId, int subjectPaperId, int testTypeId){
		MarkSheet markSheet = null;
		
		Storage store = StorageFactory.getStorage(getMarksTable(classStrmId,subjectPaperId,testTypeId),null);

		Vector list = store.read(new MarkSheet().getClass());
		if(list != null && list.size() > 0)
			markSheet = (MarkSheet)list.elementAt(0);
		return new MarkSheetData(markSheet);
	}
	
	public static MarkSheetDataList getMarkSheets() {
		MarkSheetDataList markSheets = new MarkSheetDataList();
		KeyValueValueList marksList = getMarksList();
		KeyValueValue value = null; MarkSheetData markSheetData = null;
		
		if(marksList != null && marksList.size() > 0){
			//if(marksList.size() > 1)
			//	throw new Exception("Bug here");
			
			for(int i=0; i<marksList.size(); i++){
				value = marksList.getValue(i);
				markSheetData = getMarks(value.getId(),value.getId1(),value.getId2());
				if(markSheetData!= null) //This if at all exists (not null) should have marks.
					markSheets.addMarkSheet(markSheetData);
			}
		}
		
		return markSheets;
	}
	
	/**
	 * 
	 * @return
	 */
	/*public static MarkSheetDataList getMarkSheets(){
		MarkSheetDataList markSheets = new MarkSheetDataList();
		
		TestData testData = getTestData();
		
		if(testData != null){
			Vector classStrms = testData.getClassStrms();
			if(classStrms != null){
				Vector classPapers = testData.getClassPapers();
				Vector testTypes = testData.getClassTestTypes();
				
				for(int i=0; i<classStrms.size(); i++)
					fillClassStrmMarkSheets((KeyValueValue)classStrms.elementAt(i),markSheets,classPapers,testTypes);
			}
		}
		
		return markSheets;
	}
	
	private static void fillClassStrmMarkSheets(KeyValueValue classStrm, MarkSheetDataList markSheets, Vector classPapers,Vector testTypes){
		
		if(classPapers != null){
			for(int i=0; i<classPapers.size(); i++){
				ValueValue val = (ValueValue)classPapers.elementAt(i);
				if(classStrm.getId1() == val.getId1()) //if same classid
					fillSubjectPaperMarkSheets(classStrm.getId1(),classStrm.getId(),val.getId2(),markSheets,testTypes);
			}
		}
	}
	
	private static void fillSubjectPaperMarkSheets(int classId,int classStrmId,int subjectPaperId, MarkSheetDataList markSheets, Vector testTypes){
		if(testTypes != null){
			MarkSheet markSheet = null; ValueValue val = null;
			for(int i=0; i<testTypes.size(); i++){			
				val = (ValueValue)testTypes.elementAt(i);
				if(val.getId1() == classId){
					markSheet = getMarks(classStrmId,subjectPaperId,val.getId2());
					if(markSheet!= null) //This if at all exists (not null) should have marks.
						markSheets.addMarkSheet(new MarkSheetData(markSheet));
				}
			}
		}
	}*/
	
	public static void deleteAllMarks(){
		
	}
	
}
