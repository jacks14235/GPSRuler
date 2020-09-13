package com.example.jack.gpsruler.toolsclass;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;


public class GpsTools {

    public static double[] toArray(Location l){
        return new double[] {l.getLatitude(), l.getLongitude(), l.getAltitude(), l.getAccuracy()};
    }

    public static Location average(ArrayList<Location> points){
        double lat = 0;
        double lon = 0;
        double alt = 0;
        for (Location point : points){
            lat += point.getLatitude();
            lon += point.getLongitude();
            alt += point.getAltitude();
        }
        lat /= points.size();
        lon /= points.size();
        alt /= points.size();
        Location loc = new Location("provider");
        loc.setLatitude(lat);
        loc.setLongitude(lon);
        loc.setAltitude(alt);
        return loc;
    }

    // absolute error in meters of the average of a bunch of measurements
    public static double pointError(ArrayList<Location> points){
        double latSquareSum = 0;
        double lonSquareSum = 0;
        for (Location point : points){
            latSquareSum += point.getAccuracy();
            lonSquareSum += point.getAccuracy();
        }
        double latError = 2.0 * Math.sqrt(latSquareSum)/ (Math.PI * points.size());
        double lonError = 2.0 * Math.sqrt(lonSquareSum)/ (Math.PI * points.size());
        return (Math.sqrt(latError * latError + lonError * lonError));
    }

    public static double distanceError(ArrayList<Location> pointA, ArrayList<Location> pointB){
        double dA = pointError(pointA);
        double dB = pointError(pointB);
        return (Math.sqrt(dA * dA + dB * dB));
    }

    // standard error of locations in one point
    public static double standardError(ArrayList<Location> points){
        Location average = average(points);
        double squareSum = 0;
        int n = points.size();
        for (Location point : points){
            double dist = average.distanceTo(point);
            squareSum += dist * dist;
        }
        double sd = Math.sqrt(squareSum / n);
        return sd / Math.sqrt(n);
    }

    // error between two points using standard error of each
    public static double standardPropagated(ArrayList<Location> pointA, ArrayList<Location> pointB){
        double dA = standardError(pointA);
        double dB = standardError(pointB);
        return (Math.sqrt(dA * dA + dB * dB));
    }

    //public static double[] removeOutliers() {

    //}

    private static double[] bubbleSort(double[] arr){
        for (double i : arr) {
            for (int j = 0; j < arr.length-1; j++) {
                if (arr[j] > arr[j+1]) {
                    double temp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = temp;
                }
            }
        }
        return arr;
    }

    // returns gmaps zoom level from 2 to 20 that two points distance x apart will fit in
    public static float distanceToZoomLvl(double x) {
        double level = (20 - (Math.log(x/1128.497)/Math.log(2)));
        if (level > 20){
            level = 20;
        }

        if (level < 2) {
            level = 2;
        }
        return (float) level;
    }

    public static ArrayList<Location> removeOutliers(ArrayList<Location> points) {
        if (points.size() <= 10){
            return (points);
        }
        Log.d("Outliers", "In remove Outliers");
        ArrayList<Location> toReturn = (ArrayList<Location>) points.clone();
        for (int i = 0; i < points.size() - 10; i++) {
            ArrayList<Location> firstPart = new ArrayList<Location>();
            ArrayList<Location> secondPart = new ArrayList<Location>();
            int c = 0;
            for (Location point : points) {
                if (c <= i) {
                    firstPart.add(point);
                } else {
                    secondPart.add(point);
                }
                c++;
            }
            double dist = average(firstPart).distanceTo(average(secondPart));
            double dist2 = average(firstPart).distanceTo((Location)firstPart.toArray()[i]);
            double error = standardError(secondPart);
            Log.d("Outliers", firstPart.size() + ", " + secondPart.size());
            Log.d("Outliers", "Dist: " + dist + ", Error: " + error);
            if (dist > 2 * error && dist2 > 2 * error) {
                Log.d("Outliers", toReturn.size() + "");
                toReturn = (ArrayList<Location>) secondPart.clone();
            }
        }
        return (toReturn);
    }

}
