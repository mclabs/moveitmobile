package org.openxdata.model;

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
public class RepeatQtnsDataList  implements Persistent {

	/** Collection of repeatQtnsData. Where each repeatQtnsData is a row of questions answered. */
	private Vector repeatQtnsDatas = new Vector();

	/** Constructs a new repeatQtnsData collection. */
	public RepeatQtnsDataList(){

	}

	public RepeatQtnsDataList(RepeatQtnsDataList repeatQtnsDataList){
		for(byte i=0; i<repeatQtnsDataList.size(); i++)
			repeatQtnsDatas.addElement(new RepeatQtnsData(repeatQtnsDataList.getRepeatQtnsData(i)));
	}

	public RepeatQtnsDataList(Vector repeatQtnsDatas){
		setRepeatQtnsData(repeatQtnsDatas);
	}

	public Vector getRepeatQtnsData() {
		return repeatQtnsDatas;
	}

	public void setRepeatQtnsData(Vector repeatQtnsDatas) {
		this.repeatQtnsDatas = repeatQtnsDatas;
	}

	public void addRepeatQtnsData(RepeatQtnsData repeatQtnsData){
		repeatQtnsDatas.addElement(repeatQtnsData);
	}

	/**
	 * Adds a RepeatQtnsData object if none exists, else overwrites it.
	 * @param repeatQtnsData 
	 */
	public void setRepeatQtnsData(RepeatQtnsData repeatQtnsData){
		if(!repeatQtnsDatas.contains(repeatQtnsData))
			repeatQtnsDatas.addElement(repeatQtnsData);
	}

	public void removeRepeatQtnsData(int index){
		repeatQtnsDatas.removeElementAt(index);
	}

	public void addRepeatQtnsData(Vector repeatQtnsDataList){
		if(repeatQtnsDataList != null){
			for(int i=0; i<repeatQtnsDataList.size(); i++ )
				this.repeatQtnsDatas.addElement(repeatQtnsDataList.elementAt(i));
		}
	}

	public void setRepeatQtnsDataById(RepeatQtnsData repeatQtnsData){
		RepeatQtnsData data;
		for(byte i=0; i<repeatQtnsDatas.size(); i++){
			data = (RepeatQtnsData)repeatQtnsDatas.elementAt(i);
			if(data.getId() == repeatQtnsData.getId()){
				repeatQtnsDatas.setElementAt(repeatQtnsData, i);
				return;
			}
		}
		repeatQtnsDatas.addElement(repeatQtnsData);
	}

	public int size(){
		return repeatQtnsDatas.size();
	}

	public RepeatQtnsData getRepeatQtnsData(int index){
		if(repeatQtnsDatas == null || repeatQtnsDatas.size() == 0)
			return null;
		return (RepeatQtnsData)repeatQtnsDatas.elementAt(index);
	}

	public String toString() {
		String val = "";
		if(repeatQtnsDatas != null && repeatQtnsDatas.size() > 0){
			for(int i=0; i<repeatQtnsDatas.size(); i++){
				RepeatQtnsData data = (RepeatQtnsData)repeatQtnsDatas.elementAt(i);
				if(data != null){
					if(val.trim().length() > 0)
						val += ":";
					val += data;
				}
			}
		}
		return val;
	}

	/** 
	 * Reads the repeatQtnsData collection object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setRepeatQtnsData(PersistentHelper.read(dis,new RepeatQtnsData().getClass()));

		//Set the runtime identifiers.
		if(repeatQtnsDatas != null){
			for(byte i=0; i<repeatQtnsDatas.size(); i++)
				((RepeatQtnsData)repeatQtnsDatas.elementAt(i)).setId((byte)(i+1));
		}
	}

	/** 
	 * Writes the repeatQtnsData collection object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.write(getRepeatQtnsData(), dos);
	}
}
