package com.codemo.www.iroads;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.vatichub.obd2.OBD2CoreConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by uwin5 on 03/24/18.
 */

public class IroadsConfiguration {

    public static final String PREFS_NAME = "Kampana-pref-file";
    public static final String PREFS_DEFAULT_VALUE = "Kampana-pref-file-default-value";
    public static final String PIDS_DEFAULT = "obd2_speed,obd2_engine_rpm";
    private static IroadsConfiguration instance;
    private ArrayList<String> defaultPidsSet, pidsSet;
    private HashMap<String, String> settingsMap;
    private SharedPreferences settings;
    private boolean exitting;
    private Context context;
    private Handler mHandler;
    private int OBD2updatespeed = 500;
    private HashSet<String> dashboardPIDsSet;

    private IroadsConfiguration() {
        dashboardPIDsSet = new HashSet<>();

        settingsMap = new HashMap<String, String>();
    }

    public static IroadsConfiguration getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    public static void init() {
        instance = new IroadsConfiguration();
    }

    public String getSetting(String key) {

        String value = settingsMap.get(key);

        if (value == null) {
            value = settings.getString(key, PREFS_DEFAULT_VALUE);
        }

        if (value.equals(PREFS_DEFAULT_VALUE)) {
            value = null;
        } else {
            settingsMap.put(key, value);
        }

        return value;

    }

    public String getSetting(String key, String defaultValue) {

        String value = getSetting(key);
        if (value == null) value = defaultValue;
        return value;

    }

    public boolean isExitting() {
        return exitting;
    }

    public void setExitting(boolean exitting) {
        this.exitting = exitting;
        OBD2CoreConfiguration.getInstance().setExiting(exitting);
    }

    public void initApplicationSettings(Context context, Handler mhandler) {
        // Restore preferences
        this.context = context;
        this.mHandler = mhandler;
        settings = context.getSharedPreferences(PREFS_NAME, 0);

        OBD2updatespeed = Integer.parseInt(settings.getString(Constants.OBD2_UPDATE_DELAY, Constants.OBD2_UPDATE_DELAY_DEFAULT + ""));

        addDefaultPIDSettings();

    }

    public void sendToastToUI(String message) {
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, message);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    public void updateQueryPIDsList() {
        HashSet<String> newSet = new HashSet<String>();
        if (dashboardPIDsSet != null) {
            newSet.addAll(dashboardPIDsSet);
        }
        ArrayList<String> requestedPIDsList = new ArrayList<String>(newSet);
        OBD2CoreConfiguration.getInstance().setRequestedPIDsList(requestedPIDsList);
    }

    public HashSet<String> getDashboardPIDsSet() {
        return dashboardPIDsSet;
    }

    public void addDefaultPIDSettings() {
        pidsSet = new ArrayList<String>();
        defaultPidsSet = new ArrayList<String>();
        String[] defaultPids = PIDS_DEFAULT.split(",");
        for (int i = 0; i < defaultPids.length; i++) {
            pidsSet.add(defaultPids[i]);
        }
    }

    public ArrayList<String> getPidsSetting() {
        return this.pidsSet;
    }


}
