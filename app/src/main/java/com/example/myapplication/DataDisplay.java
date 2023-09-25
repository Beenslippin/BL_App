package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class DataDisplay extends AppCompatActivity {

    BluetoothConnectionService mBluetoothConnection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_display);
    }

    public void SendData (byte [] data){
        mBluetoothConnection.write(data);
    }
}