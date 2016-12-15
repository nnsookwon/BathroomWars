package io.nnsookwon.bathroom_buddies;

/**
 * Created by nnsoo on 12/13/2016.
 */

public class Restroom {
    private String key;
    private String name;
    private double latitude;
    private double longitude;
    private int visits;

    public Restroom(){

    }

    public Restroom(String mKey, String mName, double mLatitude, double mLongitude, int mVisits){
        key = mKey;
        name = mName;
        latitude = mLatitude;
        longitude = mLongitude;
        visits = mVisits;
    }

    public void setKey(String mKey){
        key = mKey;
    }

    public void setName(String mName){
        name = mName;
    }

    public void setLatitude(double mLatitude){
        latitude = mLatitude;
    }

    public void setLongitude(double mLongitude){
        longitude = mLongitude;
    }

    public void setVisits(int mVisits){
        visits = mVisits;
    }

    public String getKey(){
        return key;
    }
    public String getName(){
        return name;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public int getVisits(){
        return visits;
    }

    public void incrementVisits(){
        visits++;
    }

}
