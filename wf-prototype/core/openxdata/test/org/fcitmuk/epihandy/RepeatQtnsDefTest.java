package org.fcitmuk.epihandy;

import java.util.Vector;

public class RepeatQtnsDefTest {

	public static RepeatQtnsDef getTestRepeatQtnsDef(){
		RepeatQtnsDef rptQtns = new RepeatQtnsDef();
		
		Vector options3 = new Vector();
		byte b = 0;
		options3.addElement(new OptionDef(++b,"DIARRHEA","1"));
		options3.addElement(new OptionDef(++b,"COUGH","2"));
		options3.addElement(new OptionDef(++b,"HEADACHE","3"));
		options3.addElement(new OptionDef(++b,"ULCERS","4"));
		options3.addElement(new OptionDef(++b,"STOMACHACHE","5"));
		QuestionDef qtn = new QuestionDef((byte)1,"Symptom:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_LIST_EXCLUSIVE,EpihandyConstants.EMPTY_STRING,true,true,false,"symptom",options3);
		rptQtns.addQuestion(qtn);
		
		qtn =  new QuestionDef((byte)2,"Date:",EpihandyConstants.EMPTY_STRING,false,QuestionDef.QTN_TYPE_DATE,EpihandyConstants.EMPTY_STRING,true,true,false,"date",null);
		rptQtns.addQuestion(qtn);
		
		return rptQtns;
	}
}
