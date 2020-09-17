package com.emperia.isurup.vendingmachine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.json.JSONException;

import android.graphics.drawable.BitmapDrawable;

import java.io.ByteArrayOutputStream;

import com.android.volley.Request;
import com.emperia.isurup.vendingmachine.model.Constants;
import com.google.gson.Gson;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Response;

import org.json.JSONObject;

import android.util.Log;
import android.widget.Toast;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    //TODO need access modifiers
    private ImageView imageView;
    private Button SendImage;
    private Button btnCamera;
    private Button tryAgain;
    private TextView showRecipe;
    private String text;
    private final String baseURL = "http://app.superfluid.xyz:8080"; //base url to connect
    private static final int CAMERA_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SendImage = (Button) findViewById(R.id.btnSend);
        btnCamera = (Button) findViewById(R.id.btnCamera);
        tryAgain = (Button) findViewById(R.id.tryAgain);
        imageView = (ImageView) findViewById(R.id.imageView);
        tryAgain = (Button) findViewById(R.id.tryAgain);
        SendImage.setVisibility(View.INVISIBLE);
        tryAgain.setVisibility(View.INVISIBLE);
        showRecipe = (TextView) findViewById(R.id.text_view_id);
        // JSONObject jsonObject;
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, CAMERA_REQUEST);
                }
            }
        });

        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, CAMERA_REQUEST);
                }
            }
        });
        SendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDatatoServer();
            }
        });
        showRecipe.setText(R.string.openCameraMsg);
    }

    /**
     * Handling HCI Issues
     * <p>
     * Accessibility of app for visually impaired people and user with other defects
     * <p>
     * Problem: this system does not fully support blind and other users with defects for all of its functionality.
     * <p>
     * We have been unable to provide support for this users by providing text speech functionality as of now.
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = (Bitmap) data.getExtras().get(Constants.DATA);
        imageView.setImageBitmap(bitmap);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get(Constants.DATA);
/**
 * HCI Issue: Ease of user interaction and  UX
 *
 * ISSUE : keep same text in button for relatively different task confused user.
 *
 * Solution : change the component (button) texts according to the duty of the component.
 *
 */


            btnCamera.setVisibility(View.INVISIBLE);
            tryAgain.setVisibility(View.VISIBLE);
            SendImage.setVisibility(View.VISIBLE);
            showRecipe.setText(R.string.waitRecipeMsg);
            try {
                sendImage(imageView);
            } catch (JSONException e) {
                Log.e("Error!", e.toString());
            }
        }
    }

    public void sendImage(ImageView imgPreview) throws JSONException {

        Bitmap bm = ((BitmapDrawable) imgPreview.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        sendImagetoServer(baos.toByteArray());
    }

    public void sendDatatoServer() {
        Intent send = new Intent(MainActivity.this, DeviceList.class);
        Intent machine = new Intent(MainActivity.this, BluetoothDataManager.class);

        SharedPreferences sharePref = getSharedPreferences(Constants.DEVICE_INFO, Context.MODE_APPEND);
        String mac = sharePref.getString(Constants.MAC_ADDRESS, "");
        if (mac.length() > 0) {
            machine.putExtra(Constants.DATA, text);
            startActivity(machine);
        } else {
            send.putExtra(Constants.DATA, text);
            startActivity(send);
        }

    }


    //TODO  move this logic into a separate Controller class

    /**
     * @param bitMapImage
     * @throws JSONException send image as a base64 string to server
     */
    private void sendImagetoServer(byte[] bitMapImage) throws JSONException {
        String URL = baseURL + "/api/v1/recipe";
        RequestQueue requestQ = Volley.newRequestQueue(this);

        JsonObjectRequest objectRequest = new JsonObjectRequest(
                Request.Method.POST, URL, getJsonObj(bitMapImage),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("Response", response.toString());
                        try {
                            RecipeResponseAction recipeResponseAction = new RecipeResponseAction();
                            recipeResponseAction.setJsonObject(response);
                            recipeResponseAction.run();
                            setResponseObj(response);
                            // msg(response.toString());
                        } catch (JSONException e) {
                            msg(getString(R.string.errorResponseMsg));
                        }
                        //  msg(response.toString());
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                Log.e("Response obj", response.toString());
                                Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
                            } else {
                                Log.e("Error Response", response.toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Response", error.toString());
                    }
                }

        );
        requestQ.add(objectRequest);
    }

    /**
     * Create json map
     *
     * @param image
     * @return
     */
    public Map prepareImageMap(byte[] image) {
        Map<String, Object> map = new HashMap<>();
        Map<String, String> metaMap = new HashMap<>();
        String base64Image = Base64.encodeToString(image, Base64.DEFAULT); // Encode byte array using Base64
        map.put("payloadType", "media");
        map.put("data", metaMap);
        metaMap.put("type", "image");
        metaMap.put("value", base64Image);
        return map;
    }

    /**
     * create json object to send to server
     */
    public JSONObject getJsonObj(byte[] image) throws JSONException {
        ResponseObject responseObject = new ResponseObject();
        ArrayList<Map> list = new ArrayList<>();
        list.add(prepareImageMap(image));
        responseObject.setPayloads(list);
        return new JSONObject(new Gson().toJson(responseObject));
    }

    /**
     * method to take json response from server and pass it
     */
    public void setResponseObj(JSONObject jsn) throws JSONException {
        Log.e("Response json", jsn.toString());
       // msg((jsn.toString()));
       text = jsn.toString();
        showRecipe.setText(jsn.getJSONArray("payloads").getJSONObject(0).getJSONObject("data").getJSONObject("recipe").getJSONArray("ingredients").toString());
    }

    /**
     * create toast easily
     */
    private void msg(String txt) {
        Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_LONG).show();
    }

    private class RecipeResponseAction implements Runnable {
        private JSONObject jsonObject;

        public void setJsonObject(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        @Override
        public void run() {
            try {
                setResponseObj(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
