package cs497.byu.trackme;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.firebase.database.DatabaseException;
import com.google.gson.Gson;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.udacity.friendlychat.R;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import cs497.byu.trackme.com.hs.gpxparser.GPXParser;
import cs497.byu.trackme.com.hs.gpxparser.modal.GPX;
import cs497.byu.trackme.com.hs.gpxparser.modal.Waypoint;
import cs497.byu.trackme.hikingAPI.GetTrailsResponseList;
import cs497.byu.trackme.hikingAPI.Trail;
import cs497.byu.trackme.hikingAPI.TrailComparator;
import cs497.byu.trackme.model.LocationMarker;
import cs497.byu.trackme.model.ProfileData;

import static android.app.Activity.RESULT_OK;
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

    // ADDED BY NATHAN GERONIMO
    public static final int RC_PERMISSIONS = 1000;
    private final int UPDATE_LOCATION_INTERVAL = 10;
    private final int CAMERA_REQUST_CODE = 11;
    private final String APP_TAG = "TrackMe";
    private File mCurrentPhotoPath;
    private LocationMarker mLastLocationMarker;
    private boolean isObserver;
    private ClusterManager<MarkerCluster> mClusterManager;
    private Map<LatLng, HashSet<Bitmap>> small_to_large_photos; // Key is the item Latlng as a string
    private Bitmap thumbnail;
    private String mLastPhotoTimeStamp;
    private List<Bitmap> allPictures; // Contains all the pitures ever saved

    //Hiking API
    static final String HIKING_PROJECT_KEY = "200230210-03d0722caf5c7e27136640b7f114f736";
    double lat;
    double lon;
    private List<Trail> closeHikes;
    private double trailLengthInMeters;
    private static final double METERS_PER_MILE = 1609.34;
    private long lastTimeUpdateInSeconds;
    private double totalDistanceTraveledInMeters = 0;
    private double returnTime;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        FirebaseApp.initializeApp(getActivity());
        mFirebaseDatabase = FirebaseDatabase.getInstance("https://friendlychat-3e92d.firebaseio.com/");
        mMessageDataBaseReference = mFirebaseDatabase.getReference().child("location");

        attachDatabaseReadListener();

        updateValuesFromBundle(savedInstanceState);

        mStartButton = (Button) rootView.findViewById(R.id.startButton);
        mFinishButton = (Button) rootView.findViewById(R.id.finishedButton);
        if (ProfileData.getInstance().getUserType().equals(ProfileData.USER.HIKER)) {
            mStartButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    getTrail();
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

        small_to_large_photos = Model.SINGLETON.getSmall_to_large_photos();
        allPictures = Model.SINGLETON.getAllPictures();

        Button camera = rootView.findViewById(R.id.button_camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastLocation != null) {
                    openCamera();
                } else {
                    Toast.makeText(getActivity(), "Still retrieving GPS signal", Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (!getActivity().getIntent().getExtras().getBoolean("hiker")) {
            camera.setVisibility(View.INVISIBLE);
            isObserver = true;
        } else {
            isObserver = false;
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
                //TODO: THIS IS BROKEN! Return time cannot be 5
                LocationMarker newLocationMarker = new LocationMarker(waypoints.get(i).getLatitude(), waypoints.get(i).getLongitude(), waypoints.get(i).getTime().toString(), i / 60, 24, waypoints.get(i).getElevation(), 2, 5, trailLengthInMeters, totalDistanceTraveledInMeters, 0, 0, null);
                mLastLocationMarker = newLocationMarker;
                mMessageDataBaseReference.push().setValue(newLocationMarker);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //----------Hike---------------//
    private void getTrail() {

        RequestQueue queue = Volley.newRequestQueue(this.getContext());

        if (mLastLocation != null) {
            lat = mLastLocation.getLatitude();
            lon = mLastLocation.getLongitude();
        }
        int maxDist = 1;

        String requestURL = "https://www.hikingproject.com/data/get-trails?lat=" + lat + "&lon=" + lon + "&maxDistance=" + maxDist + "&key=" + HIKING_PROJECT_KEY;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                confirmTrail(findClosestHike(response).get(0));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getTrailLengthFromUser();
            }
        });

        queue.add(stringRequest);
    }


    private List<Trail> findClosestHike(String json) {
        List<Trail> trails = parseHikes(json);

        Collections.sort(trails, new TrailComparator(lat, lon));

        return trails;
    }

    private List<Trail> parseHikes(String hikingApiJson) {
        Gson gson = new Gson();

        return gson.fromJson(hikingApiJson, GetTrailsResponseList.class).fixCoords(lat, lon).getTrails();
    }

    /**
     * show a dialog where user confirms we selected the trail they plan to hike
     *
     * @param closestHike
     */
    private void confirmTrail(final Trail closestHike) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_confirm_hike, null);

        TextView hikeName = view.findViewById(R.id.hike_name);
        hikeName.setText(closestHike.getName());

        TextView hikeLength = view.findViewById(R.id.hike_length);
        String trailLength = closestHike.getLength() + " miles long";
        hikeLength.setText(trailLength);

        Button confirmHikeButton = view.findViewById(R.id.confirm_hike_button);

        Button wrongHikeButton = view.findViewById(R.id.wrong_hike_button);

        builder.setView(view);

        final AlertDialog confirmTrailDialog = builder.create();

        confirmHikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecordingHike(closestHike.getLength());
                confirmTrailDialog.cancel();
            }
        });

        wrongHikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTrailLengthFromUser();
                confirmTrailDialog.cancel();
            }
        });

        confirmTrailDialog.show();
    }


    /**
     * we couldn't automatically determine the trail the user plans to hike
     * show a dialog where user enters distance they will hike
     */
    private void getTrailLengthFromUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_set_hike_length, null);

        final EditText editText = view.findViewById(R.id.user_input_hike_length);

        Button setHikeLengthButton = view.findViewById(R.id.set_hike_length_button);

        builder.setView(view);

        final AlertDialog getLengthDialog = builder.create();

        setHikeLengthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().trim().length() > 0) {
                    double trailLength = Double.parseDouble(editText.getText().toString());

                    startRecordingHike(trailLength);
                    getLengthDialog.cancel();
                }
            }
        });

        getLengthDialog.show();
    }

    private void startRecordingHike(double trailLengthInMiles) {
        this.trailLengthInMeters = trailLengthInMiles * METERS_PER_MILE;
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

        // Initialize the cluster manager once the map is ready to god
        mClusterManager = new ClusterManager<>(getContext(), mGoogleMap);
        mClusterManager.setRenderer(new MarkerRenderer());
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MarkerCluster>() {
            @Override
            public boolean onClusterClick(Cluster<MarkerCluster> cluster) {
                Toast.makeText(getActivity(), cluster.getSize() + " in this cluster!", Toast.LENGTH_SHORT).show();
                Model.SINGLETON.setLastLocationSaved(cluster.getPosition());
                Intent intent = new Intent(getActivity(), GalleryActivity.class);
                intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Prevents the gallery activity from being added to the backstack

                startActivity(intent);
                return false;
            }
        });

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerCluster>() {
            @Override
            public boolean onClusterItemClick(MarkerCluster markerCluster) {
                Toast.makeText(getActivity(), "Item Clicked!", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        mClusterManager.setAnimation(true);

        mGoogleMap.setOnCameraIdleListener(mClusterManager); // Waits for cluster manager to be updated
        mGoogleMap.setOnMarkerClickListener(mClusterManager); // The onclick methods are in the cluster manager
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
//        if (TESTMODE) {
//            useTestData();
//        } else {
        if (mGoogleApiClient == null) buildGoogleApiClient();
        mLocationRequest = new LocationRequest();
        int numberOfSeconds = UPDATE_LOCATION_INTERVAL;
        mLocationRequest.setInterval(1000 * numberOfSeconds); //Preferred rate in milliseconds
        mLocationRequest.setFastestInterval(1000 * numberOfSeconds);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        doLocationStuff();
//        }
    }

    private void doLocationStuff() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this); //Request current location
            } catch (IllegalStateException e) {
                buildGoogleApiClient();
                doLocationStuff();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    /**
     * This function only works for the hiker. It updates the firebase database with his/her current location
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {

        System.out.println("onLocationChanged");
        Location previousLocation = mLastLocation;
        mLastLocation = location;

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //Get the time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm"); //"yyyy-MM-dd HH:mm:ss"
        String formattedDate = df.format(calendar.getTime());

        long currentTimeInSeconds = System.currentTimeMillis() / 1000;
        long numSecondsSinceLastUpdate = currentTimeInSeconds - lastTimeUpdateInSeconds;
        lastTimeUpdateInSeconds = currentTimeInSeconds;


        //Get a time estimate
//        this.returnTime = 0.0;
        double startTime = currentTimeInSeconds;
        double currentSpeedInMetersPerSecond = 0;
        if (mapMarkers.size() > 1) {

//            if(mapMarkers.get(0).getStartTime() != 0)
            for (LocationMarker locationMarker : mapMarkers) {
                if (locationMarker.getStartTime() != 0) {
                    startTime = mapMarkers.get(0).getStartTime();
                    break;
                }
            }
            if (trailLengthInMeters <= 0) {
                for (LocationMarker locationMarker : mapMarkers) {
                    if (locationMarker.getTrailLengthInMeters() > 0)
                        trailLengthInMeters = locationMarker.getTrailLengthInMeters();
                    break;
                }
            }

            if (totalDistanceTraveledInMeters <= 0) {
                for (int i = mapMarkers.size() - 1; i >= 0; i--) {
                    if (mapMarkers.get(i).getTotalDistanceTravelledInMeters() > 0)
                        totalDistanceTraveledInMeters = mapMarkers.get(i).getTotalDistanceTravelledInMeters();
                }
            }

            double distanceTraveledSinceLastUpdate = distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude(), location.getLatitude(), location.getLongitude());
            totalDistanceTraveledInMeters += distanceTraveledSinceLastUpdate;

            currentSpeedInMetersPerSecond = distanceTraveledSinceLastUpdate / numSecondsSinceLastUpdate;
            if (currentSpeedInMetersPerSecond <= 0) currentSpeedInMetersPerSecond = 1.4;

            double remainingDistanceInMeters = trailLengthInMeters - totalDistanceTraveledInMeters;
            returnTime = remainingDistanceInMeters / currentSpeedInMetersPerSecond;
//            returnTime = currentTimeInSeconds + remainingTime;

//            LocationMarker lastMarker = mapMarkers.get(mapMarkers.size() - 1);
//            double totalDistanceInMeters = distanceBetween(latLng.latitude, latLng.longitude, lastMarker.getLatitude(), lastMarker.getLongitude());
//            //5000 meters per hour is average walking speed
//            //or 84 meters per minute
//
//            double averageRate = 1.4; //1.4m per second
//            returnTime = (totalDistanceInMeters / averageRate);
//            returnTime += mapMarkers.get(mapMarkers.size() - 2).getReturnTime(); //Add the time from the previous one.
        }

        //Store in Firebase
        LocationMarker newLocationMarker = new LocationMarker(latLng.latitude, latLng.longitude, formattedDate, location.getTime(), location.getElapsedRealtimeNanos(), location.getAltitude(), location.getSpeed(), returnTime, trailLengthInMeters, totalDistanceTraveledInMeters, currentSpeedInMetersPerSecond, startTime, null);
        try {
            mMessageDataBaseReference.push().setValue(newLocationMarker);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
        mLastLocationMarker = newLocationMarker;
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

    /**
     * This is where the observer receives the location of the hiker.
     */
    //Start listening to the database
    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {

            mChildEventListener = new ChildEventListener() {
                @Override //Called when a message is added.
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    System.out.println("onChildAdded");

                    //dataSnapshot is a "snapshot" instance of the database, and in this case, will be the new added message.
                    LocationMarker newLocationMarker = dataSnapshot.getValue(LocationMarker.class);
                    if (trailLengthInMeters > 0)
                        newLocationMarker.setTrailLengthInMeters(trailLengthInMeters);
                    lastTimeUpdateInSeconds = System.currentTimeMillis() / 1000;
                    mapMarkers.add(newLocationMarker);

                    // update current position
                    lat = newLocationMarker.getLatitude();
                    lon = newLocationMarker.getLongitude();


                    //Draw the marker
                    drawMarker(newLocationMarker);
                    if (newLocationMarker.getThumbnail() != null && isObserver) {
                        LatLng pictureLatLng = new LatLng(newLocationMarker.getLatitude(), newLocationMarker.getLongitude());
//                        insertMarker(pictureLatLng, stringToBitMap(newLocationMarker.getThumbnail()));
                        addToCluster(pictureLatLng, stringToBitMap(newLocationMarker.getThumbnail()));
                    }

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


                            if (closestMarker.getTotalDistanceTravelledInMeters() == 0) {
                                double totalTime = System.currentTimeMillis()/1000 - closestMarker.getStartTime();
                                closestMarker.setTotalDistanceTravelledInMeters(totalTime/1.4);
                            }
                            if (closestMarker.getReturnTime() == 0) {
                                double speed;
                                if (closestMarker.getCurrentSpeedInMetersPerSecond() == 0)
                                    speed = 1.4;
                                else speed = closestMarker.getCurrentSpeedInMetersPerSecond();
                                closestMarker.setReturnTime((closestMarker.getTrailLengthInMeters() - closestMarker.getTotalDistanceTravelledInMeters()) / speed);
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
//        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
        // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//                new AlertDialog.Builder(getActivity())
//                        .setTitle("Location Permission Needed")
////                        .setMessage("This app needs the Location permission, please accept to use location functionality")
//                        .setMessage("This app needs the Location, Camera, and Gallery access permissions, please accept to use location functionality")
//                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                //Prompt the user once explanation has been shown
//                                ActivityCompat.requestPermissions(getActivity(),
//                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                                        MY_PERMISSIONS_REQUEST_LOCATION);
//                            }
//                        })
//                        .create()
//                        .show();
//            } else {
        // No explanation needed, we can request the permission.
        String[] permissions = {Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};

//            ActivityCompat.requestPermissions(getActivity(), permissions, RC_PERMISSIONS);
//        }
        ActivityCompat.requestPermissions(getActivity(),
//                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                permissions,
                MY_PERMISSIONS_REQUEST_LOCATION);

//            }
//        }
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


    /**
     * CODE OF NATHAN GERONIMO
     */
    public Bitmap stringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return ThumbnailUtils.extractThumbnail(bitmap, 150, 150); // Return the thumbnail
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }


    private File createImageFile(File storage) throws IOException {
        // Create an image file title
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mLastPhotoTimeStamp = timeStamp + ".jpg";
        String photoName = timeStamp + "_";
        File storageDir = new File(storage, APP_TAG);
        if (storageDir.exists()) {
            Log.d(APP_TAG, "directory exists!");
        } else {
            if (storageDir.mkdirs()) {
                Log.d(APP_TAG, "Directory created!");
            } else {
                Log.d(APP_TAG, "Failed to create directory");
            }
        }

        File image = File.createTempFile(
                photoName,      /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = image.getAbsoluteFile();
        return image;
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
                Toast.makeText(getActivity(), "Couldn't grab photo directory", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                startActivityForResult(takePictureIntent, CAMERA_REQUST_CODE);
            }
        }
    }

    private void insertPictureToMap(LatLng currLoc, Bitmap image) {


        if (small_to_large_photos.isEmpty()) { // If the map has no markers
            HashSet<Bitmap> newSet = new HashSet<>(); // Create a new set to be inserted in to the map
            newSet.add(image);
            small_to_large_photos.put(currLoc, newSet);
        } else {
            double result = 0;
            for (LatLng locationKey : small_to_large_photos.keySet()) {
//                float[] result = new float[1];
//                Location.distanceBetween(locationKey.latitude, locationKey.longitude, currLoc.latitude, currLoc.longitude, result);
                result = distanceBetween(locationKey.latitude, locationKey.longitude, currLoc.latitude, currLoc.longitude);
                if (result <= 2) { // if distance between first and second location is less than 2m
                    small_to_large_photos.get(locationKey).add(image); // just add the image to the existing set
                } else {
                    HashSet<Bitmap> newSet = new HashSet<>(); // Create a new set to be inserted in to the map
                    newSet.add(image);
                    small_to_large_photos.put(currLoc, newSet);
                }
            }
        }

//        if (small_to_large_photos.containsKey(stringKey)) { // If there's a key, then there's a set
//            small_to_large_photos.get(stringKey).add(image); // So just add the image to that set
//        }
//        else {
//            HashSet<Bitmap> newSet = new HashSet<>(); // Create a new set to be inserted in to the map
//            newSet.add(image);
//            small_to_large_photos.put(stringKey, newSet);
//        }

    }


    private void addToCluster(LatLng location, Bitmap takenImage) {
        String title = String.valueOf(location.latitude) + " " + String.valueOf(location.longitude);
        String snippet = "snip";
        Bitmap newThumbnail = ThumbnailUtils.extractThumbnail(takenImage, 150, 150);
        MarkerCluster item = new MarkerCluster(location.latitude, location.longitude, title, snippet, newThumbnail);
        mClusterManager.addItem(item);
        mClusterManager.cluster(); // Make the markers/clusters appear immediately
        Toast.makeText(getActivity(), "Marker placed", Toast.LENGTH_SHORT).show();

        Log.e("LOCATION OF PIC TAKEN", location.toString());
        insertPictureToMap(item.getPosition(), takenImage);
        allPictures.add(takenImage);
    }

    public void savebitmap(Bitmap bmp, String fileName) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//        bmp.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        File f = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES + File.separator + APP_TAG), fileName);
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();
    }

    public static Bitmap getScaledBitmapFromUri(Context context, Uri uriImageFile) {
        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 4;
        try {
            BitmapFactory.decodeStream(context.getContentResolver()
                    .openInputStream(uriImageFile), null, options);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            return null;
        }

//        int srcWidth = options.outWidth;
//        int scale = 1;
//        while (srcWidth / 2 > 60) {
//            srcWidth /= 2;
//            scale *= 2;
//        }

        int srcHeight = options.outHeight;
        int scale = 1;
        while (srcHeight / 2 > 60) {
            srcHeight /= 2;
            scale *= 2;
        }

        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inSampleSize = scale;

        try {
            scaledBitmap = BitmapFactory.decodeStream(context
                    .getContentResolver().openInputStream(uriImageFile), null, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return scaledBitmap;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUST_CODE && resultCode == RESULT_OK) {

            // Grab bitmap for photo taken
            Bundle extras = data.getExtras();
            Bitmap takenImage = (Bitmap) extras.get("data"); // Grabs the photo taken.
            Bitmap rescaledImage = takenImage;
            Uri targetUri = data.getData();
            if (targetUri != null) {
                DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
                rescaledImage = getScaledBitmapFromUri(getContext(), targetUri);
//                imageView.setImageBitmap(photo);
            } else {
                Log.e("Rescaled", "DIDN'T WORK ");
            }

//            String[] split = mCurrentPhotoPath.toString().split("/"); // We need the full file title of the photo from this.

//            try {
//                // Send the full photo to the app created photo album
//                savebitmap(takenImage, split[split.length-1]);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

//            Intent toGallery = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//            File album = new File(Environment.getExternalStoragePublicDirectory(
//                    Environment.DIRECTORY_PICTURES + File.separator + APP_TAG), split[split.length-1]);
//            Uri content = Uri.fromFile(album);
//            toGallery.setData(content);
//            getActivity().sendBroadcast(toGallery);


            // Make the thumbnail for the map
            thumbnail = ThumbnailUtils.extractThumbnail(takenImage, 150, 150); // Makes the photo into a scaled thumbnail
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + mLastPhotoTimeStamp;
            addToCluster(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), takenImage);
//            insertMarker(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), takenImage);


            ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
            takenImage.compress(Bitmap.CompressFormat.PNG, 100, bYtE);
            byte[] byteArray = bYtE.toByteArray();
            String imageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);
            mLastLocationMarker.setThumbnail(imageFile); // Set the thumbnail after it's created

            LocationMarker newLocationMarker = mLastLocationMarker;
            mMessageDataBaseReference.push().setValue(newLocationMarker);
            mLastLocationMarker.setThumbnail(null); // erase the thumbnail saved so the picture isn't used again
        }
    }

    /**
     * Draws profile photos inside markers (using IconGenerator).
     * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
     */
    private class MarkerRenderer extends DefaultClusterRenderer<MarkerCluster> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public MarkerRenderer() {
            super(getApplicationContext(), mGoogleMap, mClusterManager);


            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(MarkerCluster item, MarkerOptions markerOptions) {
            // Draw a marker with the newly created thumbnail.
            if (isObserver) {
                mClusterImageView.setImageDrawable(new BitmapDrawable(item.getThumbnail()));
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(item.getThumbnail()));
            } else {
                mClusterImageView.setImageDrawable(new BitmapDrawable(thumbnail));
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(thumbnail));
            }

        }

        @Override
        protected void onBeforeClusterRendered(Cluster<MarkerCluster> cluster, MarkerOptions markerOptions) {
            // Places the number of markers inside of a cluster,
            // And puts the most recently added marker icon on top
            if (isObserver) {
                Bitmap newThumbnail = small_to_large_photos.get(cluster.getPosition().toString()).iterator().next();
                mClusterImageView.setImageDrawable(new BitmapDrawable(newThumbnail));
                Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
            } else {
                mClusterImageView.setImageDrawable(new BitmapDrawable(thumbnail));
                Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
            }

        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 0;
        }
    }
}