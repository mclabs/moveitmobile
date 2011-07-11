package org.openxdata.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;
import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;
import org.openxdata.model.User;

/**
 * Contains a list of form data summary.
 * 
 * @author dagmar@cell-life.org.za
 *
 */
public class FormDataSummaryList implements Persistent{

	private Vector formDataSummaries = new Vector();

	public FormDataSummaryList(){

	}

	public FormDataSummaryList(Vector formDataSummaries){
		this.formDataSummaries = formDataSummaries;
	}

	public Vector getFormDataSummaries() {
		return formDataSummaries;
	}

	public void setFormDataSummaries(Vector formDataSummaries) {
		this.formDataSummaries = formDataSummaries;
	}

	public void addFormDataSummary(FormDataSummary formDataSummary){
		formDataSummaries.addElement(formDataSummary);
	}

	public void addFormDataSummaries(Vector formDataSummaryList){
		if (formDataSummaryList != null) {
			for (int i=0, n=formDataSummaryList.size(); i<n; i++) {
				formDataSummaries.addElement(formDataSummaryList.elementAt(i));
			}
		}
	}

	public int size(){
		return formDataSummaries.size();
	}

	public User getFormDataSummary(int index){
		return (User)formDataSummaries.elementAt(index);
	}

	/**
	 * @see org.openxdata.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setFormDataSummaries(PersistentHelper.read(dis, new FormDataSummary().getClass()));
	}

	/**
	 * @see org.openxdata.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.write(getFormDataSummaries(), dos);
	}
}
