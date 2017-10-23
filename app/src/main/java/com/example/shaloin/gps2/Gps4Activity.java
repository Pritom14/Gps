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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
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
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int ACCESS_FINE_LOCATION_ID=99;
    private static final int SEND_SMS_REQUEST_ID=98;
    private static final int RECEIVE_SMS_REQUEST_ID=97;
    private static final int ACCESS_COARSE_LOCATION_ID=96;

    private TextView display;
    private Button location_button,contacts_button;
    //String number="+919707153020";
    ArrayList<String> numbers;
    ArrayList<String> result;
    SQLiteDatabase db;
    DatabaseManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps4);
        numbers=new ArrayList<>();
        result=new ArrayList<>();
        db = new UserDatabase(this).getReadableDatabase();
        manager=new DatabaseManager(Gps4Activity.this);
        location_button=(Button)findViewById(R.id.show_button);
        contacts_button=(Button)findViewById(R.id.view_button);
        display=(TextView)findViewById(R.id.location_textview);

        mGoogleApiClient = new GoogleApiClient.Builder(Gps4Activity.this)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .build();

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                ACCESS_FINE_LOCATION_ID);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.SEND_SMS},
                SEND_SMS_REQUEST_ID);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECEIVE_SMS},
                RECEIVE_SMS_REQUEST_ID);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                ACCESS_COARSE_LOCATION_ID);
        location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!manager.isEmpty()) {
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
                 else {
                    final AlertDialog.Builder dialogBuilder=new AlertDialog.Builder(Gps4Activity.this);
                    LayoutInflater inflater=Gps4Activity.this.getLayoutInflater();
                    final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
                    dialogBuilder.setView(dialogView);
                    final EditText name = (EditText) dialogView.findViewById(R.id.dialogEditNmID);
                    final EditText phone = (EditText) dialogView.findViewById(R.id.dialogEditPhID);

                    dialogBuilder.setTitle("Send To");
                    dialogBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!TextUtils.isEmpty(name.getText().toString())
                                    && !TextUtils.isEmpty(phone.getText().toString())){
                                callPlaceDetectionApi2(phone.getText().toString());
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Empty field(s)",Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog b=dialogBuilder.create();
                    b.show();
                }
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

    public ArrayList<String> getContacts(){

        Cursor cursor=db.rawQuery("SELECT * FROM "+UserDatabase.TABLE_NAME,null);
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

    private void callPlaceDetectionApi2(final String ph_number) throws SecurityException {
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
                    messageSending_individual(ph_number,placeLikelihood.getPlace().getAddress().toString());
                    break;
                }
                likelyPlaces.release();
            }
        });
    }

    public void messageSending(String message){
        SmsManager smsManager = SmsManager.getDefault();
//        smsManager.sendTextMessage(number, null, message, null, null);
        result=getContacts();
        for (int i=0;i<result.size();i++){
            smsManager.sendTextMessage(result.get(i),null,message,null,null);
            Toast.makeText(getApplicationContext(), "SMS sent : "+String.valueOf(numbers),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void messageSending_individual(String ph_number,String message){
        SmsManager smsManager=SmsManager.getDefault();
        smsManager.sendTextMessage(ph_number,null,message,null,null);
        Toast.makeText(getApplicationContext(),"SMS sent : "+String.valueOf(ph_number),
                Toast.LENGTH_LONG).show();
    }
}
