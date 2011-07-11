package org.openxdata.purcai;

import java.util.Vector;

import org.openxdata.purcai.KeyValueValue;
import org.openxdata.purcai.NameValue;
import org.openxdata.purcai.TestData;
import org.openxdata.purcai.ValueValue;

public class TestDataTest {

	private TestDataTest(){
		
	}
	
	public static TestData getTestDataTest(){
		TestData testData = new TestData();
		
		Vector classes = new Vector();
		classes.addElement(new NameValue(1,"S1"));
		
		Vector strms = new Vector(); 
		strms.addElement(new NameValue(1,"A"));
		
		Vector subjects = new Vector();
		subjects.addElement(new NameValue(1,"Mathematics"));
		
		Vector papers = new Vector();
		papers.addElement(new NameValue(1,"Paper 1"));
		
		Vector testTypes = new Vector();
		testTypes.addElement(new NameValue(2,"BOT"));
		testTypes.addElement(new NameValue(1,"Exams"));
		
		Vector classStrms = new Vector();
		classStrms.addElement(new KeyValueValue(1,1,1));
		
		Vector subjectPapers = new Vector();
		subjectPapers.addElement(new KeyValueValue(1,1,1));
		
		Vector strmSubjects = new Vector();
		strmSubjects.addElement(new ValueValue(1,1));
		
		Vector classPapers = new Vector();
		classPapers.addElement(new ValueValue(1,1));
		
		Vector classTestTypes = new Vector();
		classTestTypes.addElement(new ValueValue(1,1));
		classTestTypes.addElement(new ValueValue(1,2));
		
		testData.setClasses(classes);
		testData.setStrms(strms);
		testData.setSubjects(subjects);
		testData.setPapers(papers);
		testData.setTestTypes(testTypes);
		testData.setClassStrms(classStrms);
		testData.setSubjectPapers(subjectPapers);
		testData.setStrmSubjects(strmSubjects);
		testData.setClassPapers(classPapers);
		testData.setClassTestTypes(classTestTypes);
		
		return testData;
	}
}
