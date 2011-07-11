package org.openxdata.openclinica;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;


/**
 * 
 */
public class StudyEventList implements Persistent{

	/** Collection of study events. */
	private Vector events;

	/** Constructs a new events collection. */
	public StudyEventList(){
		super();
	}

	public StudyEventList(Vector events){
		this();
		setEvents(events);
	}

	public Vector getEvents() {
		return events;
	}

	public void setEvents(Vector events) {
		this.events = events;
	}

	public void addEvent(StudyEvent event){
		if(events == null)
			events = new Vector();
		events.addElement(event);
	}

	public void addEvents(Vector eventList){
		if(eventList != null){
			if(events == null)
				events = eventList;
			else{
				for(int i=0; i<eventList.size(); i++ )
					this.events.addElement(eventList.elementAt(i));
			}
		}
	}

	public int size(){
		if(getEvents() == null)
			return 0;
		return getEvents().size();
	}

	public StudyEvent getEvent(int index){
		return (StudyEvent)getEvents().elementAt(index);
	}

	/** 
	 * Reads the event collection object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setEvents(PersistentHelper.read(dis,new StudyEvent().getClass()));
	}

	/** 
	 * Writes the event collection object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.write(getEvents(), dos);
	}
}
