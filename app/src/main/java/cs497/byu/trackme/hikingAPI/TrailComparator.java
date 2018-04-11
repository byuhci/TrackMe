package cs497.byu.trackme.hikingAPI;

import java.util.Comparator;

public class TrailComparator implements Comparator<Trail> {

    double latitude;
    double longitude;

    public TrailComparator(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public int compare(Trail t1, Trail t2) {
        return 0;
    }

    private double getDistanceFromStart(Trail trail){
        return Math.sqrt(Math.pow(Math.abs(latitude - trail.getLatitude()), 2) + Math.pow(Math.abs(longitude - trail.getLongitude()), 2));
    }


}
