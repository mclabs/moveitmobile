package org.openxdata.util;

import org.openxdata.model.MenuTextList;


/**
 * 
 * @author daniel
 *
 */
public class MenuText {

	private static MenuTextList menuTextList = new MenuTextList();
	
	public static void setMenuTextList(MenuTextList menuTextList){
		MenuText.menuTextList = menuTextList;
	}
	
	private static String getText(short id, String defaultText){
		String text = menuTextList.getText(new Short(id));
		if(text == null)
			text = defaultText;
		return text;
	}
	
	public static String LOGIN(){
		return getText((short)1,"Login");
	}
	
	public static String USER_NAME(){
		return getText((short)2,"User Name:");
	}
	
	public static String PASSWORD(){
		return getText((short)3,"Password:");
	}
	
	public static String EXIT(){
		return getText((short)4,"Exit");
	}
	
	public static String CANCEL(){
		return getText((short)5,"Cancel");
	}
	
	public static String OK(){
		return getText((short)6,"OK");
	}
	
	public static String EDIT(){
		return getText((short)7,"Edit");
	}
	
	public static String NEW(){
		return getText((short)8,"New");
	}
	
	public static String SAVE(){
		return getText((short)9,"Save");
	}
	
	public static String DELETE(){
		return getText((short)10,"Delete");
	}
	
	public static String BACK(){
		return getText((short)11,"Back");
	}
	
	public static String YES(){
		return getText((short)12,"Yes");
	}
	
	public static String NO(){
		return getText((short)13,"No");
	}
	
	public static String BACK_TO_LIST(){
		return getText((short)14,"Back to list");
	}
	
	public static String NEXT(){
		return getText((short)15,"Next");
	}
	
	public static String PREVIOUS(){
		return getText((short)16,"Previous");
	}
	
	public static String FIRST(){
		return getText((short)17,"First");
	}
	
	public static String LAST(){
		return getText((short)18,"Last");
	}
	
	public static String SELECT(){
		return getText((short)19,"Select");
	}
	
	public static String MAIN_MENU(){
		return getText((short)20,"Main Menu");
	}
	
	public static String SELECT_STUDY(){
		return getText((short)21,"Select Study");
	}
	
	public static String SELECT_FORM(){
		return getText((short)22,"Select Form");
	}
	
	public static String DOWNLOAD_STUDIES(){
		return getText((short)23,"Download Studies");
	}
	
	public static String DOWNLOAD_FORMS(){
		return getText((short)24,"Download Forms");
	}
	
	public static String UPLOAD_DATA(){
		return getText((short)25,"Upload Data");
	}
	
	public static String SETTINGS(){
		return getText((short)26,"Settings");
	}
	
	public static String LOGOUT(){
		return getText((short)27,"Logout");
	}
	
	public static String EXIT_PROMPT(){
		return getText((short)28,"Do you really want to exit the application and lose any unsaved changes if any?");
	}
	
	public static String GENERAL(){
		return getText((short)29,"General");
	}
	
	public static String DATE_FORMAT(){
		return getText((short)30,"Date Format");
	}
	
	public static String MULTIMEDIA(){
		return getText((short)31,"Multimedia");
	}
	
	public static String LANGUAGE(){
		return getText((short)32,"Language");
	}
	
	public static String CONNECTION(){
		return getText((short)33,"Connection");
	}
	
	public static String SINGLE_QUESTION_EDIT(){
		return getText((short)34,"Single Question Edit");
	}
	
	public static String NUMBERING(){
		return getText((short)35,"Numbering");
	}
	
	public static String OK_ON_RIGHT(){
		return getText((short)36,"OK on Right");
	}
	
	public static String DELETE_AFTER_UPLOAD(){
		return getText((short)37,"Delete After Upload");
	}
	
	public static String DAY_FIRST(){
		return getText((short)38,"Day First");
	}
	
	public static String MONTH_FIRST(){
		return getText((short)39,"Month First");
	}
	
	public static String YEAR_FIRST(){
		return getText((short)40,"Year First");
	}
	
	public static String DOWNLOAD(){
		return getText((short)41,"Download");
	}
	
