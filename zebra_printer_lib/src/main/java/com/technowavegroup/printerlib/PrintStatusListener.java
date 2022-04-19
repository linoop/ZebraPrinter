package com.technowavegroup.printerlib;

import android.bluetooth.BluetoothDevice;

public interface PrintStatusListener {

    void onDeviceConnected(boolean isConnected, String statusMessage, BluetoothDevice bluetoothDevice);

    void onPrintComplete(boolean isPrinted, String printStatus);

    void onDeviceDisconnected(boolean isDisconnected, String statusMessage);

    void onDeviceError(String errorMessage);
}
