package com.example.shaloin.gps2;

import android.*;
import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Gps4Activity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = "PlacesAPIActivity";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private GoogleApiClient mGoogleApiClient;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    private TextView display;
    private Button location_button,contacts_button;
    String number="+919707153020";

    HashSet<String> numbers;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps4);

        numbers=new HashSet<>();
        db = new UserDatabase(this).getReadableDatabase();


        location_button=(Button)findViewById(R.id.show_button);
        contacts_button=(Button)findViewById(R.id.view_button);
        display=(TextView)findViewById(R.id.location_textview);

        mGoogleApiClient = new GoogleApiClient.Builder(Gps4Activity.this)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .build();

        location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGoogleApiClient.isConnected()) {
                    if (ActivityCompat.checkSelfPermission(Gps4Activity.this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Gps4Activity.this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSION_REQUEST_CODE);
                        ActivityCompat.requestPermissions(Gps4Activity.this,
                                new String[]{Manifest.permission.SEND_SMS},
                                MY_PERMISSIONS_REQUEST_SEND_SMS);
                    }
                }
                callPlaceDetectionApi();
            }
        });

        contacts_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Gps4Activity.this,DetailsActivity.class);
                startActivity(intent);
            }
        });
    }




    public HashSet<String> getContacts(){

        Cursor cursor=db.rawQuery("SELECT * FROM "+UserDatabase.TABLE_NAME,null);
        /*db.execSQL("create table " + UserDatabase.TABLE_NAME + "(" + UserDatabase._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + UserDatabase.NAME + " TEXT NOT NULL, " +
                UserDatabase.NUMBER + " TEXT);");
*/
        while (cursor.moveToNext()){
            String contact=cursor.getString(cursor.getColumnIndex(UserDatabase.NUMBER));
            numbers.add(contact);
        }
        return numbers;
    }




    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callPlaceDetectionApi();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
                break;

        }
    }


    private void callPlaceDetectionApi() throws SecurityException {
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    Log.i(LOG_TAG, String.format("Place '%s' with " +
                                    "likelihood: %g",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood()));
                    display.setText(placeLikelihood.getPlace().getAddress().toString());
                    messageSending(placeLikelihood.getPlace().getAddress().toString());
                    break;
                }
                likelyPlaces.release();

            }
        });
    }

    public void messageSending(String message){
        SmsManager smsManager = SmsManager.getDefault();
//        smsManager.sendTextMessage(number, null, message, null, null);
        getContacts();
        smsManager.sendTextMessage(String.valueOf(numbers),null,message,null,null);
        Toast.makeText(getApplicationContext(), "SMS sent."+String.valueOf(numbers),
                Toast.LENGTH_LONG).show();
    }


}
