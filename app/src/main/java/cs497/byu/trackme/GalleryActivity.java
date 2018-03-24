package cs497.byu.trackme;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.udacity.friendlychat.R;

public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);


        RecyclerView gallery = (RecyclerView) findViewById(R.id.photos_view);
        gallery.setLayoutManager(new LinearLayoutManager(this));
        gallery.setAdapter(new GalleryAdapter(this));

    }
}
