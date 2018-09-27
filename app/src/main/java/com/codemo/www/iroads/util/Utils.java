package com.codemo.www.iroads.util;


import org.json.JSONException;
import org.json.JSONObject;

import com.codemo.www.iroads.Constants;

public class Utils {

	public static JSONObject createTimeValuePair(long time, double value){
		JSONObject obj=new JSONObject();
		try {
			obj.put(Constants.TIME, time);
			obj.put(Constants.VALUE, value);
			return obj;
			
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static double getSpeedData(JSONObject data){
		
		double speed=-1;
		try {
			speed = data.getJSONObject(Constants.OBDII_REAL_TIME_DATA).getJSONObject(Constants.OBDII_SPEED).getDouble(Constants.VALUE);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return speed;
	}
	
	public static long getTimestamp(JSONObject data){
		
		long timestamp = 0;
		try {
			timestamp = data.getJSONObject(Constants.OBDII_REAL_TIME_DATA).getJSONObject(Constants.TIMESTAMP).getLong(Constants.VALUE);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return timestamp;
	}
	
	public static double getFuelRateData(JSONObject data){
		
		double fuelrate=-1;
		try {
			fuelrate = data.getJSONObject(Constants.OBDII_REAL_TIME_DATA).getJSONObject(Constants.OBDII_FUEL_RATE).getDouble(Constants.VALUE);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fuelrate;
	}
	
}
