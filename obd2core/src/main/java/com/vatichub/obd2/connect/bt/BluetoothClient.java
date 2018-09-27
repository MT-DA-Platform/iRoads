package com.vatichub.obd2.connect.bt;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.vatichub.obd2.OBD2CoreConstants;
import com.vatichub.obd2.connect.StreamCommunicator;
import com.vatichub.obd2.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothClient implements StreamCommunicator {

    private static final String TAG = OBD2CoreConstants.APPTAG + "-BTClient";
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final BluetoothSocket socket;
    private static final byte[] TERM_SEND = {0x0d};
    private Logger logger;

    public BluetoothClient(BluetoothSocket socket, Logger logger) throws IOException {
        try {
            this.socket = socket;
            mmInStream = socket.getInputStream();
            mmOutStream = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Bluetooth Client sockets not created", e);
            throw e;
        }
        this.logger = logger;
    }

    /**
     * Write to the connected OutStream.
     * @param buffer  The bytes to write
     */
    public void write(byte[] buffer) throws IOException {
        try {
            mmOutStream.write(buffer);
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
            throw e;
        }
    }

    public void write(String str, byte[] term) throws IOException {
        byte[] one = str.getBytes();
        byte[] two = term;
        byte[] combined = new byte[one.length + two.length];
        System.arraycopy(one, 0, combined, 0, one.length);
        System.arraycopy(two, 0, combined, one.length, two.length);
        write(combined);
    }

    public byte[] sendReceive(String command) throws IOException {

        if (logger != null) {
            logger.println(">> " + command);
        }

        byte[] buffer = new byte[1024];
        int char1=-1;

        int i=0;
        int length= mmInStream.available();

        for(int j=0;j<length;j++){
            char1 = mmInStream.read();
        }

        length= mmInStream.available();

        //todo FIX TERM_SEND; we dont need this method!
        write(command,TERM_SEND);

        length= mmInStream.available();

        while((char1 = mmInStream.read())!= '>'){
            buffer[i]=(byte)char1;
            i++;
        }

        String tmp = new String(buffer, 0, i);

//        	String[] lines = tmp.split("(\n|\r)+");
        String response=tmp.trim();
//        	String response=lines[lines.length-2].trim();

        if (logger != null) {
            logger.println("<< " + response);
        }
        return response.getBytes();
    }

    @Override
    public void close() throws IOException {
        try {
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
            throw e;
        }
    }
}
