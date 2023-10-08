package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.util.List;


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

            int data = intent.getIntExtra("The Data:", 1);
            String text = String.valueOf(data);
            //String text = intent.getStringExtra("The Data:");
            DataIn.append("data: ").append(text).append("\n");
            sonicdata.setText(DataIn);
        }
    };
    protected void onDestroy() { // Disables Broadcast to allow for other resources to be used.
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
    public void CommandSection(){
        byte C1 = 10;
        byte [] DataToSend ={C1};
        SendData(DataToSend);
    }
    public void SendData (byte [] data){
        mBluetoothConnection.write(data);

    }
}
