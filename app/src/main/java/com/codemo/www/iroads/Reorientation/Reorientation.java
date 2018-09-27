package com.codemo.www.iroads.Reorientation;

import com.codemo.www.iroads.Entity.Vector3D;

/**
 * Created by dushan on 3/18/18.
 */

public interface Reorientation {

    public Vector3D reorient(double xValueA, double yValueA, double zValueA, float xValueM,
                             float yValueM, float zValueM);
}
