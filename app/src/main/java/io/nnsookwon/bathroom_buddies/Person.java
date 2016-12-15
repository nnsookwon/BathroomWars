package io.nnsookwon.bathroom_buddies;

/**
 * Created by nnsoo on 11/29/2016.
 */
public class Person {
    //name and address string
    private String name;
    private String address;
    private double latitude;
    private double longitude;

    public Person() {
      /*Blank default constructor essential for Firebase*/
    }

    public Person(String mName, String mAddress, double mLongitude, double mLatitude) {
        name = mName;
        address = mAddress;
        longitude = mLongitude;
        latitude = mLatitude;
    }
    //Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLongitude(){
        return longitude;
    }

    public void setLongitude(double mLongitude){
        longitude = mLongitude;
    }
    public double getLatitude(){
        return latitude;
    }

    public void setLatitude(double mLatitude){
        latitude = mLatitude;
    }
}