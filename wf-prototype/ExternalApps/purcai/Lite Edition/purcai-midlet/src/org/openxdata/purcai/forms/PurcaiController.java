package org.openxdata.purcai.forms;

import java.util.Vector;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.openxdata.communication.TransportLayer;
import org.openxdata.communication.TransportLayerListener;
import org.openxdata.db.util.Persistent;
import org.openxdata.forms.GeneralSettings;
import org.openxdata.forms.UserManager;
import org.openxdata.model.RequestHeader;
import org.openxdata.model.ResponseHeader;
import org.openxdata.mvc.Controller;
import org.openxdata.mvc.View;
import org.openxdata.purcai.KeyValueValue;
import org.openxdata.purcai.MarkSheet;
import org.openxdata.purcai.MarkSheetDataList;
import org.openxdata.purcai.MarkSheetHeader;
import org.openxdata.purcai.NameValue;
import org.openxdata.purcai.PurcaiConstants;
import org.openxdata.purcai.StudentIdNameMark;
import org.openxdata.purcai.TestData;
import org.openxdata.purcai.ValueValue;
import org.openxdata.purcai.db.PurcaiDatabase;
import org.openxdata.util.AlertMessage;
import org.openxdata.util.AlertMessageListener;
import org.openxdata.util.DefaultCommands;

public class PurcaiController implements Controller,AlertMessageListener, TransportLayerListener{

	private final byte STATE_NONE = 0;
	private final byte STATE_CLASS = 1;
	private final byte STATE_STRM = 2;
	private final byte STATE_SUBJECT = 3;
	private final byte STATE_PAPER = 4;
	private final byte STATE_TESTTYPE = 5;
	private final byte STATE_OUT_OF = 6;
	private final byte STATE_STUDENT_MARKS = 7;

	private final byte CA_NONE = 0;
	private final byte CA_TEST_DATA_DOWNLOAD = 1;
	private final byte CA_MARK_SHEET_DOWNLOAD = 2;
	private final byte CA_MARKS_UPLOAD = 3;
	private final byte CA_MARKS_ENTRY = 4;
	private final byte CA_MARKS_ENTRY_AFTER_SHEET_DOWNLOAD = 5;
	private final byte CA_MARKS_ENTRY_AFTER_TEST_DATA_DOWNLOAD = 6;
	private final byte CA_MARKSHEET_DOWNLOAD_AFTER_TEST_DATA_DOWNLOAD = 7;

	private ListForm listForm;

	private int classId = NameValue.NULL_ID;
	private int classStrmId = NameValue.NULL_ID;
	private int subjectId = NameValue.NULL_ID;
	private int subjectPaperId = NameValue.NULL_ID;
	private int testTypeId = NameValue.NULL_ID;

	private String className;
	private String strmName;
	private String subjectName;
	private String paperName;
	private String testTypeName;

	private MarkSheet markSheet;

	private byte outOf = StudentIdNameMark.NULL_MARK;

	private byte currentAction = CA_NONE;

	private byte state = STATE_NONE;

	private static final String APP_TITLE = " - PurcAI";

	Display display;
	Displayable prevScreen;
	TransportLayer transportLayer;
	AlertMessage alertMsg;
	UserManager userMgr;

	TestData testData;

	public PurcaiController(Display display, Displayable prevScreen,TransportLayer transportLayer,UserManager userMgr){
		this.display = display;
		this.prevScreen = prevScreen;
		this.transportLayer = transportLayer;
		this.userMgr = userMgr;

		alertMsg = new AlertMessage(display, prevScreen.getTitle(), prevScreen,this);
	}

	public int getClassId() {
		return classId;
	}

	public void setClassId(int classId) {
		this.classId = classId;
	}

	public int getClassStrmId() {
		return classStrmId;
	}

	public void setClassStrmId(int classStrmId) {
		this.classStrmId = classStrmId;
	}

	public ListForm getListForm() {
		return listForm;
	}

	public void setListForm(ListForm listForm) {
		this.listForm = listForm;
	}

	public int getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(int subjectId) {
		this.subjectId = subjectId;
	}

	public int getSubjectPaperId() {
		return subjectPaperId;
	}

	public void setSubjectPaperId(int subjectPaperId) {
		this.subjectPaperId = subjectPaperId;
	}

	public int getTestTypeId() {
		return testTypeId;
	}

	public void setTestTypeId(int testTypeId) {
		this.testTypeId = testTypeId;
	}

	public byte getOutOf() {
		return outOf;
	}

