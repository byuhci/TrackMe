package cs497.byu.trackme;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.udacity.friendlychat.R;

/**
 * Created by NAG on 3/21/18.
 */

public class GalleryHolder extends RecyclerView.ViewHolder {

    final private Activity galleryActivity;
    private ImageView imageView;


    public GalleryHolder(View view, final Activity galleryActivity) {
        super(view);

        this.galleryActivity = galleryActivity;


        imageView = (ImageView) view.findViewById(R.id.photo);
        imageView.setClickable(true);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(galleryActivity, "Image clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(Bitmap bitmap) {
        this.imageView.setImageBitmap(bitmap);
    }
}
