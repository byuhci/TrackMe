package com.google.firebase.udacity.friendlychat.model;

import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by mitchclements on 10/17/17.
 */

public class LocationMarker {

    //Location location;
    //Calendar calendar;

    double latitude;
    double longitude;
    String formattedTime;
    long readableTime;
    long exactTime;
    double speed;
    double elevation;
    double returnTime;

    public LocationMarker() {
    }

    public LocationMarker(double latitude, double longitude, String formattedTime, long readableTime, long exactTime, double elevation, double speed, double returnTime) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.formattedTime = formattedTime;
        this.exactTime = exactTime;
        this.elevation = elevation;
        this.readableTime = readableTime;
        this.speed = speed;
        this.returnTime = returnTime;
    }

    //----Getters and Setters---//

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    public void setFormattedTime(String time) {
        this.formattedTime = time;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public long getReadableTime() {
        return readableTime;
    }

    public void setReadableTime(long readableTime) {
        this.readableTime = readableTime;
    }

    public long getExactTime() {
        return exactTime;
    }

    public void setExactTime(long exactTime) {
        this.exactTime = exactTime;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getReturnTime() {
        return returnTime;
    }

    public int findReturnTimeInMinutes(){
        return (int) (getReturnTime() / 60);
    }

    public void setReturnTime(double timeToStart) {
        this.returnTime = timeToStart;
    }


    /*
    public LocationMarker(Location location, Calendar calendar) {
        this.location = location;
        this.calendar = calendar;
    }*/

    /*
    public LocationMarker(LatLng latlng){
        Location temp = new Location(LocationManager.GPS_PROVIDER);
        temp.setLatitude(latlng.latitude);
        temp.setLongitude(latlng.longitude);
    }*/

    /*
    public String getTime(){
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss"); //"yyyy-MM-dd HH:mm:ss"
        String formattedDate = df.format(calendar.getTime());
        return ("Current Position " + formattedDate);
    }*/
}
