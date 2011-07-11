package org.openxdata.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.openxdata.db.util.Persistent;


/**
 * A condition which is part of a rule. For definition of a rule, go to the Rule class.
 * E.g. If sex is Male. If age is greater than than 4. etc
 *
 *@author Daniel Kayiwa
 */
public class Condition implements Persistent{

	/** The unique identifier of the question referenced by this condition. */
	private byte questionId = OpenXdataConstants.NULL_ID;

	/** The operator of the condition. Eg Equal to, Greater than, etc. */
	private byte operator = OpenXdataConstants.OPERATOR_NULL;

	/** The aggregate function. Eg Length, Value. */
	private byte function = OpenXdataConstants.FUNCTION_VALUE;

	/** The value checked to see if the condition is true or false.
	 * For the above example, the value would be 4 or the id of the Male option.
	 * For a list of options this value is the option id, not the value or text value.
	 */
	private String value = OpenXdataConstants.EMPTY_STRING;

	private String secondValue = OpenXdataConstants.EMPTY_STRING;

	/** The unique identifier of a condition. */
	private byte id = OpenXdataConstants.NULL_ID;

	/** Creates a new condition object. */
	public Condition(){

	}

	/** Copy constructor. */
	public Condition(Condition condition){
		this(condition.getId(),condition.getQuestionId(),condition.getOperator(),condition.getFunction(),condition.getValue());
	}

	/**
	 * Creates a new condition object from its parameters. 
	 * 
	 * @param id - the numeric identifier of the condition.
	 * @param questionId - the numeric identifier of the question.
	 * @param operator - the condition operator.
	 * @param value - the value to be equated to.
	 */
	public Condition(byte id,byte questionId, byte operator, byte function, String value) {
		this();
		setQuestionId(questionId);
		setOperator(operator);
		setFunction(function);
		setValue(value);
		setId(id);
	}

