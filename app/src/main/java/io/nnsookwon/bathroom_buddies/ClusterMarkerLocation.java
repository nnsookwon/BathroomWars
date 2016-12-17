package io.nnsookwon.bathroom_buddies;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by nnsoo on 12/14/2016.
 */

public class ClusterMarkerLocation implements ClusterItem {

    private LatLng position;
    private Restroom restroom;
    private RestroomVisit restroomVisit;

    public ClusterMarkerLocation(){

    }

    public ClusterMarkerLocation(double latitude, double longitude, Restroom mRestroom) {
        this(latitude, longitude, mRestroom, null);
    }

    public ClusterMarkerLocation(double latitude, double longitude,
                                 Restroom mRestroom, RestroomVisit mRestroomVistit){
        position = new LatLng(latitude, longitude);
        restroom = mRestroom;
        restroomVisit = mRestroomVistit;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public Restroom getRestroom(){
        return restroom;
    }

    public RestroomVisit getRestroomVisit(){
        return restroomVisit;
    }

    public void setPosition( LatLng mPosition ) {
        position = mPosition;
    }

    public void setRestroom(Restroom mRestroom){
        restroom = mRestroom;
    }

    public void setRestroomVisit(RestroomVisit mRestroomVisit){
        restroomVisit = mRestroomVisit;
    }
}