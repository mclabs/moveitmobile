package org.openxdata.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import org.openxdata.db.util.AbstractRecord;
import org.openxdata.db.util.PersistentHelper;

/**
 * Contains data collected for a form. 
 * In the MVC world, this is the model representing the form data.
 * This separation of form data and definition is for 
 * increased performance.
 * 
 * @author Daniel Kayiwa
 *
 */
public class FormData  extends AbstractRecord{

	/** The collection of pages of data. */
	private Vector pages;

	/** The numeric unique identifier of the form definition that this data represents. 
	 * This is made int instead of byte because users may have values bigger than 256.
	 * */
	private int defId = OpenXdataConstants.NULL_ID;

	/** 
	 * Reference to the form definition. This is just for increased performance
	 * when the form is loaded on the device in edit mode, but is never stored (persisted), 
	 * and is also never sent back to the server during syncronization. It's instead the
	 * id which is used to get the form definition, and hence is the one sent to server.
	 */
	private FormDef def;  

	/** The description of data collected in the form. This is derived from
	 * the form definition description template. This field is not stored
	 * since it can be built on the fly from the form data.
	 */
	private String dataDescription = OpenXdataConstants.EMPTY_STRING;

	/** Constructs a form data object. */
	public FormData(){
		super();
	}

	/** Copy constructor. */
	public FormData(FormData data){
		setRecordId(data.getRecordId());
		setDataDescription(data.getDataDescription());
		setDefId(data.getDefId());
		def = data.getDef();
		copyPages(data.getPages());
		setRecordId(data.getRecordId());
		buildQuestionDataDescription();
	}

	/**
	 * Creates a formdata object from a form definition.
	 * 
	 * @param id - the unique numeric identifier of the form data per form definition of a study.
	 * @param def - the form definition.
	 */
	public FormData(FormDef def){
		this();
		setDefId(def.getId());		
		setDef(def);
	}

	public Vector getPages() {
		return pages;
	}

	public void setPages(Vector pages) {
		this.pages = pages;
	}

	public FormDef getDef() {
		return def;
	}

	public String setDef(FormDef def) {
		this.def = def;
		this.defId = def.getId();
		return updateFormDef();
	}

	public int getDefId() {
		return defId;
	}

	public void setDefId(int defId) {
		this.defId = defId;
	}

	public String getDataDescription() {
		return dataDescription;
	}

	public void setDataDescription(String dataDescription) {
		this.dataDescription = dataDescription;
	}

	private void copyPages(Vector pgs){
		if(pgs != null){
			pages  = new Vector();
			for(int i=0; i<pgs.size(); i++)
				pages.addElement(new PageData((PageData)pgs.elementAt(i)));
		}
	}

	/** Creates page and question data from their corresponding definitions. */
	private void createFormData(){
		Vector pages = new Vector();
		for(byte i=0; i<this.getDef().getPages().size(); i++){
			PageDef pageDef = (PageDef)this.getDef().getPages().elementAt(i);
			Vector questions = new Vector();
			for(int j=0; j<pageDef.getQuestions().size(); j++){
				QuestionDef qtnDef = (QuestionDef)pageDef.getQuestions().elementAt(j);
				QuestionData qtnData = new QuestionData(qtnDef);
				questions.addElement(qtnData);
			}
			PageData pageData = new PageData(questions,pageDef);
			pages.addElement(pageData);
		}

		this.setPages(pages);

		buildDataDescription();
		buildQuestionDataDescription();
	}

