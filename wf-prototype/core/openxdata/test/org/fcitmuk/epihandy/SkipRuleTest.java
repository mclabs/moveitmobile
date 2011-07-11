package org.fcitmuk.epihandy;

import java.util.Vector;

public class SkipRuleTest {

	public SkipRuleTest(){
		super();
	}
	
	public static Vector getTestRules(){
		/*Vector rules = new Vector();
		
		Condition con = new Condition(toByte(1),toByte(8),EpihandyConstants.OPERATOR_EQUAL,"2");
		Vector conds = new Vector();
		conds.addElement(con);
		
		Vector targs = new Vector();
		targs.addElement(new Byte(toByte(1)));
		targs.addElement(new Byte(toByte(2)));
		targs.addElement(new Byte(toByte(10)));
		
		SkipRule rule = new SkipRule(toByte(1),conds,EpihandyConstants.ACTION_HIDE,targs);
		rules.addElement(rule);
		
		
		con = new Condition(toByte(1),toByte(8),EpihandyConstants.OPERATOR_NOT_EQUAL,"2");
		conds = new Vector();
		conds.addElement(con);
	
		rule = new SkipRule(toByte(2),conds,EpihandyConstants.ACTION_SHOW,targs);
		rules.addElement(rule);
		
		return rules;*/
		
		Condition con = ConditionTest.getTestCondition();
		Vector conds = new Vector();
		conds.addElement(con);
		
		Vector targs = new Vector();
		targs.addElement(new Byte(FormDefTest.getTestFormDefMinusRules().getQuestion("pregnant").getId()));
		
		Vector rules = new Vector();
		SkipRule rule = new SkipRule(toByte(1),conds,EpihandyConstants.ACTION_HIDE,targs);
		rules.addElement(rule);
		
		con = ConditionTest.getTestCondition2();
		conds = new Vector();
		conds.addElement(con);
		rule = new SkipRule(toByte(1),conds,EpihandyConstants.ACTION_SHOW,targs);
		rules.addElement(rule);
		
		return rules;
	}
	
	public static Vector getPatientTestRules(){
		/*Vector rules = new Vector();
		
		Condition con = new Condition(toByte(1),toByte(8),EpihandyConstants.OPERATOR_EQUAL,"2");
		Vector conds = new Vector();
		conds.addElement(con);
		
		Vector targs = new Vector();
		targs.addElement(new Byte(toByte(1)));
		targs.addElement(new Byte(toByte(2)));
		targs.addElement(new Byte(toByte(10)));
		
		SkipRule rule = new SkipRule(toByte(1),conds,EpihandyConstants.ACTION_HIDE,targs);
		rules.addElement(rule);
		
		
		con = new Condition(toByte(1),toByte(8),EpihandyConstants.OPERATOR_NOT_EQUAL,"2");
		conds = new Vector();
		conds.addElement(con);
	
		rule = new SkipRule(toByte(2),conds,EpihandyConstants.ACTION_SHOW,targs);
		rules.addElement(rule);
		
		return rules;*/
		
		Condition con = ConditionTest.getPatientTestCondition1();
		Vector conds = new Vector();
		conds.addElement(con);
		
		Vector targs = new Vector();
		targs.addElement(new Byte(toByte(9))); //The pregnancy question
		
		Vector rules = new Vector();
		SkipRule rule = new SkipRule(toByte(1),conds,EpihandyConstants.ACTION_HIDE,targs);
		rules.addElement(rule);
		
		con = ConditionTest.getPatientTestCondition2();
		conds = new Vector();
		conds.addElement(con);
		rule = new SkipRule(toByte(2),conds,EpihandyConstants.ACTION_SHOW,targs);
		rules.addElement(rule);
		
		return rules;
	}
	
	public static byte toByte(int val){
		return FormDefTest.toByte(val);
	}
}
