package org.openxdata.purcai;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;

/**
 * 
 * @author daniel
 *
 */
public class MarkSheetDataList implements Persistent{
	
	/** Collection of marksheets (MarkSheetData). */
	private Vector markSheets;
	
	/** Constructs a new MarkSheetData collection. */
	public MarkSheetDataList(){
		
	}
	
	public MarkSheetDataList(Vector markSheets){
		setMarkSheets(markSheets);
	}
	
	public Vector getMarkSheets() {
		return markSheets;
	}

	public void setMarkSheets(Vector markSheets) {
		this.markSheets = markSheets;
	}
	
	public void addMarkSheet(MarkSheetData markSheetData){
		if(markSheets == null)
			markSheets = new Vector();
		markSheets.addElement(markSheetData);
	}
	
	public void addMarkShees(Vector markSheetList){
		if(markSheetList != null){
			if(markSheets == null)
				markSheets = markSheetList;
			else{
				for(int i=0; i<markSheetList.size(); i++ )
					this.markSheets.addElement(markSheetList.elementAt(i));
			}
		}
	}
	
	public int size(){
		if(getMarkSheets() == null)
			return 0;
		return getMarkSheets().size();
	}
	
	public MarkSheetData getMarkSheet(int index){
		return (MarkSheetData)getMarkSheets().elementAt(index);
	}
	
	/** 
	 * Reads the patient collection object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setMarkSheets(PersistentHelper.read(dis,new MarkSheetData().getClass()));
	}

	/** 
	 * Writes the patient collection object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.write(getMarkSheets(), dos);
	}
}
