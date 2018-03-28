package cs497.byu.trackme;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.udacity.friendlychat.R;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by NAG on 3/21/18.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryHolder> {

    private Map<String, HashSet<Bitmap>> small_to_large_photos;
    private Activity galleryActivity;
//    private Iterator

    public GalleryAdapter(Activity galleryActivity) {
        this.galleryActivity = galleryActivity;
        small_to_large_photos = Model.SINGLETON.getSmall_to_large_photos();
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

            Log.e("KEY", map.getKey());
            for (Bitmap image : map.getValue()) {
                Log.e("SET", map.getValue().toString());
                Log.e("VALUE", image.toString());

                holder.setImageView(image);
            }
        }

    }


    @Override
    public int getItemCount() {
         return Model.SINGLETON.getTotalPicCount();
    }
}
