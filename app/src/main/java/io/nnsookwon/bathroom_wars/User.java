package io.nnsookwon.bathroom_wars;

import java.util.ArrayList;

/**
 * Created by nnsoo on 12/9/2016.
 */

public class User {

    private String userName;
    private String uid;
    private String facebookId;
    private String photoUrl;
    private ArrayList<RestroomVisit> restroomVisits = new ArrayList<>();

    public User(){

    }

    public User(String mUserName, String mUid, String mProviderId, String mPhotoUrl){
        userName = mUserName;
        uid = mUid;
        facebookId = mProviderId;
        photoUrl = mPhotoUrl;
    }

    public void setUserName(String mUserName) {
        userName = mUserName;
    }

    public void setUid(String mUid){
        uid = mUid;
    }

    public void setFacebookId(String mId){
        facebookId = mId;
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
        return uid;
    }
    public String getFacebookId(){
        return facebookId;
    }
    public String getPhotoUrl() {
        return photoUrl;
    }

    public ArrayList<RestroomVisit> getRestroomVisits(){
        return restroomVisits;
    }

    public String toString(){
        return uid + " " + userName;
    }
}
