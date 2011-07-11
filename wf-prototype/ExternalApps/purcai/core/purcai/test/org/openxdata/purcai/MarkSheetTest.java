package org.openxdata.purcai;

import java.util.Vector;

import org.openxdata.purcai.MarkSheet;
import org.openxdata.purcai.StudentIdMark;
import org.openxdata.purcai.StudentIdNameMark;

public class MarkSheetTest {

	private MarkSheetTest(){
		
	}
	
	public static MarkSheet getMarkSheetTest(){
		MarkSheet markSheet = new MarkSheet();
		
		markSheet.setHeader(MarkSheetHeaderTest.getMarkSheetHeaderTest());
		
		Vector studentMarks = new Vector();
		studentMarks.addElement(new StudentIdNameMark(1,"Kitaka Brian",StudentIdMark.NULL_MARK));
		studentMarks.addElement(new StudentIdNameMark(2,"Namubiru Mary",StudentIdMark.NULL_MARK));
		studentMarks.addElement(new StudentIdNameMark(3,"Jonie Gibro",StudentIdMark.NULL_MARK));
		studentMarks.addElement(new StudentIdNameMark(4,"Mago James",StudentIdMark.NULL_MARK));
		studentMarks.addElement(new StudentIdNameMark(5,"Sematta Paul",StudentIdMark.NULL_MARK));
		studentMarks.addElement(new StudentIdNameMark(6,"Kisembo Moses",StudentIdMark.NULL_MARK));
		studentMarks.addElement(new StudentIdNameMark(7,"Abel Rasmusen",StudentIdMark.NULL_MARK));
		studentMarks.addElement(new StudentIdNameMark(8,"Zam Harriet",StudentIdMark.NULL_MARK));
		studentMarks.addElement(new StudentIdNameMark(9,"Yolam Ben",StudentIdMark.NULL_MARK));
		studentMarks.addElement(new StudentIdNameMark(10,"Linda Lumala",StudentIdMark.NULL_MARK));
		
		markSheet.setStudentMarks(studentMarks);
		
		return markSheet;
	}
}
