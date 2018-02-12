package cs497.byu.trackme;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.support.v4.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.udacity.friendlychat.R;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import cs497.byu.trackme.com.hs.gpxparser.GPXParser;
import cs497.byu.trackme.com.hs.gpxparser.modal.GPX;
import cs497.byu.trackme.com.hs.gpxparser.modal.Waypoint;
import cs497.byu.trackme.model.LocationMarker;
import cs497.byu.trackme.model.ProfileData;

import static com.facebook.FacebookSdk.getApplicationContext;

public class MapsFragment extends Fragment
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //Google Maps
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mLastMarker;
    Marker mFirstMarker;

    //Data
    boolean TESTMODE = false;
    ArrayList<LocationMarker> mapMarkers = new ArrayList<>();

    //UI
    Button mStartButton;
    Button mFinishButton;

    //Firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessageDataBaseReference;
    private ChildEventListener mChildEventListener;

    boolean mRequestingLocationUpdates; //If we are requesting location updates

    public final String REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY";
    public final String LOCATION_KEY = "LOCATION_KEY";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessageDataBaseReference = mFirebaseDatabase.getReference().child("location");

        attachDatabaseReadListener();

        updateValuesFromBundle(savedInstanceState);

        mStartButton = (Button) rootView.findViewById(R.id.startButton);
        mFinishButton = (Button) rootView.findViewById(R.id.finishedButton);
        if (ProfileData.getInstance().getUserType().equals(ProfileData.USER.HIKER)) {
            mStartButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startRecordingHike();
                    mFinishButton.setVisibility(View.VISIBLE);
                }
            });
            mFinishButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    finishHike();
                    mStartButton.setVisibility(View.VISIBLE);
                }
            });
        } else {
            mStartButton.setVisibility(View.INVISIBLE);
        }

        if (!mRequestingLocationUpdates) {
            getActivity().setTitle("Waiting To Start");
        }

        return rootView;
    }

    private void useTestData() {
        GPXParser p = new GPXParser();
        try {
            AssetManager assetManager = getContext().getAssets();
            InputStream inputStream = assetManager.open("River_run.gpx");
            GPX gpx = p.parseGPX(inputStream);
            ArrayList<Waypoint> waypoints = gpx.getTracks().iterator().next().getTrackSegments().iterator().next().getWaypoints();
            for (int i = 0; i < waypoints.size(); i += 60) {
                waypoints.get(i).getTime();
                waypoints.get(i).getElevation();
                waypoints.get(i).getLatitude();
                waypoints.get(i).getLongitude();

                //Store in Firebase
                LocationMarker newLocationMarker = new LocationMarker(waypoints.get(i).getLatitude(), waypoints.get(i).getLongitude(), waypoints.get(i).getTime().toString(), i / 60, 24, waypoints.get(i).getElevation(), 2, 5); //TODO: THIS IS BROKEN! Return time cannot be 5
                mMessageDataBaseReference.push().setValue(newLocationMarker);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startRecordingHike() {
        clearData();
        mStartButton.setVisibility(View.INVISIBLE);

        mRequestingLocationUpdates = true;
        startLocationUpdates();
        Toast.makeText(getApplicationContext(), "Sharing Location", Toast.LENGTH_SHORT).show();
    }

    private void clearData() {
        mFirebaseDatabase.getReference("location").removeValue();
        mapMarkers.clear();
        mGoogleMap.clear();
        mFirstMarker = null;
        mLastMarker = null;
    }

    private void finishHike() {
        mFinishButton.setVisibility(View.INVISIBLE);
        mRequestingLocationUpdates = false;
    }

    //----------Map----------------//
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mGoogleMap.setPadding(0, 0, 0, 0);

        mGoogleMap.getUiSettings().setCompassEnabled(true);
        mGoogleMap.getUiSettings().setTiltGesturesEnabled(true);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                if (ProfileData.getInstance().getUserType() == ProfileData.USER.HIKER) {
                    mGoogleMap.setMyLocationEnabled(true);
                }
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else { //Permission granted at download
            buildGoogleApiClient();
            if (ProfileData.getInstance().getUserType() == ProfileData.USER.HIKER) {
                mGoogleMap.setMyLocationEnabled(true);
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        if (TESTMODE) {
            useTestData();
        } else {
            mLocationRequest = new LocationRequest();
            int numberOfSeconds = 5;
            mLocationRequest.setInterval(1000 * numberOfSeconds); //Preferred rate in milliseconds
            mLocationRequest.setFastestInterval(1000 * numberOfSeconds);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this); //Request current location
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        System.out.println("onLocationChanged");
        mLastLocation = location;

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //Get the time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm"); //"yyyy-MM-dd HH:mm:ss"
        String formattedDate = df.format(calendar.getTime());

        //Get a time estimate
        double returnTime = 0;
        if (mapMarkers.size() > 1) {
            LocationMarker lastMarker = mapMarkers.get(mapMarkers.size() - 1);
            double totalDistanceInMeters = distanceBetween(latLng.latitude, latLng.longitude, lastMarker.getLatitude(), lastMarker.getLongitude());
            //5000 meters per hour is average walking speed
            //or 84 meters per minute
            double averageRate = 1.4; //1.4m per second
            returnTime = (totalDistanceInMeters / averageRate);
            returnTime += mapMarkers.get(mapMarkers.size() - 2).getReturnTime(); //Add the time from the previous one.
        }

        //Store in Firebase
        LocationMarker newLocationMarker = new LocationMarker(latLng.latitude, latLng.longitude, formattedDate, location.getTime(), location.getElapsedRealtimeNanos(), location.getAltitude(), location.getSpeed(), returnTime);
        mMessageDataBaseReference.push().setValue(newLocationMarker);
    }

    private void zoomToLocation(LatLng latLng) {
        CameraUpdate cameraUpdate;
        if (mGoogleMap.getCameraPosition().zoom < 5) {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        } else {
            cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
        }
        mGoogleMap.animateCamera(cameraUpdate);
    }


    //Called when Database is updated
    private void drawMarker(LocationMarker locationMarker) {
        //Create the marker and add it
        LatLng latLng = new LatLng(locationMarker.getLatitude(), locationMarker.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);

        //Assign the first and last markers
        if (mFirstMarker == null) {
            markerOptions.title("Start Time: " + locationMarker.getFormattedTime());
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            mFirstMarker = mGoogleMap.addMarker(markerOptions);
            zoomToLocation(mFirstMarker.getPosition());
        } else {
            if (mLastMarker != null) {
                mLastMarker.remove();
            }
            markerOptions.title("Last Reported Time: " + locationMarker.getFormattedTime());
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mLastMarker = mGoogleMap.addMarker(markerOptions);
            zoomToLocation(mLastMarker.getPosition());
        }

        //Draw the polyline
        if (mapMarkers.size() > 1) {
            LocationMarker previousMarker = mapMarkers.get(mapMarkers.size() - 2); //Get size, -1 to get last item, -1 again to get the second to last
            LatLng previousLatLng = new LatLng(previousMarker.getLatitude(), previousMarker.getLongitude());

            PolylineOptions polyline = new PolylineOptions()
                    .add(previousLatLng, latLng)
                    .width(5)
                    .color(Color.RED);
            mGoogleMap.addPolyline(polyline);
        }

    }

    //----------Firebase Database------------//
    private void attachDatabaseListener() {
        mMessageDataBaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LocationMarker newLocationMarker = dataSnapshot.getValue(LocationMarker.class);

                //Draw the marker
                drawMarker(newLocationMarker);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }


    //Start listening to the database
    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override //Called when a message is added.
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    System.out.println("onChildAdded");

                    //dataSnapshot is a "snapshot" instance of the database, and in this case, will be the new added message.
                    LocationMarker newLocationMarker = dataSnapshot.getValue(LocationMarker.class);
                    mapMarkers.add(newLocationMarker);

                    //Draw the marker
                    drawMarker(newLocationMarker);

                    //Update Time Estimate
                    if (getActivity() != null) {
                        {
                            //Find the closet Marker
                            LocationMarker closestMarker = newLocationMarker;
                            for (int i = mapMarkers.size() - 1; i >= 0; i--) {
                                float distance = distanceBetween(newLocationMarker.getLatitude(), newLocationMarker.getLongitude(), mapMarkers.get(i).getLatitude(), mapMarkers.get(i).getLongitude());
                                if (distance < 10) { //Within 20 meters
                                    closestMarker = mapMarkers.get(i);
                                }
                            }

                            //Display the return time
                            if (closestMarker.findReturnTimeInMinutes() == 1) {
                                getActivity().setTitle("Estimated Return Time: " + closestMarker.findReturnTimeInMinutes() + " minute");
                            } else {
                                getActivity().setTitle("Estimated Return Time: " + closestMarker.findReturnTimeInMinutes() + " minutes");
                            }

                        }
                    }

                }

                @Override //When the contents gets edited
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override //when the content gets deleted.
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override //Called when a message changes position in the list.
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override //Some sort of error happens.
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mMessageDataBaseReference.addChildEventListener(mChildEventListener);
        }
    }

    float distanceBetween(double lat1, double lon1, double lat2, double lon2) {
        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lon1);
        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lon2);
        float distanceInMeters = loc1.distanceTo(loc2);
        return distanceInMeters;
    }

    //Stop listening to database
    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessageDataBaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }


    //----------Saving States----------------//
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mLastLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
            }

            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mLastLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            //updateUI();
        }
    }


    //----------Life Cycle----------------//
    @Override
    public void onPause() {
        super.onPause();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    //----------PERMISSION----------------//

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


}