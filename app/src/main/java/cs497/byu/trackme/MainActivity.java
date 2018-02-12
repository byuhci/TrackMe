package cs497.byu.trackme;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.facebook.FacebookSdk;
import com.google.firebase.udacity.friendlychat.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        mapsFragment = new MapsFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.content, mapsFragment).commit();
    }

    private MapsFragment mapsFragment;



}