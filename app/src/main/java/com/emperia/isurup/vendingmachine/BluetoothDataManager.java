package com.emperia.isurup.vendingmachine;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.view.View;
import android.widget.Button;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import android.widget.TextView;

import com.emperia.isurup.vendingmachine.model.Constants;

import java.io.IOException;

public class BluetoothDataManager extends ActionBarActivity {

    private ProgressDialog progress;
    private boolean isBtConnected = false;
    private BluetoothAdapter myBluetooth = null;
    private BluetoothSocket btSocket = null;
    private String deviceAddress = null;
    private String display;
    private String deviceInfo;
    private ArrayList<String> ingredientPortList;
    private Button buttonOn, buttonOff, buttonDis;
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //UUID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent addressInt = getIntent();
        //TODO move this getting of shared preferences to a different method
        SharedPreferences sharedPref = getSharedPreferences(Constants.DEVICE_INFO, Context.MODE_APPEND);
        String macAddress = sharedPref.getString(Constants.MAC_ADDRESS, "");
        deviceInfo = sharedPref.getString("deviceInfo", "");
        deviceAddress = addressInt.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the deviceAddress of the bluetooth device
        //display = addressInt.getStringExtra("data");
        if (deviceAddress == null || deviceAddress.length() == 0) {
            deviceAddress = macAddress;
        }
        try {
            JSONObject jsonObj = new JSONObject(getIntent().getStringExtra(Constants.DATA));
            decodeJson(jsonObj);
        } catch (JSONException e) {
            Log.e("Error in StringExtra", e.toString());
        }


        setContentView(R.layout.activity_send_data); //view of the BluetoothDataManager

        //bind UI
        buttonOn = (Button) findViewById(R.id.button2);
        buttonOff = (Button) findViewById(R.id.button3);
        buttonDis = (Button) findViewById(R.id.button4);

        new ConnectBT().execute(); //Call the class to connect

        //commands to be sent to bluetooth
        buttonOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDatatoDevice();    //method to pass data
            }
        });

        buttonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOffDevice();  //method to turn off LEDS
            }
        });

        buttonDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect(); //revoke the connection from current paired device
            }
        });

        TextView result = (TextView) findViewById(R.id.result);
        result.setText(display);
    }

    private void Disconnect() {
        if (btSocket != null) //If the btSocket is busy
        {
            try {
                btSocket.close(); //close connection
            } catch (IOException e) {
                msg("Error");
            }
        }
        finish(); //return to the first layout

    }

    // send command to close ports in superfluid machine
    private void closePort(String port) {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(port.getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    // send command to open ports in superfluid machine
    private void openPort(String port) {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(port.getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    //send ingredient ports to device to open
    public void sendDatatoDevice() {
        for (String port : ingredientPortList) {
            openPort(port);
        }
    }

    //send ingredient ports to device to close
    public void turnOffDevice() {
        for (String port : ingredientPortList) {
            closePort(port);
        }

    }

    public void decodeJson(JSONObject jsn) {
        JSONObject slicedJson = new JSONObject();
        JSONArray slicedIngJson = new JSONArray();
        ingredientPortList = new ArrayList<String>();
        // msg("WORKED"+ jsn.toString()); //
        try {

            slicedJson = jsn.getJSONArray("payloads").getJSONObject(0).getJSONObject("data");// access to ingredients in response json
            slicedIngJson = slicedJson.getJSONObject("recipe").getJSONArray("ingredients");
            display = slicedIngJson.toString();
        } catch (JSONException e) {
            Log.e("JsonExeption", e.toString());
        }

        for (int i = 0; i < slicedIngJson.length(); i++) {
            try {
                String str = slicedIngJson.getJSONObject(i).getJSONObject("generatedIngredient").getString("name");
                if (str.equalsIgnoreCase("lemon")) {
                    ingredientPortList.add("P1"); // add port 1 to array
                }
                if (str.equalsIgnoreCase("salt")) {
                    ingredientPortList.add("P2"); // add port 2 to array
                }
                if (str.equalsIgnoreCase("milk")) {
                    ingredientPortList.add("P3"); // add port 3 to array
                }
                if (str.equalsIgnoreCase("caffeine")) {
                    ingredientPortList.add("P4"); // add port 4 to array
                }
                if (str.equalsIgnoreCase("suger")) {
                    ingredientPortList.add("P5"); // add port 5 to array
                }

            } catch (JSONException ex) {
                Log.e("Error in sliced Json", ex.toString());
            }
        }

    }

    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(BluetoothDataManager.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(deviceAddress);//connects to the device's deviceAddress and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            } catch (IOException e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
                msg("Sorry. Connection fails with device!");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg(getString(R.string.connectionFailed) + deviceInfo + getString(R.string.tryAgain));
                finish();
            } else {
                msg("Connected!");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}