package com.example.jack.gpsruler;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jack.gpsruler.toolsclass.GpsTools;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public int REQUEST_FINE_LOCATION = 1;


    private Button startAbutton;
    private Button startBbutton;
    private Button stopButton;
    private Button mapButton;
    private Button clearAButton;
    private Button clearBButton;
    private TextView statusTextView;
    private TextView aposTextView;
    private TextView bposTextView;
    private TextView distanceTextView;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ArrayList<Location> pointAPoints = new ArrayList<Location>();
    private ArrayList<Location> pointBPoints = new ArrayList<Location>();
    private boolean aActive = false;
    private boolean bActive = false;

    final double METERS_TO_FEET = 3.28084;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startAbutton = (Button) findViewById(R.id.startAbutton);
        startBbutton = (Button) findViewById(R.id.startBbutton);
        stopButton = (Button) findViewById(R.id.stopButton);
        mapButton = (Button) findViewById(R.id.mapbutton);
        clearAButton = (Button) findViewById(R.id.clearAbutton);
        clearBButton = (Button) findViewById(R.id.clearBbutton);

        statusTextView = (TextView) findViewById(R.id.statustextview);
        aposTextView = (TextView) findViewById(R.id.aposition);
        bposTextView = (TextView) findViewById(R.id.bposition);
        distanceTextView = (TextView) findViewById(R.id.averagepos);

        setupButtons();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (aActive) {
                    pointAPoints.add(location);
                    updateAMeasurements();
                }

                if (bActive){
                    pointBPoints.add(location);
                    updateBMeasurements();
                }

                if (pointAPoints.size() > 0 && pointBPoints.size() > 0) {
                    updateDistanceMeasurements();
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
        };

        checkPermissions();

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions(){
        //Toast.makeText(this, "Checking Permissions", Toast.LENGTH_SHORT).show();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
            locationManager.requestLocationUpdates("gps", 1000, 0, locationListener);
        }else{
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "GPS permission is required to measure distances",
            Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                checkPermissions();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupButtons() {
        startAbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bActive){
                    Toast.makeText(MainActivity.this, "Please stop recording B first",
                            Toast.LENGTH_SHORT).show();
                }else{
                    aActive = true;
                    statusTextView.setText("Recording A");
                }
            }
        });
        startBbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (aActive){
                    Toast.makeText(MainActivity.this, "Please stop recording A first",
                            Toast.LENGTH_SHORT).show();
                }else{
                    bActive = true;
                    statusTextView.setText("Recording B");
                }
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(aActive || bActive)) {
                    return;
                }
                boolean aActiveCopy = aActive;
                boolean bActiveCopy = bActive;
                Toast.makeText(MainActivity.this, "Stopping Recording",
                        Toast.LENGTH_SHORT);
                aActive = false;
                bActive = false;
                statusTextView.setText("Paused");
                Log.d("Before tag", aActiveCopy + "");
                if (aActiveCopy){
                    Log.d("In here", "Working");
                    final ArrayList<Location> noOutliers = GpsTools.removeOutliers(pointAPoints);
                    if (noOutliers.size() < pointAPoints.size()) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Outliers Detected")
                                .setMessage(String.format("Outliers have been detected in the first " +
                                                "%d points your gps recorded. You can choose to keep or " +
                                                "discard these points. Discarding them will decrease your " +
                                                "error from %.1f to %.1f", pointAPoints.size() -
                                                noOutliers.size(), GpsTools.standardError(pointAPoints),
                                        GpsTools.standardError(noOutliers)))
                                .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pointAPoints = (ArrayList<Location>) noOutliers.clone();
                                        updateAMeasurements();
                                        if (pointAPoints.size() > 0 && pointBPoints.size() > 0) {
                                            updateDistanceMeasurements();
                                        }
                                        Log.d("Outliers Deleted", noOutliers.size() + ", "
                                                + pointAPoints.size());
                                    }
                                })
                                .setNegativeButton("Keep", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create().show();
                    }
                }

                if (bActiveCopy){
                    final ArrayList<Location> noOutliers = GpsTools.removeOutliers(pointBPoints);
                    if (noOutliers.size() < pointBPoints.size()) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Outliers Detected")
                                .setMessage(String.format("Outliers have been detected in the first " +
                                        "%d points your gps recorded. You can choose to keep or " +
                                        "discard these points. Discarding them will decrease your " +
                                        "error from %.1f to %.1f", pointBPoints.size() -
                                        noOutliers.size(), GpsTools.standardError(pointBPoints),
                                        GpsTools.standardError(noOutliers)))
                                .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pointBPoints = (ArrayList) noOutliers.clone();
                                        updateBMeasurements();
                                        if (pointAPoints.size() > 0 && pointBPoints.size() > 0) {
                                            updateDistanceMeasurements();
                                        }
                                    }
                                })
                                .setNegativeButton("Keep", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create().show();
                    }
                }
            }
        });
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(getApplicationContext(), MapActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("PointA", pointAPoints);
                bundle.putSerializable("PointB", pointBPoints);
                startIntent.putExtras(bundle);
                startActivity(startIntent);
            }
        });

        clearAButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete all points in point A?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pointAPoints = new ArrayList<Location>();
                                aposTextView.setText("");
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });
        clearBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete all points in point B?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pointBPoints = new ArrayList<Location>();
                                bposTextView.setText("");
                                aActive = false;
                                bActive = false;
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });
    }

    public void updateAMeasurements (){
        Location locA = GpsTools.average(pointAPoints);
        aposTextView.setText(String.format("Latitude: %.6f\nLongitude: %.6f\nElevation: " +
                        "%.0f\nError: %.1f (%d samples)", locA.getLatitude(), locA.getLongitude(),
                locA.getAltitude() * METERS_TO_FEET, GpsTools.standardError(pointAPoints)
                        * METERS_TO_FEET, pointAPoints.size()));
    }

    public void updateBMeasurements() {
        Location locB = GpsTools.average(pointBPoints);
        bposTextView.setText(String.format("Latitude: %.6f\nLongitude: %.6f\nElevation: " +
                        "%.0f\nError: %.1f (%d samples)", locB.getLatitude(), locB.getLongitude(),
                locB.getAltitude() * METERS_TO_FEET, GpsTools.standardError(pointBPoints)
                        * METERS_TO_FEET, pointBPoints.size()));
    }

    public void updateDistanceMeasurements() {
        Location locA = GpsTools.average(pointAPoints);
        Location locB = GpsTools.average(pointBPoints);
        double distance = locA.distanceTo(locB);
        distanceTextView.setText(String.format("Distance: %.0f\nError: %.1f",
                distance * METERS_TO_FEET, GpsTools.standardPropagated(pointAPoints,
                        pointBPoints) * METERS_TO_FEET));
    }





}
