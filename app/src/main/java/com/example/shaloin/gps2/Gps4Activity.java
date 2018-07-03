package com.example.shaloin.gps2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;

public class Gps4Activity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener {

    private static final String LOG_TAG = "PlacesAPIActivity";
    private static final int GOOGLE_API_CLIENT_ID = 0;

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int ACCESS_FINE_LOCATION_ID=99;
    private static final int SEND_SMS_REQUEST_ID=98;
    private static final int RECEIVE_SMS_REQUEST_ID=97;
    private static final int ACCESS_COARSE_LOCATION_ID=96;
    private static final int REQUEST_LOCATION=199;
    //String number="+919707153020";
    ArrayList<String> numbers;
    ArrayList<String> result;
    SQLiteDatabase db;
    DatabaseManager manager;
    private TextView display;
    private Button location_button, contacts_button;
    private double currentLatitude;
    private double currentLongitude;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;
    private PendingResult<LocationSettingsResult> locationSettingsResultPendingResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps4);
        numbers=new ArrayList<>();
        result=new ArrayList<>();
        db = new UserDatabase(this).getReadableDatabase();
        manager=new DatabaseManager(Gps4Activity.this);
        locationRequest =new LocationRequest();
        location_button=(Button)findViewById(R.id.show_button);
        contacts_button=(Button)findViewById(R.id.view_button);
        display=(TextView)findViewById(R.id.location_textview);

        mGoogleApiClient = new GoogleApiClient.Builder(Gps4Activity.this)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(Gps4Activity.this)
                .addOnConnectionFailedListener(Gps4Activity.this)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .build();
        mGoogleApiClient.connect();

        /*ActivityCompat.requestPermissions(this,
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
                ACCESS_COARSE_LOCATION_ID);*/
        setPermissions();


        location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!manager.isEmpty()) {
                    /*if (mGoogleApiClient.isConnected()) {
                        if (ActivityCompat.checkSelfPermission(Gps4Activity.this,
                                android.Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(Gps4Activity.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    PERMISSION_REQUEST_CODE);
                            ActivityCompat.requestPermissions(Gps4Activity.this,
                                    new String[]{Manifest.permission.SEND_SMS},
                                    MY_PERMISSIONS_REQUEST_SEND_SMS);
                            setPermissions();
                        }
                    }*/
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

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);

        } else {
            //If everything went fine lets get latitude and longitude
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();

            Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
        }


        locationRequest=LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30*1000);
        locationRequest.setFastestInterval(5*1000);

        LocationSettingsRequest.Builder builder=new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        locationSettingsResultPendingResult=LocationServices.SettingsApi
                .checkLocationSettings(mGoogleApiClient,builder.build());
        locationSettingsResultPendingResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {

                final Status status=locationSettingsResult.getStatus();
                final LocationSettingsStates states=locationSettingsResult.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        //All location settings are satisfied. The client can initialize location requests here
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        //Location settings are not satisfied but could be fixed by showing user a dialog
                        try {
                            status.startResolutionForResult(Gps4Activity.this,REQUEST_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        //Location settings are not satisfied and we have no way to fix the settings
                        //so we cannot show the dialog
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_LOCATION:
                switch (resultCode){
                    case Activity.RESULT_OK:
                        Toast.makeText(getApplicationContext(),
                                "Location Enabled!!",Toast.LENGTH_LONG).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getApplicationContext(),
                                "Location not enabled!!",Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

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
                            "SMS sending faild, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
                break;

        }
    }

    private void setPermissions(){

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_PHONE_STATE, Manifest.permission_group.LOCATION},
                SEND_SMS_REQUEST_ID);

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
        message += " https://maps.google.com/maps/?q=" + currentLatitude + "," + currentLongitude;

        for (int i=0;i<result.size();i++){

            try{
                smsManager.sendTextMessage(result.get(i),null,message,null,null);
                Toast.makeText(getApplicationContext(), "SMS sent : "+String.valueOf(numbers),
                        Toast.LENGTH_LONG).show();
            } catch (Exception e){
                Toast.makeText(getApplicationContext(),
                        "SMS sending failed "+e.getMessage(),Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    public void messageSending_individual(String ph_number, String message){
        SmsManager smsManager=SmsManager.getDefault();
        try{
            message += " https://maps.google.com/maps/?q=" + currentLatitude + "," + currentLongitude;
            smsManager.sendTextMessage(ph_number,null,message,null,null);
            Toast.makeText(getApplicationContext(),"SMS sent : "+String.valueOf(ph_number),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e){
            Toast.makeText(getApplicationContext(),
                    "SMS sending failed "+e.getMessage(),Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();

    }
}
