package com.codemo.www.iroads;

import android.location.Location;
import com.codemo.www.iroads.Fragments.HomeFragment;

/**
 * Created by aminda on 3/9/2018.
 */


public class HomeController {
    public static void updateLocation(Location loc){
        HomeFragment.updateLocation(loc);
    }

    public static void updateOBD2Data(int speed,int rpm){
        HomeFragment.updateOBD2Data(speed, rpm);
    }

    public static void setMainActivity(MainActivity activity){
        HomeFragment.setMainActivity(activity);
    }

    public static void startSaving(){
        HomeFragment.startSaving();
    }

    public static void stopSaving(){
        HomeFragment.stopSaving();
    }
}
