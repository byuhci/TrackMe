package cs497.byu.trackme.model;

import android.graphics.Bitmap;
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
    private double latitude;
    private double longitude;
    private String formattedTime;
    private long readableTime;
    private long exactTime;
    private double speed;
    private double elevation;
    private double returnTime;
    private double trailLengthInMeters;
    private String thumbnail;
    private double totalDistanceTravelledInMeters;
    private double currentSpeedInMetersPerSecond;
    private double startTime;

    public LocationMarker() {
    }

    public LocationMarker(double latitude, double longitude, String formattedTime, long readableTime,
                          long exactTime, double elevation, double speed, double returnTime, double trailLengthInMeters, double totalDistanceTravelledInMeters, double currentSpeedInMetersPerSecond, double startTime, String thumbnail) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.formattedTime = formattedTime;
        this.exactTime = exactTime;
        this.elevation = elevation;
        this.readableTime = readableTime;
        this.speed = speed;
        this.returnTime = returnTime;
        this.thumbnail = thumbnail;
        this.trailLengthInMeters = trailLengthInMeters;
        this.totalDistanceTravelledInMeters = totalDistanceTravelledInMeters;
        this.currentSpeedInMetersPerSecond = currentSpeedInMetersPerSecond;
        this.startTime = startTime;
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

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public double getTrailLengthInMeters() {
        return trailLengthInMeters;
    }

    public void setTrailLengthInMeters(double trailLengthInMeters) {
        this.trailLengthInMeters = trailLengthInMeters;
    }

    public double getTotalDistanceTravelledInMeters() {
        return totalDistanceTravelledInMeters;
    }

    public void setTotalDistanceTravelledInMeters(double totalDistanceTravelledInMeters) {
        this.totalDistanceTravelledInMeters = totalDistanceTravelledInMeters;
    }

    public double getCurrentSpeedInMetersPerSecond() {
        return currentSpeedInMetersPerSecond;
    }

    public void setCurrentSpeedInMetersPerSecond(double currentSpeedInMetersPerSecond) {
        this.currentSpeedInMetersPerSecond = currentSpeedInMetersPerSecond;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
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