	public static String HTTP(){
		return getText((short)42,"Http");
	}
	
	public static String BLUETOOTH(){
		return getText((short)43,"Bluetooth");
	}
	
	public static String PICTURE_FORMAT(){
		return getText((short)44,"Picture Format eg jpeg");
	}
	
	public static String PICTURE_WIDTH(){
		return getText((short)45,"Picture Width eg 320");
	}
	
	public static String PICTURE_HEIGHT(){
		return getText((short)46,"Picture Height eg 240");
	}
	
	public static String VIDEO_FORMAT(){
		return getText((short)47,"Video Format");
	}
	
	public static String AUDIO_FORMAT(){
		return getText((short)48,"Audio Format");
	}
	
	public static String ENCODINGS(){
		return getText((short)49,"Encodings");
	}
	
	public static String STUDY(){
		return getText((short)50,"Study:");
	}
	
	public static String UPLOAD_BEFORE_DOWNLOAD_PROMPT(){
		return getText((short)51,"has data to upload. Please first upload or delete this data before getting new form definitions.");
	}
	
	public static String DOWNLOAD_STUDY_FORMS_PROMPT(){
		return getText((short)52,"Do you really want to download forms in Study: ?");
	}
	
	public static String DOWNLOAD_FORMS_PROMPT(){
		return getText((short)53,"Do you really want to download forms?");
	}
	
	public static String UN_UPLOADED_DATA_PROMPT(){
		return getText((short)54,"There is data which is not yet uploaded to the server. Please first upload or delete this data before getting a new list of");
	}
	
	public static String FORMS(){
		return getText((short)55,"Forms");
	}
	
	public static String STUDIES(){
		return getText((short)56,"Studies");
	}
	
	public static String DOWNLOAD_STUDIES_PROMPT(){
		return getText((short)57,"Do you really want to download the list of studies? If yes, you will have to redownload all the forms.");
	}
	
	public static String DOWNLOAD_LANGUAGES_PROMPT(){
		return getText((short)58,"Do you really want to download the list of languages?");
	}
	
	public static String DOWNLOAD_FORMS_FIRST(){
		return getText((short)59,"Please first download forms.");
	}
	
	public static String UPLOAD_DATA_PROMPT(){
		return getText((short)60,"Do you really want to upload the collected data?");
	}
	
	public static String UPLOAD_FORM_DATA_PROMPT(){
		return getText((short)60,"Do you really want to upload the selected forms data?");
	}

	public static String STUDY_LIST_DOWNLOAD(){
		return getText((short)61,"Study List Download");
	}
	
	public static String DOWNLOADING_STUDY_LIST(){
		return getText((short)62,"Downloading Study List");
	}
	
	public static String FORM_DOWNLOAD(){
		return getText((short)63,"Form Download");
	}
	
	public static String DOWNLOADING_FORMS(){
		return getText((short)64,"Downloading Forms");
	}
	
	public static String DOWNLOADING_USERS(){
		return getText((short)65,"Downloading Users");
	}
	
	public static String LANGUAGE_DOWNLOAD(){
		return getText((short)66,"Language Download");
	}
	
	public static String DOWNLOADING_LANGUAGES(){
		return getText((short)67,"Downloading Languages");
	}
	
	public static String DATA_UPLOAD(){
		return getText((short)68,"Data Upload");
	}
	
	public static String UPLOADING_DATA(){
		return getText((short)69,"Uploading Data");
	}
	
	public static String NO_UPLOAD_DATA(){
		return getText((short)70,"No data to upload.");
	}
	
	public static String PROBLEM_SAVING_DOWNLOAD(){
		return getText((short)71,"Problem saving downloaded data");
	}
	
	public static String STUDY_DOWNLOAD_SAVED(){
		return getText((short)72,"Study(s) downloaded and saved successfully");
	}
	
	public static String USER_DOWNLOAD_SAVED(){
		return getText((short)73,"User(s) downloaded and saved successfully");
	}
	
	public static String NO_SERVER_STUDY_FORMS(){
		return getText((short)74,"The server has no forms in study:");
	}
	
