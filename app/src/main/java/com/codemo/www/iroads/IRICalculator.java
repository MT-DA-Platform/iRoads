package com.codemo.www.iroads;

import java.util.ArrayList;

/**
 * Created by aminda on 3/31/2018.
 */

public class IRICalculator {

    private static final String TAG = "IRICalculator";

    /*private static ArrayList<Double> dataArray;
    private static ArrayList<Double> timeArray;
    private static ArrayList<Location> locationArray;
    private static boolean previousDirection;
    private static Location cuurentLoc;
    private StringBuilder dataReport;

    public IRICalculator() {
        dataArray = new ArrayList<Double>(); // create array for storing acceleration data
        timeArray = new ArrayList<Double>(); // create array for storing time
        locationArray = new ArrayList<Location>(); // create array for storing locations
        previousDirection = true; // if positive
    }*/
    private static int pulseCountUsingSlope = 0;
    private static double slopeMethodSensivity = -0.2;
    private static double upperBoundary = 11;
    private static double lowerBoundary = 9.8;
    private static double candidateValue = 0;
    private static boolean candidate = false;
    private static boolean notSelected = true;

    private static ArrayList<Double> dataQueue = new ArrayList<Double>();
    private static int pulseCountUsing_aWindow = 0;
    private static double windowMethodSensivity = -0.01;

    private static ArrayList<Double> dataQueue_for_mean_and_standard_deviation =
            new ArrayList<Double>();
    private static int pulseCountUsing_mean_and_standard_deviation = 0;
    private static double mean_and_standard_deviationMethodSensivity = 101;

    public static void setMean_and_standard_deviationMethodSensivity
            (double mean_and_standard_deviationMethodSensivity) {
        IRICalculator.mean_and_standard_deviationMethodSensivity =
                mean_and_standard_deviationMethodSensivity;
    }

    public static void setSlopeMethodSensivity(double slopeMethodSensivity) {
        IRICalculator.slopeMethodSensivity = slopeMethodSensivity;
    }

    public static void setWindowMethodSensivity(double windowMethodSensivity) {
        IRICalculator.windowMethodSensivity = windowMethodSensivity;
    }

    public static double mean(ArrayList<Double> data){
        double temp = 0.0;
        for(int i = 0; i < data.size(); i++ ){
            temp += data.get(i);
        }
        double mean = (double) temp / (double) data.size();
        return mean;
    }

    public static double standardDeviation(double mean, ArrayList<Double> data){
        double temp = 0.0;
        for (int i = 0; i < data.size(); i++) {
            temp += Math.pow(mean - data.get(i), 2);
        }
        double sd = (double) temp / (double) data.size();
        return sd;
    }

    public static double processIRI_using_mean_and_standard_deviation(double z){

        dataQueue_for_mean_and_standard_deviation.add(z);

        if(dataQueue_for_mean_and_standard_deviation.size() ==
                mean_and_standard_deviationMethodSensivity){

//            Log.d(TAG,"********************** Window is full:");

            double mean = mean(dataQueue_for_mean_and_standard_deviation);
            double sd = standardDeviation(mean, dataQueue_for_mean_and_standard_deviation);
            dataQueue_for_mean_and_standard_deviation.remove(0);
            if (z > mean && (z - (mean + 0.2)) >= 3 * sd) {
                pulseCountUsing_mean_and_standard_deviation++;
//                Log.d(TAG,"********************** pulseCountUsing_mean_and_standard_deviation:" +
//                        pulseCountUsing_mean_and_standard_deviation);
            }

            return (pulseCountUsing_mean_and_standard_deviation + pulseCountUsing_aWindow);

        } else {
            return processIRI_using_aWindow(z);
        }

    }

    public static double processIRI_using_aWindow(double z){
        if(dataQueue.size() < 3){
            if(dataQueue.isEmpty()){
                dataQueue.add(z);
            } else{
                if(dataQueue.get(dataQueue.size() -1) != z){
                    dataQueue.add(z);
                }
            }

        }

        if(dataQueue.size() == 3){
            if(dataQueue.get(0) < dataQueue.get(1) + windowMethodSensivity && dataQueue.get(1) +
                    windowMethodSensivity > dataQueue.get(2)){
                pulseCountUsing_aWindow++;
                dataQueue.clear();
            } else {
                dataQueue.remove(0);
            }
        }
//        Log.d(TAG,"********************** pulseCountUsing_aWindow:" + pulseCountUsing_aWindow);
        return pulseCountUsing_aWindow;
    }

