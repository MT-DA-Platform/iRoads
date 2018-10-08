package com.codemo.www.iroads.Database;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.codemo.www.iroads.MainActivity;
import com.codemo.www.iroads.MobileSensors;
import com.codemo.www.iroads.SensorDataProcessor;

/**
 * Created by uwin5 on 04/01/18.
 */

public class DatabaseHandler {

    private Manager manager;
    private static Database database;
    private String mSyncGatewayUrl = "http://iroads.projects.mrt.ac.lk:4984/db/";
    private static final String TAG = "DatabaseHandler";


    public DatabaseHandler(Context context){
        try {
            manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
            database = getManager().getDatabase("iroads");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

    }

    public static void saveToDatabase(){
        // The properties that will be saved on the document
        Map<String, Object> properties = new HashMap<String, Object>();

        Log.d("DATA====",SensorData.getMacceX());
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
        properties.put("time",System.currentTimeMillis());
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


    // Replication
    public void startReplication() {
        URL url = null;
        try {
            url = new URL(mSyncGatewayUrl);
            Replication push=database.createPushReplication(url);
            push.addChangeListener(new Replication.ChangeListener()  {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    if (event.getStatus() == Replication.ReplicationStatus.REPLICATION_STOPPED){
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

    public static void printDocCount(){
        Log.d("DOC___COUNT",database.getDocumentCount()+"");

    }

    public Manager getManager() {
        return manager;
    }

    public Database getDatabase() {
        return database;
    }

    public static void saveJourneyName(){
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

    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("iroads.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void saveToDataBaseNew(Context context){
        Log.d("Start","==============================================================");
        Log.d("Databse","==========================="+database.getDocumentCount()+"===================================");
        String jid = null;
        try {
            JSONArray jsonArray = new JSONArray(loadJSONFromAsset(context));
            JSONObject item1 = (JSONObject) jsonArray.get(0);
            jid = item1.getString("journeyID");
            for (int i = 0; i < jsonArray.length(); i++) {
                Log.d("Writing","==============================="+i+"========================");
                JSONObject item = (JSONObject) jsonArray.get(i);
                // The properties that will be saved on the document
                Map<String, Object> properties = new HashMap<String, Object>();

                properties.put("journeyID", item.getString("journeyID"));
                properties.put("imei", item.getString("imei"));
                properties.put("lat", item.getString("lat"));
                properties.put("lon", item.getString("lon"));
                properties.put("obdSpeed", item.getString("obdSpeed"));
                properties.put("gpsSpeed", item.getString("gpsSpeed"));
                properties.put("obdRpm", item.getString("obdRpm"));
                properties.put("acceX", item.getString("acceX"));
                properties.put("acceY", item.getString("acceY"));
                properties.put("acceZ", item.getString("acceZ"));
                properties.put("acceX_raw", item.getString("acceX_raw"));
                properties.put("acceY_raw", item.getString("acceY_raw"));
                properties.put("acceZ_raw", item.getString("acceZ_raw"));
                properties.put("time",item.getString("time"));
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("Databse","==========================="+database.getDocumentCount()+"===================================");

        // The properties that will be saved on the document
        Map<String, Object> properties1 = new HashMap<String, Object>();

        properties1.put("journeyID", jid);
        properties1.put("journeyName", "HTC-Tida-Walpola-Matara");
        properties1.put("dataType", "trip_names");
        // Create a new document
        Document document = database.createDocument();
        // Save the document to the database
        try {
            document.putProperties(properties1);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        Log.d("Databse","==========================="+database.getDocumentCount()+"===================================");

        Log.d("End","==============================================================");
    }


//    public void writeToFile11(String data,Context context) {
//        try {
//            File logFile =new File("sdcard/log.txt");
////            if(){
////
////            }
////            FileWriter out = new FileWriter(new File(context.getFilesDir(), "myFile.txt"));
////            out.write(data);
////            out.close();
////            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("myfile.txt", Context.MODE_PRIVATE));
////            outputStreamWriter.write(data);
////            outputStreamWriter.close();
//
//        }
//        catch (IOException e) {
//            Log.e("Exception", "File write failed: " + e.toString());
//        }
//    }

}
