package org.openxdata.util;


/** 
 * This interface is the means of communication betwen the AlerMessage class and api users. 
 * 
 * @author Daniel
 *
 */
public interface AlertMessageListener {
	
	public static byte MSG_OK = 1;
	public static byte MSG_CANCEL = 2;
	
	public void onAlertMessage(byte msg);
}
