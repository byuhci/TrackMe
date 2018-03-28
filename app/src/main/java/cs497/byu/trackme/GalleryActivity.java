package cs497.byu.trackme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.google.firebase.udacity.friendlychat.R;

public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);


        RecyclerView gallery = (RecyclerView) findViewById(R.id.photos_view);
        gallery.setLayoutManager(new LinearLayoutManager(this));
        gallery.setAdapter(new GalleryAdapter(this));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish(); // Go back to the maps
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
