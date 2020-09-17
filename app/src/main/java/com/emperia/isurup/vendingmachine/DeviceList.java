package com.emperia.isurup.vendingmachine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;
import android.view.View;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.emperia.isurup.vendingmachine.model.Constants;

import java.util.Set;
import java.util.ArrayList;


public class DeviceList extends ActionBarActivity {


    private BluetoothAdapter myAdapter = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";

    Button buttonPaired;
    ListView deviceList; // keep available device list
    String receiveData; //to keep receive data from intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        deviceList = (ListView) findViewById(R.id.listView); //bind with UI
        buttonPaired = (Button) findViewById(R.id.button);  //bind with UI
        Bundle dataBundle = getIntent().getExtras(); //create a bundle for receive data
        receiveData = dataBundle.getString(Constants.DATA);

        myAdapter = BluetoothAdapter.getDefaultAdapter(); // if current device has bluetooth
        //check if device has bluetooth or not
        if (myAdapter == null) {   //if device does'nt has bluetooth the app will display a toast and terminate
            Toast.makeText(getApplicationContext(), "Bluetooth functionality is not available", Toast.LENGTH_LONG).show();
            finish();
        }
        //else if device has bluetooth it asks for permission to start bluetooth
        else if (!myAdapter.isEnabled()) {
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        }
// when press this button it will show current paired bluetooth devices.
        buttonPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPairedDevices();
            }
        });

    }


    /**
     * this method gets current paired device list from device
     */
    private void getPairedDevices() {
        pairedDevices = myAdapter.getBondedDevices();
        ArrayList list = new ArrayList(); //locally keep paired device list

        if (pairedDevices.size() > 0) {
            //if device has paired devices this for each loop will get device name and deviceAddress of each device
            for (BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        }

        // if there is no paired devices app will display a toast
        else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        deviceList.setAdapter(adapter);
        // if user clicks on item from paired device list
        deviceList.setOnItemClickListener(deviceClickListener);

    }


    // this method will execute when user clicks on Single item in device list
    private AdapterView.OnItemClickListener deviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17); // this will get the MAC deviceAddress of the device
            SharedPreferences sharedPref = getSharedPreferences(Constants.DEVICE_INFO, Context.MODE_APPEND);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(Constants.MAC_ADDRESS, address);
            editor.putString("deviceInfo", info);
            editor.commit();
            /**
             * Security Issue: Device Connectivity Handling.
             *
             * Problem: User able to select the device to connect from current pared device list.
             * Solution: We select the device in initially before the system expose to users. then the
             * application will keep the necessary information in shared preferences. user cant see
             * the Activity for configure and connect device
             */

            Intent deviceIntent = new Intent(DeviceList.this, BluetoothDataManager.class); //device intent
            deviceIntent.putExtra(EXTRA_ADDRESS, address); //pass the MAC deviceAddress of the device to next activity
            deviceIntent.putExtra(Constants.DATA, receiveData);   //pass data to Send data activity
            startActivity(deviceIntent); // starts next activity
        }
    };

    //call common super class methods
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return true;
    }


}