	/**
	 * Updates the data objects with the defs in the current formdef reference.
	 */
	private String updateFormDef(){

		String s = "0";


		try{
			if(getPages() == null || getPages().size() == 0)
				createFormData();

			s = "1";

			//After loading a form data from RMS, it has ids for def objects which need to be set at runtime
			for(byte i=0; i<this.getPages().size(); i++){
				s = "2";
				PageData pageData = (PageData)this.getPages().elementAt(i);
				s = "3";
				PageDef pageDef = (PageDef)this.def.getPages().elementAt(i);
				s = "4";
				pageData.setDef(pageDef);
				s = "5";
				
				String id = "";
				//for(byte w=0; w<pageData.getQuestions().size(); w++){
				//	id += "," + ((QuestionData)pageData.getQuestions().elementAt(w)).getId();
					
				for(int j=0; j<pageData.getQuestions().size(); j++){
					s = "6";
					QuestionData qtnData = (QuestionData)pageData.getQuestions().elementAt(j);
					s = "7";
					QuestionDef qtnDef = pageDef.getQuestion(qtnData.getId());
					s = "8";
					
					if(qtnDef == null)
						continue;
					
					id += qtnData.getId() + ",=";
					try{
						id += qtnData.getAnswer() + ",";
					}catch(Exception ex){id += ex.getMessage() + ",=";}
					
					s += " (" + qtnData.getId() + ") ";
					s += " {" + id + "} ";
					
					//s += " {" + pageDef.getQuestions() + "} ";
					//s += " !" + pageDef.getQuestions().size() + "! ";
					s += " ="; //+ pageDef.getQuestions().size() + "! ";
					for(int n = 0; n < pageDef.getQuestions().size(); n++)
						s += "," + ((QuestionDef)pageDef.getQuestions().elementAt(n)).getId();
					s += " =";
					
					qtnData.setDef(qtnDef);
					s = "9";

					if(qtnData.getAnswer() != null && (qtnDef.getType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE)){
						s = "10";
						((OptionData)qtnData.getAnswer()).setDef((OptionDef)qtnDef.getOptions().elementAt(Integer.parseInt(qtnData.getOptionAnswerIndices().toString())));
					}
					else if(qtnData.getAnswer() != null && qtnDef.getType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
						s = "11";
						Vector answers = (Vector)qtnData.getAnswer();
						s = "12";
						for(byte k=0; k<answers.size(); k++){
							s = "13";
							OptionData option = (OptionData)answers.elementAt(k);
							s = "14";
							option.setDef((OptionDef)qtnDef.getOptions().elementAt(((Byte)((Vector)qtnData.getOptionAnswerIndices()).elementAt(k)).byteValue()));
							s = "15";
						}
					}
					else if(qtnData.getAnswer() != null && qtnDef.getType() == QuestionDef.QTN_TYPE_REPEAT){
						s = "16";
						RepeatQtnsDataList answer = (RepeatQtnsDataList)qtnData.getAnswer();
						s = "17";
						for(byte k=0; k<answer.size(); k++){
							s = "18";
							RepeatQtnsData data = answer.getRepeatQtnsData(k);
							s = "19";
							data.setDef(qtnDef.getRepeatQtnsDef());
							s = "20";
						}
					}
				}	
			}

			s = "21";

			try{
			updateDynamicOptions();
			}catch(Exception ex){}
		}
		catch(Exception ex){
			return s + ":" + ex.getMessage();
		}

		return null;
	}

	public QuestionData getQuestion(byte id){
		for(byte i=0; i<this.getPages().size(); i++){
			PageData page = (PageData)this.getPages().elementAt(i);
			for(int j=0; j<page.getQuestions().size(); j++){
				QuestionData qtn = (QuestionData)page.getQuestions().elementAt(j);
				if(qtn.getDef().getId() == id)
					return qtn;
			}
		}

		return null;
	}

	public QuestionData getQuestion(String varName){
		for(byte i=0; i<this.getDef().getPages().size(); i++){
			PageDef page = (PageDef)this.getDef().getPages().elementAt(i);
			for(int j=0; j<page.getQuestions().size(); j++){
				QuestionDef qtn = (QuestionDef)page.getQuestions().elementAt(j);
				if(qtn.getVariableName().equals(varName))
					return getQuestion(qtn.getId());
			}
		}

		return null;
	}

	public Object getAnswer(String varName){
		QuestionData qtn = getQuestion(varName);
		if(qtn != null)
			return qtn.getAnswer();
		return null;
	}

	public void setValue(String varName, String val){
		QuestionData qtn = getQuestion(varName);
		if(qtn != null)
			qtn.setTextAnswer(val);
	}

