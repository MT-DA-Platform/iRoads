package com.codemo.www.iroads;

import android.util.Log;

import com.codemo.www.iroads.Database.SensorData;
import com.codemo.www.iroads.Entity.Vector3D;
import com.codemo.www.iroads.Fragments.SignalProcessor;
import com.codemo.www.iroads.Reorientation.NericellMechanism;
import com.codemo.www.iroads.Reorientation.Reorientation;
import com.codemo.www.iroads.Reorientation.ReorientationType;
import com.codemo.www.iroads.Reorientation.WolverineMechanism;

/**
 * Created by dushan on 4/3/18.
 */

public class SensorDataProcessor {

    private static final String TAG = "SensorDataProcessor";

    private static Reorientation reorientation=null;
    private static ReorientationType reorientationType;

    private static SignalProcessor signalProcessorX = new SignalProcessor();// process raw
    // accelerations with constant noise
    private static SignalProcessor signalProcessorY = new SignalProcessor();
    private static SignalProcessor signalProcessorZ = new SignalProcessor();

    private static SignalProcessor signalProcessorXReoriented = new SignalProcessor();// process
    // reoriented accelerations without constant noise
    private static SignalProcessor signalProcessorYReoriented = new SignalProcessor();
    private static SignalProcessor signalProcessorZReoriented = new SignalProcessor();

    private static SignalProcessor highPassSignalProcessorZ =new SignalProcessor();

    private static IRICalculator iriCalculator=new IRICalculator();

    private static double reorientedAx;// reoriented filtered ax value
    private static double reorientedAy;
    private static double reorientedAz;

    private static double reorientedAxWithNoise;// reoriented raw ax value
    private static double reorientedAyWithNoise;
    private static double reorientedAzWithNoise;

    private static double avgFilteredAx; // filtered ax value(constant noise exists)
    private static double avgFilteredAy;
    private static double avgFilteredAz;

    private static double highPassFilteredAz;

    private static double rms;

    private static double iri = 0;

    private static boolean nericellReorientation = false; // indicates the reorientation mechanism.


    /**
     * this method is triggering when sensor data changing from MobileSensor.java
     */
    public static void updateSensorDataProcessingValues(){
        stableOperation(); // do stable operations if vehicle is not moving

        updateAvgFilteredX();

        updateAvgFilteredY();

        updateAvgFilteredZ();

        updateCurrentReorientedAccelerations();

        updateHighPassFilteredZ();

        updateRms();

        //updating iri should be below updateAvgFilteredZ();
        updateIRI();
    }


    /*
    getters
     */
    public static double getReorientedAx() {
//        Log.d(TAG,"reorientedAx" + reorientedAx);
        return reorientedAx;
    }

    public static double getReorientedAy() {
//        Log.d(TAG,"reorientedAy" + reorientedAy);
        return reorientedAy;
    }

    public static double getReorientedAz() {
//        Log.d(TAG,"reorientedAz" + reorientedAz);
        return reorientedAz;
    }

    public static double getAvgFilteredAx() {
        return avgFilteredAx;
    }

    public static double getAvgFilteredAy() {
        return avgFilteredAy;
    }

    public static double getAvgFilteredAz() {
        return avgFilteredAz;
    }

    public static double getHighPassFilteredAz() {
        return highPassFilteredAz;
    }

    public static double getRms() {
        return rms;
    }

    public static double getIri() {
        return iri;
    }






    /*
    reorientation mechanism
     */
    public static ReorientationType getReorientationType() {
        return reorientationType;
    }

    public static void setReorientation(ReorientationType type){
        reorientationType=type;
        if (type==ReorientationType.Nericel){
            nericellReorientation = true; // indicates the reorientation mechanism
            reorientation=new NericellMechanism();
            Log.d(TAG,"Reorientation set to Nericel");
        }else if (type==ReorientationType.Wolverine){
            nericellReorientation = false;
            reorientation=new WolverineMechanism();
            Log.d(TAG,"Reorientation set to Wolverine");
        }
    }





