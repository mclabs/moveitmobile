package org.openxdata.forms;

/**
 * This class contains properties specifying the text that should
 * be whitelabeled in the midlet.
 * 
 * See the ant file build-configuration.xml that replaces the tokens
 * with the values in the build.properties and creates the class 
 * in the correct package so it can be used in the application.
 *  
 * @author dagmar@cell-life.org
 */
public class MidletConstants  {
	
	public static final String TITLE = "@midlet.name@ @version.major@.@version.minor@";

}