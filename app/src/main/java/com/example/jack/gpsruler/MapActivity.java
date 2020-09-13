package com.example.jack.gpsruler;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import com.example.jack.gpsruler.toolsclass.GpsTools;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    private int REQUEST_COURSE_GPS = 3;
    private int REQUEST_FINE_GPS = 1;
    private int REQUEST_INTERNET = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        checkPermissions(Manifest.permission.INTERNET, REQUEST_INTERNET,
                "Internet is required to load the map");
        checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_FINE_GPS,
                "Location is required to see current location on the map");
        checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_COURSE_GPS,
                "Location is required to see current location on the map");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private void drawMarkers(ArrayList<Location> pointA, ArrayList<Location> pointB) {
        double radius = 1;
        // point a gps points
        for (Location point : pointA){
            mMap.addCircle(new CircleOptions().center(new LatLng(point.getLatitude(),
                    point.getLongitude())).fillColor(0xFFFF0000).strokeColor(0x00000000).
                    radius(radius));
        }
        // point b gps points
        for (Location point : pointB){
            mMap.addCircle(new CircleOptions().center(new LatLng(point.getLatitude(),
                    point.getLongitude())).fillColor(0xFF00C8FF).strokeColor(0x00000000).
                    radius(radius));
        }
        // center markers
        Location aAverage = GpsTools.average(pointA);
        LatLng aAverageLL = new LatLng(aAverage.getLatitude(),
                aAverage.getLongitude());
        mMap.addMarker(new MarkerOptions().position(aAverageLL));
        Location bAverage = GpsTools.average(pointB);
        LatLng bAverageLL = new LatLng(bAverage.getLatitude(),
                bAverage.getLongitude());
        mMap.addMarker(new MarkerOptions().position(bAverageLL));
        mMap.addPolyline(new PolylineOptions().add(aAverageLL).add(bAverageLL).color(Color.RED).width(5));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        ArrayList<Location> pointA = (ArrayList<Location>) bundle.getSerializable("PointA");
        ArrayList<Location> pointB = (ArrayList<Location>) bundle.getSerializable("PointB");


        mMap = googleMap;

        // Move camera to center of all points if points exist, else to US center
        LatLng center;
        Location aCenter = GpsTools.average(pointA);
        Location bCenter = GpsTools.average(pointB);
        if (pointA.size() + pointB.size() > 0) {
            ArrayList<Location> centers = new ArrayList<Location>();
            centers.add(aCenter);
            centers.add(bCenter);
            Location centerLoc = GpsTools.average(centers);
            center = new LatLng(centerLoc.getLatitude(), centerLoc.getLongitude());
        } else {
            center = new LatLng(39.8283, 98.5795); // center of US
        }
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(center));
        float zoomLvl = GpsTools.distanceToZoomLvl(aCenter.distanceTo(bCenter))-4;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(center.latitude,
                center.longitude), zoomLvl));

        drawMarkers(pointA, pointB);

    }





    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions(String permission, int requestcode, String message) {
        //Toast.makeText(this, "Checking Permissions", Toast.LENGTH_SHORT).show();
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }else{
            if (shouldShowRequestPermissionRationale(permission)) {
                Toast.makeText(this, message,
                        Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{permission}, requestcode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

}