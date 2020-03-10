package com.intellisight.geogeo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.view.View;
import android.view.textclassifier.TextLinks;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private TextView textLatLong;
    private TextView mTextViewResult;
    private RequestQueue mQueue;
    double variable_lat,variable_lng,bearing;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // coordinates
        textLatLong = findViewById(R.id.textLatLong);

        findViewById(R.id.btnCoordinate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION);
                }
                else {
                    getCurrentLocation();
                }
            }

        });


        //address
        mTextViewResult = findViewById(R.id.text_view_result);
        Button buttonParse = findViewById(R.id.button_parse);

        mQueue = Volley.newRequestQueue(this);

        buttonParse.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                //getCurrentLocation();
                jsonParse(variable_lat,variable_lng,255.33);
                //jsonParse();
            }
        });
    }


    //for coordinates
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults ){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length >0){
           getCurrentLocation();
        }
        else{
            Toast.makeText(this, "permission denied",Toast.LENGTH_SHORT).show();
        }

    }


    //for coordinates
    private void getCurrentLocation() {

        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {

                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            double latitude =
                                    locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            double longitude =
                                    locationResult.getLocations().get(latestLocationIndex).getLongitude();
                            textLatLong.setText(
                                    String.format(
                                            "Latitude : %s\nLongitude: %s",
                                            latitude,
                                            longitude
                                    )
                            );
                            Location location = new Location("provider not available");
                            location.setLatitude(latitude);
                            location.setLongitude(longitude);
                            variable_lat = latitude;
                            variable_lng = longitude;
                        }


                    }

                }, Looper.getMainLooper());
    }


    private void jsonParse(double variable_lat, double variable_lng, double bearing){
    //private void jsonParse(){


        double[] new_coordinate = get_new_coordinate(variable_lat,variable_lng,0.01,bearing );

        /*
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                +String.valueOf(37.4219983) +","+ String.valueOf(-122.084)
                + "&radius=30&fields=geometry,name&key=AIzaSyBvVpNqioImNC5lIq6bdRnGpvBHsQCHXw4";
        */



        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                    +String.valueOf(new_coordinate[0]) +","+ String.valueOf(new_coordinate[1])
                    + "&radius=300&fields=geometry,name&key=AIzaSyBvVpNqioImNC5lIq6bdRnGpvBHsQCHXw4";


        /*
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                +"34.4137629,-119.8414525"
                + "&radius=300&fields=geometry,name&key=AIzaSyBvVpNqioImNC5lIq6bdRnGpvBHsQCHXw4";
    */
        /*
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=37.4219983,-122.084&radius=300\n" +
                "&fields=geometry,name\n"+
                "&key= AIzaSyBvVpNqioImNC5lIq6bdRnGpvBHsQCHXw4\n";

         */


// String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452" +
                //"&result_type=street_address" +
                //"&key=AIzaSyDeG8SLUlpEIBwcq7HodVBOb6G6vE7TVLg";

        //we need to decide whether it is jason object or jason array
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {// when call successfully
                        // we need to put the name of array to ()
                        try {
                            JSONArray jsonArray = response.getJSONArray("results");
                                for( int i = 0; i < jsonArray.length();i++){
                                    JSONObject places = jsonArray.getJSONObject(i);

                                    String name = places.getString("name");

                                    //int age = employee.getInt("age");
/*
                                    JSONObject geometry = response.getJSONObject("geometry");
                                    JSONObject location = geometry.getJSONObject("location");
                                    double lat = location.getDouble("lat");
                                    **/


                                   mTextViewResult.append(name
                                           // + ", "+ String.valueOf(lat)
                                            + "\n");
                                }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {// when call failed
                error.printStackTrace();

            }
        });

        mQueue.add(request);
    }

    //get new coordinates
    static double[] get_new_coordinate(double lat, double lng, double d, double bearing){
        double earth_radius = 6378.1;
        double bearing_radian= Math.toRadians(bearing);
        double lat_radian = Math.toRadians(lat);
        double lng_radian = Math.toRadians(lng);

        double new_lat = Math.asin(Math.sin(lat_radian)*Math.cos(d/earth_radius)+
                            Math.cos(lat_radian)*Math.sin(d/earth_radius)*Math.cos(bearing_radian));
        double new_lng = lng_radian+
                            Math.atan2(Math.sin(bearing_radian)*Math.sin(d/earth_radius)*Math.cos(lat_radian),
                                    Math.cos(d/earth_radius)-Math.sin(lat_radian)*Math.sin(new_lat));

        double[] coordinates = new double[2];
        coordinates[0] = Math.toDegrees(new_lat);
        coordinates[1] = Math.toDegrees(new_lng);

        return(coordinates);

    }

    }

