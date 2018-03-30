package cs497.byu.trackme;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.udacity.friendlychat.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by NAG on 3/21/18.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryHolder> {

    private Map<String, HashSet<Bitmap>> small_to_large_photos;
    private Activity galleryActivity;
    private List<Bitmap> allPictures; // All the pictures in the app will be saved to this list
    private LatLng keyPosition;

    public GalleryAdapter(Activity galleryActivity, LatLng keyPosition) {
        this.galleryActivity = galleryActivity;
        small_to_large_photos = Model.SINGLETON.getSmall_to_large_photos();
        allPictures = getAllPictures();
        this.keyPosition = keyPosition;
    }

    private List<Bitmap> getAllPictures() {
        List<Bitmap> list = new ArrayList<>();

        for (Map.Entry<String, HashSet<Bitmap>> map : small_to_large_photos.entrySet()) {
            for (Bitmap image : map.getValue()) {
                list.add(image);
            }
        }

        return list;
    }

    @Override
    public GalleryHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final LayoutInflater layoutInflater = LayoutInflater.from(galleryActivity);
        View view = layoutInflater.inflate(R.layout.photo_item, parent, false);

        return new GalleryHolder(view, galleryActivity);
    }

    @Override
    public void onBindViewHolder(GalleryHolder holder, int position) {

        for (Map.Entry<String, HashSet<Bitmap>> map : small_to_large_photos.entrySet()) {
            if (keyPosition.toString().equals(map.getKey())) {
                for (Bitmap image : map.getValue()) {

                    // This ensures that every image will be listed in the recycler view.
                    // If the image in the map, at the cooresponding Latlng, matches
                    if (allPictures.get(position) == image) {
                        holder.setImageView(image);

                    }
                }
            }


        }

    }


    @Override
    public int getItemCount() {
         return Model.SINGLETON.getTotalPicCount(keyPosition.toString());
    }
}
