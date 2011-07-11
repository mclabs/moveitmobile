package org.openxdata.communication.bluetooth;

import java.util.Vector;

// jsr082 API
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;


/**
 * @author Daniel Kayiwa
 * 
 * This class shields bluetooth inner details from the user by doing the work
 * of searching for bluetooth devices and services returning the bluetooth url
 * which can then be used for connections.
 * 
 * NOTE: When there are many bluetooth devices in the vicinity, the search time for 
 * 		 bluetooth devices and services will be proportional to their number. You 
 * 		 could optimize this by providing a bluetooth device name.
 *
 */
public final class BluetoothClient implements DiscoveryListener {
	
    /** identifies the server */
    private UUID SERVER_UUID;

    /** Shows the engine is ready to work. */
    private static final int READY = 0;

    /** Shows the engine is searching bluetooth devices. */
    private static final int DEVICE_SEARCH = 1;

    /** Shows the engine is searching bluetooth services. */
    private static final int SERVICE_SEARCH = 2;

    /** Keeps the current state of engine. */
    private int state = READY;

    /** Keeps the discovery agent reference. */
    private DiscoveryAgent discoveryAgent;

    /** Collects the remote devices found during a search. */
    private Vector /* RemoteDevice */ devices = new Vector();

    /** Collects the services found during a search. */
    private Vector /* ServiceRecord */ records = new Vector();

    /** Keeps the device discovery return code. */
    private int discType;

    /** Keeps the services search IDs (just to be able to cancel them). */
    private int[] searchIDs;

    /** Optimization: keeps service search pattern. */
    private UUID[] uuidSet;
    
    /** Reference to the bluetooth event listener. */
    private BluetoothClientListener eventListener;
    
    /** 
     * The name of the device running the bluetooth service. This optional field
     * makes service search faster by eliminating unecessary service search from
     * devices we do not intend to. It also helps resolve conflicts which occur 
     * when more than on device runs the same service (as per service uuid)
     */
    private String deviceName;

    /**
     * Constructs the bluetooth client, but it is initialized
     * in the different thread to "avoid dead lock".
     */
    public BluetoothClient(String uuid,BluetoothClientListener eventListener) {
    	this.eventListener = eventListener;
    	this.SERVER_UUID = new UUID(uuid,false);
    	init();
    }

    /**
     * Process the search/download requests.
     */
    public void init() {

        try {
            // create/get a local device and discovery agent
            LocalDevice localDevice = LocalDevice.getLocalDevice();
            discoveryAgent = localDevice.getDiscoveryAgent();
        } catch (Exception e) {
        	eventListener.errorOccured("Can't initialize bluetooth: ", e);
        }

        // initialize some optimization variables
        uuidSet = new UUID[2];

        // ok, we are interested in btspp services only
        uuidSet[0] = new UUID(0x1101);

        // and only the known one.
        uuidSet[1] = SERVER_UUID;
    }
    
    public String getServiceUrl(String deviceName){
    	this.deviceName = deviceName;
    	searchDevices();
    	searchServices();
    	    	
    	if(records.size() == 0)
    		return null;
    	
    	//The first service is enough.
    	return ((ServiceRecord)this.records.elementAt(0)).getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
    }
    
    public String getDeviceName(){
    	return deviceName;
    }
    
