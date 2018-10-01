package com.codemo.www.iroads;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.codemo.www.iroads.Database.DatabaseHandler;
import com.codemo.www.iroads.Database.SensorData;
import com.codemo.www.iroads.Fragments.GraphFragment;
import com.pathsense.android.sdk.location.PathsenseDetectedActivities;
import com.pathsense.android.sdk.location.PathsenseDetectedActivity;
import com.pathsense.android.sdk.location.PathsenseInVehicleLocation;

import java.util.ArrayList;
import java.util.List;

public class PathsenseInVehicleReceiver extends BroadcastReceiver {
    private static final String TAG="PathsenseReceiver";
    private static long lastTime;
    private static String lastStatus = "";

    public static String getLastStatus() {
        return lastStatus;
    }

    public static void setLastStatus(String lastStatus) {
        PathsenseInVehicleReceiver.lastStatus = lastStatus;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        PathsenseInVehicleLocation inVehicleLocation = PathsenseInVehicleLocation.fromIntent(intent);
        if (inVehicleLocation != null)
        {
            Log.d(TAG+"_vehi","inVehicleLocation"+inVehicleLocation.toString());

        }


        PathsenseDetectedActivities detectedActivities = PathsenseDetectedActivities.fromIntent(intent);
        if (detectedActivities != null)
        {
            List<PathsenseDetectedActivity> detectedActivitiesList = detectedActivities.getDetectedActivities();

            StringBuilder sb=new StringBuilder();
            sb.append("Pathsense\n\n");
            ArrayList<PathsenseDetectedActivity> maxConf=new ArrayList<>();
            int i=0;
            for (PathsenseDetectedActivity activity:detectedActivitiesList){
                
                sb.append(activity.getDetectedActivity())
                        .append("\t\tConf: "+activity.getConfidence())
                        .append("\t\ttime: "+activity.getTimestamp()).append("\n");


                if (i==0){
                    maxConf.add(detectedActivitiesList.get(0));
                    i=1;
                    continue;
                }

                if (maxConf.get(0).getConfidence()<activity.getConfidence()){
                    maxConf.clear();
                    maxConf.add(activity);
                }else if (maxConf.get(0).getConfidence()==activity.getConfidence()){
                    maxConf.add(activity);
                }




            }
            sb.append("\n\n_");

            Log.d(TAG+"_act",sb.toString());

            StringBuilder sbmax=new StringBuilder();
            for (PathsenseDetectedActivity maxActivity:maxConf){

                sbmax.append(maxActivity.getDetectedActivity()).append(",");
            }

            Toast.makeText(context, sbmax.toString(), Toast.LENGTH_LONG).show();
            String required_status = "IN_VEHICLE,";
//            String required_status = "ON_FOOT,WALKING,";
            if(sbmax.toString().equals(required_status)){
                if(getLastStatus().equals(required_status)){
                    if(GraphFragment.isStarted()){
                        //  do nothing..
                    }else{
                        startJourney();
                        Toast.makeText(context, " JOURNEY STARTED", Toast.LENGTH_LONG).show();
                    }
                }
//                setLastTime(System.currentTimeMillis());
            }else{
                Log.d(TAG," diffrernt status 444444444444");
                if(GraphFragment.isStarted()){
                    Log.d(TAG," is started 444444444444");
                    if(getLastStatus().equals(required_status) || getLastStatus().equals("UNKNOWN,")){
                        //  do nothing..
                    }else{
                        stopJourney();
                        Toast.makeText(context, " JOURNEY STOPPED", Toast.LENGTH_LONG).show();
                    }
                }
            }

            setLastStatus(sbmax.toString());

        }
    }

    public void startJourney(){
        GraphFragment.setStarted(true);
        SensorData.setJourneyId(SensorData.getDeviceId()+ System.currentTimeMillis());
        DatabaseHandler.saveJourneyName();
    }

    public void stopJourney(){
        GraphFragment.setStarted(false);
    }

    public static long getLastTime() {
        return lastTime;
    }

    public static void setLastTime(long lastTime) {
        PathsenseInVehicleReceiver.lastTime = lastTime;
    }
}
