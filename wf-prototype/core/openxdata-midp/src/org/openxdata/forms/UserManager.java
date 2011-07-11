package org.openxdata.forms;

import java.util.Random;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.openxdata.db.OpenXdataDataStorage;
import org.openxdata.db.util.Settings;
import org.openxdata.model.User;
import org.openxdata.model.UserList;
import org.openxdata.mvc.AbstractView;
import org.openxdata.util.AlertMessage;
import org.openxdata.util.AlertMessageListener;
import org.openxdata.util.DefaultCommands;
import org.openxdata.util.MenuText;



/**
 * 
 * @author Daniel
 *
 */
public class UserManager extends AbstractView implements AlertMessageListener {

	private static final String STORAGE_NAME_SETTINGS = "util.Settings";
	private static final String KEY_LAST_USERNAME = "lastusername";
	
	private AlertMessage alertMsg;
	private boolean loggedOn = false; //false;   //thi is just for development to prevent the tiresome logons
	
	/** The currently logged on user. */
	private static User user;
	private LoginForm loginForm;
	private LogonListener logonListener;
	
	public UserManager(Display display,Displayable prevScreen,String title,LogonListener logonListener){
		super();
		setDisplay(display);
		setTitle(title);
		setPrevScreen(prevScreen);
		setLogonListener(logonListener);
	}

	/** 
	 * Check if user is already logged on.
	 * 
	 * @return - True if the user is already logged on, else False.
	 */
	public boolean isLoggedOn() {
		return loggedOn;
	}

	private void setLoggedOn(boolean loggedOn) {
		this.loggedOn = loggedOn;
	}
	
	public void setLogonListener(LogonListener logonListener){
		this.logonListener = logonListener;
	}
	
	public void logOn(){
		
		setLoggedOn(false);
		
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		
		LoginForm frm = new LoginForm(this.title + " - " + MenuText.LOGIN(),settings.getSetting(KEY_LAST_USERNAME));
		
		if(GeneralSettings.isOkOnRight()){
			frm.addCommand(DefaultCommands.cmdOk);
			frm.addCommand(DefaultCommands.cmdCancel);
		}
		else{
			frm.addCommand(DefaultCommands.cmdOk);
			frm.addCommand(DefaultCommands.cmdCancel);
		}
			
		frm.setCommandListener(this);
		
		alertMsg = new AlertMessage(display,title,frm,this);

		display.setCurrent(frm);
	}
	
	/**
	 * Check to see if a user is authorised. As for now, we are not checking user
	 * passwords because of danger of tranfering passwords over the place.
	 * The password check will be done at the server. This means that all
	 * one needs to know is username and gets into the system on device,
	 * which is not good and hence needs more input. But for now, that's it.
	 * 
	 * @param name - the user name.
	 * @param password - the user password.
	 * @return true when valid user, else false.
	 */
	private boolean isUserValid(String name, String password) throws Exception {
		//I think it makes sense to force users enter a user name and password
		//even when no user information has yet been loaded from the server.
		if(name.trim().length() == 0 || password.trim().length() == 0)
			return false;
		
		UserList users = OpenXdataDataStorage.getUsers();
		if(users == null || users.size() == 0)
			return true; //No users in db so every one has free entry
		
		user = null;
		for(int i=0; i<users.size(); i++){
			user = users.getUser(i);
			if(user.getName().toLowerCase().equals(name.toLowerCase()) /*&& user.getPassword().equals(password)*/)
				return authenticate(user,password);
		}
		
		return false;
	}
	
	private boolean authenticate(User user, String password) throws Exception {
		String hashedPassword = encodeString(password + user.getSalt(), false);
		boolean result = (hashedPassword.equals(user.getPassword()));
		if (!result) {
			String hashedPassword2 = encodeString(password + user.getSalt(), true);
			result = (hashedPassword2.equals(user.getPassword()));
		}
		return result;
	}
	
