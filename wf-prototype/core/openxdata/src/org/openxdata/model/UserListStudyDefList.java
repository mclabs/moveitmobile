package org.openxdata.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;


/**
 * Contains a list of users and list of studies, combined together.
 * 
 * @author dagmar@cell-life.org
 */
public class UserListStudyDefList implements Persistent{

	/** A list of users. */
	private UserList users;

	/** A list of form definitions in a study. */
	private StudyDefList studyDefList;

	/** Default constructor. */
	public UserListStudyDefList(){

	}

	/**
	 * Constructs a new users and study form definitions list.
	 * 
	 * @param users the users list.
	 * @param studyDefList a studyDef list.
	 */
	public UserListStudyDefList(UserList users, StudyDefList studyDefList){
		setUsers(users);
		setStudyDefList(studyDefList);
	}

	public StudyDefList getStudyDefList() {
		return studyDefList;
	}

	public void setStudyDefList(StudyDefList studyDefList) {
		this.studyDefList = studyDefList;
	}
	
	public int totalForms() {
		int num = 0;
		for(byte i=0; i<studyDefList.size(); i++ ) {
			StudyDef sd = studyDefList.getStudy(i);
			Vector forms = sd.getForms();
			if (forms != null) {
				num += forms.size();
			}
		}
		return num;
	}

	public UserList getUsers() {
		return users;
	}

	public void setUsers(UserList users) {
		this.users = users;
	}

	/**
	 * @see org.openxdata.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		UserList users = new UserList();
		users.read(dis);
		setUsers(users);

		StudyDefList studyDefList = new StudyDefList();
		studyDefList.read(dis);
		setStudyDefList(studyDefList);
	}

	/**
	 * @see org.openxdata.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		getUsers().write(dos);
		getStudyDefList().write(dos);
	}
}

