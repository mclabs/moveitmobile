package org.openxdata.purcai;

import org.openxdata.purcai.MarkSheetHeader;

public class MarkSheetHeaderTest {

	private MarkSheetHeaderTest(){
		
	}
	
	public static MarkSheetHeader getMarkSheetHeaderTest(){
		
		MarkSheetHeader header = new MarkSheetHeader();
		
		header.setClassStrmId(1);
		header.setSubjectPaperId(1);
		header.setTestTypeId(1);
		header.setOutOf((byte)100);
		
		return header;
	}
	
	public static MarkSheetHeader getMarkSheetHeaderTest2(){
		
		MarkSheetHeader header = new MarkSheetHeader();
		
		header.setClassStrmId(1);
		header.setSubjectPaperId(1);
		header.setTestTypeId(2);
		header.setOutOf((byte)100);
		
		return header;
	}
}
