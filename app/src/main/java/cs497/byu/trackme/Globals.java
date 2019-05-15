package cs497.byu.trackme;

import android.graphics.Bitmap;
import android.os.Handler;

import java.io.File;

public class Globals {
    private static Globals instance;

    private double distance;
    public void setDistance(double value)
    {
        this.distance = value;
    }
    public double getDistance()
    {
        return this.distance;
    }

    private String lastPhotoTimeStamp;
    public void setLastPhotoTimeStamp(String value) { this.lastPhotoTimeStamp = value; }
    public String getLastPhotoTimeStamp() { return this.lastPhotoTimeStamp; }

    private MapsFragment mapsFragment;
    public void setMapsFragment(MapsFragment value) { this.mapsFragment = value; }
    public MapsFragment getMapsFragment() { return this.mapsFragment; }

    private Handler handler;
    public void setHandler(Handler value) { this.handler = value; }
    public Handler getHandler() { return this.handler; }

    private File imageToDisplay;
    public void setImageToDisplay(File value) { this.imageToDisplay = value; }
    public File getImageToDisplay() { return this.imageToDisplay; }

    public static synchronized Globals getInstance()
    {
        if(instance == null)
        {
            instance = new Globals();
            instance.setDistance(0.0);
        }
        return instance;
    }
}
