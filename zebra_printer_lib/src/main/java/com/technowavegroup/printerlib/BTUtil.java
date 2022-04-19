package com.technowavegroup.printerlib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import com.zebra.sdk.comm.BluetoothConnectionInsecure;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.operations.internal.PrinterCalibrator;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BTUtil {
    public static final int BT_ENABLE_REQUEST_CODE = 100;
    public static final int PRINT_TEMPLATE_RESULT_CODE = 101;
    public static final String PRINTER_UUID = "00001101-0000-1000-8000-00805f9b34fb";
    @SuppressLint("StaticFieldLeak")
    private static BTUtil btUtilInstance;
    private final Activity activity;
    private final List<BluetoothDevice> paredDevices = new ArrayList<>();
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BluetoothDevice bluetoothDevice = null;
    private final String defaultPrinterMac;
    private boolean printSuccess;
    private Connection connection;
    private PrintStatusListener printStatusListener;

    private static synchronized BTUtil getInstance(Activity activity) {
        if (btUtilInstance == null) {
            btUtilInstance = new BTUtil(activity);
        }
        return btUtilInstance;
    }

    public BTUtil(Activity activity) {
        this.activity = activity;
        defaultPrinterMac = BTPrefManager.getInstance(activity).getPrinterMacAddress();
        findBTPrinter();
    }

    public void findBTPrinter() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                printStatusListener.onDeviceError("Bluetooth adapter not found!");
            }
            //assert bluetoothAdapter != null;
            if (!bluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(intent, BT_ENABLE_REQUEST_CODE);
            }
            Set<BluetoothDevice> availParedDevices = bluetoothAdapter.getBondedDevices();
            if (availParedDevices.size() > 0) {
                for (BluetoothDevice device : availParedDevices) {
                    ParcelUuid[] uuids = device.getUuids();
                    for (ParcelUuid uuid : uuids) {
                        if (uuid.getUuid().toString().equals(PRINTER_UUID)) {
                            paredDevices.add(device);
                            break;
                        }
                    }
                    if (defaultPrinterMac.equals(device.getAddress())) {
                        bluetoothDevice = device;
                    }
                }
            } else {
                printStatusListener.onDeviceError("No bluetooth device available!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void chooseBTDeviceDialog(PrintStatusListener printStatusListener) {
        BTDeviceListDialog btDeviceListDialog = new BTDeviceListDialog(activity, paredDevices, device -> {
            bluetoothDevice = device;
            BTPrefManager.getInstance(activity).savePrinterMacAddress(bluetoothDevice.getAddress());
            printStatusListener.onDeviceConnected(true, bluetoothDevice.getName()+" is set as default printer.", bluetoothDevice);
            //connectBTDevice("Please Wait", "Connecting to printer...", printStatusListener);
        });
        btDeviceListDialog.show();
    }

    private boolean isConnected = false;
    private Handler handler;

    public void connectBTDevice(String title, String message, PrintStatusListener printStatusListener) {
        this.printStatusListener = printStatusListener;
        if (defaultPrinterMac.equals("")) {
            printStatusListener.onDeviceConnected(false, "Failed to get default printer!", bluetoothDevice);
            return;
        }
        handler = new Handler();
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.showProgress(title, message);
        progressDialog.show();
        new Thread(() -> {
            try {
                if (bluetoothDevice != null) {
                    //Old code
                    /*UUID uuid = UUID.fromString(PRINTER_UUID);
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                    bluetoothSocket.connect();
                    outputStream = bluetoothSocket.getOutputStream();
                    inputStream = bluetoothSocket.getInputStream();*/
                    /*
                     * New code
                     * */

                    if (connection == null) {
                        connection = new BluetoothConnectionInsecure(bluetoothDevice.getAddress());
                        if (Looper.myLooper() == null) {
                            Looper.prepare();
                        }
                        connection.open();
                    } else if (!BTPrefManager.getInstance(activity).getPrinterMacAddress().equals(bluetoothDevice.getAddress())) {
                        connection = new BluetoothConnectionInsecure(bluetoothDevice.getAddress());
                        if (Looper.myLooper() == null) {
                            Looper.prepare();
                        }
                        connection.open();
                    }
                    isConnected = true;
                } else {
                    isConnected = false;
                }
                progressDialog.dismissProgress();
            } catch (Exception e) {
                e.printStackTrace();
                isConnected = false;
                progressDialog.dismissProgress();
            } finally {
                handler.post(() -> {
                    if (isConnected) {
                        printStatusListener.onDeviceConnected(true, "Printer connected successfully", bluetoothDevice);
                        //BTPrefManager.getInstance(activity).savePrinterMacAddress(bluetoothDevice.getAddress());
                    } else {
                        printStatusListener.onDeviceConnected(false, "Failed to connect printer!", bluetoothDevice);
                    }
                });
            }
        }).start();
    }

    public void print(String text) {
        handler = new Handler();
        if (bluetoothDevice != null && isConnected) {
            new Thread(() -> {
                printSuccess = false;
                try {
                    //Old codes
                    //outputStream.write(text.getBytes());
                    connection.write(text.getBytes());
                    printSuccess = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    printSuccess = false;
                } finally {
                    handler.post(() -> {
                        if (printSuccess)
                            printStatusListener.onPrintComplete(true, "Print completed");
                        else
                            printStatusListener.onPrintComplete(false, "Printer write failure!!!");
                    });
                }
            }).start();
        } else {
            printStatusListener.onPrintComplete(false, "Failed to connect printer!");
        }
    }

    public void printerCalibrate() {
        handler = new Handler();
        if (bluetoothDevice != null && isConnected) {
            new Thread(() -> {
                printSuccess = false;
                try {
                    PrinterCalibrator printerCalibrator = new PrinterCalibrator(connection,PrinterLanguage.ZPL);
                    printerCalibrator.execute();
                    printSuccess = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    printSuccess = false;
                } finally {
                    handler.post(() -> {
                        if (printSuccess)
                            printStatusListener.onPrintComplete(true, "Printer calibration completed");
                        else
                            printStatusListener.onPrintComplete(false, "Printer calibration failure!!!");
                    });
                }
            }).start();
        } else {
            printStatusListener.onPrintComplete(false, "Failed to connect printer!");
        }
    }

    public void printImage(final Bitmap bitmap, int x, int y, int width, int height) {
        //in this case PrinterLanguage.ZPL was passed into the method
        if (connection != null) {
            if (connection.isConnected()) {
                new Thread(() -> {
                    try {
                        PrinterLanguage language = PrinterLanguage.LINE_PRINT;
                        ZebraPrinter printer = ZebraPrinterFactory.getInstance(language, connection);  //used to be ZebraPrinterFactory.getInstance(connection) for RW420
                        printer.printImage(new ZebraImageAndroid(bitmap), x, y, width, height, false);
                        handler.post(()->{
                            printStatusListener.onPrintComplete(true,"Print completed");
                        });

                    } catch (ConnectionException e) {
                        handler.post(()->{
                            printStatusListener.onPrintComplete(false,"Failed to print");
                        });
                        e.printStackTrace();
                        try {
                            connection.close();
                        } catch (ConnectionException e1) {
                            e1.printStackTrace();
                        }
                    } finally {
                        if (bitmap != null && !bitmap.isRecycled()) {
                            bitmap.recycle();
                            System.gc();
                        }
                    }
                }).start();
            } else {
                printStatusListener.onPrintComplete(false,"Failed to print");
            }
        }
    }

    public void disconnectBTDevice() {
        handler = new Handler();
        if (bluetoothDevice != null && isConnected) {
            new Thread(() -> {
                try {
                    //Old code
                    /*outputStream.close();
                    inputStream.close();
                    bluetoothSocket.close();*/
                    //New code
                    connection.close();
                    Looper.myLooper().quit();
                    isConnected = false;
                    Log.d("printer connection", "Device disconnected successfully");
                } catch (Exception e) {
                    e.printStackTrace();
                    connection = null;
                } finally {
                    handler.post(() -> {
                        if (isConnected) {
                            printStatusListener.onDeviceDisconnected(false, "Failed to disconnect!!");
                            Log.d("printer connection", "Failed to disconnect!!");
                        } else {
                            printStatusListener.onDeviceDisconnected(true, "Device disconnected successfully");
                        }
                    });
                }
            }).start();
        } else {
            printStatusListener.onDeviceDisconnected(false, "Device not found");
        }
    }

    /*public void dropAllBTDevice() {
        handler = new Handler();
        if (bluetoothDevice != null && isConnected) {
            new Thread(() -> {
                try {
                    //Old code
                    *//*outputStream.close();
                    inputStream.close();
                    bluetoothSocket.close();*//*
                    //New code
                    connection.close();
                    Looper.myLooper().quit();
                    isConnected = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    connection = null;
                }
            }).start();
        }
    }*/
}
