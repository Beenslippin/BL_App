package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.nfc.Tag;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionService";

    private static final String appName = "SonicAPP";

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66"); //Unique identifier
    private final BluetoothAdapter BA;
    Context mContext;

    private AcceptThread mInsecureAcceptThread;

    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID; //global UUID

    private ConnectedThread mConnectedThread;
    ProgressBar mProgressBar;

    // This Service will use Bluetooth Sockets.
    // Bluetooth sockets is the connection point that allows for an application to exchange data
    // With another bluetooth device.
    public BluetoothConnectionService(Context context) {
       mContext =context;
        BA = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        //Local server socket
        private final BluetoothServerSocket mServerSocket;

        //creating a new listening server socket

        public AcceptThread(){
            BluetoothServerSocket tmp = null;
            try{

                tmp =BA.listenUsingInsecureRfcommWithServiceRecord(appName,MY_UUID_INSECURE);
                Log.d(TAG,"AcceptThread: Setting up server using" + MY_UUID_INSECURE);

            } catch (IOException e) {

                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );

            }
            mServerSocket = tmp;
        }

        public void run(){
            Log.d(TAG,"run: AcceptThread is Running");

            BluetoothSocket socket = null; // accept thread will hang here until connection is accepted
            try{
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG, "run:RFCOM server socket start....");

                socket = mServerSocket.accept(); //sitting here till connection AYE O-KAY
            }catch(IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }
            if(socket != null){
                connected(socket,mmDevice);
            }
            Log.i(TAG,"END mAcceptThread ");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                mServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage() );
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread { //this thread connects allows for the connection of both devices through the socket.
        private BluetoothSocket mSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) { //default constructor
            Log.d(TAG, "ConnectThread: started."); //Log to show connect thread has started
            mmDevice = device; //bluetooth device
            deviceUUID = uuid; //Unique ID
        }


        public void run(){ //Will automatically run/execute when a ConnectThread object or AcceptThread object is created

            BluetoothSocket tmp = null;
            Log.i(TAG,"RUN mConnectedThread");
            //get bl socket for a connection with the given bluetooth device

            try { //take temporary bluetooth socket and make an RFCOMM socket
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "+ MY_UUID_INSECURE );
                tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }
            mSocket = tmp;

            BA.cancelDiscovery(); // Canceling discovery as memory intensive


            try { // this is a blocking call and will only return on a successful connection or an exception
                mSocket.connect(); // MAKING CONNECTION TO BLUETOOTH SOCKET
            } catch (IOException e) {
                // IF EXCEPTION CLOSE SOCKET
                try {
                    mSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE ); //MESSAGE: if call fails
            }
            connected(mSocket,mmDevice); // IF message not thrown advance here and call method CONNECTED
        }
        public void cancel() { // CANCEL METHOD
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage() );
            }
        }
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() { //used to initiate Accept thread.
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection and create a new one
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start(); //starts the thread
        }
    }

    /**
     AcceptThread starts and sits waiting for a connection.
     Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     **/
    public void startClient(BluetoothDevice device,UUID uuid){ //Initiates Connect Thread
    Log.d(TAG,"startClient: Started");

    //lets user now we are trying to make a connection will try implement a progress bar
        Toast.makeText(mContext, "Connecting, Please Wait", Toast.LENGTH_SHORT).show(); // to initiate connection
        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){ //Default Constructor Connected Thread will be Managing Connection.
            // (Point of time of when a connection has been made)
            Log.d(TAG,"ConnectedThread: Starting");

            //Declaring Variables
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //Connection has been established
            Toast.makeText(mContext, "Connection has been made", Toast.LENGTH_SHORT).show();

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run (){
            //Creating a Byte Array object that will collect the input from the input stream

            //Buffer store for input stream
            byte [] buffer = new byte[1024];

            // Integer object that will read the input from the input stream
            // Return from read()
            int bytes;

            // Keep listening to Input Stream until new information is IN (exception)
            while(true){
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0 , bytes); // Convert Byte to a string.
                    Log.d(TAG,"InputStream: " + incomingMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

            }
        }

    }
}
