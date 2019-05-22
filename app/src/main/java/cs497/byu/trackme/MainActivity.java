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
import com.google.firebase.FirebaseApiNotAvailableException;
import com.google.firebase.FirebaseApp;
import com.google.firebase.udacity.friendlychat.R;

public class MainActivity extends AppCompatActivity {

    private MapsFragment mapsFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        mapsFragment = new MapsFragment();

        Globals g = Globals.getInstance();
        g.setMapsFragment(mapsFragment);

        getSupportFragmentManager().beginTransaction().add(R.id.content, mapsFragment).commit();
    }




}