    /**
     * Invoked by system when a new remote device is found -
     * remember the found device.
     */
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) { 
    	try{
	        // same device may be found several times during single search
	        if (devices.indexOf(btDevice) == -1){
	        	String name = btDevice.getFriendlyName(false);
	        	if((deviceName == null || deviceName.trim().length() == 0) || deviceName.toUpperCase().equals(name.toUpperCase())){
	        		devices.addElement(btDevice);
	        		deviceName = name;
	        	}
	        	
	        	//If device name was given and we have found it, then there is no need
	        	//of searching for other devices (Some sort of optimization)
	        	//if(deviceName != null && deviceName.trim().length() > 0 && deviceName.toUpperCase().equals(name.toUpperCase()))
	        	//	discoveryAgent.cancelInquiry(this);
	        }
    	}
    	catch(Exception ex){
    		//ex.printStackTrace();
    	}
    }

    /**
     * Invoked by system when device discovery is done.
     * <p>
     * Remember the discType
     * and process its evaluation in another thread.
     */
    public void inquiryCompleted(int discType) {
        this.discType = discType;
        synchronized (this) {
            notify();
        }
    }

    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        for (int i = 0; i < servRecord.length; i++)
            records.addElement(servRecord[i]);
        
        //we don't need any more search after finding the first one.
        discoveryAgent.cancelInquiry(this);
        discoveryAgent.cancelServiceSearch(searchIDs[0]);
     }

    public void serviceSearchCompleted(int transID, int respCode) {
        // first, find the service search transaction index
        int index = -1;

        for (int i = 0; i < searchIDs.length; i++) {
            if (searchIDs[i] == transID) {
                index = i;
                break;
            }
        }

        // error - unexpected transaction index
        if (index == -1) {
           ; //System.err.println("Unexpected transaction index: " + transID);
            // process the error case here
        } else {
            searchIDs[index] = -1;
        }

        /*
         * Actually, we do not care about the response code -
         * if device is not reachable or no records, etc.
         */

        // make sure it was the last transaction
        for (int i = 0; i < searchIDs.length; i++) {
            if (searchIDs[i] != -1) {
                return;
            }
        }

        // ok, all of the transactions are completed
        synchronized (this) {
            notify();
        }
    }

    /** Sets the request to search the devices/services. */
    void requestSearch() {
        synchronized (this) {
            notify();
        }
    }

    /** Cancel's the devices/services search. */
   public void cancelSearch() {
        synchronized (this) {
            if (state == DEVICE_SEARCH) {
                discoveryAgent.cancelInquiry(this);
            } else if (state == SERVICE_SEARCH) {
                for (int i = 0; i < searchIDs.length; i++) {
                    discoveryAgent.cancelServiceSearch(searchIDs[i]);
                }
            }
        }
    }

    /**
     * Search for bluetooth devices.
     *
     * @return false if should end the component work.
     */
    private boolean searchDevices() {
        // ok, start a new search then
        state = DEVICE_SEARCH;
        devices.removeAllElements();

        try {
            discoveryAgent.startInquiry(DiscoveryAgent.GIAC, this);
        } catch (BluetoothStateException e) {
        	eventListener.errorOccured("Can't start inquiry now: ", e);
             return true;
        }
    
        try {
        	synchronized(this){
        		wait(); // until devices are found
        	}
        } catch (InterruptedException e) {
            //System.err.println("Unexpected interruption: " + e);
            return false;
        }

        // no?, ok, let's check the return code then
        switch (discType) {
        case INQUIRY_ERROR:
        	eventListener.errorOccured("Device discovering error.", null);
 
        // fall through
        case INQUIRY_TERMINATED:
            // make sure no garbage in found devices list
            devices.removeAllElements();

            // nothing to report - go to next request
            //System.out.println("INQUIRY_TERMINATED");
            break;

        case INQUIRY_COMPLETED:

            if (devices.size() == 0) 
            	eventListener.errorOccured("No devices in range", null);

            // go to service search now
            break;

        default:
            // what kind of system you are?... :(
            //System.err.println("system error:" + " unexpected device discovery code: " + discType);
            //destroy();

            return false;
        }

       
        return true;
    }

    /**
     * Search for proper service.
     *
     * @return false if should end the component work.
     */
    private boolean searchServices() {
        state = SERVICE_SEARCH;
        records.removeAllElements();
        searchIDs = new int[devices.size()];

        boolean isSearchStarted = false;

        for (int i = 0; i < devices.size(); i++) {
            RemoteDevice rd = (RemoteDevice)devices.elementAt(i);

            try {
            	if(searchIDs != null)
                searchIDs[i] = discoveryAgent.searchServices(null, uuidSet, rd, this);
            	//searchIDs[i] = discoveryAgent.searchServices(null, uuidSet, rd, this);
            } catch (BluetoothStateException e) {
                //System.err.println("Can't search services for: " + rd.getBluetoothAddress() +
                //    " due to " + e);
                searchIDs[i] = -1;

                continue;
            }

            isSearchStarted = true;
        }

        // at least one of the services search should be found
        if (!isSearchStarted) {
        	eventListener.errorOccured("Can't search services.", null);
             return true;
        }

        try {
        	synchronized(this){
        		wait(); // until services are found
        	}
        } catch (InterruptedException e) {
        	eventListener.errorOccured("Unexpected interruption:", e);
            return false;
        }

        // actually, no services were found
        if (records.size() == 0)
        	eventListener.errorOccured("No proper services were found", null);

        return true;
    }
}

