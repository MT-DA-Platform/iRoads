package com.codemo.www.iroads.Reorientation;

import java.util.ArrayList;

import android.hardware.GeomagneticField;
import android.hardware.SensorManager;
import android.util.Log;

import com.codemo.www.iroads.Entity.Vector3D;
import com.codemo.www.iroads.MobileSensors;

/**
 * Created by dushan on 3/18/18.
 */

public class WolverineMechanism implements Reorientation{
    private static final String TAG = "Wolverine";
    /*private Vector3D gravityVector;

    public Vector3D getGravityVector() {
        return gravityVector;
    }

    public void setGravityVector(Vector3D gravityVector) {
        if (this.gravityVector==null){
            this.gravityVector = gravityVector;
        }

    }*/


    @Override
    public Vector3D reorient( double xValueA, double yValueA, double zValueA, float xValueM, float yValueM, float zValueM) {
        Log.d(TAG,"********************** Wolverine run by pivi *************" );
        float[] rotation = new float[9];
        float[] inclination = new float[9];
        float[] gravity = {(float) xValueA, (float) yValueA, (float) zValueA};
        float[] geomagnetic = {xValueM, yValueM, zValueM};
        boolean  rotationMatrixOk= SensorManager.getRotationMatrix(rotation, inclination, gravity,
                geomagnetic);
        Log.d(TAG,"********************** rotation matrix sucessful:" + rotationMatrixOk);
        /*if(rotationMatrixOk) {
            for (int i=0 ; i< rotation.length ; i++) {
                Log.d(TAG,"********************** rotation matirx generated:" + rotation[i]);
            }
        }*/

        float geometryAx = rotation[0]*gravity[0] + rotation[1]*gravity[1] + rotation[2]*gravity[2];
        float geometryAy = rotation[3]*gravity[0] + rotation[4]*gravity[1] + rotation[5]*gravity[2];
        float geometryAz = rotation[6]*gravity[0] + rotation[7]*gravity[1] + rotation[8]*gravity[2];

        float latitude = (float)MobileSensors.getLat();
        float longitude = (float)MobileSensors.getLon();
        float altitude = (float)MobileSensors.getAlt();
        long timeMilis = System.currentTimeMillis();
        /*Log.d(TAG,"********************** lat:" + latitude);
        Log.d(TAG,"********************** lon:" + longitude);
        Log.d(TAG,"********************** alt:" + altitude);
        Log.d(TAG,"********************** time:" + timeMilis);*/


        GeomagneticField geomagneticField = new GeomagneticField(latitude, longitude, altitude,timeMilis);
        float magneticDeclination = geomagneticField.getDeclination();
        //Log.d(TAG,"********************** magneticDeclination:" + magneticDeclination);
        float bearing  = (float)MobileSensors.getBearing();
        //Log.d(TAG,"********************** bearing:" + bearing);

        float teta = bearing - magneticDeclination;
        double ay = geometryAy * Math.cos(teta) - geometryAx * Math.sin(teta);
        double ax = geometryAy * Math.sin(teta) + geometryAx * Math.cos(teta);
        double az = geometryAz;
        Log.d(TAG,"********************** ay:" + ay);
        Log.d(TAG,"********************** ax:" + ax);
        Log.d(TAG,"********************** az:" + az);

        Vector3D accelerationVector = new Vector3D();
        accelerationVector.setX(ax);// doing proper assignment of values to axises
        accelerationVector.setY(az);
        accelerationVector.setZ(-ay);

        return accelerationVector;

    }




    /**
     *
     * @param X
     * @param Y
     * @param Z
     * @param xAngle
     * @param yAngle
     * @param zAngle
     * @return rotated vector
     */
    /*public Vector3D rotateVector(double X, double Y, double Z, double xAngle, double yAngle, double zAngle){

        double rotatedX=
                X * ( Math.cos(zAngle) * Math.cos(yAngle) + Math.sin(zAngle) * Math.sin(xAngle) * Math.sin(yAngle) ) +
                Y * ( - Math.sin(zAngle) * Math.cos(yAngle) + Math.cos(zAngle) * Math.sin(xAngle) * Math.sin(yAngle) ) +
                Z * ( Math.cos(xAngle) * Math.sin(yAngle) );

        double rotatedY=
                X * ( Math.sin(zAngle) * Math.cos(xAngle) ) +
                Y * ( Math.cos(zAngle) * Math.cos(xAngle) ) +
                Z * ( -Math.sin(xAngle) );

        double rotatedZ=
                X * ( - Math.cos(zAngle) * Math.sin(yAngle)   +   Math.sin(zAngle) * Math.sin(xAngle) * Math.cos(yAngle) ) +
                Y * ( Math.sin(zAngle) * Math.sin(yAngle)   +   Math.cos(zAngle) * Math.sin(xAngle) * Math.cos(yAngle) ) +
                Z * ( Math.cos(xAngle) * Math.cos(yAngle) );

        Vector3D rotatedValues = new Vector3D();
        rotatedValues.setX(rotatedX);
        rotatedValues.setY(rotatedY);
        rotatedValues.setZ(rotatedZ);

        return rotatedValues;

    }*/

    /**
     *
     * @param vector1
     * @param vector2
     * @return cross product
     */
    /*public Vector3D getCrossProduct(Vector3D vector1, Vector3D vector2){

        double u1, u2, u3, v1, v2, v3;
        u1 = vector1.getX();
        u2 = vector1.getY();
        u3 = vector1.getZ();

        v1 = vector2.getX();
        v2 = vector2.getY();
        v3 = vector2.getZ();

        double uvi, uvj, uvk;
        uvi = u2 * v3 - v2 * u3;
        uvj = v1 * u3 - u1 * v3;
        uvk = u1 * v2 - v1 * u2;

        Vector3D crossProduct = new Vector3D();
        crossProduct.setX(uvi);
        crossProduct.setY(uvj);
        crossProduct.setZ(uvk);

        return crossProduct;
    }*/

    /**
     *
     * @param matrix arraylist of vectors
     * @param vectors
     * @return
     */
    /*public Vector3D getMatrixVectorMultiplication(ArrayList<Vector3D> matrix, Vector3D vectors){
        double x=matrix.get(0).getX()*vectors.getX() +  matrix.get(0).getY()*vectors.getY() + matrix.get(0).getZ()*vectors.getZ();
        double y=matrix.get(1).getX()*vectors.getX() +  matrix.get(1).getY()*vectors.getY() + matrix.get(1).getZ()*vectors.getZ();
        double z=matrix.get(2).getX()*vectors.getX() +  matrix.get(2).getY()*vectors.getY() + matrix.get(2).getZ()*vectors.getZ();

        Vector3D value=new Vector3D();
        value.setX(x);
        value.setY(y);
        value.setZ(z);

        return value;

    }*/

    /**
     *
     * @param matrixForRotatingToGeometricAxes
     * @param accelerationVector
     * @return
     */
    /*private Vector3D clearGravityFromAcclerations(ArrayList<Vector3D> matrixForRotatingToGeometricAxes, Vector3D accelerationVector) {

        double xAngle;
        double yAngle;
        double zAngle;

        //removing gravity vector from accelerations
        // TODO: 3/31/18 implement this

        return accelerationVector;

    }*/

}