	public static String FORM_DOWNLOAD_SAVED(){
		return getText((short)75,"Form(s) downloaded and saved successfully");
	}
	
	public static String NO_LANGUAGES(){
		return getText((short)76,"The server has no languages");
	}
	
	public static String LANGUAGE_DOWNLOAD_SAVED(){
		return getText((short)77,"Languages(s) downloaded and saved successfully");
	}
	
	public static String DATA_UPLOAD_PROBLEM(){
		return getText((short)78,"Problem uploading data");
	}
	
	public static String DATA_UPLOAD_SUCCESS(){
		return getText((short)79,"Data uploaded successfully.");
	}
	
	public static String DATA_UPLOAD_FAILURE(){
		return getText((short)80,"Failed to upload data.");
	}
	
	public static String PROBLEM_CLEANING_STORE(){
		return getText((short)81,"Data uploaded but problem occured cleaning local store");
	}
	
	public static String UNKNOWN_UPLOAD(){
		return getText((short)82,"Unknown upload");
	}
	
	public static String CONNECTION_TYPE(){
		return getText((short)83,"Connection Type");
	}
	
	public static String BLUETOOTH_SERVICE_ID(){
		return getText((short)84,"Bluetooth Service ID:");
	}
	
	public static String BLUETOOTH_DEVICE_NAME(){
		return getText((short)85,"Bluetooth Device Name:");
	}
	
	public static String PROBLEM_HANDLING_REQUEST(){
		return getText((short)86,"Problem handling request:");
	}
	
	public static String CONNECTING_TO_SERVER(){
		return getText((short)87,"Connecting to server.........");
	}
	
	public static String TRANSFERING_DATA(){
		return getText((short)88,"Connected to server. Transfering data........");
	}
	
	public static String PROBLEM_HANDLING_STREAMS(){
		return getText((short)89,"Problem handling streams");
	}
	
	public static String SERVER_PROCESS_FAILURE(){
		return getText((short)90,"Problems occured while processing request on server.");
	}
	
	public static String SERVER_INVALID_URL(){
		return getText((short)91,"Please check your Server URL under Http Connection Settings.");
	}
	
	public static String ACCESS_DENIED(){
		return getText((short)91,"Access denied");
	}
	
	public static String RESPONSE_CODE_FAIL(){
		return getText((short)92,"Response code not OK=");
	}
	
	public static String DEVICE_PERMISSION_DENIED(){
		return getText((short)93,"You do not have sufficient privileges to perform this operation. Please contact your administrator.");
	}
	
	public static String GETTING_BLUETOOTH_URL(){
		return getText((short)94,"Getting bluetooth service url.........");
	}
	
	public static String OPENING_BLUETOOTH_CONNECTION(){
		return getText((short)95,"Opening bluetooth service connection");
	}
	
	public static String GETTINGS_STREAM(){
		return getText((short)96,"Getting streams");
	}
	
	public static String OPEN_CONNECTION_FAIL(){
		return getText((short)97,"Failed to open connection");
	}
	
	public static String PROBLEM_OPENING_STREAMS(){
		return getText((short)98,"Problem opening streams");
	}
	
	public static String OPERATION_CANCEL_PROMPT(){
		return getText((short)99,"Do you really want to cancel this operation?");
	}
	
	public static String CONNECTION_SETTINGS(){
		return getText((short)100,"Connection Settings");
	}
	
	public static String PLAYING(){
		return getText((short)101,"Playing...........");
	}
	
	public static String INIT_PROBLEM(){
		return getText((short)102,"Problem initializing=");
	}
	
	public static String VIEW_PROBLEM(){
		return getText((short)103,"Problem viewing=");
	}
	
	public static String NOT_SUPPORTED_FEATURE(){
		return getText((short)104,"Feature not supported");
	}
	
	public static String NO_VIDEO_CONTROL(){
		return getText((short)105,"Not supported by this device. (No video control)");
	}
	
	public static String EDIT_PROBLEM(){
		return getText((short)106,"Problem editing:");
	}
	
	public static String RECORDING(){
		return getText((short)107,"Recording...........");
	}
	
