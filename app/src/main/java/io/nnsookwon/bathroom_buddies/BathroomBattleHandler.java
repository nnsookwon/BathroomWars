package io.nnsookwon.bathroom_buddies;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

/**
 * Created by nnsoo on 12/15/2016.
 */

public class BathroomBattleHandler {

    public static final int RADIUS = 500;
    public static final int TRANSPARENCY = 60;

    private GoogleMap gMap;
    private String userDisplayName;
    private HashMap<String, Marker> markers;


    BathroomBattleHandler(GoogleMap mMap, String displayName){
        gMap = mMap;
        userDisplayName = displayName;
        markers = new HashMap<>();
    }

    public void init() {
        gMap.addMarker(new MarkerOptions()
                .position(gMap.getCameraPosition().target)
                .title("hello"));
    }

    public void addMarker(String key, LatLng position){
        Marker marker = gMap.addMarker(new MarkerOptions()
                .position(position)
                .title(userDisplayName));
        markers.put(key, marker);

        Circle territoryCircle = gMap.addCircle(new CircleOptions()
                            .center(position)
                            .radius(RADIUS)
                            .fillColor(Color.argb(TRANSPARENCY, 255, 0, 255))
                            .strokeWidth(0));

    }





}
