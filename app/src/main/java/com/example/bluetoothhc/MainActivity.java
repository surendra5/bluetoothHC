package com.example.bluetoothhc;

import android.bluetooth.BluetoothSocket;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static com.example.bluetoothhc.R.id.listView;

public class MainActivity extends AppCompatActivity {
    private  BluetoothSocket socket;
    private  BluetoothDevice mmDevice;
   // private UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
   static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    ListView listView;
    TextView statusView;
    Button searchButton;
    ArrayList<String> bluetoothDevices = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    //defining a common tag
    private final static String TAG = "Main Activity";
    //BT adapter is one for the entire system so define it globally

    private BluetoothAdapter bluetoothAdaptera;


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("Action2",action);
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                statusView.setText("Finished");
                searchButton.setEnabled(true);
                Log.i("Action3","df");
            }
            else if(BluetoothDevice.ACTION_FOUND.equals(action)){
                Log.i("Action34","dfsff");
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                String Address = device.getAddress(); // MAC address
                //distance between deivce or signal strength, more negative favourable
                //String rssi = Integer.toString(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE));
                Log.i("device found","Name: "+ name +"Address: " + Address );
//                String deviceInfo = "";
//                if(name== null || name.equals("")){
//                    deviceInfo += Address;
//                }
//                else{
//                    deviceInfo += name ;
//                }
                String deviceInfo=Address;
                if(!bluetoothDevices.contains(deviceInfo)) {
                    bluetoothDevices.add(deviceInfo);
                }
                arrayAdapter.notifyDataSetChanged();
            }

        }
    };


    public void SearchClicked(View view){
        arrayAdapter.clear();
        statusView.setText("Searching...");
        searchButton.setEnabled(false);
        Log.i("info - searchClicked","search button Disabled");
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        if(bluetoothAdaptera.isDiscovering()){
            bluetoothAdaptera.cancelDiscovery();
            Log.i("SearchCliced","Canceling discovery");
        }
        Log.i("SearchCliced","Starting Discovering");
        bluetoothAdaptera.startDiscovery();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //assing UI Components
        listView = findViewById(R.id.listView);
        statusView = findViewById(R.id.statusTextView);
        searchButton = findViewById(R.id.SearchButtonView);

        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,bluetoothDevices);
        listView.setAdapter(arrayAdapter);

        //getting the BT adapter
        bluetoothAdaptera = BluetoothAdapter.getDefaultAdapter();

        // check if bluetooth adapter is present in the device
        if(bluetoothAdaptera == null){
            Toast.makeText(getApplicationContext(),"Device doesn't support Bluetooth", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        int REQUEST_ENABLE_BT = 1;
        //check if BT is enabled
        if(!bluetoothAdaptera.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.EXTRA_DEVICE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(broadcastReceiver, intentFilter);
// WE NEED TO CREATE ANOTHER LIST FOR PAIRED DEVICES FOR ONLCIK EVENTS FUNCTION
        Set<BluetoothDevice> pairedDevices = bluetoothAdaptera.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.i("device",""+deviceName +" "+deviceHardwareAddress);
                if(deviceName== null || deviceName.equals("")){
                    bluetoothDevices.add(deviceHardwareAddress );
                }
                else{
                    bluetoothDevices.add(deviceHardwareAddress );

                    //  bluetoothDevices.add(deviceName );
                }
            }
        }



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.i("clickList1","itemclicked");


            String address = (String)arrayAdapter.getItem(position);
            BluetoothDevice device = bluetoothAdaptera.getRemoteDevice(address);
            Log.i("BTDevice C Name", device.getAddress());
            Log.d(TAG, address);

            BluetoothSocket tmp=null;

            bluetoothAdaptera.cancelDiscovery();
            mmDevice = device;
            try {
                // Use the UUID of the device that discovered //
                if (mmDevice != null)
                {
                    Log.i(TAG, "Device Name1: " + mmDevice.getName());
                    Log.i(TAG, "Device UUID1: " + mmDevice.getUuids()[0].getUuid());
                    tmp = device.createRfcommSocketToServiceRecord( mmDevice.getUuids()[0].getUuid());
//                    createMethod = device.getClass().getMethod("createInsecureRfcommSocket", new Class[] { int.class });
//                    tmp = (BluetoothSocket)createMethod.invoke(device, 1);

                    // tmp = device.createInsecureRfcommSocketToServiceRecord(mmDevice.getUuids()[0].getUuid());
                    Log.i("DEvice UUID","socket created successfully with device uuid");
                }
                else Log.d(TAG, "Device is null.");
            }
            catch (NullPointerException e)
            {
                Log.d(TAG, " UUID from device is null, Using Default UUID, Device name: " + device.getName());
                try {
                    tmp = device.createRfcommSocketToServiceRecord(myUUID);
                    Log.i("default UUID","socket created successfully with default uuid");
                } catch (IOException e1) {
                    e1.printStackTrace();
                    Log.i("IOEX","printStack");
                }
            }
            catch (IOException e) {
                Log.i("IOException","2");
            }

            socket = tmp;

            try {
                Log.i(TAG,"Trying to connect");
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect();
                Log.i(TAG,"connection_successful");
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    socket.close();
                    Log.i(TAG,"connection failed - socket closed");
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

        }
    });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(broadcastReceiver);
    }
}