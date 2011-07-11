package org.openxdata.communication;


/**
 * 
 * @author daniel
 *
 */
public class ConnectionParameter {

	private byte connectionType;
	private String name;
	private String value;
	
	public ConnectionParameter(byte connectionType, String name, String value) {
		super();
		this.connectionType = connectionType;
		this.name = name;
		this.value = value;
	}

	public int getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(byte connectionType) {
		this.connectionType = connectionType;
	}

	public String getName() {
		return name;
	}

	public void setName(String key) {
		this.name = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
