package com.vatichub.obd2.connect;

import android.bluetooth.BluetoothDevice;

import java.util.Map;

public interface BTServiceCallback {
    void onStateChanged(int oldState, int newState, BluetoothDevice device);

    void onConnecting(BluetoothDevice device);
    void onConnectedDevice(BluetoothDevice device);
    void onConnectedCar();
    void onDisconnected(BluetoothDevice device, Map<String, Object> args);
    void onConnectionFailed(BluetoothDevice device);
    void onConnectionLost(BluetoothDevice device);
}
