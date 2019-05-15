package cs497.byu.trackme;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by NAG on 3/5/18.
 */

public class MarkerCluster implements ClusterItem {

    // Data members
    private LatLng position;
    private String title;
    private String snippet;
    private Bitmap thumbnail;

    // Constructors
    public MarkerCluster(double latitude, double longitude) { // Cosntructor if we don't have a title
        this.position = new LatLng(latitude, longitude);
    }
    public MarkerCluster(double latitude, double longitude, String title, String snippet, Bitmap thumbnail) {
        this.position = new LatLng(latitude, longitude);
        this.title = title;
        this.snippet = snippet;
        this.thumbnail = thumbnail;
    }


    // Getter
    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }
}
