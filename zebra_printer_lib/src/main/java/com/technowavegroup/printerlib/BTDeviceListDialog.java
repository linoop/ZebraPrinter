package com.technowavegroup.printerlib;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BTDeviceListDialog extends AppCompatDialog implements View.OnClickListener {
    private final Activity context;
    List<BluetoothDevice> bluetoothDevices;
    private BTSelectDeviceListener btSelectDeviceListener;

    public BTDeviceListDialog(Activity context, List<BluetoothDevice> bluetoothDevices, BTSelectDeviceListener btSelectDeviceListener) {
        super(context);
        this.context = context;
        this.bluetoothDevices = bluetoothDevices;
        this.btSelectDeviceListener = btSelectDeviceListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_device_list_dialog);
        //setTitle("Choose device");
        //context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RecyclerView recyclerViewDeviceList = findViewById(R.id.recyclerViewDeviceList);

        Button close = findViewById(R.id.buttonClose);
        Button save = findViewById(R.id.buttonOk);
        close.setOnClickListener(this);
        save.setOnClickListener(this);

        assert recyclerViewDeviceList != null;
        recyclerViewDeviceList.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewDeviceList.addItemDecoration(new RecyclerViewDecoration((int) context.getResources().getDimension(R.dimen.spacing_small)));
        recyclerViewDeviceList.setAdapter(new BTDeviceListAdapter(this, bluetoothDevices, btSelectDeviceListener));

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonOk) {
            //BTPrefManager.getInstance(context).savePrintTemplatePath(PRINT_TEMPLATE_FILE_PATH);
        }
        dismiss();
    }
}
