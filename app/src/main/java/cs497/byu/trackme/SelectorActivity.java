package cs497.byu.trackme;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.firebase.FirebaseApp;
import com.google.firebase.udacity.friendlychat.R;

import cs497.byu.trackme.model.ProfileData;


public class SelectorActivity extends AppCompatActivity {

    private LinearLayout mHikeOption;
    private LinearLayout mObserverOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideActionBar();
        setContentView(R.layout.activity_selection);


        mHikeOption = (LinearLayout) findViewById(R.id.hike);
        mHikeOption.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ProfileData.getInstance().setUserType(ProfileData.USER.HIKER);
                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);

                //ADDED CODE BY NATHAN GERONIMO
                mainActivityIntent.putExtra("hiker", true); // Determines if camera button should appear or not
                startActivity(mainActivityIntent);
            }
        });

        mObserverOption = (LinearLayout) findViewById(R.id.observe);
        mObserverOption.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ProfileData.getInstance().setUserType(ProfileData.USER.OBSERVER);
                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);

                // ADDED CODE BY NATHAN GERONIMO
                mainActivityIntent.putExtra("hiker", false); // Determines if camera button should appear or not
                startActivity(mainActivityIntent);
            }
        });
    }

    //WARNING: Make this is called before setContentView()
    private void hideActionBar() {
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            // Hide ActionBar
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        }
    }
}
