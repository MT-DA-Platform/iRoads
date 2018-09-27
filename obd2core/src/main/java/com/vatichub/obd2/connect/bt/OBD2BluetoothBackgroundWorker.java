package com.vatichub.obd2.connect.bt;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.vatichub.obd2.OBD2CoreConfiguration;
import com.vatichub.obd2.OBD2CoreConstants;
import com.vatichub.obd2.OBD2EventManager;
import com.vatichub.obd2.api.OBD2Client;
import com.vatichub.obd2.connect.BTServiceCallback;
import com.vatichub.obd2.exception.OBD2ClientException;
import com.vatichub.obd2.impl.ELM327ClientImpl;
import com.vatichub.obd2.logging.AppFileLogger;
import com.vatichub.obd2.util.OBD2CoreUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
public class OBD2BluetoothBackgroundWorker extends Thread {

    // Debugging
    private static final String TAG = OBD2CoreConstants.APPTAG + "-ELM327BTClient";
    private static final boolean D = true;
    private BTServiceCallback callback;

    private AppFileLogger appfilelogger;
    private BluetoothCommandService mCommandService;
    private OBD2Client obd2Client;

    public OBD2BluetoothBackgroundWorker(BluetoothCommandService mCommandService, BluetoothSocket socket, BTServiceCallback callback) throws IOException {
        Log.d(TAG, "create OBD2BluetoothBackgroundWorker");
        this.callback = callback;
        OBD2CoreConfiguration obd2CoreConfigs = OBD2CoreConfiguration.getInstance();
        appfilelogger = obd2CoreConfigs.getAppFileLogger();
        this.mCommandService = mCommandService;
        BluetoothClient bluetoothClient = new BluetoothClient(socket, appfilelogger);
        obd2Client = new ELM327ClientImpl(bluetoothClient);
    }

    public void run() {
        Log.i(TAG, "BEGIN OBD2BluetoothBackgroundWorker");

        try {
            obd2Client.init();

            OBD2CoreConfiguration obd2CoreConfigs = OBD2CoreConfiguration.getInstance();
            obd2CoreConfigs.setSupportedPIDsBool(obd2Client.getSupportedPIDsInBinary());
            obd2CoreConfigs.setSupportedPIDsList(obd2Client.getSupportedPIDsList());

            //send a callback
            callback.onConnectedCar();

            OBD2EventManager obd2EventManager = OBD2EventManager.getInstance();

            while (!obd2CoreConfigs.isExitting() && !obd2Client.isClosed()) {

                List<String> queryPIDsList = obd2CoreConfigs.getQueryPIDsList();

                //Initialize a new json data object to send.
                JSONObject dataobj = new JSONObject();
                JSONObject datapairs = new JSONObject();

                for (int i = 0; i < queryPIDsList.size(); i++) {
                    String pid = queryPIDsList.get(i);
                    if (!obd2Client.isClosed()) {
                        double result = obd2Client.getPIDValue(pid);
                        long curtime = System.currentTimeMillis();
                        datapairs.put(pid, OBD2CoreUtils.createTimeValuePair(
                                curtime, result));
                    }
                } // for

                try {
                    JSONObject timeobj = new JSONObject();
                    timeobj.put(OBD2CoreConstants.VALUE, System.currentTimeMillis());
                    datapairs.put(OBD2CoreConstants.SIDDHI_TIMESTAMP, timeobj);

                    dataobj.put(OBD2CoreConstants.OBDII_REAL_TIME_DATA, datapairs);

                    Log.i(OBD2CoreConstants.APPTAG, dataobj.toString());
                    obd2EventManager.addDataObj(dataobj);

                } catch (JSONException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                try {
                    sleep(OBD2CoreConfiguration.getInstance().getOBD2UpdateSpeed());
                } catch (InterruptedException ignored) {
                }

            } //while

            if (!obd2Client.isClosed()) {
                obd2Client.close();
            }

        } catch (Exception e1) {

            // END

            //Change indicator icons is done by this too.
            mCommandService.stop(mCommandService.getCurrentDevice(), e1);
            try {
                obd2Client.close();
            } catch (OBD2ClientException e) {
                Log.e(TAG,"Unable to close OBD2Client", e);
            }
            OBD2CoreConfiguration obd2CoreConfigs = OBD2CoreConfiguration.getInstance();
            obd2CoreConfigs.setSupportedPIDsBool(new boolean[OBD2CoreConstants.MAX_PIDS_SUPPORTED]);

            e1.printStackTrace();
            appfilelogger.println(Log.getStackTraceString(e1), true);
        }

    }

    public void cancel() {
        try {
            if (!obd2Client.isClosed()) {
                obd2Client.close();
            }
        } catch (OBD2ClientException e) {
            Log.e(TAG, "cancel() of obd2Client failed", e);
        }
    }
}