package io.nnsookwon.bathroom_buddies;

import java.util.ArrayList;

/**
 * Created by nnsoo on 12/9/2016.
 */

public class User {

    private String userName;
    private String uId;
    private String photoUrl;
    private ArrayList<RestroomVisit> restroomVisits = new ArrayList<>();

    public User(){

    }

    public User(String mUserName, String mUid, String mPhotoUrl){
        userName = mUserName;
        uId = mUid;
        photoUrl = mPhotoUrl;
    }

    public void setUserName(String mUserName) {
        userName = mUserName;
    }

    public void setUid(String mUid){
        uId = mUid;
    }

    public void setPhotoUrl(String mPhotoUrl) {
        photoUrl = mPhotoUrl;
    }

    public void addRestroomVisits(RestroomVisit restroomVisit){
        restroomVisits.add(restroomVisit);
    }

    public String getUserName(){
        return userName;
    }
    public String getUid() {
        return uId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public ArrayList<RestroomVisit> getRestroomVisits(){
        return restroomVisits;
    }
}
