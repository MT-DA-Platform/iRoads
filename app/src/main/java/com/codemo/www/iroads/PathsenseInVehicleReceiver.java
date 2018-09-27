package com.codemo.www.iroads;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.pathsense.android.sdk.location.PathsenseDetectedActivities;
import com.pathsense.android.sdk.location.PathsenseDetectedActivity;
import com.pathsense.android.sdk.location.PathsenseInVehicleLocation;

import java.util.ArrayList;
import java.util.List;

public class PathsenseInVehicleReceiver extends BroadcastReceiver {
    private static final String TAG="PathsenseReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        PathsenseInVehicleLocation inVehicleLocation = PathsenseInVehicleLocation.fromIntent(intent);
        if (inVehicleLocation != null)
        {
            Log.d(TAG+"_vehi","inVehicleLocation");

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

            Toast.makeText(context,
                    sbmax.toString(), Toast.LENGTH_LONG).show();

        }
    }
}