	public static String PLAY_PROBLEM(){
		return getText((short)108,"Problem playing:");
	}
	
	public static String DELETE_PROMPT(){
		return getText((short)109,"Do you really want to delete this");
	}
	
	public static String IMAGE_SAVE_PROBLEM(){
		return getText((short)110,"Problem saving image:");
	}
	
	public static String RECODING_SAVE_PROBLEM(){
		return getText((short)111,"Problem saving recording:");
	}
	
	public static String DATA_LIST(){
		return getText((short)112,"Data List");
	}
	
	public static String DATA_LIST_DISPLAY_PROBLEM(){
		return getText((short)113,"Problem showing form data list.");
	}
	
	public static String FORM_DELETE_PROMPT(){
		return getText((short)114,"Do you really want to delete form:");
	}
	
	public static String FORM_SAVE_SUCCESS(){
		return getText((short)115,"Form Saved Successfully.");
	}
	
	public static String NO_SELECTED_STUDY(){
		return getText((short)116,"No study selected");
	}
	
	public static String NO_STUDY_FORMS(){
		return getText((short)117,"No forms in current study");
	}
	
	public static String FORM_DATA_DISPLAY_PROBLEM(){
		return getText((short)118,"Problem displaying form data. Possibly data out of sync with form definition.");
	}
	
	public static String NEXT_PAGE(){
		return getText((short)119,"Next Page");
	}
	
	public static String PREVIOUS_PAGE(){
		return getText((short)120,"Previous Page");
	}
	
	public static String FORM_DISPLAY_PROBLEM(){
		return getText((short)121,"Problem displaying form");
	}
	
	public static String QUESTIONS(){
		return getText((short)123,"questions");
	}
	
	public static String FORM_CLOSE_PROMPT(){
		return getText((short)124,"Do you want to close this form without saving any changes you may have made?");
	}
	
	public static String REQUIRED_PROMPT(){
		return getText((short)125,"Please enter missing values. Questions marked with *");
	}
	
	public static String ANSWER_MINIMUM_PROMPT(){
		return getText((short)126,"Please answer atleast one question or choose Cancel");
	}
	
	public static String INVALID_NAME_PASSWORD(){
		return getText((short)127,"Invalid User Name or Password");
	}
	
	public static String HIDE_STUDIES(){
		return getText((short)128,"Hide Studies");
	}
	
	public static String MAIN_MENU_VIEW(){
		return getText((short)128,"Main Menu");
	}
	
	public static String UPLOAD_ALL_FORM_DATA(){
		return getText((short)129,"Upload All Form Data");
	}
	
	public static String SELECT_LANGUAGE(){
		return getText((short)-1,"Select Language");
	}
	
	public static String NO_LANGUAGES_FOUND(){
		return getText((short)-2,"No languages found. Do you want to download them?");
	}
	
	public static String NO_FORM_DEF(){
		return getText((short)-3,"Cannot find form definition");
	}
	
	public static String MENU_TEXT_DOWNLOAD(){
		return getText((short)-4,"Menu Text Download");
	}
	
	public static String DOWNLOADING_MENU_TEXT(){
		return getText((short)-5,"Downloading Menu Text");
	}
	
	public static String MENU_TEXT_DOWNLOAD_SAVED(){
		return getText((short)-6,"Menu text downloaded and saved successfully");
	}
	
	public static String USER_DOWNLOAD_URL(){
		return getText((short)-7,"Users download url:");
	}
	
	public static String SERVER_URL(){
		return getText((short)-8,"Server url:");
	}
	
	public static String DATA_UPLOAD_URL(){
		return getText((short)-9,"Data upload url:");
	}
	
	public static String DATA_DELETE_PROMPT(){
		return getText((short)-10,"Do you really want to delete data collected on this form?");
	}
	
	public static String NO_MENU_TEXT(){
		return getText((short)-11,"The server has no Menu Text for this language");
	}
	
	public static String MENU_TEXT_DOWNLOAD_PROMPT(){
		return getText((short)-12,"Do you want to download the menu for this language?");
	}
}
