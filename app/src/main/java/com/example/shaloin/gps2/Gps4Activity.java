package com.example.shaloin.gps2;

import android.*;
import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.util.List;

public class Gps4Activity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = "PlacesAPIActivity";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private GoogleApiClient mGoogleApiClient;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    private TextView display1,display2,display3,display4,display5;
    private Button button;
    String number="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps4);
        button=(Button)findViewById(R.id.showL);
        display1=(TextView)findViewById(R.id.location2);
        display2=(TextView)findViewById(R.id.location3);
        display3=(TextView)findViewById(R.id.location4);
        display4=(TextView)findViewById(R.id.location5);
        display5=(TextView)findViewById(R.id.location6);

        mGoogleApiClient = new GoogleApiClient.Builder(Gps4Activity.this)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .build();

        button.setOnClickListener(new View.OnClickListener() {
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
                    } else {
                        final AlertDialog.Builder dialogBuilder=new AlertDialog.Builder(Gps4Activity.this);
                        LayoutInflater inflater=Gps4Activity.this.getLayoutInflater();
                        final View dialogView=inflater.inflate(R.layout.custom_dialog,null);
                        dialogBuilder.setView(dialogView);

                        final EditText phoneN=(EditText)dialogView.findViewById(R.id.dialogEditPhID);
                        dialogBuilder.setTitle("Send Location");
                        dialogBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String number2=phoneN.getText().toString();
                                number=number2;
                                callPlaceDetectionApi();
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
            }
        });
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

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.add_number:



            default:
            return super.onOptionsItemSelected(item);
        }
    }*/

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
//                    Toast.makeText(getApplicationContext(),String.format("Place '%s' with " +
//                                    "likelihood: %g",
//                            placeLikelihood.getPlace().getName(),
//                            placeLikelihood.getLikelihood()),Toast.LENGTH_LONG).show();
//                    display.setText(String.format("Place '%s' with " +
//                                    "likelihood: %g",
//                            placeLikelihood.getPlace().getName(),
//                            placeLikelihood.getLikelihood()));
                    //display1.setText(placeLikelihood.getPlace().getName().toString());
                    display2.setText(placeLikelihood.getPlace().getAddress().toString());


//                    sendMySMS(placeLikelihood.getPlace().getAddress().toString());
//                    SmsManager smsManager = SmsManager.getDefault();
//                    smsManager.sendTextMessage(number, null, placeLikelihood.getPlace().getAddress().toString(),
//                            null, null);
//                    Toast.makeText(getApplicationContext(), "SMS sent.",
//                            Toast.LENGTH_LONG).show();
                    messageSending(placeLikelihood.getPlace().getAddress().toString());
                    break;
                    //likelyPlaces.release();
                }
                likelyPlaces.release();

            }
        });
    }

    public void messageSending(String message){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, message, null, null);
        Toast.makeText(getApplicationContext(), "SMS sent.",
                Toast.LENGTH_LONG).show();
    }


}
