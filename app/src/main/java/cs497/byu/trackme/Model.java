package cs497.byu.trackme;

import android.graphics.Bitmap;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by NAG on 3/21/18.
 *
 * Class simply contains all the important data for the app.
 */

public class Model {

    // Use singleton method
    public static Model SINGLETON = new Model();

    // Data members
    private Map<String, HashSet<Bitmap>> small_to_large_photos;
    private List<Bitmap> allPictures; // All the pictures in the app will be saved to this list
    private LatLng lastLocationSaved;


    // Constructor
    private Model() {
        small_to_large_photos = new HashMap<>();
        allPictures = new ArrayList<>();
        lastLocationSaved = null;
    }

    // Methods
    public int getTotalPicCount(String key) {
        int count = 0;

        for (Map.Entry<String, HashSet<Bitmap>> map : small_to_large_photos.entrySet()) {
            if (key.equals(map.getKey())){
                for (Bitmap image : map.getValue()) {
                    count++;
                }
            }
        }

        return count;
    }

    // Getters and Setters
    public Map<String, HashSet<Bitmap>> getSmall_to_large_photos() {
        return small_to_large_photos;
    }

    public void setSmall_to_large_photos(Map<String, HashSet<Bitmap>> small_to_large_photos) {
        this.small_to_large_photos = small_to_large_photos;
    }

    public List<Bitmap> getAllPictures() {
        return allPictures;
    }

    public void setAllPictures(List<Bitmap> allPictures) {
        this.allPictures = allPictures;
    }

    public LatLng getLastLocationSaved() {
        return lastLocationSaved;
    }

    public void setLastLocationSaved(LatLng lastLocationSaved) {
        this.lastLocationSaved = lastLocationSaved;
    }

//    public LatLng getLastLocation() { return new LatLng(this.lastLocationSaved.latitude, this.lastLocationSaved.longitude); }
}
