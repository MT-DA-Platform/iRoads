package com.codemo.www.iroads.Database;

/**
 * Created by uwin5 on 04/01/18.
 */

public class SensorData {

    private static String mobdSpeed = "0.0";
    private static String mobdRpm = "0.0";
    private static String mlat = "0.0";
    private static String mlon = "0.0";
    private static String macceX = "";
    private static String macceY = "";
    private static String macceZ = "";
    private static String deviceId = "";
    private static String journeyId = "";

    public static String getJourneyId() {
        return journeyId;
    }

    public static void setJourneyId(String journeyId) {
        SensorData.journeyId = journeyId;
    }

    public static String getMobdSpeed() {
        return mobdSpeed;
    }

    public static void setMobdSpeed(String mobdSpeed) {
        SensorData.mobdSpeed = mobdSpeed;
    }

    public static String getMobdRpm() {
        return mobdRpm;
    }

    public static void setMobdRpm(String mobdRpm) {
        SensorData.mobdRpm = mobdRpm;
    }

    public static String getMlat() {
        return mlat;
    }

    public static void setMlat(String mlat) {
        SensorData.mlat = mlat;
    }

    public static String getMlon() {
        return mlon;
    }

    public static void setMlon(String mlon) {
        SensorData.mlon = mlon;
    }

    public static String getMacceX() {
        return macceX;
    }

    public static void setMacceX(String macceX) {
        SensorData.macceX = macceX;
    }

    public static String getMacceY() {
        return macceY;
    }

    public static void setMacceY(String macceY) {
        SensorData.macceY = macceY;
    }

    public static String getMacceZ() {
        return macceZ;
    }

    public static void setMacceZ(String macceZ) {
        SensorData.macceZ = macceZ;
    }

    public static String getDeviceId() {
        return deviceId;
    }

    public static void setDeviceId(String deviceId) {
        SensorData.deviceId = deviceId;
    }
}
