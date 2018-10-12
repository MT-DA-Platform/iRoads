package com.codemo.www.iroads.Database;

import android.content.Context;
import android.util.Log;

import com.codemo.www.iroads.MainActivity;
import com.codemo.www.iroads.MobileSensors;
import com.codemo.www.iroads.SensorDataProcessor;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by uwin5 on 04/01/18.
 */

public class DatabaseHandler {

    private static final String TAG = "DatabaseHandler";
    private static Database database;
    private Manager manager;
    private String mSyncGatewayUrl = "http://iroads.projects.mrt.ac.lk:4984/db/";


    public DatabaseHandler(Context context) {
        try {
            manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
            database = getManager().getDatabase("iroads");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

    }

    public static void saveToDatabase() {
        // The properties that will be saved on the document
        Map<String, Object> properties = new HashMap<String, Object>();

        Log.d("DATA====", SensorData.getMacceX());
        properties.put("journeyID", SensorData.getJourneyId());
        properties.put("imei", SensorData.getDeviceId());
        properties.put("model", SensorData.getModel());
        properties.put("lat", SensorData.getMlat());
        properties.put("lon", SensorData.getMlon());
        properties.put("obdSpeed", SensorDataProcessor.vehicleSpeed());
        properties.put("gpsSpeed", MobileSensors.getGpsSpeed());
        properties.put("obdRpm", SensorData.getMobdRpm());
        properties.put("acceX", SensorDataProcessor.getReorientedAx());
        properties.put("acceY", SensorDataProcessor.getReorientedAy());
        properties.put("acceZ", SensorDataProcessor.getReorientedAz());
        properties.put("acceX_raw", SensorData.getMacceX());
        properties.put("acceY_raw", SensorData.getMacceY());
        properties.put("acceZ_raw", SensorData.getMacceZ());
        properties.put("magnetX", SensorData.getMagnetX());
        properties.put("magnetY", SensorData.getMagnetY());
        properties.put("magnetZ", SensorData.getMagnetZ());
        properties.put("gyroX", SensorData.getGyroX());
        properties.put("gyroY", SensorData.getGyroY());
        properties.put("gyroZ", SensorData.getGyroZ());
        properties.put("time", System.currentTimeMillis());
        properties.put("dataType", "data_item");

        // Create a new document
        Document document = database.createDocument();
        // Save the document to the database
        try {
            document.putProperties(properties);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public static void saveJourneyName() {
        // The properties that will be saved on the document
        Map<String, Object> properties = new HashMap<String, Object>();

        properties.put("journeyID", SensorData.getJourneyId());
        properties.put("journeyName", "latest");
        properties.put("startLat", SensorData.getMlat());
        properties.put("startLon", SensorData.getMlon());
        properties.put("dataType", "trip_names");
        // Create a new document
        Document document = database.createDocument();
        // Save the document to the database
        try {
            document.putProperties(properties);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    // Replication
    public void startReplication() {
        URL url = null;
        try {
            url = new URL(mSyncGatewayUrl);
            Replication push = database.createPushReplication(url);
            push.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if (event.getStatus() == Replication.ReplicationStatus.REPLICATION_STOPPED) {
                        Log.i(TAG, "Replication stopped");
                        MainActivity.setReplicationStopped(true);
                    }
                }
            });
            push.start();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public Manager getManager() {
        return manager;
    }

}
