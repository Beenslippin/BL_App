package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;


public class DataDisplay extends AppCompatActivity {

    TextView sonicdata;
    StringBuilder DataIn;
    Toolbar toolbar;
    BluetoothConnectionService mBluetoothConnection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_display);
        toolbar = findViewById(R.id.mytoolbar);
        setSupportActionBar(toolbar);
        sonicdata = findViewById(R.id.SonicData);
        DataIn = new StringBuilder();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("DataIn")); // Need to set braodcast receiver.

    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("The Data:");
            DataIn.append(text + "\n");
            sonicdata.setText(DataIn);
        }
    };
    protected void onDestroy() { // Disables Broadcast to allow for other resources to be used.
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public void SendData (byte [] data){
        mBluetoothConnection.write(data);
    }
}