    public static double processIRI_usingSlope(double z){

        if(lowerBoundary < z && z < upperBoundary) {
            if(notSelected) {
                if(!candidate){
                    candidate = true;
                    candidateValue = z;
                }else{
                    if (candidateValue - z < slopeMethodSensivity){// finds a peak
                        pulseCountUsingSlope++;
                        notSelected = false;
                    }
                    candidate = false;
                }

            }else {
                if (candidateValue > z){ // looks for the next peak
                    notSelected = true;
                    candidateValue = 0;
                }
            }
        }




//        boolean directionChanged = false;
        /*double area = 0.0;
        boolean currentDirection = true;  // if positive
        double earthGravity = 9.82;
        double thresholdPos = 0.4;
        double thresholdNeg = -0.4;
        double accelerationDiff = z-earthGravity;
        if(accelerationDiff > thresholdPos || accelerationDiff < thresholdNeg ){
            Double time = Double.valueOf(System.currentTimeMillis());

//            Log.d(TAG,"--------------- time ---------"+time);

            if(accelerationDiff > 0.0){
//                Log.d(TAG,"--------------- Accelleration POSITIVE Diff ---------  "+accelerationDiff);
            }else{
//                Log.d(TAG,"++++++++++++++++ Accelleration NEGATIVE Diff ++++++++++  "+accelerationDiff);
                currentDirection = false;
            }
            if(previousDirection != currentDirection){
                if(dataArray.size() > 1){
                    area = calculateArea();
//                    Log.d(TAG,"++++++++++++++++ Calculated Area ++++++++++  "+area);
                }
                dataArray.clear();
                timeArray.clear();
                dataArray.add(accelerationDiff);
                timeArray.add(time);
                locationArray.add(cuurentLoc);
            }else{
                dataArray.add(accelerationDiff);
                timeArray.add(time);
            }
            previousDirection = currentDirection;
//            dataReport.append("\nAcceleration Difference: "+accelerationDiff);
//            dataReport.append(" TimeStamp: "+time);
//            dataReport.append(" Location: "+cuurentLoc.getLatitude() + " " + cuurentLoc.getLongitude());

        }else{
            // do nothing
        }
        if(Math.abs(area) < 1.0 ){
            return area;
        }else{
            return 0.0;
        }*/

//        Log.d(TAG,"********************** pulseCountUsingSlope:" + pulseCountUsingSlope);
        return pulseCountUsingSlope;
    }

    /*public static double calculateArea(){
        double sum= 0.0;
        double totTime=0.0;
        double displacement=0.0;
        for (int i= 0; i<dataArray.size()-1; i++){
            double deltaT = timeArray.get(i+1)-timeArray.get(i);
//            Log.d(TAG,"********************** Delta time *************" + deltaT);
            if(deltaT < 145.0){
                sum += (dataArray.get(i) + dataArray.get(i+1)) * deltaT;
                totTime += deltaT;
            }else{
                if(sum != 0.0){
                    double velocityDiff = sum / 2000;
//                    Log.d(TAG,"********************** Velocity Diff *************" + velocityDiff);
                    displacement += velocityDiff * totTime / 2000;
//                    dataReport.append("\nDisplacement: " + displacement);
//                    Log.d(TAG,"********************** Displacement ********************************************************" + displacement);
                    sum = 0.0;
                    totTime = 0.0;
                }
            }
        }
        if(sum != 0.0){
            double velocityDiff = sum / 2000;
//            Log.d(TAG,"********************** Velocity Diff *************" + velocityDiff);
            displacement += velocityDiff * totTime / 2000;
//            dataReport.append("\nDisplacement: " + displacement);
//            Log.d(TAG,"********************** Displacement ********************************************************" + displacement);
        }
        return displacement;
//        totTime = dataArray.get(dataArray.size()-1) - dataArray.get(0)


    }*/
}
