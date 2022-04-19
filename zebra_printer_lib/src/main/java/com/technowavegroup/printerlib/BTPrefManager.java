package com.technowavegroup.printerlib;

import android.content.Context;
import android.content.SharedPreferences;

public class BTPrefManager {
    private static final String PRINTER_PREF = "printer_pref";
    private static BTPrefManager BTPrefManagerInstance;
    private Context context;

    public BTPrefManager(Context context) {
        this.context = context;
    }

    public static synchronized BTPrefManager getInstance(Context context) {
        if (BTPrefManagerInstance == null) {
            BTPrefManagerInstance = new BTPrefManager(context);
        }
        return BTPrefManagerInstance;
    }

    public void savePrinterMacAddress(String macAddress) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PRINTER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("macAddress", macAddress);
        editor.apply();
    }

    public String getPrinterMacAddress() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PRINTER_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getString("macAddress", "");
    }

    public boolean savePrintTemplatePath(String template) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PRINTER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("printTemplate", template);
        return editor.commit();
    }
    public String getPrintTemplatePath() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PRINTER_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getString("printTemplate", "");
    }

    public boolean clearPrinterMacAddress(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PRINTER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        return editor.commit();
    }
}