	public void setValue(String varName, Object val){
		QuestionData qtn = getQuestion(varName);
		if(qtn != null)
			qtn.setAnswer(val);
	}

	public Date getDateValue(String varName){
		QuestionData qtn = getQuestion(varName);
		if(qtn != null)
			return (Date)qtn.getAnswer();
		return null;
	}

	public void setDateValue(String varName, Date dateVal){
		QuestionData qtn = getQuestion(varName);
		if(qtn != null)
			qtn.setAnswer(dateVal);
	}

	public String getTextValue(String varName){
		QuestionData qtn = getQuestion(varName);
		if(qtn != null)
			return qtn.getTextAnswer();

		return null;
	}

	public void setTextValue(String varName, String strVal){
		QuestionData qtn = getQuestion(varName);
		if(qtn != null)
			qtn.setTextAnswer(strVal);
	}

	public boolean containsQuestion(String varName){
		return getQuestion(varName) != null;
	}

	public String getOptionValue(String varName){
		QuestionData qtn = getQuestion(varName);
		if(qtn != null && qtn.getAnswer() != null)
			return ((OptionData)qtn.getAnswer()).getDef().getVariableName();

		return null;
	}

	public void setOptionValue(String varName, String strVal){
		QuestionData qtn = getQuestion(varName);
		if(qtn != null)
			qtn.setTextAnswer(strVal);
	}

	public boolean setOptionValueIfOne(String varName){
		QuestionData qtn = getQuestion(varName);
		if(qtn != null)
			return qtn.setOptionValueIfOne();
		return false;
	}

	public Vector getOptionValues(String varName){
		QuestionData qtn = getQuestion(varName);
		if(qtn != null){
			Vector ret = new Vector();
			Vector options = (Vector)qtn.getAnswer();
			for(byte i=0; i<options.size(); i++)
				ret.addElement(((OptionData)options.elementAt(i)).getDef().getVariableName());

			return ret;
		}

		return null;
	}

	//this needs to be finished and tested
	/*public void setOptionValues(String varName, Vector vals){
		QuestionData qtn = getQuestion(varName);
		if(qtn != null)
			qtn.setOptionAnswers(vals);
	}*/

	/**
	 * Check whether a form's data is entered correctly. 
	 * No missing mandatory fields, not values out of range, etc.
	 * 
	 * @return - true if the data is correct, else false.
	 */
	public boolean isRequiredAnswered(){

		//Check and return if you find just one question which is not valid.
		for(byte i=0; i<pages.size(); i++){
			PageData page = (PageData)pages.elementAt(i);
			for(int j=0; j<page.getQuestions().size(); j++){
				QuestionData qtn = (QuestionData)page.getQuestions().elementAt(j);
				if(!qtn.isValid())
					return false;
			}
		}

		return true;
	}

	/**
	 * Checks to see if a form has atleast one answer.
	 * 
	 * @return true if yes, else false
	 */
	public boolean isFormAnswered(){
		for(byte i=0; i<pages.size(); i++){
			PageData page = (PageData)pages.elementAt(i);
			for(int j=0; j<page.getQuestions().size(); j++){
				QuestionData qtn = (QuestionData)page.getQuestions().elementAt(j);
				if(qtn.isAnswered())
					return true;
			}
		}

		return false;
	}

	/**
	 * Gets value collected for a given question.
	 * 
	 * @param varName - the string unique identifier for the question.
	 * @return - the data value.
	 */
	private String getValue(String varName){
		QuestionData qtn = this.getQuestion(varName);
		if(qtn == null)
			return varName; //wrong variable name
		return qtn.getTextAnswer();
	}

