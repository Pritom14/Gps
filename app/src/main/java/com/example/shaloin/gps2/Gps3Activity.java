package com.example.shaloin.gps2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Gps3Activity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    private Button display;
    private TextView displayLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private RequestQueue requestQueue;
    private double lat;
    private double lng;
    String address="";
    String number="+919707153020";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps3);

        display = (Button) findViewById(R.id.displayL);
        displayLocation=(TextView)findViewById(R.id.location1);
        requestQueue = Volley.newRequestQueue(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new myLocationlistener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);

        display.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getLocation(lat,lng);

                Log.e("Latitude", String.valueOf(lat));
                Log.e("Longitude", String.valueOf(lng));
                Log.e("city",address);
                Toast.makeText(getApplicationContext(), "City : " + address, Toast.LENGTH_LONG);
                displayLocation.setText("City : "+address);
                if (ContextCompat.checkSelfPermission(Gps3Activity.this,
                        Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(Gps3Activity.this,
                            Manifest.permission.SEND_SMS)) {
                    } else {
                        ActivityCompat.requestPermissions(Gps3Activity.this,
                                new String[]{Manifest.permission.SEND_SMS},
                                MY_PERMISSIONS_REQUEST_SEND_SMS);
                    }
                    return;
                }
                messageSending();

//                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
//                        "http://maps.googleapis.com/maps/api/geocode/json?latlng="+ 24.7593+","+92.7839 +"&sensor=true",
//                        new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            address = response.getJSONArray("results").getJSONObject(0).getString("formatted_address");
//                            //textViewCity.setText(address);
//                            //Toast.makeText(getApplicationContext(), "City : " + address, Toast.LENGTH_LONG);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//
//                    }
//                });
//                requestQueue.add(request);
            }
        });
    }

    private class myLocationlistener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                lat = location.getLatitude();
                lng = location.getLongitude();
                //Toast.makeText(getApplicationContext(),"Latitude: "+lat+"\nLongitude: "+lng,Toast.LENGTH_LONG).show();
                getLocation(lat,lng);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }

    public void getLocation(double latitude,double longitude){
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                "http://maps.googleapis.com/maps/api/geocode/json?latlng="+ latitude+","+longitude +"&sensor=true",
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            address = response.getJSONArray("results").getJSONObject(0).getString("formatted_address");
                            //textViewCity.setText(address);
                            //Toast.makeText(getApplicationContext(), "City : " + address, Toast.LENGTH_LONG);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(request);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    messageSending();

                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

    }

    public void messageSending(){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, address, null, null);
        Toast.makeText(getApplicationContext(), "SMS sent.",
                Toast.LENGTH_LONG).show();
    }
}
