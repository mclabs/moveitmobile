package org.openxdata.openmrs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;

public class CohortList implements Persistent{

	private Vector cohorts = new Vector();

	public CohortList(){

	}

	public CohortList(Vector cohorts) {
		this.cohorts = cohorts;
	}

	public Vector getCohorts() {
		return cohorts;
	}

	public void setCohorts(Vector cohorts) {
		this.cohorts = cohorts;
	}

	public void addCohort(Cohort cohort){
		cohorts.addElement(cohort);
	}

	public void addCohorts(Vector cohortList){
		if(cohortList != null){
			for(int i=0; i<cohortList.size(); i++ )
				this.cohorts.addElement(cohortList.elementAt(i));
		}
	}

	public int size(){
		return cohorts.size();
	}

	public Cohort getCohort(int index){
		return (Cohort)cohorts.elementAt(index);
	}

	/** 
	 * Reads the patient field collection object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setCohorts(PersistentHelper.read(dis,new Cohort().getClass()));
	}

	/** 
	 * Writes the patient field collection object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.write(getCohorts(), dos);
	}
}
