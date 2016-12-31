package io.nnsookwon.bathroom_wars;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nnsoo on 12/15/2016.
 */

public class BathroomBattleHandler {

    public static final int RADIUS = 500;
    public static final int TRANSPARENCY = 40; //% transparency of circle fill

    private GoogleMap gMap;
    private String userDisplayName;
    private HashMap<String, Marker> markers;
    private HashMap<Marker, Circle> circles;
    private boolean isShowing;

    private int r;
    private int g;
    private int b;

    //colorString is a hex color code
    BathroomBattleHandler(GoogleMap mMap, String displayName, String colorString){
        gMap = mMap;
        userDisplayName = displayName;
        markers = new HashMap<>();
        circles = new HashMap<>();
        int color = (int)Long.parseLong(colorString, 16);
        r = (color >> 16) & 0xFF;
        g = (color >> 8) & 0xFF;
        b = (color >> 0) & 0xFF;
        isShowing = true;

    }

    public boolean isShowing(){
        return isShowing;
    }

    public void addMarker(String key, Restroom restroom){
        for (Marker marker: markers.values()){
            Restroom mRestroom = (Restroom) marker.getTag();
            if (mRestroom.equals(restroom)) {
                break;
            }
            //prevent duplicate locations from being placed on map
        }
        LatLng position= new LatLng(restroom.getLatitude(), restroom.getLongitude());
        Marker marker = gMap.addMarker(new MarkerOptions()
                .position(position)
                .title(userDisplayName)
                .snippet(restroom.getName()));
        marker.setTag(restroom);
        markers.put(key, marker);

        Circle territoryCircle = gMap.addCircle(new CircleOptions()
                            .center(position)
                            .radius(RADIUS)
                            .fillColor(Color.argb((int)((100-TRANSPARENCY)/100.0*255), r, g, b))
                            .strokeWidth(0));
        circles.put(marker, territoryCircle);

    }

    public void hideTerritory(){
        for(Map.Entry<Marker, Circle> entry: circles.entrySet()){
            entry.getKey().setVisible(false);
            entry.getValue().setVisible(false);
        }
    }

    public void showTerritory(){
        for(Map.Entry<Marker, Circle> entry: circles.entrySet()){
            entry.getKey().setVisible(true);
            entry.getValue().setVisible(true);
        }
    }

    public boolean toggleVisibility(){
        isShowing = !isShowing;
        if (isShowing)
            showTerritory();
        else
            hideTerritory();
        return isShowing;
    }

    public int getNumberOfBathrooms(){
        return markers.size();
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }



}
