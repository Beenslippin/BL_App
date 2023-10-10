package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;

public class DataDisplay extends AppCompatActivity {

   PointsGraphSeries<DataPoint> series;
    TextView sonicdata;
    StringBuilder DataIn;
    Toolbar toolbar;
    BluetoothConnectionService mBluetoothConnection;
    Button SendData;
    GraphView graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_display);

        toolbar = findViewById(R.id.mytoolbar);
        setSupportActionBar(toolbar);
        sonicdata = findViewById(R.id.SonicData);
        DataIn = new StringBuilder();
        SendData = findViewById(R.id.SendData);
        graph =findViewById(R.id.graph);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("DataIn")); // Need to set braodcast receiver.
        mBluetoothConnection = new BluetoothConnectionService(DataDisplay.this);



        init();
        SendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte [] DataToSend ={0,1,2,3,4,5,6};
                mBluetoothConnection.write(DataToSend);
            }
        });
    }
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


           for(int i= 0; i < 1; i++){
                int data = intent.getIntExtra("The Data:", 1);
                double x = series.getHighestValueX() + 1.0;
                series.appendData(new DataPoint(x, data),true , 1000);
            }
            //set some properties
            series.setShape(PointsGraphSeries.Shape.POINT);
            series.setColor(Color.BLUE);
            series.setSize(10f);

            //set Scrollable and Scaleable
            graph.getViewport().setScalable(true);
            graph.getViewport().setScalableY(true);
            graph.getViewport().setScrollable(true);
            graph.getViewport().setScrollableY(true);

            //set manual y bounds
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMaxY(255);
            graph.getViewport().setMinY(0);

            //set manual x bounds
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMaxX(100);
            graph.getViewport().setMinX(-10);

            graph.addSeries(series);

//            for(int i = 0; i < 100; i++){
//                int x = 0;
//                x++;
//                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
//                        new DataPoint(x, data),
//                });
//            }
//            graph.addSeries(series);

            //String text = String.valueOf(data);
            //String text = intent.getStringExtra("The Data:"); (Unused atm)
            //DataIn.append("data: ").append(text);
            //sonicdata.setText(DataIn);
        }
    };
    private void init(){
        series = new PointsGraphSeries<>();

    }
    protected void onDestroy() { // Disables Broadcast to allow for other resources to be used.
        super.onDestroy();
        unregisterReceiver(mReceiver);
        if(mBluetoothConnection!= null){
            mBluetoothConnection.resetBuffer();
        }
    }
    public void CommandSection(){
        byte C1 = 10;
        byte [] DataToSend ={0,1,2,3,4,5,6};
        //SendData(DataToSend);
    }
    public void SendData (byte [] data){
        mBluetoothConnection.write(data);
    }

}
