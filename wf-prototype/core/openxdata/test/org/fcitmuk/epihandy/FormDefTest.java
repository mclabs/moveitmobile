package org.fcitmuk.epihandy;

import java.util.Vector;

public class FormDefTest {
	
	public FormDefTest(){
		super();
	}
	
	public static FormDef getTestFormDefMinusRules(){
		Vector questions = new Vector();
		
		byte qtnId = 1;
		QuestionDef qn = new QuestionDef(qtnId,"PatientID:","The unique identifier of the patient.",true,QuestionDef.QTN_TYPE_TEXT,EpihandyConstants.EMPTY_STRING,true,true,false,"patient_identifier",null);
		questions.addElement(qn);
				
		qtnId++;
		Vector options = new Vector();
		byte b = 0;
		options.addElement(new OptionDef(++b,"Mr","Mr"));
		options.addElement(new OptionDef(++b,"Mrs","Mrs"));
		options.addElement(new OptionDef(++b,"Miss","Miss"));
		options.addElement(new OptionDef(++b,"Dr","Dr"));
		qn = new QuestionDef(qtnId,"Prefix:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_LIST_EXCLUSIVE,EpihandyConstants.EMPTY_STRING,true,true,false,"prefix",options);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"Family Name:",EpihandyConstants.EMPTY_STRING,true,QuestionDef.QTN_TYPE_TEXT,EpihandyConstants.EMPTY_STRING,true,true,false,"family_name",null);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"Given Name:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_TEXT,EpihandyConstants.EMPTY_STRING,true,true,false,"given_name",null);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"Middle Name:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_TEXT,EpihandyConstants.EMPTY_STRING,true,true,false,"middle_name",null);
		questions.addElement(qn);
		
		qtnId++;
		Vector options1 = new Vector();
		b = 0;
		options1.addElement(new OptionDef(++b,"Male","M"));
		options1.addElement(new OptionDef(++b,"Female","F"));
		qn = new QuestionDef(qtnId,"${family_name}$'s Sex:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_LIST_EXCLUSIVE,EpihandyConstants.EMPTY_STRING,true,true,false,"gender",options1);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"${family_name}$'s Age:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_NUMERIC,EpihandyConstants.EMPTY_STRING,true,true,false,"age",null);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"${family_name}$'s Weight:","The weight of the patient in Kgs.",false,QuestionDef.QTN_TYPE_DECIMAL,EpihandyConstants.EMPTY_STRING,true,true,false,"weight",null);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"Is ${family_name}$ Pregnant?:","Olina olubuto?.",false,QuestionDef.QTN_TYPE_BOOLEAN,EpihandyConstants.EMPTY_STRING,true,true,false,"pregnant",null);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"Are u in Ginis4?:","Oli mu giya etali ntufu?.",false,QuestionDef.QTN_TYPE_BOOLEAN,EpihandyConstants.EMPTY_STRING,true,true,false,"ginis4",null);
		questions.addElement(qn);
				
		qtnId++;
		qn = new QuestionDef(qtnId,"Whats ${family_name}$'s Birth Date:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_DATE,EpihandyConstants.EMPTY_STRING,true,true,false,"birthdate",null);
		questions.addElement(qn);
		
		qtnId++;
		Vector options2 = new Vector();
		b = 0;
		options2.addElement(new OptionDef(++b,"Mbale Regional Hospital","1"));
		options2.addElement(new OptionDef(++b,"Mbarar Referal Hospital","2"));
		qn = new QuestionDef(qtnId,"Location:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_LIST_EXCLUSIVE,EpihandyConstants.EMPTY_STRING,true,true,false,"location_id",options2);
		questions.addElement(qn);
		
		qtnId++;
		Vector options3 = new Vector();
		b = 0;
		options3.addElement(new OptionDef(++b,"D4T","1"));
		options3.addElement(new OptionDef(++b,"3TC","2"));
		options3.addElement(new OptionDef(++b,"ABICAVIR","3"));
		options3.addElement(new OptionDef(++b,"TRUVADA","4"));
		options3.addElement(new OptionDef(++b,"AZT","5"));
		qn = new QuestionDef(qtnId,"ARVs ${family_name}$ is allergic to:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_LIST_MULTIPLE,EpihandyConstants.EMPTY_STRING,true,true,false,"arvallery",options3);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"patient_guid:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_TEXT,EpihandyConstants.EMPTY_STRING,false,true,false,"patient_guid",null);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"Test identifier_type:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_TEXT,"4",false,true,false,"identifier_type",null);
		questions.addElement(qn);
		
		Vector pages = new Vector();
		PageDef pg = new PageDef("Page1",Byte.parseByte(String.valueOf("1")),questions);
		pages.addElement(pg);

		return new FormDef(Byte.parseByte(String.valueOf("1")),"Test Form", "testform",pages,null,"${family_name}$ in ${location_id}$");
	}
	
	public static FormDef getTestFormDef(){
		FormDef formDef = getTestFormDefMinusRules();
		formDef.setRules(SkipRuleTest.getTestRules());
		return formDef;
	}
	
	public static FormDef getTestFormDef2(){
		Vector questions = new Vector();
		
		byte qtnId = 1;
		QuestionDef qn = new QuestionDef(qtnId,"PID:","Thep",true,QuestionDef.QTN_TYPE_TEXT,null,true,true,false,"pi",null);
		questions.addElement(qn);

		/*byte qtnId = 1;
		QuestionDef qn = new QuestionDef(qtnId,"PatientID:","The unique identifier of the patient.",true,QuestionDef.QTN_TYPE_TEXT,null,true,true,false,"patient_identifier",null);
		questions.addElement(qn);
				
		qtnId++;
		qn = new QuestionDef(qtnId,"Age:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_NUMERIC,null,true,true,false,"age",null);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"Weight:","The weight of the patient in Kgs.",false,QuestionDef.QTN_TYPE_DECIMAL,null,true,true,false,"weight",null);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"Are u in Ginis4?:","Oli mu giya etali ntufu?.",false,QuestionDef.QTN_TYPE_BOOLEAN,null,true,true,false,"ginis4",null);
		questions.addElement(qn);
		
		qtnId++;
		Vector options = new Vector();
		byte b = 0;
		options.addElement(new OptionDef(++b,"Mr","Mr"));
		options.addElement(new OptionDef(++b,"Mrs","Mrs"));
		options.addElement(new OptionDef(++b,"Miss","Miss"));
		options.addElement(new OptionDef(++b,"Dr","Dr"));
		qn = new QuestionDef(qtnId,"Prefix:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_LIST_EXCLUSIVE,null,true,true,false,"prefix",options);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"Birth Date:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_DATE,null,true,true,false,"birthdate",null);
		questions.addElement(qn);
		
		qtnId++;
		Vector options1 = new Vector();
		b = 0;
		options1.addElement(new OptionDef(++b,"Male","M"));
		options1.addElement(new OptionDef(++b,"Female","F"));
		qn = new QuestionDef(qtnId,"Sex:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_LIST_EXCLUSIVE,null,true,true,false,"gender",options1);
		questions.addElement(qn);
		
		qtnId++;
		Vector options2 = new Vector();
		b = 0;
		options2.addElement(new OptionDef(++b,"Mbale Regional Hospital","1"));
		options2.addElement(new OptionDef(++b,"Mbarara Referal Hospital","2"));
		options2.addElement(new OptionDef(++b,"Masaka Referal Hospital","3"));
		qn = new QuestionDef(qtnId,"Location:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_LIST_EXCLUSIVE,null,true,true,false,"location_id",options2);
		questions.addElement(qn);
		
		qtnId++;
		Vector options3 = new Vector();
		b = 0;
		options3.addElement(new OptionDef(++b,"D4T","1"));
		options3.addElement(new OptionDef(++b,"3TC","2"));
		options3.addElement(new OptionDef(++b,"ABICAVIR","3"));
		options3.addElement(new OptionDef(++b,"TRUVADA","4"));
		options3.addElement(new OptionDef(++b,"AZT","5"));
		qn = new QuestionDef(qtnId,"Alleric ARVs:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_LIST_MULTIPLE,null,true,true,false,"arvallery",options3);
		questions.addElement(qn);

		qtnId++;
		qn = new QuestionDef(qtnId,"Family Name:",EpihandyConstants.EMPTY_STRING,true,QuestionDef.QTN_TYPE_TEXT,null,true,true,false,"family_name",null);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"Given Name:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_TEXT,null,true,true,false,"given_name",null);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"Middle Name:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_TEXT,null,true,true,false,"middle_name",null);
		questions.addElement(qn);
		
		/*qtnId++;
		qn = new QuestionDef(qtnId,"Second Family Name:",EpihandyConstants.EMPTY_STRING,true,QuestionDef.QTN_TYPE_TEXT,null,true,true,false,"second_family_name",null);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"Second Given Name:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_TEXT,null,true,true,false,"second_given_name",null);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"Second Middle Name:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_TEXT,null,true,true,false,"second_middle_name",null);
		questions.addElement(qn);*/
					
		Vector pages = new Vector();
		PageDef pg = new PageDef("Page1",Byte.parseByte(String.valueOf("1")),questions);
		pages.addElement(pg);

		return new FormDef(Byte.parseByte(String.valueOf("3")),"Test Form", "testform",pages,null,null/*"${family_name}$ in ${location_id}$"*/);
	}

	public static FormDef getPatientFormDef(){
		
		Vector questions = new Vector();
		
		byte qtnId = 0; //QuestionDef.QTN_TYPE_TEXT
		QuestionDef qn = new QuestionDef(++qtnId,"PatientID:","The unique identifier of the patient.",true,QuestionDef.QTN_TYPE_TEXT,EpihandyConstants.EMPTY_STRING,true,true,false,"patient_identifier",null);
		questions.addElement(qn);
		
		RepeatQtnsDef rptQtnsDef = RepeatQtnsDefTest.getTestRepeatQtnsDef();
		qn = new QuestionDef(++qtnId,"Symptoms shown:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_REPEAT,EpihandyConstants.EMPTY_STRING,true,true,false,"symptoms",rptQtnsDef);
		questions.addElement(qn);
		rptQtnsDef.setQtnDef(qn);
		
		//qn = new QuestionDef(toByte(2),"Age:",EpihandyConstants.EMPTY_STRING,true,QuestionDef.QTN_TYPE_NUMERIC,EpihandyConstants.EMPTY_STRING,true,true,false,"age",null);
		//questions.addElement(qn);
		
		qn = new QuestionDef(++qtnId,"Weight:","The weight of the patient in Kgs.",false,QuestionDef.QTN_TYPE_DECIMAL,EpihandyConstants.EMPTY_STRING,true,true,false,"weight",null);
		questions.addElement(qn);
		
		Vector options = new Vector();
		options.addElement(new OptionDef(toByte(1),"Mr","Mr"));
		options.addElement(new OptionDef(toByte(2),"Mrs","Mrs"));
		options.addElement(new OptionDef(toByte(3),"Miss","Miss"));
		options.addElement(new OptionDef(toByte(4),"Dr","Dr"));
		qn = new QuestionDef(++qtnId,"Prefix:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_LIST_EXCLUSIVE,EpihandyConstants.EMPTY_STRING,true,true,false,"prefix",options);
		questions.addElement(qn);
		
		qn = new QuestionDef(++qtnId,"Family Name:",EpihandyConstants.EMPTY_STRING,true,QuestionDef.QTN_TYPE_TEXT,EpihandyConstants.EMPTY_STRING,true,true,false,"family_name",null);
		questions.addElement(qn);
		
		qn = new QuestionDef(++qtnId,"Given Name:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_TEXT,EpihandyConstants.EMPTY_STRING,true,true,false,"given_name",null);
		questions.addElement(qn);
		
		qn = new QuestionDef(++qtnId,"Middle Name:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_TEXT,EpihandyConstants.EMPTY_STRING,true,true,false,"middle_name",null);
		questions.addElement(qn);
		
		Vector options1 = new Vector();
		options1.addElement(new OptionDef(toByte(1),"Male","M"));
		options1.addElement(new OptionDef(toByte(2),"Female","F"));
		qn = new QuestionDef(++qtnId,"Sex:",EpihandyConstants.EMPTY_STRING,true,QuestionDef.QTN_TYPE_LIST_EXCLUSIVE,EpihandyConstants.EMPTY_STRING,true,true,false,"gender",options1);
		//qn.setRules(SkipRuleTest.getTestRules());
		questions.addElement(qn);
		
		qn = new QuestionDef(++qtnId,"Is ${family_name}$ Pregnant?:","Olina olubuto?.",false,QuestionDef.QTN_TYPE_BOOLEAN,EpihandyConstants.EMPTY_STRING,false,true,false,"pregnant",null);
		questions.addElement(qn);
		//Vector rules = new Vector();
		//rules.addElement(new Integer(1));
		//qn.setRules(RuleTest.getTestRules());
		
		qn = new QuestionDef(++qtnId,"Birth Date:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_DATE,EpihandyConstants.EMPTY_STRING,true,true,false,"birthdate",null);
		questions.addElement(qn);
		
		Vector options2 = new Vector();
		options2.addElement(new OptionDef(toByte(1),"Mbale Regional Hospital","1"));
		options2.addElement(new OptionDef(toByte(2),"Mbarara Referal Hospital","2"));
		qn = new QuestionDef(++qtnId,"Location:",EpihandyConstants.EMPTY_STRING,true,QuestionDef.QTN_TYPE_LIST_EXCLUSIVE,EpihandyConstants.EMPTY_STRING,true,true,false,"location_id",options2);
		questions.addElement(qn);
		
		Vector options3 = new Vector();
		options3.addElement(new OptionDef(toByte(1),"D4T","1"));
		options3.addElement(new OptionDef(toByte(2),"3TC","2"));
		options3.addElement(new OptionDef(toByte(3),"ABICAVIR","3"));
		options3.addElement(new OptionDef(toByte(4),"TRUVADA","4"));
		options3.addElement(new OptionDef(toByte(5),"AZT","5"));
		qn = new QuestionDef(++qtnId,"Allergic ARVs?:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_LIST_MULTIPLE,EpihandyConstants.EMPTY_STRING,true,true,false,"arvallery",options3);
		questions.addElement(qn);
		
		qn = new QuestionDef(++qtnId,"patient_guid:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_TEXT,EpihandyConstants.EMPTY_STRING,false,true,false,"patient_guid",null);
		questions.addElement(qn);
		
		qn = new QuestionDef(++qtnId,"identifier_type:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_TEXT,"4",false,true,false,"identifier_type",null);
		questions.addElement(qn);
		

		
		Vector pages = new Vector();
		PageDef pg = new PageDef("Page1",toByte(1),questions);
		pages.addElement(pg);
		
		//second page
		Vector questions2 = new Vector();
		qn = new QuestionDef(++qtnId,"Start Time:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_TIME,EpihandyConstants.EMPTY_STRING,true,true,false,"startime",null);
		questions2.addElement(qn);
		
		qn = new QuestionDef(++qtnId,"Start Date & Time:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_DATE_TIME,EpihandyConstants.EMPTY_STRING,true,true,false,"startdate",null);
		questions2.addElement(qn);
		
		PageDef pg2 = new PageDef("Page2",toByte(2),questions2);
		pages.addElement(pg2);
		
//		third page
		Vector questions3 = new Vector();
		qn = new QuestionDef(++qtnId,"First Question page3:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_TEXT,EpihandyConstants.EMPTY_STRING,true,true,false,"firstquery",null);
		questions3.addElement(qn);
		
		qn = new QuestionDef(++qtnId,"Second Question on page3:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_TEXT,EpihandyConstants.EMPTY_STRING,true,true,false,"secondquery",null);
		questions3.addElement(qn);
		
		PageDef pg3 = new PageDef("Page3",toByte(3),questions3);
		pages.addElement(pg3);		

		return new FormDef(toByte(2),"New Patient", "newpatient",pages, SkipRuleTest.getPatientTestRules(),"${family_name}$ in ${location_id}$");
	}
	
	public static FormDef getTBFormDef(){
		Vector questions = new Vector();
		
		byte qtnId = 1;
		QuestionDef qn = new QuestionDef(qtnId,"PatientID:","The unique identifier of the patient.",true,QuestionDef.QTN_TYPE_TEXT,EpihandyConstants.EMPTY_STRING,true,true,false,"patient_identifier",null);
		questions.addElement(qn);
						
		qtnId++;
		qn = new QuestionDef(qtnId,"First Name:",EpihandyConstants.EMPTY_STRING,true,QuestionDef.QTN_TYPE_TEXT,EpihandyConstants.EMPTY_STRING,true,true,false,"family_name",null);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"Last Name:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_TEXT,EpihandyConstants.EMPTY_STRING,true,true,false,"given_name",null);
		questions.addElement(qn);
				
		qtnId++;
		Vector options1 = new Vector();
		byte b = 0;
		options1.addElement(new OptionDef(++b,"Male","M"));
		options1.addElement(new OptionDef(++b,"Female","F"));
		qn = new QuestionDef(qtnId,"${family_name}$'s Gender:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_LIST_EXCLUSIVE,EpihandyConstants.EMPTY_STRING,true,true,false,"gender",options1);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"${family_name}$'s Age:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_NUMERIC,EpihandyConstants.EMPTY_STRING,true,true,false,"age",null);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"${family_name}$'s Weight:","The weight of the patient in Kgs.",false,QuestionDef.QTN_TYPE_DECIMAL,EpihandyConstants.EMPTY_STRING,true,true,false,"weight",null);
		questions.addElement(qn);
				
		qtnId++;
		Vector options2 = new Vector();
		b = 0;
		options2.addElement(new OptionDef(++b,"Positive","1"));
		options2.addElement(new OptionDef(++b,"Negative","2"));
		qn = new QuestionDef(qtnId,"Sputum:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_LIST_EXCLUSIVE,EpihandyConstants.EMPTY_STRING,true,true,false,"location_id",options2);
		questions.addElement(qn);
		
		qtnId++;
		qn = new QuestionDef(qtnId,"Is ${family_name}$ On Treatment?:","Is ${family_name}$ already on treatment?.",false,QuestionDef.QTN_TYPE_BOOLEAN,EpihandyConstants.EMPTY_STRING,true,true,false,"pregnant",null);
		questions.addElement(qn);
		
		qtnId++;
		Vector options3 = new Vector();
		b = 0;
		options3.addElement(new OptionDef(++b,"Pulmonary","1"));
		options3.addElement(new OptionDef(++b,"Extra Pulmonary","2"));
		qn = new QuestionDef(qtnId,"Type:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_LIST_MULTIPLE,EpihandyConstants.EMPTY_STRING,true,true,false,"arvallery",options3);
		questions.addElement(qn);
		
		qtnId++;
		Vector options4 = new Vector();
		b = 0;
		options4.addElement(new OptionDef(++b,"TB","1"));
		options4.addElement(new OptionDef(++b,"MDR-TB","2"));
		qn = new QuestionDef(qtnId,"Classification:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_LIST_EXCLUSIVE,EpihandyConstants.EMPTY_STRING,true,true,false,"arvallery",options4);
		questions.addElement(qn);
				
		Vector pages = new Vector();
		PageDef pg = new PageDef("Page1",Byte.parseByte(String.valueOf("1")),questions);
		pages.addElement(pg);

		return new FormDef(Byte.parseByte(String.valueOf("3")),"TB Form", "tbform",pages,null,"${family_name}$ ${given_name}$");
	}
	
	public static byte toByte(int val){
		return Byte.parseByte(String.valueOf(val));
	}
}