	public void setOutOf(byte outOf) {
		this.outOf = outOf;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getPaperName() {
		return paperName;
	}

	public void setPaperName(String paperName) {
		this.paperName = paperName;
	}

	public String getStrmName() {
		return strmName;
	}

	public void setStrmName(String strmName) {
		this.strmName = strmName;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public String getTestTypeName() {
		return testTypeName;
	}

	public void setTestTypeName(String testTypeName) {
		this.testTypeName = testTypeName;
	}

	public void enterMarks(Display display, Displayable prevScreen){
		this.display = display;
		this.prevScreen = prevScreen;

		currentAction = CA_MARKS_ENTRY_AFTER_TEST_DATA_DOWNLOAD;
		if(downloadTestDataIfMissing())
			return;

		doMarksEntry();
	}

	private void doMarksEntry(){
		currentAction = CA_MARKS_ENTRY;
		startTestTypeSelection();
	}

	public void startTestTypeSelection(){
		listForm = new ListForm();
		listForm.setController(this);
		listForm.setPrevScreen(prevScreen);
		listForm.setDisplay(display);

		//testData = PurcaiDatabase.getTestData();

		//if(downloadTestDataIfMissing())
		//	return;

		/*if(testData == null || testData.getClasses() == null || testData.getClasses().size() == 0){
			alertMsg.show("Please first download Classes & Subjects");
			currentAction = CA_NONE;
			return;
		}*/

		displayClasses();
	}

	private void handleCancelCommand(){
		switch(state){
		case STATE_CLASS:
			displayMainWindow();
			break;
		case STATE_STRM:
			displayClasses();
			break;
		case STATE_SUBJECT:
			displayStrms();
			break;
		case STATE_PAPER:
			displaySubjects();
			break;
		case STATE_TESTTYPE:
			displayPapers();
			break;
		case STATE_OUT_OF:
			displayTestTypes();
			break;
		case STATE_STUDENT_MARKS:
			displayOutOf();
			break;
		}
	}

	private void handleOkCommand(NameValue selection){
		switch(state){
		case STATE_CLASS:
			setClassId(selection.getId());
			setClassName(selection.getName());
			displayStrms();
			break;
		case STATE_STRM:
			setClassStrmId(selection.getId());
			setStrmName(selection.getName());
			displaySubjects();
			break;
		case STATE_SUBJECT:
			setSubjectId(selection.getId());
			setSubjectName(selection.getName());
			displayPapers();
			break;
		case STATE_PAPER:
			setSubjectPaperId(selection.getId());
			setPaperName(selection.getName());
			displayTestTypes();
			break;
		case STATE_TESTTYPE:
			setTestTypeId(selection.getId());
			setTestTypeName(selection.getName());
			if(currentAction == CA_MARKS_ENTRY){
				if(getStudentMarks())
					displayOutOf();
			}
			else if(currentAction == CA_MARK_SHEET_DOWNLOAD)
				alertMsg.showConfirm("Do you really want to download this mark sheet: {" + getMarkSheetName() +"}?");
			break;
		case STATE_OUT_OF:
			displayStudentMarks();
			break;
		case STATE_STUDENT_MARKS:
			displayMainWindow();
			break;
		}
	}

	private String getMarkSheetName(){
		return getClassName() + " " + getStrmName() + " " + getSubjectName() + " " + getPaperName() + " " + getTestTypeName();
	}

	private void displayClasses(){
		listForm.setTitle("Class"+APP_TITLE);

		Vector list = testData.getClasses();		
		listForm.showData(list,getClassId(),false);

		state = STATE_CLASS;
	}

	private String getName(int id,Vector nameValueList){
		if(nameValueList == null)
			return null;

		NameValue val;
		for(int i=0; i<nameValueList.size(); i++){
			val = (NameValue)nameValueList.elementAt(i);
			if(id == val.getId())
				return val.getName();
		}

		return null;
	}

	private int getId1(int id, Vector list){
		if(list == null)
			return -1;

		KeyValueValue val;
		for(int i=0; i<list.size(); i++){
			val = (KeyValueValue)list.elementAt(i);
			if(val.getId() == id)
				return val.getId1();
		}

		return -1;
	}

	private String getKeyName(int key, Vector keyList, Vector nameList){
		if(keyList == null || nameList == null)
			return  null;

		KeyValueValue val;
		for(int i=0; i<keyList.size(); i++){
			val = (KeyValueValue)keyList.elementAt(i);
			if(val.getId() == key)
				return getName(val.getId2(),nameList);
		}

		return null;
	}

	private void displayStrms(){
		listForm.setTitle("Stream"+APP_TITLE);

		Vector list = new Vector();
		Vector strms = testData.getStrms();	
		Vector classStrms = testData.getClassStrms();

		KeyValueValue val;
		if(strms != null && classStrms != null){
			for(int i=0; i<classStrms.size(); i++){
				val = (KeyValueValue)classStrms.elementAt(i);
				if(val.getId1() == classId)
					list.addElement(new NameValue(val.getId(),getName(val.getId2(),strms)));
			}
		}

		listForm.showData(list,getClassStrmId(),true);
		state = STATE_STRM;
	}

	private void displaySubjects(){
		listForm.setTitle("Subject"+APP_TITLE);

		Vector list = new Vector();
		Vector subjects = testData.getSubjects();	
		Vector strmSubjects = testData.getStrmSubjects();

		ValueValue val;
		if(subjects != null && strmSubjects != null){
			for(int i=0; i<strmSubjects.size(); i++){
				val = (ValueValue)strmSubjects.elementAt(i);
				if(val.getId1() == classStrmId)
					list.addElement(new NameValue(val.getId2(),getName(val.getId2(),subjects)));
			}
		}

		listForm.showData(list,getSubjectId(),true);

		state = STATE_SUBJECT;
	}

	private void displayPapers(){
		listForm.setTitle("Paper"+APP_TITLE);

		Vector list = new Vector();
		Vector papers = testData.getPapers();	
		Vector classPapers = testData.getClassPapers();
		Vector subjectPapers = testData.getSubjectPapers();

		ValueValue val;
		if(papers != null && classPapers != null){
			for(int i=0; i<classPapers.size(); i++){
				val = (ValueValue)classPapers.elementAt(i);
				if(val.getId1() == classId && getId1(val.getId2(),subjectPapers) == subjectId)
					list.addElement(new NameValue(val.getId2(),getKeyName(val.getId2(),subjectPapers,papers)));
			}
		}

		listForm.showData(list,getSubjectPaperId(),true);

		state = STATE_PAPER;
	}

	private void displayTestTypes(){
		listForm.setTitle("Test Type"+APP_TITLE);

		Vector list = new Vector();
		Vector testTypes = testData.getTestTypes();	
		Vector classTestTypes = testData.getClassTestTypes();

		ValueValue val;
		if(testTypes != null && classTestTypes != null){
			for(int i=0; i<classTestTypes.size(); i++){
				val = (ValueValue)classTestTypes.elementAt(i);
				if(val.getId1() == classId)
					list.addElement(new NameValue(val.getId2(),getName(val.getId2(),testTypes)));
			}
		}

		listForm.showData(list,getTestTypeId(),true);
		state = STATE_TESTTYPE;
	}

	private void displayOutOf(){
		MarkEditForm markEditor = new MarkEditForm(prevScreen.getTitle(),display,listForm.getScreen(),this);
		markEditor.editMark(new StudentIdNameMark(-1,"Marked Out Of",getOutOf()),(byte)100, "Please enter the highest possible mark for this test.",false,false,-1,-1);
		state = STATE_OUT_OF;
	}

	private boolean getStudentMarks(){
		markSheet = PurcaiDatabase.getMarkSheet(classStrmId, subjectPaperId, testTypeId);

		if(markSheet == null){
			currentAction = CA_MARKS_ENTRY_AFTER_SHEET_DOWNLOAD;
			alertMsg.showConfirm("This mark sheet is not downloaded. Do you want to download it now?");
			return false;
		}

		setOutOf(markSheet.getHeader().getOutOf());

		return true;
	}

	private void displayStudentMarks(){		

		markSheet.getHeader().setOutOf(getOutOf());

		MarkSheetForm frm = new MarkSheetForm(getMarkSheetName(),display,prevScreen,this);
		frm.show(markSheet,GeneralSettings.isSingleQtnEdit());

		state = STATE_STUDENT_MARKS;
	}

	private void displayMainWindow(){
		listForm.getDisplay().setCurrent(listForm.getPrevScreen());
	}

	public void downloadTestData(){
		currentAction = CA_TEST_DATA_DOWNLOAD;
		alertMsg.showConfirm("Do you really want to download classes & subjects?");
	}

	public void downloadMarkSheet(){
		//currentAction = CA_NONE; //may bring bug as brand knew.

		//testData = PurcaiDatabase.getTestData();

		currentAction = CA_MARKSHEET_DOWNLOAD_AFTER_TEST_DATA_DOWNLOAD;
		if(downloadTestDataIfMissing())
			return;

		doMarkSheetDownload();
	}

	private void doMarkSheetDownload(){
		currentAction = CA_MARK_SHEET_DOWNLOAD;
		startTestTypeSelection();
	}

	private boolean downloadTestDataIfMissing(){
		testData = PurcaiDatabase.getTestData();
		if(testData == null || testData.getClasses() == null || testData.getClasses().size() == 0){
			alertMsg.showConfirm("You need to first download classes & subjects. Do you want to go ahead and download them?");
			return true;
		}
		return false;
	}

	/**
	 * Should we upload mark sheets one by one or all at a go.
	 *
	 */
	public void uploadMarkSheet(){
		currentAction = CA_NONE;

		MarkSheetDataList markSheetDataList = PurcaiDatabase.getMarkSheets();
		if(markSheetDataList == null || markSheetDataList.size() == 0){
			alertMsg.show("No marks entered to upload.");
			return;
		}

		currentAction = CA_MARKS_UPLOAD;
		alertMsg.showConfirm("Do you really want to upload marks to the server?");
	}

	public void downloaded(Persistent dataOutParams, Persistent dataOut){
		String message = null;

		if(currentAction == CA_TEST_DATA_DOWNLOAD || currentAction == CA_MARKS_ENTRY_AFTER_TEST_DATA_DOWNLOAD
				|| currentAction == CA_MARKSHEET_DOWNLOAD_AFTER_TEST_DATA_DOWNLOAD){
			message = "Problem saving downloaded Classes & Subjects";
			try{
				if(dataOut != null){
					testData = (TestData)dataOut;
					PurcaiDatabase.saveTestData(testData);
					if(currentAction == CA_TEST_DATA_DOWNLOAD)
						message = "Classes & Subjects downloaded and saved successfully";
					else{
						message = null;
						if(currentAction == CA_MARKS_ENTRY_AFTER_TEST_DATA_DOWNLOAD)
							doMarksEntry();
						else if(currentAction == CA_MARKSHEET_DOWNLOAD_AFTER_TEST_DATA_DOWNLOAD)
							doMarkSheetDownload();
						return; //we dont want currentAction to be set to CA_NONE
					}
				}
				else
					message = "No Classes & Subjects downloaded.";
			}catch(Exception e){
				message += e.getMessage();
			}
		}	
		else if(currentAction == CA_MARK_SHEET_DOWNLOAD || currentAction == CA_MARKS_ENTRY_AFTER_SHEET_DOWNLOAD){
			message = "Problem saving downloaded Mark Sheet";
			try{
				if(dataOut != null){
					System.out.println("afterdownload="+classStrmId+" "+subjectPaperId+" "+testTypeId);
					PurcaiDatabase.saveMarkSheet(classStrmId,subjectPaperId,testTypeId,(MarkSheet)dataOut);
					if(currentAction == CA_MARKS_ENTRY_AFTER_SHEET_DOWNLOAD){//Check if user wanted to fill marksheet which ended up being downloaded because of its abscence.
						message = null;
						currentAction = CA_MARKS_ENTRY;
						if(getStudentMarks())
							displayOutOf();
					}
					else
						message = "Mark Sheet downloaded and saved successfully";
				}
				else
					message = "No Mark Sheet downloaded.";
			}catch(Exception e){
				message += e.getMessage();
			}
		}	
		else //Must be forms download because we have only two kinds of downloads.
		{
			message = "Trouble"+currentAction;
			userMgr.logOut();
		}

		currentAction = CA_NONE;

		if(message != null)
			alertMsg.show(message);
	}

	//not used for now
	public void uploaded(Persistent dataOutParams, Persistent dataOut){
		String message = "Problem uploading data";

		if (currentAction == CA_MARKS_UPLOAD) {
			try {
				//ResponseHeader status = (ResponseHeader) dataOutParams;
				//TODO for now status is sent as data instead of parameter, which needs to be fixed.
				ResponseHeader status = (ResponseHeader) dataOut;
				if (status.isSuccess()) {
					//TODO Do we really want to delete these marks?
					PurcaiDatabase.deleteAllMarks();
					message = "Marks uploaded successfully.";

					//if (transportLayerListener != null)
					//	transportLayerListener.uploaded(dataOutParams, dataOut);
				} else
					message = "Failed to upload Marks.";
			} catch (Exception e) {
				e.printStackTrace();
				message = "Marks uploaded but problem occured cleaning local store";
			}
		} else
			message = "Unknown upload";

		currentAction = CA_NONE;
		alertMsg.show(message);
	}

	private void startTestDataDownload(){
		RequestHeader comnParam = new RequestHeader();
		comnParam.setAction(PurcaiConstants.ACTION_DOWNLOAD_TESTDATA);
		comnParam.setUserName(userMgr.getUserName());
		comnParam.setPassword(userMgr.getPassword());
		transportLayer.download(comnParam, null, new ResponseHeader(), new TestData(), this,userMgr.getUserName(),userMgr.getPassword(),null);		
	}

	private void startMarkSheetDownload(){
		RequestHeader comnParam = new RequestHeader();
		comnParam.setAction(PurcaiConstants.ACTION_DOWNLOAD_MARKSHEET);
		comnParam.setUserName(userMgr.getUserName());
		comnParam.setPassword(userMgr.getPassword());
		transportLayer.download(comnParam, new MarkSheetHeader(classStrmId,subjectPaperId,testTypeId), new ResponseHeader(), new MarkSheet(), this,userMgr.getUserName(),userMgr.getPassword(),null);		
	}

	private void startMarkSheetUpload(){
		RequestHeader comnParam = new RequestHeader();
		comnParam.setAction(PurcaiConstants.ACTION_UPLOAD_MARKS);
		comnParam.setUserName(userMgr.getUserName());
		comnParam.setPassword(userMgr.getPassword());
		transportLayer.upload(comnParam, (MarkSheetDataList)PurcaiDatabase.getMarkSheets(), new ResponseHeader(), new ResponseHeader(), this,userMgr.getUserName(),userMgr.getPassword(),null);		
	}

	public void errorOccured(String errorMessage, Exception e){
		currentAction = CA_NONE;
		if(e != null)
			errorMessage += " : "+ e.getMessage();
		alertMsg.showError(errorMessage);
	}

	public void cancelled(){
		display.setCurrent(prevScreen);
	}

	public void onAlertMessage(byte msg){
		if(msg == AlertMessageListener.MSG_OK){
			if(currentAction == CA_TEST_DATA_DOWNLOAD || currentAction == CA_MARKS_ENTRY_AFTER_TEST_DATA_DOWNLOAD
					|| currentAction == CA_MARKSHEET_DOWNLOAD_AFTER_TEST_DATA_DOWNLOAD){
				alertMsg.showProgress("Classes & Subjects Dowload","Downloading classes & subjects");
				startTestDataDownload();
			}
			else if(currentAction == CA_MARK_SHEET_DOWNLOAD || currentAction == CA_MARKS_ENTRY_AFTER_SHEET_DOWNLOAD){
				alertMsg.showProgress("Mark Sheet Dowload","Downloading Mark Sheet");
				startMarkSheetDownload();
			}
			else if(currentAction == CA_MARKS_UPLOAD){
				alertMsg.showProgress("Mark Sheet Upload","Uploading Mark Sheet");
				startMarkSheetUpload();
			}
			else if(currentAction == CA_MARKS_ENTRY)
				//displayPapers();
				displayTestTypes();
			else
				alertMsg.turnOffAlert();
		}
		else{
			if(currentAction == CA_MARK_SHEET_DOWNLOAD || currentAction == CA_MARKS_ENTRY_AFTER_SHEET_DOWNLOAD){
				if(currentAction == CA_MARKS_ENTRY_AFTER_SHEET_DOWNLOAD)
					currentAction = CA_MARKS_ENTRY;
				displayPapers();			
			}
			else
				alertMsg.turnOffAlert();
		}
	}

	public void execute(View view, Object cmd, Object data){
		if(cmd == DefaultCommands.cmdMainMenu)
			displayMainWindow();
		else if(data instanceof NameValue){
			if(cmd == DefaultCommands.cmdCancel)
				handleCancelCommand();	
			else
				handleOkCommand((NameValue)data);
		}
		else if(data instanceof MarkSheet){
			if(cmd != DefaultCommands.cmdCancel){
				PurcaiDatabase.saveMarkSheet(classStrmId, subjectPaperId, testTypeId, (MarkSheet)data);
				alertMsg.show("Marks saved successfully.");
			}
			else
				displayTestTypes();
		}
		else if(data instanceof StudentIdNameMark){
			if(cmd == DefaultCommands.cmdCancel)
				displayTestTypes();
			else{
				setOutOf(((StudentIdNameMark)data).getMark());
				displayStudentMarks();
			}
		}
		else
			System.out.println("asdasfasfasfasf");
	}

	public void updateCommunicationParams(){

	}
}
