package org.openxdata.forms;

/**
 * This class contains properties specifying configurable UI settings
 * such as "Single Question Edit"
 * 
 * See the ant file build-configuration.xml that replaces the tokens
 * with the values in the antbuild.properties and creates the class 
 * in the correct package so it can be used in the application.
 *  
 * @author dagmar@cell-life.org
 */
public class FormsConstants  {
	
	public static final boolean SINGLE_QUESTION_EDIT = @single.question.edit@;
	public static final boolean QUESTION_NUMBERING = @question.numbering@;
	public static final boolean OK_ON_RIGHT = @ok.on.right@;
	public static final boolean IS_HIDE_STUDIES = @is.hide.studies@;
	public static final boolean USE_STUDY_NUMERIC_ID = @use.study.numeric.id@;
	public static final boolean MAIN_MENU = @main.menu@;
}