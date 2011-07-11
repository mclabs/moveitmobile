package org.openxdata.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.db.util.AbstractRecord;

/**
 * Contains a summary of submitted form data
 * (form data description + identifier) 
 * 
 * @author dagmar@cell-life.org
 */
public class FormDataSummary  extends AbstractRecord {
	
	private String description;
	private int formDataId;

	/** Constructs a form data summary object. */
	public FormDataSummary(){
		super();
	}

	public void read(DataInputStream dis) throws IOException,
			InstantiationException, IllegalAccessException {
		setDescription(dis.readUTF());
		setFormDataId(dis.readInt());	
	}

	public void write(DataOutputStream dos) throws IOException {
		dos.writeUTF(getDescription());
		dos.writeInt(getFormDataId());
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getFormDataId() {
		return formDataId;
	}

	public void setFormDataId(int formDataId) {
		this.formDataId = formDataId;
	}
}