	 /**
     * @param string to encode
     * @return the SHA-1 encryption of a given string
     */
    public static String encodeString(String strToEncode, boolean hexString2) throws Exception {
    	//SHA512Digest digEng = new SHA512Digest();
    	SHA1Digest digEng = new SHA1Digest();
    	
    	//pick a specific character encoding, don't rely on the platform default
  		byte[] input = strToEncode.getBytes("UTF-8");
  		digEng.update(input, 0, input.length);
  		
  		byte[] digest = new byte[digEng.getDigestSize()];
  		digEng.doFinal(digest, 0);
   		
  		if (!hexString2) {
  			return hexString(digest);
  		} else {
  			return hexString2(digest);
  		}
    }
    
    /**
     * @param Byte array to convert to HexString
     * @return Hexidecimal based string
     */
    
	private static String hexString(byte[] b) {
		if (b == null || b.length < 1)
			return "";
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			s.append(Integer.toHexString(b[i] & 0xFF));
		}
		return new String(s);
	}
	
	private static String hexString2(byte[] b) {
		StringBuffer buf = new StringBuffer();
		char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		int len = b.length;
		int high = 0;
		int low = 0;
		for (int i = 0; i < len; i++) {
			high = ((b[i] & 0xf0) >> 4);
			low = (b[i] & 0x0f);
			buf.append(hexChars[high]);
			buf.append(hexChars[low]);
		}
		
		return buf.toString();
	}
	
	/**
	 * Gets login information from the user and check if he or she is authorised.
	 * @param d
	 */
	private void validateUser(Displayable d) throws Exception {
		loginForm = (LoginForm)d;
		if(isUserValid(loginForm.getUserName(),loginForm.getPassword())){
			setLoggedOn(true);
			saveUserName();
			boolean displayPrevScreen = true;
			if(logonListener != null)
				displayPrevScreen = logonListener.onLoggedOn();
			if(displayPrevScreen)
				display.setCurrent(prevScreen);
			GlobalVariables.username = loginForm.getUserName();
			GlobalVariables.password = loginForm.getPassword();
		}
		else
			alertMsg.show(MenuText.INVALID_NAME_PASSWORD());
	}
	
	private void saveUserName(){
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		settings.setSetting(KEY_LAST_USERNAME, loginForm.getUserName());
		settings.saveSettings();		
	}
	
	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		try{
			if(c == DefaultCommands.cmdOk)
				handleOkCommand(d);
			else if(c == DefaultCommands.cmdCancel)
				handleCancelCommand(d);
		}
		catch(Exception e){
			alertMsg.showError(e.getMessage());
			//e.printStackTrace();
		}
	}
	
	/**
	 * Processes the cancel command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleCancelCommand(Displayable d){
		if(logonListener != null)
			logonListener.onLogonCancel();
		else
			display.setCurrent(prevScreen);
	}
	
	/**
	 * Processes the OK command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleOkCommand(Displayable d) throws Exception {
		validateUser(d);
	}
	
	public void onAlertMessage(byte msg){
		display.setCurrent(loginForm);
	}
	
	public int getUserId(){
		if(user == null){
			System.out.println("No userid, so using -1");
			return -1;
		}
		
		return user.getUserId();
	}
	
	public String getUserName(){
		//user.getName(); If no users are downloaded yet, 
		//we want to just use the user supplied name as the user reference will be null.
		return  loginForm.getUserName();
	}
	
	public String getPassword(){
		//user.getPassword(); Same as for user name.
		return loginForm.getPassword();
	}
	
	public void logOut(){
		setLoggedOn(false);
	}
	
	/**
     * This method will generate a random string 
     * 
     * @return a secure random token.
     */
    public static String getRandomToken() throws Exception {
    	Random rnd = new Random();
    	return encodeString(Long.toString(System.currentTimeMillis()) 
    			+ Long.toString(rnd.nextLong()), false);
    }

}