    /*
    value updating methods
     */

    /**
     * this should run always or once in a peroid to keep updating reorientation.
     */
    public static void updateCurrentReorientedAccelerations(){

        if (reorientation!=null) {
            Vector3D reoriented= reorientation.reorient(getAvgFilteredAx(), getAvgFilteredAy(),
                    getAvgFilteredAz(), MobileSensors.getCurrentMagneticX(),
                    MobileSensors.getCurrentMagneticY(), MobileSensors.getCurrentMagneticZ());

            reorientedAxWithNoise=reoriented.getX();
            reorientedAx = signalProcessorXReoriented.averageFilter(reorientedAxWithNoise);
            reorientedAyWithNoise=reoriented.getY();
            reorientedAy = signalProcessorYReoriented.averageFilter(reorientedAyWithNoise);
            reorientedAzWithNoise=reoriented.getZ();
            reorientedAz = signalProcessorZReoriented.averageFilter(reorientedAzWithNoise);

        }else {
            Log.d(TAG,"set reorientation type first");
        }
    }


    public static void updateAvgFilteredX(){
       avgFilteredAx =signalProcessorX.averageFilterWithConstantNoise(MobileSensors.getCurrentAccelerationX());
    }

    public static void updateAvgFilteredY(){
        avgFilteredAy =signalProcessorY.averageFilterWithConstantNoise(MobileSensors.getCurrentAccelerationY());
    }

    public static void updateAvgFilteredZ(){
        avgFilteredAz =signalProcessorZ.averageFilterWithConstantNoise(MobileSensors.getCurrentAccelerationZ());
    }

    public static void updateHighPassFilteredZ(){
        highPassFilteredAz = highPassSignalProcessorZ.averageFilter(MobileSensors.getCurrentAccelerationZ());
    }



    public static void updateRms(){
        rms= Math.sqrt(Math.pow(MobileSensors.getCurrentAccelerationX(),2)
                +Math.pow(MobileSensors.getCurrentAccelerationY(),2)+
                Math.pow(MobileSensors.getCurrentAccelerationZ(),2));
    }



    public static void updateIRI(){

        /**
         * get avgFilteredZ for this method input.
         * if avgFilteredZ not updated in updateSensorDataProcessingValues() below line should be uncommented
         */
        //updateAvgFilteredZ();


        /**
         * only if avg filtering is updating,
         */
        //double k = getReorientedAy();
        if (getReorientedAy() > 9.5){
            //iriCalculator.processIRI_usingSlope(getReorientedAy());
            iri = iriCalculator.processIRI_using_aWindow(getReorientedAy());
            //iri = iriCalculator.processIRI_using_mean_and_standard_deviation(getReorientedAy());

        }
    }

    /**
     * gives vehicle speed using obd if obd available in the vehicle else using GPS.
     * @return
     */
    public static double vehicleSpeed(){
        String check = SensorData.getMobdSpeed(); // checks wether obd exists
        if(check == null) {
            double speed =  MobileSensors.getGpsSpeed();// gets GPS speed
            return speed;
        } else {
            double speed = Double.parseDouble(SensorData.getMobdSpeed());// gets OBD speed
            return speed;
        }
    }

    /**
     * conduct precalculations when vehicle is stopped.
     */
    public static void stableOperation(){
        double speed = SensorDataProcessor.vehicleSpeed();
        if(speed < 2.0){
            signalProcessorXReoriented.setConstantFactor(reorientedAxWithNoise);// collects
            // constant noise
            signalProcessorYReoriented.setConstantFactor(reorientedAyWithNoise - 9.8);
            signalProcessorZReoriented.setConstantFactor(reorientedAzWithNoise);
            if (nericellReorientation){
                ((NericellMechanism)reorientation).setStable(true); // calculates euler angles
            }
        } else {
            if (nericellReorientation){
                ((NericellMechanism)reorientation).setStable(false);
            }
        }
    }

}
