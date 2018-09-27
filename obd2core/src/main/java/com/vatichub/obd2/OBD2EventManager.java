package com.vatichub.obd2;

import android.util.Log;

import com.vatichub.obd2.api.OBD2EventListener;
import com.vatichub.obd2.bean.OBD2Event;
import com.vatichub.obd2.util.OBD2CoreUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/* Manages all the graphs which are added to the UI. Whenever a graph is added to the UI, 
 * the related GeneralGraph object(super class) should be added to this. This will recieve
 * data as JSON objects to be drawn in the graphs and then the data is routed to the
 * related graph. Activities(Observers) will be notified when a graph is changed. For that 
 * they should be registered in this class(This is an Observable class).  
 */

/*Example format of the JSON object
 * 
 * "{"obd2_real_time_data":
 * 		{"obd2_rpm":
 * 			{"value":489,"time":0},
 * 		"obd2_speed":
 * 			{"value":6,"time":0}
 * 		}}\n";
 */

public class OBD2EventManager {

	private static OBD2EventManager instance;
	//private Hashtable<String, ArrayList<GeneralDisplay>> graphtablelist;
	//private DataLogger datalogger; todo add eventbased one
	private OBD2CoreConfiguration obd2CoreConfigs;
	//private TripDistanceCalculator tripDistanceCalculator;
	//private FuelEfficiencyCalculator fuelConsumptionCalculator;
    private List<OBD2EventListener> obd2EventListeners;
	private static final String TAG = "OBD2EventManager";
	
	private OBD2EventManager(){
		obd2CoreConfigs = OBD2CoreConfiguration.getInstance();
		//graphtablelist=new Hashtable<String, ArrayList<GeneralDisplay>>();
        obd2EventListeners = new ArrayList<OBD2EventListener>();
		//datalogger = obd2CoreConfigs.getDataLogger();
		//tripDistanceCalculator = obd2CoreConfigs.getTripDistanceCalculator();
		//fuelConsumptionCalculator = obd2CoreConfigs.getFuelConsumptionCalculator();
	}

    public static OBD2EventManager getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    public static void init() {
        instance = new OBD2EventManager();
    }

	public static void removeInstance(){ 
		instance = null;
	}

	public void addDataObj(JSONObject dataobj){

        for (OBD2EventListener listener : obd2EventListeners) {
			Log.d(TAG,"Listner " + listener + " recieving obd2 event");
            listener.receiveOBD2Event(new OBD2Event(dataobj));
        }

        /*
		try {
			datalogger.addDataObj(dataobj);
			tripDistanceCalculator.addDataObj(dataobj);
			fuelConsumptionCalculator.addDataObj(dataobj);
			
			String roottype=dataobj.names().getString(0);
			
			if(Constants.validateRootDataType(roottype)){
				ArrayList<GeneralDisplay> glist=graphtablelist.get(roottype);
				if(glist!=null){
					for (GeneralDisplay g : glist)
						g.addData(dataobj.getJSONObject(roottype));
				}
			}else{
				Log.e(Constants.APPTAG,"Unsupported root data type :" + roottype);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		*/
	}

    public void sendSinglePIDValue (String pid, String value) {
        JSONObject dataobj = new JSONObject();
        JSONObject datapairs = new JSONObject();

        try {
            long curtime = System.currentTimeMillis();
            datapairs.put(pid, OBD2CoreUtils.createTimeValuePair(
                    curtime, Double.parseDouble(value + "")));

            JSONObject timeobj = new JSONObject();
            timeobj.put(OBD2CoreConstants.VALUE, System.currentTimeMillis());
            datapairs.put(OBD2CoreConstants.SIDDHI_TIMESTAMP, timeobj);

            dataobj.put(OBD2CoreConstants.OBDII_REAL_TIME_DATA, datapairs);

            Log.i(OBD2CoreConstants.APPTAG, dataobj.toString());
            addDataObj(dataobj);
        } catch (JSONException e) {
            e.printStackTrace();
            OBD2CoreConfiguration.getInstance().getAppFileLogger().println(Log.getStackTraceString(e), true);
        }
    }

    public void registerOBD2EventListener (OBD2EventListener listener) {
        if (!obd2EventListeners.contains(listener)) {
            obd2EventListeners.add(listener);
        }
    }

    public void unregisterOBD2EventListener (OBD2EventListener listener) {
        if (obd2EventListeners.contains(listener)) {
            obd2EventListeners.remove(listener);
        }
    }
}
