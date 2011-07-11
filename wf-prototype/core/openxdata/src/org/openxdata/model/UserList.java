package org.openxdata.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;
import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;
import org.openxdata.model.User;

/**
 * Contains a list of users.
 * 
 * @author Daniel
 *
 */
public class UserList implements Persistent{

	private Vector users = new Vector();

	public UserList(){

	}

	public UserList(Vector users){
		this.users = users;
	}

	public Vector getUsers() {
		return users;
	}

	public void setUsers(Vector users) {
		this.users = users;
	}

	public void addUser(User user){
		users.addElement(user);
	}

	public void addUsers(Vector userList){
		if(userList != null){
			for(int i=0; i<userList.size(); i++ )
				this.users.addElement(userList.elementAt(i));
		}
	}

	public int size(){
		return users.size();
	}

	public User getUser(int index){
		return (User)users.elementAt(index);
	}

	/**
	 * @see org.openxdata.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setUsers(PersistentHelper.read(dis,new User().getClass()));
	}

	/**
	 * @see org.openxdata.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.write(getUsers(), dos);
	}
}
