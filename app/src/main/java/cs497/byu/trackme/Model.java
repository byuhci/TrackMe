package cs497.byu.trackme;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.HashSet;
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


    // Constructor
    private Model() {
        small_to_large_photos = new HashMap<>();
    }

    // Methods
    public int getTotalPicCount() {
        int count = 0;

        for (Map.Entry<String, HashSet<Bitmap>> map : small_to_large_photos.entrySet()) {
            for (Bitmap image : map.getValue()) {
                count++;
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
}
