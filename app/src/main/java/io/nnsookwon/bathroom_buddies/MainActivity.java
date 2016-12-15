package io.nnsookwon.bathroom_buddies;

import android.Manifest.permission;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.google.android.gms.common.api.GoogleApiClient.Builder;
import static io.nnsookwon.bathroom_buddies.R.id.map;

/**
 * Created by nnsoo on 11/29/2016.
 */

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GeoQueryEventListener
{

    protected static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE = 10;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE = 20;
    private static final int INITIAL_ZOOM_LEVEL = 14;
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    private GoogleMap gMap;

    private DatabaseReference usersDatabaseRef;
    private DatabaseReference restroomsDatabaseRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GeoFire restroomsGeoFire;
    private GeoQuery geoQuery;

    private Map<String,Marker> markers;

    private Button buttonRecordRestroomVisit;
    private Button buttonCreateNewRestroomLocation;

    private double userLongitude;
    private double userLatitude;
    private User user;
    private Restroom restroomSelected;
    private boolean showPersonalHistory;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonRecordRestroomVisit = (Button) findViewById(R.id.button_record_restroom_visit);
        buttonCreateNewRestroomLocation = (Button) findViewById(R.id.button_create_new_restroom_location);
        showPersonalHistory = false;
        initFirebase();

        markers = new HashMap<String, Marker>();
        userLatitude = 0;
        userLongitude = 0;
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);
        geoQuery = restroomsGeoFire.queryAtLocation(new GeoLocation(userLatitude, userLongitude),INITIAL_ZOOM_LEVEL);
        checkPermissions();
        buildGoogleApiClient();


    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.button_show_personal_history:
                item.setChecked(!item.isChecked());
                showPersonalHistory = item.isChecked();
                removeMarkers();
                if (showPersonalHistory){
                    geoQuery.removeAllListeners();
                    populatePersonalMap();
                } else {
                   geoQuery.addGeoQueryEventListener(this);
                }
                break;
            case R.id.button_sign_out:
                mFirebaseAuth.signOut();
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                break;
        }
        return true;
    }

    private void initFirebase() {
        //init Firebase DB
        usersDatabaseRef =  FirebaseDatabase.getInstance().getReference("Users");
        restroomsDatabaseRef = FirebaseDatabase.getInstance().getReference("Restrooms");
        restroomsGeoFire = new GeoFire(FirebaseDatabase.getInstance().getReference("Restrooms GeoFire"));


        setValueEventListener();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        user = new User();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            user.setUserName(mFirebaseUser.getDisplayName());
            user.setUid(mFirebaseUser.getUid());
            user.setPhotoUrl(mFirebaseUser.getPhotoUrl().toString());


            usersDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                //check if user is in database yet
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.hasChild(mFirebaseUser.getUid())) {
                        usersDatabaseRef.child(user.getUid()).setValue(user);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    public void recordRestroomVisit(View v){
        if (restroomSelected == null){
            Toast.makeText(this, "Select a restroom on the map!", Toast.LENGTH_LONG).show();
        } else {
            //ask to confirm

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Record Restroom Visit?");
            builder.setMessage("Did you just use the restroom: " + restroomSelected.getName() + " ?");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int whichButton) {
                    RestroomVisit restroomVisit = new RestroomVisit(
                            restroomSelected, Calendar.getInstance().getTimeInMillis());
                    usersDatabaseRef.child(user.getUid()).child("restroomVisits").push().setValue(restroomVisit);

                    d.dismiss();
                }
            });

            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int whichButton) {
                    d.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

        }
    }

    public void showAddRestroomLocationDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add New Restroom Location");
        builder.setMessage("Enter name of restroom: ");
        final EditText input = new EditText(MainActivity.this);
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setSingleLine();
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int whichButton) {
                String newLocationName = input.getText().toString().trim().replaceAll("\\s+", " ");
                //removes leading and trailing spaces, and multiple spaces in between

                if (newLocationName.length() > 0) {
                    //input must be more than just spaces
                    Marker marker = gMap.addMarker(new MarkerOptions()
                            .position(new LatLng(userLatitude,userLongitude))
                            .title(newLocationName)
                            .draggable(false));
                    String restroomKey = restroomsDatabaseRef.push().getKey();
                    Restroom restroom = new Restroom(restroomKey, newLocationName, userLatitude, userLongitude, 0);
                    restroomsDatabaseRef.child(restroomKey).setValue(restroom);
                    restroomsGeoFire.setLocation(restroomKey, new GeoLocation(userLatitude,userLongitude));
                    marker.setTag(restroom);
                    markers.put(restroomKey, marker);
                }
                d.dismiss();
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int whichButton) {
                d.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void checkPermissions(){
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding

            ActivityCompat.requestPermissions(this,
                    new String[]{permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE);

            ActivityCompat.requestPermissions(this,
                    new String[]{permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE);
            //public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                    int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE:
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void onMapReady(GoogleMap googleMap) {
        LatLng latLng = new LatLng(-33.852, 151.211);
        gMap = googleMap;
        gMap.addMarker(new MarkerOptions().position(latLng));
        gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, INITIAL_ZOOM_LEVEL));
        gMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (!showPersonalHistory) {
                    // Update the search criteria for this geoQuery
                    LatLng center = gMap.getCameraPosition().target;
                    double radius = zoomLevelToRadius(gMap.getCameraPosition().zoom);
                    geoQuery.setCenter(new GeoLocation(center.latitude, center.longitude));
                    // radius in km
                    geoQuery.setRadius(radius / 1000);
                }
            }
        });
        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                restroomSelected = (Restroom)marker.getTag();
                return false;
            }
        });


        checkPermissions();
        try{
            gMap.setMyLocationEnabled(true);
            gMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    getCurrentLocation();
                    return false;
                }
            });
        }catch (SecurityException e){
            Log.e(TAG, "Unable to retrieve user location (permissions not granted)");
        }
    }

    public void setValueEventListener() {
        ValueEventListener dbListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                //Person post = dataSnapshot.getValue(Post.class);
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("ERROR", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        usersDatabaseRef.addValueEventListener(dbListener);
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        geoQuery.addGeoQueryEventListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        // remove all event listeners to stop updating in the background
        geoQuery.removeAllListeners();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        getCurrentLocation();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    //Getting current location
    private void getCurrentLocation() {
        //Creating a location object
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                userLatitude = mLastLocation.getLatitude();
                userLongitude = mLastLocation.getLongitude();
                moveMap();
            } else {
                Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
            }
        }catch (SecurityException e) {
            Log.e(TAG, "Unable to retrieve user location (permissions not granted)");
        }
    }

    //Function to move the map
    private void moveMap() {
        //String to display current latitude and longitude
        String msg = userLatitude + ", " + userLongitude;

        //Creating a LatLng Object to store Coordinates
        LatLng latLng = new LatLng(userLatitude, userLongitude);

       /* //Adding marker to map
        gMap.addMarker(new MarkerOptions()
                .position(latLng) //setting position
                .draggable(true) //Making the marker draggable
                .title("Current Location")); //Adding a title*/

        //Moving the camera
        gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        //Animating the camera
        gMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //Displaying current coordinates in toast
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public void populatePersonalMap(){
        usersDatabaseRef.child(user.getUid()).child("restroomVisits")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy hh:mm", Locale.US);
                for (DataSnapshot restroomVisitSnapshot: dataSnapshot.getChildren()){
                    RestroomVisit restroomVisit = restroomVisitSnapshot.getValue(RestroomVisit.class);
                    Restroom restroom = restroomVisit.getRestroom();
                    // Add a new marker to the map, labeled with restroom name, not draggable
                    Marker marker = gMap.addMarker(new MarkerOptions()
                            .position(new LatLng(restroom.getLatitude(), restroom.getLongitude()))
                            .title(restroom.getName())
                            .snippet(simpleDateFormat.format(restroomVisit.getTimeMilli()))
                            .draggable(false));
                    marker.setTag(restroom);
                    markers.put(restroomVisitSnapshot.getKey(),marker);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /******* GeoQuery ********/


    @Override
    public void onKeyEntered(final String key, final GeoLocation location) {
        Log.d("onKeyEntered", "fired");
        restroomsDatabaseRef.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Restroom restroom = dataSnapshot.getValue(Restroom.class);

                // Add a new marker to the map, labeled with restroom name, not draggable
                Marker marker = gMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location.latitude, location.longitude))
                        .title(restroom.getName())
                        .draggable(false));
                marker.setTag(restroom);
                markers.put(key, marker);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onKeyExited(String key) {
        // Remove any old marker
        Marker marker = markers.get(key);
        if (marker != null) {
            marker.remove();
            markers.remove(key);
        }
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        // Move the marker
        Marker marker = markers.get(key);
        if (marker != null) {
            animateMarkerTo(marker, location.latitude, location.longitude);
        }
    }

    @Override
    public void onGeoQueryReady() {
    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("There was an unexpected error querying GeoFire: " + error.getMessage())
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Animation handler for old APIs without animation support
    private void animateMarkerTo(final Marker marker, final double lat, final double lng) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long DURATION_MS = 3000;
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final LatLng startPosition = marker.getPosition();
        handler.post(new Runnable() {
            @Override
            public void run() {
                float elapsed = SystemClock.uptimeMillis() - start;
                float t = elapsed/DURATION_MS;
                float v = interpolator.getInterpolation(t);

                double currentLat = (lat - startPosition.latitude) * v + startPosition.latitude;
                double currentLng = (lng - startPosition.longitude) * v + startPosition.longitude;
                marker.setPosition(new LatLng(currentLat, currentLng));

                // if animation is not finished yet, repeat
                if (t < 1) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private double zoomLevelToRadius(double zoomLevel) {
        return 16384000/Math.pow(2, zoomLevel);
    }

    public void removeMarkers(){
        gMap.clear();
        markers.clear();
    }



}
