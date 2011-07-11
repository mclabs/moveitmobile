package org.fcitmuk.epihandy;

public class ConditionTest {
	
	/**
	 * Gets a test condition of if sex is female.
	 * 
	 * @return the condition reference.
	 */
	public static Condition getTestCondition(){
		byte b = Byte.parseByte("7");
		return new Condition(b,b,EpihandyConstants.OPERATOR_EQUAL,"1");
	}
	
	public static Condition getTestCondition2(){
		byte b = Byte.parseByte("7");
		return new Condition(Byte.parseByte("8"),b,EpihandyConstants.OPERATOR_EQUAL,"2");
	}
	
	public static Condition getPatientTestCondition1(){
		byte b = Byte.parseByte("8");
		return new Condition(b,b,EpihandyConstants.OPERATOR_EQUAL,"1");
	}
	
	public static Condition getPatientTestCondition2(){
		byte b = Byte.parseByte("8");
		return new Condition(b,b,EpihandyConstants.OPERATOR_EQUAL,"2");
	}
}
