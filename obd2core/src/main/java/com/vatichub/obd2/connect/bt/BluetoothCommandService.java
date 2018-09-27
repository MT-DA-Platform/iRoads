package com.vatichub.obd2.connect.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.vatichub.obd2.OBD2CoreConfiguration;
import com.vatichub.obd2.OBD2CoreConstants;
import com.vatichub.obd2.connect.BTServiceCallback;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BluetoothCommandService {
	// Debugging
    private static final String TAG = OBD2CoreConstants.APPTAG+"-BTComService";
    private static final boolean D = true;
    
    private OBD2CoreConfiguration obd2CoreConfigs;
    
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    
    //private static final UUID MY_UUID = UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
    
    // Member fields
    private final BluetoothAdapter mAdapter;
    private ConnectAttemptThread mConnectAttemptThread;
    
    private OBD2BluetoothBackgroundWorker mConnectedThread;
    private BTServiceCallback callback;

    private int mState;
    private BluetoothDevice currentDevice;

    // OBD2CoreConstants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED_ELM = 3;  //  now connected to the bluetooth device
    public static final int STATE_CONNECTED_CAR = 6; //now connected to Car identifying the protocol
	public static final int STATE_CONNECTED_MOCK = 4; // now connected to mock ELM embedded in the app
	public static final int STATE_CONNECTED_MOCK_CAR = 5; // now connected to mock ECU embedded in the app
    public static final int STATE_CONNECTED_VINLI = 7; // now connected to Vinli adaptor

    public static final String ARGS_REASON = "reason";

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     */
    public BluetoothCommandService(BTServiceCallback callback) {
    	mAdapter = BluetoothAdapter.getDefaultAdapter();
    	mState = STATE_NONE;
    	this.callback = callback;
    	obd2CoreConfigs = OBD2CoreConfiguration.getInstance();
    }

    public BluetoothDevice getCurrentDevice() {
        return currentDevice;
    }

        /**
         * Set the current state of the chat connection
         * @param newState  An integer defining the current connection state
         */
    private synchronized void setState(int newState, BluetoothDevice device) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + newState);
        int oldState = mState;
        currentDevice = device;
        mState = newState;
        callback.onStateChanged(oldState, newState, device);
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }
    
    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectAttemptThread != null) {
            mConnectAttemptThread.cancel(); mConnectAttemptThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        setState(STATE_LISTEN, null);
    }
    
    /**
     * Start the ConnectAttemptThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
    	if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectAttemptThread != null) {
                mConnectAttemptThread.cancel(); mConnectAttemptThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectAttemptThread = new ConnectAttemptThread(device);
                
        mConnectAttemptThread.start();

        callback.onConnecting(device);
        setState(STATE_CONNECTING, device);
    }
    
    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) throws IOException {
        if (D) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectAttemptThread != null) {
            mConnectAttemptThread.cancel(); mConnectAttemptThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to manage the connection and perform transmissions
        
//        mConnectedThread = new SimpleBluetoothClient(socket);
        mConnectedThread = new OBD2BluetoothBackgroundWorker(this, socket, callback);
        
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity

        // save connected device
        //mSavedDevice = device;
        // reset connection lost count
        //mConnectionLostCount = 0;

        callback.onConnectedDevice(device);
        setState(STATE_CONNECTED_ELM, device);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop(BluetoothDevice device, Exception e1) {
        if (D) Log.d(TAG, "stop");
        if (mConnectAttemptThread != null) {
            mConnectAttemptThread.cancel(); mConnectAttemptThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        Map<String, Object> args = new HashMap<>();
        args.put(ARGS_REASON, e1.getMessage());
        callback.onDisconnected(device, args);
        setState(STATE_NONE, null);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed(BluetoothDevice device) {
        callback.onConnectionFailed(device);
        setState(STATE_LISTEN, null);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost(BluetoothDevice device) {
        callback.onConnectionLost(device);
    	setState(STATE_LISTEN, null);
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectAttemptThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectAttemptThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectAttemptThread");
            setName("ConnectAttemptThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed(mmDevice);
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                BluetoothCommandService.this.start();
                return;
            }

            // Reset the ConnectAttemptThread because we're done
            synchronized (BluetoothCommandService.this) {
                mConnectAttemptThread = null;
            }

            // Start the connected thread
            String address = mmDevice.getAddress();
            obd2CoreConfigs.addSetting(OBD2CoreConstants.LAST_CONNECTED_BT_ADDR, address);

            try {
                connected(mmSocket, mmDevice);
            } catch (IOException e) {
                Log.e(TAG, "Failed to connect to " + address, e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

}