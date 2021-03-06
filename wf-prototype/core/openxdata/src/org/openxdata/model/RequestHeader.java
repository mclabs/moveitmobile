package org.openxdata.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.db.util.Persistent;


/**
 * Containts the connection request header details of user name, password,
 * and what action to execute.
 * 
 * @author Daniel Kayiwa
 *
 */
public class RequestHeader implements Persistent{

	/** No status specified yet. */
	public static final byte ACTION_NONE = -1;

	/** Status to get a list of studies. */
	public static final byte ACTION_DOWNLOAD_STUDY_LIST = 2;

	/** Status to get a list of form definitions in a study. */
	public static final byte ACTION_DOWNLOAD_STUDY_FORMS = 3;

	/** Status to get a list of form definitions in a list of studies. */
	public static final byte ACTION_DOWNLOAD_STUDIES_FORMS = 4;

	/** Status to save a list of form data. */
	public static final byte ACTION_UPLOAD_DATA = 5;

	/** Status to download a list of users from the server. */
	public static final byte ACTION_DOWNLOAD_USERS = 7;

	/** Status to download a list of users and forms from the server. */
	public static final byte ACTION_DOWNLOAD_USERS_AND_FORMS = 11;

	/** Status to download a list of users and studies from the server. */
	public static final byte ACTION_DOWNLOAD_USERS_AND_ALL_FORMS = 12;
	
	/** Status to download a list of languages. */
	public static final byte ACTION_DOWNLOAD_LANGUAGES = 15;
	
	/** Status to download menu text in the selected language. */
	public static final byte ACTION_DOWNLOAD_MENU_TEXT = 16;
	

	/** The current status. This could be a request or return code status. */
	public byte action = ACTION_NONE;

	private String userName = OpenXdataConstants.EMPTY_STRING;
	private String password = OpenXdataConstants.EMPTY_STRING;
	private static String serializer = "epihandyser";
	private String locale = "en";


	/** Constructs a new communication parameter. */
	public RequestHeader(){
		super();
	}

	public byte getAction() {
		return action;
	}

	public void setAction(byte action) {
		this.action = action;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public static String getSerializer() {
		return serializer;
	}

	public static void setSerializer(String serializer) {
		RequestHeader.serializer = serializer;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * @see org.openxdata.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException{
		dos.writeUTF(getUserName());
		dos.writeUTF(getPassword());
		dos.writeUTF(getSerializer());
		dos.writeUTF(getLocale());
		dos.writeByte(getAction());
	}

	/**
	 * @see org.openxdata.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException,InstantiationException,IllegalAccessException{
		setUserName(dis.readUTF());
		setPassword(dis.readUTF());
		setSerializer(dis.readUTF());
		setLocale(dis.readUTF());
		setAction(dis.readByte());
	}
}


/** The folder where to put form xml collected data in NON database mode. 
 * The files are named starting with form definition variable name and
 * ending with the form data id. 
 * For each submitted set of form data, a new folder, 
 * under the folder with the name of the study, under folder with name of user,
 * is created having a name
 * which is composed of the date and time of submission.
 * This will most of the times ensure uniqueness of the files, but if a 
 * file of the same name is found in the same folder,
 * this form data is considerered not saved and the user will be required
 * to resolve this, as it can be dangerous to automatically overwrite such data.
 * */
//public static final String FORMS_DATA_FOLDER = "FormsDataFolder";
