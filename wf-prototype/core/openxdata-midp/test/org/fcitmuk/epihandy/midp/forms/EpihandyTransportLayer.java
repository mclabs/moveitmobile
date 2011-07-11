package org.fcitmuk.epihandy.midp.forms;


import org.fcitmuk.communication.DefaultTransportLayer;
import org.fcitmuk.db.util.PersistentByte;
import org.fcitmuk.epihandy.RequestHeader;
import org.fcitmuk.epihandy.ResponseHeader;
import org.fcitmuk.epihandy.StudyData;
import org.fcitmuk.epihandy.StudyDataList;
import org.fcitmuk.epihandy.StudyDefListTest;
import org.fcitmuk.epihandy.UserList;
import org.fcitmuk.epihandy.UserStudyDefLists;

public class EpihandyTransportLayer extends DefaultTransportLayer {

	private final static byte CON_TYPE_TESTING = 6;
	
	public EpihandyTransportLayer(){
		super();
	}
	
	protected void initConnectionTypes(){
		super.initConnectionTypes();
		addConnectionType(CON_TYPE_TESTING, "Testing");
		//if(getConType() == TransportLayer.CON_TYPE_NULL)
		//	setConType(CON_TYPE_TESTING);
	}
	
	protected void handleRequest() {
		if(this.getConType() == CON_TYPE_TESTING)
			handleTestConnectionType();
		else
			super.handleRequest();
	}
	
	private void handleTestConnectionType(){
		try{
			int action = ((RequestHeader)dataInParams).getAction();

			//The next stream value is determined by the status of the communication parameter.
			 if(action == RequestHeader.ACTION_DOWNLOAD_STUDY_FORMS)
				 dowbloadStudyForms();
			 else if(action == RequestHeader.ACTION_DOWNLOAD_STUDY_LIST)
				 dowbloadStudies();
			 else if(action == RequestHeader.ACTION_UPLOAD_DATA)
				 uploadForms();
			 else if(action == RequestHeader.ACTION_DOWNLOAD_USERS_AND_FORMS)
				 dowbloadStudyForms();
				 
			if(this.isDownload)
				getEventListener().downloaded(dataOutParams,dataOut);
			else
				getEventListener().uploaded(dataOutParams,dataOut);
		}
		catch(Exception e){
			//e.printStackTrace();
			getEventListener().errorOccured(e.getMessage(), e);
		}
	}
	
	private void dowbloadStudyForms(){
		PersistentByte studyIdParam = (PersistentByte)dataIn;
		
		//TODO For now, this is based on the assumption that the study index position will be its id minus one.
		dataOut = new UserStudyDefLists(new UserList(),StudyDefListTest.getTestStudyDefList(true).getStudy((byte)(studyIdParam.getValue()-1)));
		((ResponseHeader)dataOutParams).setStatus((dataOut != null) ? ResponseHeader.STATUS_SUCCESS : ResponseHeader.STATUS_ERROR);
	}
	
	private void dowbloadStudies(){
		dataOut = StudyDefListTest.getTestStudyDefList(false);
		((ResponseHeader)dataOutParams).setStatus((dataOut != null) ? ResponseHeader.STATUS_SUCCESS : ResponseHeader.STATUS_ERROR);
	}
	
	private void uploadForms(){
		StudyDataList studyDataList = (StudyDataList)dataIn;
		if(studyDataList.getStudies() != null)	{
			System.out.println("Server got :" + studyDataList.getStudies().size() + " Studies");
			System.out.println("Server got :" + ((StudyData)studyDataList.getStudies().elementAt(0)).getForms().size() + " forms");
		}
		else
			System.out.println("No data to upload.");
				
		/*Vector studies = studyDataList.getStudies();
		for(int i=0; i<studies.size(); i++){
			StudyData studyData = (StudyData)studies.elementAt(i);
			Vector forms = studyData.getForms();
			for(int j=0; j<forms.size(); j++){
				FormData formData = (FormData)forms.elementAt(j);
				formData.setDef(FormDefTest.getTestFormDef());
				if(formData != null){
					//String xml = EpihandyXform.fromFormData2XformModel(formData);
					//System.out.println(xml);
				}
			}
		}*/
		
		((ResponseHeader)dataOutParams).setStatus(ResponseHeader.STATUS_SUCCESS);
	}
}
