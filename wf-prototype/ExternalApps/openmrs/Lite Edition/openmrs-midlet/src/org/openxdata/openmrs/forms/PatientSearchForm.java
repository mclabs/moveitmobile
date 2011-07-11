package org.openxdata.openmrs.forms;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

import org.openxdata.db.util.Settings;
import org.openxdata.forms.GeneralSettings;
import org.openxdata.util.Utilities;


/**
 * 
 * @author daniel
 *
 */
public class PatientSearchForm extends Form {

	private static final String KEY_INCLUDE_SERVER_SEARCH = "INCLUDE_SERVER_SEARCH";
	
	private TextField txtName;
	private TextField txtId;
	private ChoiceGroup cgSearchType;
	
	public PatientSearchForm(String title) {
		super(title);	
		txtId = new TextField("Patient Identifier:","",100,TextField.ANY);
		txtName = new TextField("Name:","",100,TextField.ANY);
		cgSearchType = new ChoiceGroup("Search including",Choice.MULTIPLE);
		cgSearchType.append("Server", null);
		
		Settings settings = new Settings(GeneralSettings.STORAGE_NAME_SETTINGS,true);
		cgSearchType.setSelectedIndex(0, Utilities.stringToBoolean(settings.getSetting(KEY_INCLUDE_SERVER_SEARCH)));
		
		append(txtId);
		append(txtName);
		append(cgSearchType);
	}
	
	public String getId(){
		return this.txtId.getString();
	}
	
	public void setId(String id){
		this.txtId.setString(id);
	}
	
	public String getName(){
		return this.txtName.getString();
	}
	
	public void setName(String name){
		this.txtId.setString(name);
	}
	
	public boolean includeServerSearch(){
		Settings settings = new Settings(GeneralSettings.STORAGE_NAME_SETTINGS,true);
		settings.setSetting(KEY_INCLUDE_SERVER_SEARCH,Utilities.booleanToString((cgSearchType.isSelected(0))));
		settings.saveSettings();

		return cgSearchType.isSelected(0);
	}
}
