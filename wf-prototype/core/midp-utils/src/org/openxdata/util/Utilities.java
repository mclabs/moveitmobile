package org.openxdata.util;

import java.util.Calendar;
import java.util.Date;

public class Utilities {
	
	/** 
	 * Creates a new Globally Unique Identifier.
	 * 
	 * @return - the new guid.
	 */
	/*public static String getNewGuid(){
		return String.valueOf(new java.util.Date().getTime()); //this needs to be replaced with a realistic implementation
	}*/
	
	public static String dateToString(Date d, byte format){
		if(d == null)
			return null;
		
		Calendar cd = Calendar.getInstance(java.util.TimeZone.getDefault()/*java.util.TimeZone.getTimeZone("GMT+830")*/);
		cd.setTime(d);
		String year = "" + cd.get(Calendar.YEAR);
		String month = "" + (cd.get(Calendar.MONTH)+1);
		String day = "" + cd.get(Calendar.DAY_OF_MONTH);
		
		if (month.length()<2)
			month = "0" + month;
		
		if (day.length()<2)
			day = "0" + day;
		
		if(format == 0)
			return day + "-" + month + "-" + year;
		else if(format == 1)
			return month + "-" + day + "-" + year;
		return year + "-" + month + "-" + day;
	}
	
	public static boolean stringToBoolean(String val){
		return stringToBoolean(val,false);
	}
	
	public static boolean stringToBoolean(String val, boolean defaultValue){
		if(val == null) {
			return defaultValue;
		} else {
			return !val.equals("0");
		}
	}
	
	public static String booleanToString(boolean val){
		if(val)
			return "1";
		return "0";
	}
	
	/*public static String getIMEI(){
		String imei = null;
		
		imei = System.getProperty("phone.imei"); //nokia
		
		if(imei == null || imei.trim().length() == 0)
			imei = System.getProperty("com.nokia.IMEI"); //nokia
		
		if(imei == null || imei.trim().length() == 0)
			imei = System.getProperty("com.sonyericsson.imei"); //Sony-Ericsson
		
		if(imei == null || imei.trim().length() == 0)
			imei = System.getProperty("com.samsung.imei"); //Samsung
		
		if(imei == null || imei.trim().length() == 0)
			imei = System.getProperty("com.siemens.imei"); //Siemens
		
		if(imei == null || imei.trim().length() == 0)
			imei = System.getProperty("IMEI"); //Motorola
		
		if(imei == null || imei.trim().length() == 0)
			imei = System.getProperty("com.motorola.IMEI"); //Motorola
		
		return imei;
	}*/
}