	public void buildDataDescription(){
		//String s = "Where does ${name}$ come from?";
		String f,v,text = getDef().getDescriptionTemplate();

		try{
			if(text == null || text.trim().length() == 0)
				this.dataDescription  = "Data: " + this.getRecordId();
			else{
				int startIndex,j,i = 0;
				do{
					startIndex = i; //mark the point where we found the first $ character.

					i = text.indexOf("${",startIndex); //check the opening $ character
					if(i == -1)
						break; //token not found.

					j = text.indexOf("}$",i+1); //check the closing $ character
					if(j == -1)
						break; //closing token not found. possibly wrong syntax.

					f = text.substring(0,i); //get the text before token
					v = getValue(text.substring(i+2, j)); //append value of token.

					f += (v == null) ? "" : v;
					f += text.substring(j+2, text.length()); //append value after token.

					text = f;

				}while (true); //will break out when dollar symbols are out.

				this.dataDescription = text;
			}
		}catch(Exception ex){
			this.dataDescription = getDef().getDescriptionTemplate();
		}
	}

	public void buildQuestionDataDescription(){
		for(byte i=0; i<pages.size(); i++){
			PageData page = (PageData)pages.elementAt(i);
			for(int j=0; j<page.getQuestions().size(); j++)
				buildQuestionDataDescription((QuestionData)page.getQuestions().elementAt(j));
		}
	}

	//TODO This needs to be refactored with buildDataDescription() above
	private void buildQuestionDataDescription(QuestionData qtn){
		//String s = "Where does ${name}$ come from?";
		String f,v,text = qtn.getDef().getText();
		boolean found =  false;
		int startIndex,j,i = 0;
		do{
			startIndex = i; //mark the point where we found the first $ character.

			i = text.indexOf("${",startIndex); //check the opening $ character
			if(i == -1)
				break; //token not found.

			j = text.indexOf("}$",i+1); //check the closing $ character
			if(j == -1)
				break; //closing token not found. possibly wrong syntax.

			f = text.substring(0,i); //get the text before token
			v = getValue(text.substring(i+2, j)); //append value of token.
			if(v == null || v.trim().length() == 0)
				break;

			f += v;
			f += text.substring(j+2, text.length()); //append value after token.

			text = f;

			found = true;
		}while (true); //will break out when dollar symbols are out.

		if(found)
			qtn.setDataDescription(text);
		else
			qtn.setDataDescription(null);		
	}

	public String toString() {
		return this.dataDescription;
	}

	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setDefId(dis.readInt());
		setPages(PersistentHelper.read(dis,new PageData().getClass()));
	}

	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(getDefId());
		PersistentHelper.write(getPages(), dos);
	}

	public void updateDynamicOptions(){
		for(byte i=0; i<this.getPages().size(); i++){
			PageData pageData = (PageData)this.getPages().elementAt(i);
			for(int j=0; j<pageData.getQuestions().size(); j++)
				updateDynamicOptions((QuestionData)pageData.getQuestions().elementAt(j), true);
		}
	}

	public void updateDynamicOptions(QuestionData questionData, boolean setAswerDef){
		QuestionDef qtnDef = questionData.getDef();

		if(!(qtnDef.getType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || qtnDef.getType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC))
			return;

		DynamicOptionDef dynamicOptionDef = def.getDynamicOptions(questionData.getDef().getId());
		if(dynamicOptionDef == null)
			return;

		QuestionDef childQuestionDef = def.getQuestion(dynamicOptionDef.getQuestionId());
		if(childQuestionDef == null)
			return;

		OptionDef optionDef = questionData.getDef().getOptionWithValue(questionData.getValueAnswer());
		Vector optionList = null;
		if(optionDef != null)
			optionList = dynamicOptionDef.getOptionList(optionDef.getId());

		childQuestionDef.setOptions(optionList);
		if(setAswerDef && optionList != null){
			QuestionData qtnData = getQuestion(childQuestionDef.getId());			
			if(qtnData.getAnswer() != null)
				((OptionData)qtnData.getAnswer()).setDef((OptionDef)childQuestionDef.getOptions().elementAt(Integer.parseInt(qtnData.getOptionAnswerIndices().toString())));
		}

		QuestionData childQuestionData = getQuestion(childQuestionDef.getId());

		if(!setAswerDef)
			childQuestionData.setAnswer(null);

		updateDynamicOptions(childQuestionData,setAswerDef);
	}
}
