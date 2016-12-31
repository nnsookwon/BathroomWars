package io.nnsookwon.bathroom_wars;

import android.Manifest.permission;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.google.android.gms.common.api.GoogleApiClient.Builder;
import static com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import static com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import static com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import static io.nnsookwon.bathroom_wars.R.id.map;

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

    private static final int GENERAL_MAP = 1;
    private static final int PERSONAL_MAP = 2;
    private static final int BATTLE_MAP = 3;

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

    private Map<String, Marker> markers;
    private ArrayAdapter<FacebookFriend> friendsListAdapter;

    private double userLongitude;
    private double userLatitude;
    private User user;
    private Restroom restroomSelected;

    private int state;

    private BathroomBattleHandler personalHandler;
    private BathroomBattleHandler friendHandler;

    private boolean facebookSDKInitilized;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        state = GENERAL_MAP;
        facebookSDKInitilized = false;
        initFirebase();

        markers = new HashMap<>();
        friendsListAdapter = new ArrayAdapter<FacebookFriend>(this, android.R.layout.select_dialog_singlechoice);
        personalHandler = null;
        friendHandler = null;
        userLatitude = 0;
        userLongitude = 0;
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);
        geoQuery = restroomsGeoFire.queryAtLocation(new GeoLocation(userLatitude, userLongitude),INITIAL_ZOOM_LEVEL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkPermissions();
        buildGoogleApiClient();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (state != BATTLE_MAP) {
            personalHandler = null;
            friendHandler = null;
        }
        removeMarkers();
        switch (item.getItemId()) {
            case R.id.button_show_locations:
                setMapState(GENERAL_MAP);
                break;
            case R.id.button_show_personal_history:
                setMapState(PERSONAL_MAP);
                break;
            case R.id.button_battle:
                showFriendsListDialog();
                break;
            case R.id.button_sign_out:
                mFirebaseAuth.signOut(); //sign out of firebase
                LoginManager.getInstance().logOut(); //sign out of facebook
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

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            FacebookSdk.sdkInitialize(getApplicationContext(), new FacebookSdk.InitializeCallback() {
                @Override
                public void onInitialized() {
                    if (AccessToken.getCurrentAccessToken() != null){
                        facebookSDKInitilized = true;
                    }
                }
            });
            user = new User();
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
                    setValueEventListener();
                    while (!facebookSDKInitilized);
                    initFacebookFriends();
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
                    String key = usersDatabaseRef.child(user.getUid()).child("restroomVisits").push().getKey();
                    RestroomVisit restroomVisit = new RestroomVisit(
                            key, restroomSelected, Calendar.getInstance().getTimeInMillis());
                    usersDatabaseRef.child(user.getUid() + "/restroomVisits/" + key).setValue(restroomVisit);
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
        View dialogView = View.inflate(MainActivity.this, R.layout.add_new_restroom_dialog, null);
        builder.setView(dialogView);
        final EditText input = (EditText)dialogView.findViewById(R.id.new_restroom_name);
        final CheckBox publicCheckBox = (CheckBox) dialogView.findViewById(R.id.public_restroom);
        final CheckBox addNowCheckBox = (CheckBox) dialogView.findViewById(R.id.add_now);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int whichButton) {

                String newLocationName = input.getText().toString().trim().replaceAll("\\s+", " ");
                //removes leading and trailing spaces, and multiple spaces in between

                if (newLocationName.length() > 0) {
                    //input must be more than just spaces
                    String restroomKey = restroomsDatabaseRef.push().getKey();
                    Restroom restroom = new Restroom(restroomKey, newLocationName, userLatitude, userLongitude, 0);

                    if (publicCheckBox.isChecked()) {
                        //add to DB, so anyone can see
                        restroomsDatabaseRef.child(restroomKey).setValue(restroom);
                        restroomsGeoFire.setLocation(restroomKey, new GeoLocation(userLatitude,userLongitude));
                    }
                    if (addNowCheckBox.isChecked()) {
                        restroomSelected = restroom;
                        String key = usersDatabaseRef.child(user.getUid()).child("restroomVisits").push().getKey();
                        RestroomVisit restroomVisit = new RestroomVisit(
                                key, restroomSelected, Calendar.getInstance().getTimeInMillis());
                        usersDatabaseRef.child(user.getUid() + "/restroomVisits/" + key).setValue(restroomVisit);

                    }
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
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
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
        gMap.getUiSettings().setMapToolbarEnabled(false); //hide navigation toolbar
        gMap.addMarker(new MarkerOptions().position(latLng));
        gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, INITIAL_ZOOM_LEVEL));

        gMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (state == GENERAL_MAP)
                    restroomSelected = (Restroom)(marker.getTag());
                return false;
            }
        });
        gMap.setOnCameraIdleListener(new OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (state == GENERAL_MAP) {
                    // Update the search criteria for this geoQuery
                    LatLng center = gMap.getCameraPosition().target;
                    double radius = zoomLevelToRadius(gMap.getCameraPosition().zoom);
                    geoQuery.setCenter(new GeoLocation(center.latitude, center.longitude));
                    // radius in km
                    geoQuery.setRadius(radius / 1000);
                }
            }
        });
        gMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            //delete personal restroom visits by long clicking on info window
            @Override
            public void onInfoWindowLongClick(final Marker marker) {
                if (state == PERSONAL_MAP){
                    RestroomVisit restroomVisit = (RestroomVisit) marker.getTag();
                    if (restroomVisit != null) {
                        final String key = restroomVisit.getKey();
                        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Delete Record")
                                .setMessage("Would you like to delete this record?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        usersDatabaseRef.child(user.getUid() + "/restroomVisits/" + key).removeValue();
                                        marker.remove();
                                        markers.remove(key);
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create();
                        dialog.show();

                    }
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkPermissions();
        try{
            gMap.setMyLocationEnabled(true);
            gMap.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
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
        if (state == GENERAL_MAP) {
            geoQuery.addGeoQueryEventListener(this);
            setGeneralMapButtonsVisibility(true);
        }
        Log.d("State monitor", "onStart " + state);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        //remove all event listeners to stop updating in the background
        geoQuery.removeAllListeners();
        Log.d("State monitor", "onStop " + state);
    }

    protected void onResume(){
        super.onResume();
        Log.d("State monitor", "onResume");
    }

    protected void onPause(){
        super.onPause();
        Log.d("State monitor", "onPause");
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
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
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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
        //Creating a LatLng Object to store Coordinates
        LatLng latLng = new LatLng(userLatitude, userLongitude);

        //Moving the camera
        gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        //Animating the camera
        gMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    public void populatePersonalMap() {
        usersDatabaseRef.child(user.getUid() + "/restroomVisits").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy HH:mm", Locale.US);
                        for (DataSnapshot restroomVisitSnapshot : dataSnapshot.getChildren()) {
                            RestroomVisit restroomVisit = restroomVisitSnapshot.getValue(RestroomVisit.class);
                            if (restroomVisit != null) {
                                Restroom restroom = restroomVisit.getRestroom();
                                if (restroom != null) {
                                    Marker marker = gMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(restroom.getLatitude(), restroom.getLongitude()))
                                            .title(restroom.getName())
                                            .snippet(simpleDateFormat.format(restroomVisit.getTimeMilli()))
                                            .draggable(false));
                                    marker.setTag(restroomVisit);
                                    markers.put(restroomVisitSnapshot.getKey(), marker);
                                }
                            }
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

                if(restroom != null) {
                    Marker marker = gMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.latitude, location.longitude))
                            .title(restroom.getName())
                            .draggable(false));
                    marker.setTag(restroom);
                    markers.put(key, marker);
                }
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
        if (marker != null){
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

    public void initFacebookFriends(){

        while (user == null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //grab User's Facebook ID
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                    /* handle the result */
                       try {
                           JSONObject json = response.getJSONObject();
                           if (user.getFacebookId() == null) {
                                user.setFacebookId(json.getString("id"));
                                usersDatabaseRef.child(mFirebaseUser.getUid() + "/facebookId").
                                        setValue(user.getFacebookId());
                           }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();

        //Grab User's friends also using this app
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                    /* handle the result */
                        Log.d("facebook friends:" ,response.toString());
                        try {
                            JSONObject json = response.getJSONObject();
                            JSONArray jArray = json.getJSONArray("data");
                            for (int i = 0; i < jArray.length(); i++){
                                JSONObject jsonFriend = jArray.getJSONObject(i);
                                FacebookFriend friend =
                                        new FacebookFriend(jsonFriend.getString("id"), jsonFriend.getString("name"));
                                friendsListAdapter.add(friend);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }
        ).executeAsync();
    }

    public void showFriendsListDialog(){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle("Choose a friend");

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setMapState(GENERAL_MAP);
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(friendsListAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String friendFacebookId = friendsListAdapter.getItem(which).getId();
                String friendName = friendsListAdapter.getItem(which).getUserName();
                setMapState(BATTLE_MAP);
                populateBattleMap(friendFacebookId, friendName);
            }
        });
        builderSingle.show();
    }

    public void populateBattleMap(final String friendFacebookId, final String friendName){
        String friendUid = "";
        Log.d("facebook friend id", friendFacebookId);
        personalHandler =
                new BathroomBattleHandler(gMap, user.getUserName(), "1768ea");
        friendHandler =
                new BathroomBattleHandler(gMap, friendName, "c910b3");
        String friendFirstName = friendName.split("\\s")[0]; //split at whitespace
        ((Button)findViewById(R.id.button_friend_territory))
                .setText(friendFirstName + "'s " + getResources().getString(R.string.territory));

        usersDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //*******Populate personal battle map*******
                DataSnapshot userRestroomVisitsSnapshot = dataSnapshot.child(user.getUid() + "/restroomVisits");
                for (DataSnapshot restroomVisitSnapshot : userRestroomVisitsSnapshot.getChildren()) {
                    RestroomVisit restroomVisit = restroomVisitSnapshot.getValue(RestroomVisit.class);
                    if (restroomVisit != null) {
                        Restroom restroom = restroomVisit.getRestroom();
                        if (restroom != null) {
                            personalHandler.addMarker(restroomVisitSnapshot.getKey(), restroom);
                        }
                    }
                }

                //*******Populate friend's battle map*******
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    if (friendFacebookId.equals(userSnapshot.child("facebookId").getValue())){
                        for (DataSnapshot restroomVisitSnapshot : userSnapshot.child("restroomVisits").getChildren()) {
                            RestroomVisit restroomVisit = restroomVisitSnapshot.getValue(RestroomVisit.class);
                            if (restroomVisit != null) {
                                Restroom restroom = restroomVisit.getRestroom();
                                if (restroom != null) {
                                    friendHandler.addMarker(restroomVisitSnapshot.getKey(), restroom);
                                }
                            }
                        }
                        break;
                    }
                }
            }

                @Override
                public void onCancelled (DatabaseError databaseError){

                }
        });
    }


    public void setGeneralMapButtonsVisibility(boolean visible){
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.buttons_general_map);
        if (visible)
            linearLayout.setVisibility(View.VISIBLE);
        else
            linearLayout.setVisibility(View.GONE);
    }

    public void setBattleMapButtonsVisibility(boolean visible){
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.buttons_battle_map);
        if (visible)
            linearLayout.setVisibility(View.VISIBLE);
        else
            linearLayout.setVisibility(View.GONE);
    }

    public void togglePersonalTerritory(View v){
        if (personalHandler != null) {
            personalHandler.toggleVisibility();
            if (personalHandler.isShowing())
                v.setAlpha(1);
            else
                v.setAlpha(0.5f);
        }
    }

    public void toggleFriendTerritory(View v){
        if (friendHandler != null) {
            friendHandler.toggleVisibility();
            if (friendHandler.isShowing())
                v.setAlpha(0.9f);
            else
                v.setAlpha(0.7f);
        }
    }

    public void setMapState(int mapState){
        geoQuery.removeAllListeners();
        setGeneralMapButtonsVisibility(false);
        setBattleMapButtonsVisibility(false);

        switch (mapState) {
            case GENERAL_MAP:
                state = GENERAL_MAP;
                setGeneralMapButtonsVisibility(true);
                geoQuery.addGeoQueryEventListener(this);
                break;
            case PERSONAL_MAP:
                state = PERSONAL_MAP;
                populatePersonalMap();
                break;
            case BATTLE_MAP:
                state = BATTLE_MAP;
                setBattleMapButtonsVisibility(true);
                break;
        }
    }




}