	public byte getOperator() {
		return operator;
	}
	public void setOperator(byte operator) {
		this.operator = operator;
	}
	public byte getQuestionId() {
		return questionId;
	}
	public void setQuestionId(byte questionId) {
		this.questionId = questionId;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public byte getId() {
		return id;
	}
	public void setId(byte conditionId) {
		this.id = conditionId;
	}

	public byte getFunction() {
		return function;
	}

	public void setFunction(byte function) {
		this.function = function;
	}

	public String getSecondValue() {
		return secondValue;
	}

	public void setSecondValue(String secondValue) {
		this.secondValue = secondValue;
	}

	/**
	 * Test if a condition is true or false.
	 */
	public boolean isTrue(FormData data, boolean validation){
		String tempValue = value;
		boolean ret = true;

		try{
			QuestionData qn = data.getQuestion(this.questionId);

			if(value.startsWith(data.getDef().getVariableName()+"/")){
				QuestionData qn2 = data.getQuestion("/"+value);
				if(qn2 != null){
					value = qn2.getValueAnswer();
					if(value == null || value.trim().length() == 0){
						value = tempValue;
						if(qn.getAnswer() == null || qn.getValueAnswer().trim().length() == 0)
							return true; //Both questions not answered yet
						return false;
					}
					else if(qn.getAnswer() == null || qn.getValueAnswer().trim().length() == 0){
						if(qn.getDef().getType() != QuestionDef.QTN_TYPE_REPEAT){
							value = tempValue;
							return false;
						}
					}
				}
			}

			switch(qn.getDef().getType()){
			case QuestionDef.QTN_TYPE_TEXT:
				ret = isTextTrue(qn,validation);
				break;
			case QuestionDef.QTN_TYPE_REPEAT:
			case QuestionDef.QTN_TYPE_NUMERIC:
				ret = isNumericTrue(qn,validation);
				break;
			case QuestionDef.QTN_TYPE_DATE:
				ret = isDateTrue(qn,validation);
				break;
			case QuestionDef.QTN_TYPE_DATE_TIME:
				ret = isDateTimeTrue(qn,validation);
				break;
			case QuestionDef.QTN_TYPE_DECIMAL:
				ret = isDecimalTrue(qn,validation);
				break;
			case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
			case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
				ret = isListExclusiveTrue(qn,validation);
				break;
			case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
				ret = isListMultipleTrue(qn,validation);
				break;
			case QuestionDef.QTN_TYPE_TIME:
				ret = isTimeTrue(qn,validation);
				break;
			case QuestionDef.QTN_TYPE_BOOLEAN:
				ret = isTextTrue(qn,validation);
				break;
			}

		}
		catch(Exception ex){
			//ex.printStackTrace();
		}

		value = tempValue;

		return ret;
	}

	private void truncateDecimalPoints() {
		if (value != null && value.indexOf('.') > 0)
			value = value.substring(0,value.indexOf('.'));
		
		if (secondValue != null && secondValue.indexOf('.') > 0)
			secondValue = secondValue.substring(0,secondValue.indexOf('.'));
	}
	
	private String removeDecimalPoints(String value) {
		String newValue = null;
		if (value != null) {
			int indexOfDecimal = value.indexOf('.');
			if (indexOfDecimal > 0)
				newValue = value.substring(0, indexOfDecimal);
			if (indexOfDecimal < value.length())
				newValue = newValue + value.substring(indexOfDecimal+1);
			while (newValue.length() != 10) {
				newValue = newValue + "0"; // pad the values so they are comparable - i.e. 99.09 to 99.8
			}
		}
		return newValue;
	}
	
	private boolean isNumericTrue(QuestionData data, boolean validation){
		//return value.equals(data.getTextAnswer());

		try{			
			if(data.getValueAnswer() == null || data.getValueAnswer().trim().length() == 0){
				if(validation && operator == OpenXdataConstants.OPERATOR_IS_NOT_NULL)
					return false;
				else if(validation || operator == OpenXdataConstants.OPERATOR_NOT_EQUAL ||
						operator == OpenXdataConstants.OPERATOR_NOT_BETWEEN)
					return true;
				return operator == OpenXdataConstants.OPERATOR_IS_NULL;
			}
			else if(operator == OpenXdataConstants.OPERATOR_IS_NOT_NULL)
				return true;
			
			truncateDecimalPoints();

			long answer = Long.parseLong(data.getValueAnswer());
			long longValue = Long.parseLong(value);

			long secondLongValue = longValue;
			if(secondValue != null && secondValue.trim().length() > 0)
				secondLongValue = Long.parseLong(secondValue);

			if(operator == OpenXdataConstants.OPERATOR_EQUAL)
				return longValue == answer;
			else if(operator == OpenXdataConstants.OPERATOR_NOT_EQUAL)
				return longValue != answer;
			else if(operator == OpenXdataConstants.OPERATOR_LESS)
				return answer < longValue;
			else if(operator == OpenXdataConstants.OPERATOR_LESS_EQUAL)
				return answer < longValue || longValue == answer;
			else if(operator == OpenXdataConstants.OPERATOR_GREATER)
				return answer > longValue;
				else if(operator == OpenXdataConstants.OPERATOR_GREATER_EQUAL)
					return answer > longValue || longValue == answer;
					else if(operator == OpenXdataConstants.OPERATOR_BETWEEN)
						return answer > longValue && longValue < secondLongValue;
						else if(operator == OpenXdataConstants.OPERATOR_NOT_BETWEEN)
							return !(answer > longValue && longValue < secondLongValue);
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}

		return false;
	}

//	TODO Should this test be case sensitive?
	private boolean isTextTrue(QuestionData data, boolean validation){
		//return value.equals(data.getTextAnswer());

		Object answer = data.getValueAnswer();

		if(function == OpenXdataConstants.FUNCTION_VALUE){
			if(answer == null || answer.toString().trim().length() == 0){
				if(validation && operator == OpenXdataConstants.OPERATOR_IS_NOT_NULL)
					return false;
				else if(validation || operator == OpenXdataConstants.OPERATOR_NOT_EQUAL ||
						operator == OpenXdataConstants.OPERATOR_NOT_START_WITH ||
						operator == OpenXdataConstants.OPERATOR_NOT_CONTAIN)
					return true;

				return operator == OpenXdataConstants.OPERATOR_IS_NULL;
			}
			else if(operator == OpenXdataConstants.OPERATOR_IS_NOT_NULL)
				return true;

			if(operator == OpenXdataConstants.OPERATOR_EQUAL)
				return value.equals(data.getValueAnswer());
			else if(operator == OpenXdataConstants.OPERATOR_NOT_EQUAL)
				return !value.equals(data.getValueAnswer());
			else if(operator == OpenXdataConstants.OPERATOR_STARTS_WITH)
				return answer.toString().startsWith(value);
			else if(operator == OpenXdataConstants.OPERATOR_NOT_START_WITH)
				return !answer.toString().startsWith(value);
			else if(operator == OpenXdataConstants.OPERATOR_CONTAINS)
				return answer.toString().indexOf(value) >= 0;
				else if(operator == OpenXdataConstants.OPERATOR_NOT_CONTAIN)
					return !(answer.toString().indexOf(value) >= 0);
		}
		else{
			if(answer == null || answer.toString().trim().length() == 0)
				return true;
			
			long len1 = 0, len2 = 0, len = 0;
			if(value != null && value.trim().length() > 0)
				len1 = Long.parseLong(value);
			if(secondValue != null && secondValue.trim().length() > 0)
				len2 = Long.parseLong(secondValue);

			len = answer.toString().trim().length();
			
			if(operator == OpenXdataConstants.OPERATOR_EQUAL)
				return len == len1;
			else if(operator == OpenXdataConstants.OPERATOR_NOT_EQUAL)
				return len != len1;
			else if(operator == OpenXdataConstants.OPERATOR_LESS)
				return len < len1;
			else if(operator == OpenXdataConstants.OPERATOR_LESS_EQUAL)
				return len <= len1;
			else if(operator == OpenXdataConstants.OPERATOR_GREATER)
				return len > len1;
			else if(operator == OpenXdataConstants.OPERATOR_GREATER_EQUAL)
				return len >= len1;
			else if(operator == OpenXdataConstants.OPERATOR_BETWEEN)
				return len > len1 && len < len2;
			else if(operator == OpenXdataConstants.OPERATOR_NOT_BETWEEN)
				return !(len > len1 && len < len2);
		}

		return false;
	}

	/**
	 * Tests if the passed parameter date value is equal to the value of the condition.
	 * 
	 * @param data - passed parameter date value.
	 * @return - true when the two values are the same, else false.
	 */
	private boolean isDateTrue(QuestionData qtn, boolean validation){
		//return value.equals(data.getTextAnswer());

		try{
			if(qtn.getAnswer() == null || qtn.getAnswer().toString().trim().length() == 0){
				if(validation && operator == OpenXdataConstants.OPERATOR_IS_NOT_NULL)
					return false;
				else if(validation || operator == OpenXdataConstants.OPERATOR_NOT_EQUAL ||
						operator == OpenXdataConstants.OPERATOR_NOT_BETWEEN)
					return true;
				return operator == OpenXdataConstants.OPERATOR_IS_NULL;
			}
			else if(operator == OpenXdataConstants.OPERATOR_IS_NOT_NULL)
				return true;

			if(!(qtn.getAnswer() instanceof Date) && qtn.getAnswer().equals(qtn.getDef().getDefaultValue()))
				return (validation ? true : false);

			TimeZone timeZone = java.util.TimeZone.getDefault();
			Calendar calenderAnswer = Calendar.getInstance(timeZone/*java.util.TimeZone.getTimeZone("GMT")*/); //"GMT"//+830
			calenderAnswer.setTime((Date)qtn.getAnswer());


			Calendar calenderdateValue = Calendar.getInstance(timeZone/*java.util.TimeZone.getTimeZone("GMT")*/); //"GMT"//+830
			if(isDateFunction(value))
				calenderdateValue.setTime(getDateFunctionValue(value));	
			else
				calenderdateValue.setTime(fromString2Date(value));


			Calendar calenderdateSecondDateValue = Calendar.getInstance(timeZone/*java.util.TimeZone.getTimeZone("GMT")*/); //"GMT"//+830
			if(secondValue != null && secondValue.trim().length() > 0){
				if(isDateFunction(secondValue))
					calenderdateSecondDateValue.setTime(getDateFunctionValue(secondValue));	
				else
					calenderdateSecondDateValue.setTime(fromString2Date(secondValue));
			}

			if(operator == OpenXdataConstants.OPERATOR_EQUAL)
				return calenderdateValue.equals(calenderAnswer);
			else if(operator == OpenXdataConstants.OPERATOR_NOT_EQUAL)
				return !calenderdateValue.equals(calenderAnswer);
			else if(operator == OpenXdataConstants.OPERATOR_LESS)
				return calenderAnswer.before(calenderdateValue);
			else if(operator == OpenXdataConstants.OPERATOR_LESS_EQUAL)
				return calenderAnswer.before(calenderdateValue) || calenderdateValue.equals(calenderAnswer);
			else if(operator == OpenXdataConstants.OPERATOR_GREATER)
				return calenderAnswer.after(calenderdateValue);
			else if(operator == OpenXdataConstants.OPERATOR_GREATER_EQUAL)
				return calenderAnswer.after(calenderdateValue) || calenderdateValue.equals(calenderAnswer);
			else if(operator == OpenXdataConstants.OPERATOR_BETWEEN)
				return calenderAnswer.after(calenderdateValue) && calenderdateValue.before(calenderdateSecondDateValue);
			else if(operator == OpenXdataConstants.OPERATOR_NOT_BETWEEN)
				return !(calenderAnswer.after(calenderdateValue) && calenderdateValue.before(calenderdateSecondDateValue));
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}

		return false;

		/*try{
			if(data.getValueAnswer() == null || data.getValueAnswer().trim().length() == 0){
				if(operator == EpihandyConstants.OPERATOR_NOT_EQUAL ||
				   operator == EpihandyConstants.OPERATOR_NOT_BETWEEN)
						return true;
				return operator == EpihandyConstants.OPERATOR_IS_NULL;
			}

			Date answer = DateUtil.getDateTimeFormat().parse(data.getValueAnswer());
			Date dateValue = DateUtil.getDateTimeFormat().parse(value);

			Date secondDateValue = dateValue;
			if(secondValue != null && secondValue.trim().length() > 0)
				secondDateValue = DateUtil.getDateTimeFormat().parse(secondValue);

			if(operator == EpihandyConstants.OPERATOR_EQUAL)
				return dateValue.equals(answer);
			else if(operator == EpihandyConstants.OPERATOR_NOT_EQUAL)
				return !dateValue.equals(answer);
			else if(operator == EpihandyConstants.OPERATOR_LESS)
				return answer.before(dateValue);
			else if(operator == EpihandyConstants.OPERATOR_LESS_EQUAL)
				return answer.before(dateValue) || dateValue.equals(answer);
			else if(operator == EpihandyConstants.OPERATOR_GREATER)
				return answer.after(dateValue);
			else if(operator == EpihandyConstants.OPERATOR_GREATER_EQUAL)
				return answer.after(dateValue) || dateValue.equals(answer);
			else if(operator == EpihandyConstants.OPERATOR_BETWEEN)
				return answer.after(dateValue) && dateValue.before(secondDateValue);
			else if(operator == EpihandyConstants.OPERATOR_NOT_BETWEEN)
				return !(answer.after(dateValue) && dateValue.before(secondDateValue));
		}
		catch(Exception ex){
			ex.printStackTrace();
		}

		return false;*/
	}

	private boolean isDateFunction(String value){
		if(value == null)
			return false;

		return (value.toLowerCase().equals("'now()'") || value.toLowerCase().equals("'date()'")
				||value.toLowerCase().equals("'getdate()'") || value.toLowerCase().equals("'today()'"));
	}

	private Date getDateFunctionValue(String function){
		return new Date();
	}

	private Date fromString2Date(String value){
		return new Date(); //TODO needs to parse this and create proper date;
	}

	private boolean isDateTimeTrue(QuestionData data, boolean validation){
		return isDateTrue(data,validation);//value.equals(data.getTextAnswer());
	}

	private boolean isTimeTrue(QuestionData data, boolean validation){
		return value.equals(data.getTextAnswer());
	}

	private boolean isListMultipleTrue(QuestionData data, boolean validation){
		/*if(data.answerContainsValue(value))
			return true;
		return value.equals(data.getTextAnswer());*/

		try{
			if(data.getValueAnswer() == null || data.getValueAnswer().trim().length() == 0){
				if(validation && operator == OpenXdataConstants.OPERATOR_IS_NOT_NULL)
					return false;
				else if(validation || operator == OpenXdataConstants.OPERATOR_NOT_EQUAL || 
						operator == OpenXdataConstants.OPERATOR_NOT_IN_LIST)
					return true;
				return operator == OpenXdataConstants.OPERATOR_IS_NULL;
			}
			else if(operator == OpenXdataConstants.OPERATOR_IS_NOT_NULL)
				return true;
			//return qtn.getValueAnswer().contains(value);

			switch(operator){
			case OpenXdataConstants.OPERATOR_EQUAL:
				return data.getValueAnswer().toString().indexOf(value) >= 0;//data.getValueAnswer().equals(value);
			case OpenXdataConstants.OPERATOR_NOT_EQUAL:
				return !(data.getValueAnswer().indexOf(value) >= 0);//!data.getValueAnswer().equals(value);
			case OpenXdataConstants.OPERATOR_IN_LIST:
				return value.indexOf(data.getValueAnswer()) >= 0;
			case OpenXdataConstants.OPERATOR_NOT_IN_LIST:
				return !(value.indexOf(data.getValueAnswer()) >= 0);
			default:
				return false;
			}
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}
		return false;
	}

	private boolean isListExclusiveTrue(QuestionData data, boolean validation){

		//If any value is null, we assume false.
		//Therefore OPERATOR_NOT_EQUAL will always return true
		//while OPERATOR_EQUAL returns false.
		//This will help make conditions false when any value is not yet filled.
		/*if(data.getOptionAnswerIndices() == null || value == null)
			return operator != EpihandyConstants.OPERATOR_EQUAL;

		//For the sake of performance, we dont compare the actual value.
		//We instead use the index.		
		byte val1 = Byte.parseByte(data.getOptionAnswerIndices().toString());
		val1 += 1;

		byte val2 = Byte.parseByte(value);

		switch(operator){
		case EpihandyConstants.OPERATOR_EQUAL:
			return val1 == val2;
		case EpihandyConstants.OPERATOR_NOT_EQUAL:
			return val1 != val2;
		default:
			return false;
		}*/

		try{
			if(data.getValueAnswer() == null || data.getValueAnswer().trim().length() == 0){
				//return operator != PurcConstants.OPERATOR_EQUAL;
				if(validation && operator == OpenXdataConstants.OPERATOR_IS_NOT_NULL)
					return false;
				else if(validation || operator == OpenXdataConstants.OPERATOR_NOT_EQUAL || 
						operator == OpenXdataConstants.OPERATOR_NOT_IN_LIST)
					return true;
				return operator == OpenXdataConstants.OPERATOR_IS_NULL;
			}
			else if(operator == OpenXdataConstants.OPERATOR_IS_NOT_NULL)
				return true;

			switch(operator){
			case OpenXdataConstants.OPERATOR_EQUAL:
				return data.getValueAnswer().equals(value);
			case OpenXdataConstants.OPERATOR_NOT_EQUAL:
				return !data.getValueAnswer().equals(value);
			case OpenXdataConstants.OPERATOR_IN_LIST:
				return value.indexOf(data.getValueAnswer()) > 0;
			case OpenXdataConstants.OPERATOR_NOT_IN_LIST:
				return !(value.indexOf(data.getValueAnswer()) > 0);
			default:
				return false;
			}
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}

		return false;
	}

	private boolean isDecimalTrue(QuestionData data, boolean validation){
		//return value.equals(data.getTextAnswer());

		try{
			if(data.getValueAnswer() == null || data.getValueAnswer().trim().length() == 0){
				if(validation && operator == OpenXdataConstants.OPERATOR_IS_NOT_NULL)
					return false;
				else if(validation || operator == OpenXdataConstants.OPERATOR_NOT_EQUAL ||
						operator == OpenXdataConstants.OPERATOR_NOT_BETWEEN)
					return true;
				return operator == OpenXdataConstants.OPERATOR_IS_NULL;
			}
			else if(operator == OpenXdataConstants.OPERATOR_IS_NOT_NULL)
				return true;
			
			
			//Since we store decimals as strings, we can almost safely do the equality comparison
			//when still in string format. One disadvantage of of this is that for instance
			// 07 != 7 when doing string comparison.
			if(operator == OpenXdataConstants.OPERATOR_EQUAL)
				return value.equals(data.getValueAnswer());
			
			long /*float*/ answer = Long.parseLong(removeDecimalPoints(data.getValueAnswer())); //Float.parseFloat(data.getValueAnswer());
			long /*float*/ floatValue = Long.parseLong(removeDecimalPoints(value)); //Float.parseFloat(value);

			long /*float*/ secondFloatValue = floatValue;
			if(secondValue != null && secondValue.trim().length() > 0)
				secondFloatValue = Long.parseLong(removeDecimalPoints(secondValue)); //Float.parseFloat(secondValue);
			
			if(operator == OpenXdataConstants.OPERATOR_NOT_EQUAL)
				return floatValue != answer;
			else if(operator == OpenXdataConstants.OPERATOR_LESS)
				return answer < floatValue;
			else if(operator == OpenXdataConstants.OPERATOR_LESS_EQUAL)
				return answer < floatValue || floatValue == answer;
			else if(operator == OpenXdataConstants.OPERATOR_GREATER)
				return answer > floatValue;
			else if(operator == OpenXdataConstants.OPERATOR_GREATER_EQUAL)
				return answer > floatValue || floatValue == answer;
			else if(operator == OpenXdataConstants.OPERATOR_BETWEEN)
				return answer > floatValue && floatValue < secondFloatValue;
			else if(operator == OpenXdataConstants.OPERATOR_NOT_BETWEEN)
				return !(answer > floatValue && floatValue < secondFloatValue);
		}
		catch(Exception ex){
			//ex.printStackTrace();
		}

		return false;
	}

	/** 
	 * Reads the condition object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setId(dis.readByte());
		setQuestionId(dis.readByte());
		setOperator(dis.readByte());
		setValue(dis.readUTF());
		setFunction(dis.readByte());
	}

	/** 
	 * Writes the Condition object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(getId());
		dos.writeByte(getQuestionId());
		dos.writeByte(getOperator());
		dos.writeUTF(getValue());
		dos.writeByte(getFunction());
	}

	public String getValue(FormData data){
		if(value.startsWith(data.getDef().getVariableName()+"/")){
			QuestionData qn = data.getQuestion("/"+value);
			if(qn != null)
				return qn.getValueAnswer();
		}
		return value;
	}
}