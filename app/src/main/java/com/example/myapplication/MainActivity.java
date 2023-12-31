package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";
//    public MainActivity(){ // Initiating Permissions Checks On Start up, Might need to be changed if not Working
//        checkBTPermissions();
//        if (!BA.isEnabled()) {
//            enableDisableBT();
//            Log.d(TAG, "OnClick: EnablingBluetooth and Requesting permission");
//        }
//    }
    TextView ListTxt;
    ListView listview;
    BluetoothAdapter BA;
    public ArrayList<BluetoothDevice> BTDevices = new ArrayList<>();
    public DeviceListAdapter DLA;
    BluetoothConnectionService mBluetoothConnection;
    ProgressBar progressBar;// Declaring the Service class to pass back and fourth

    private final Handler Timer = new Handler();
     private static final UUID MY_UUID_INSECURE =
                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Unique identifier@Override
    BluetoothDevice mBTDevice;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Applications
        ListTxt = findViewById(R.id.ListTxt);
        listview = findViewById(R.id.listview);
        Toolbar toolbar = findViewById(R.id.mytoolbar);
        setSupportActionBar(toolbar);
        BTDevices = new ArrayList<>();

        //Intent filter for pairing of device
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver3, filter);

        //Defining Bluetooth Adapter to default value
        BA = BluetoothAdapter.getDefaultAdapter();

        //Item Listener when Device displayed is clicked
        listview.setOnItemClickListener(MainActivity.this);

        progressBar = findViewById(R.id.progressBar);

    }

    // PERMISSION GRANTING:
    //Granting or Requesting permission as Google play SDK > 31 run time permission security update
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) { //Basic Switch to check state of a request code when checking for permission, if permission not Granted then request.
            case 1:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission has been granted", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Bluetooth: Connect Permission Granted");

                } else if (grantResults.length > 0) {
                    Toast.makeText(this, "Permission is Required before Proceeding", Toast.LENGTH_SHORT).show();
                }
                break;

            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission has been granted", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Bluetooth: Scan Permission Granted");

                } else if (grantResults.length > 0) {
                    Toast.makeText(this, "Permission is Required before Proceeding", Toast.LENGTH_SHORT).show();
                }
                break;

            case 3:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission has been granted", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "");

                } else if (grantResults.length > 0) {
                    Toast.makeText(this, "Permission is Required before Proceeding", Toast.LENGTH_SHORT).show();
                }
                break;
            case 4:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Location Has been Permitted", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    // Enables the bluetooth of the device
    public void enableDisableBT() {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            }
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            if (BA == null) {
                Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            if (!BA.isEnabled()) {
                Log.d(TAG, "enableDisableBT: Enabling Bluetooth");
                Intent enableBTintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                startActivity(enableBTintent);

                IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED); //Filter that intercepts bluetooth status and logs it.
                registerReceiver(mBroadcastReceiver1, BTIntent);
            }
            if (BA.isEnabled()) {
                Log.d(TAG, "enableDisableBT: Bluetooth Already Enabled");

                IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED); //Filter that intercepts bluetooth status and logs it.
                registerReceiver(mBroadcastReceiver1, BTIntent);

            }
        }
    }

    //for discovering available devices
    public void discover(View view) {

        checkBTPermissions();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
            }
        }
        if(!BA.isEnabled()){
            enableDisableBT();
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED  ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (BA.isDiscovering()) {
                BA.cancelDiscovery();
                Log.d(TAG, "btnDiscover: Canceling discovery.");

                //check BT permissions in manifest
                //checkBTPermissions();

                BA.startDiscovery();
                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mBroadcastReceiver2, discoverDevicesIntent);
            }

            if (!BA.isDiscovering()) {
                Log.d(TAG, "Discover: Discovery is Starting");
                //checkBTPermissions();
                BA.startDiscovery();
                IntentFilter discoverDeviceIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mBroadcastReceiver2, discoverDeviceIntent);


            }
        }
    }

    //Initializing and Logging the State of BLUETOOTH
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) { //Allows to follow the states of bluetooth and log them.*/
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);// ERROR IF ONE OCCURS
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };


    //Discovery Broadcaster
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "OnReceive: Action Found");

            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                }
            }
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); // EXTRA = Parcel device, allows you to store device
                    if (device != null && "SonicBoom".equals(device.getName())){
                        BTDevices.add(device);

                        assert device != null;
                        Log.d(TAG, "OnReceive:" + device.getName() + ": " + device.getAddress());
                        DLA = new DeviceListAdapter(context, R.layout.device_adapter_view, BTDevices);
                        listview.setAdapter(DLA);
                    }


                }
            }
        }
    };

    //Bonding States
    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.S)
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device1 = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 possible cases
                //Case 1: Bonded Already

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                }
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {


                    //When a Bond is created (Pairing is Initialized) these if Statements will be executed
                    if (device1.getBondState() == BluetoothDevice.BOND_BONDED) {
                        Log.d(TAG, "BroadcastReceiver: BOND_BONDED");
                        mBTDevice = device1; //Assign global BTdevice to the Device its Paired with
                        progressBar.setVisibility(View.INVISIBLE);
                        StartConnection();
                    }
                    //case 2: Creating a Bond
                    if (device1.getBondState() == BluetoothDevice.BOND_BONDING) {
                        Log.d(TAG, "BroadcastReceiver: BOND_BONDING");
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    //case 3 Bond is Broken
                    if (device1.getBondState() == BluetoothDevice.BOND_NONE) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "BroadcastReceiver: BOND_NONE");
                    }
                }
            }
        }
    };

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private void checkBTPermissions() {
       if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED  ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 4);
           Log.d(TAG,"Location Request has been requested");
       }
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
//            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
//            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
//            if (permissionCheck != 0) {
//
//                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
//            }
//        } else {
//            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
//        }
    }

    protected void onDestroy() { // Disables Broadcast to allow for other resources to be used.
        super.onDestroy();
        Log.d(TAG,"OnDestroy: Called");
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        mBluetoothConnection.resetBuffer();
    }

    // When A Device is clicked
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    // When Device is clicked create a BOND and Start ConnectionService
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        // Permission Check for Scan and Connect
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED  ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN}, 1);
        }

        // Always Cancel Discovery as it is memory intensive
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED){
            BA.cancelDiscovery();
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "OnItemClick: Device has been clicked on");

            // Save Name and Address
            String deviceName = BTDevices.get(i).getName();
            String DeviceAddress = BTDevices.get(i).getAddress();

            Log.d(TAG, "OnItemClick: Device Name" + deviceName);
            Log.d(TAG, "OnItemClick: Device Address" + DeviceAddress);


                // Device is not paired, initiate pairing
                Log.d(TAG, "Pairing with " + deviceName);
                Toast.makeText(this, "Pairing with: " + deviceName, Toast.LENGTH_SHORT).show();
                BTDevices.get(i).createBond();

                if(BTDevices.get(i).getBondState()==BluetoothDevice.BOND_BONDED){
                    StartConnection();
                }
            // Start ConnectionService, Ensure BT device is assigned first
            mBTDevice = BTDevices.get(i);
            mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
            // Connection WIll Start and the ACCEPTTHREAD will sit there waiting for connection until we Bond state is BONDED.
            // This will initiate the STARTCONNECTION Method that will try connect to the other devices Accept thread.
            // Once Completed the CONNECTEDTHREAD Will start
            // Now data can start being sent back and forward.

        }

    }

    // Create method for starting connection
    // Connection will fail and app will crash if  haven't paired first
    @SuppressLint("MissingPermission")
    public void StartConnection(){ //Pass Device and UUID

                // Creating a Pop up box to commence the connection to allow for Connect Thread to commence
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Start Connection");
                builder.setMessage("Would you like to Start the Connection");
                //If Start clicked Start connection clicked otherwise close dialog box and close broadcast.
                builder.setPositiveButton("Start", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startBTConnection(mBTDevice,MY_UUID_INSECURE);
                        OpenDataDisplay(); // Open new page *** might not work CHECK//*** CHECK MIGHT NOT WORK
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onDestroy(); // May not need this.
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
    }


    // Starting the Connection to the device.
        @SuppressLint("MissingPermission")
        public void startBTConnection(BluetoothDevice device, UUID uuid){
            Log.d(TAG,"StartBTConnection: Initializing RFCOM Bluetooth Connection");
            Toast.makeText(this, "Connection Starting with" + mBTDevice.getName(), Toast.LENGTH_SHORT).show();
            mBluetoothConnection.startClient(device,uuid); // Calls Start Client Thread in Connection service that will Start the ConnectThread method
        }
    //Sending Data (Bytes)

    public void CommandSection(){
        byte C1 = 10;
        byte [] DataToSend ={C1};
        SendData(DataToSend);
    }

    public void SendData (byte [] data){
        mBluetoothConnection.write(data);

    }
    //Need to call StartBtConnection somewhere to start client

    // UPDATE: Preparing New activity once ESP32 is connected to display data
    public void OpenDataDisplay (){
        Intent dataintent =new Intent(this, DataDisplay.class);
        startActivity(dataintent);
    }


}

// To Do:
// Filter for only ESP32 SONIC BOOM
// Take data and display on Datadisplay
// Send data back to ESP32
