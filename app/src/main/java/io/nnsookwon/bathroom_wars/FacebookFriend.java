package io.nnsookwon.bathroom_wars;

/**
 * Created by nnsoo on 12/16/2016.
 */

public class FacebookFriend {
    private String facebookId;
    private String userName;
    private String photoUrl;

    public FacebookFriend(){

    }
    public FacebookFriend(String id, String name, String url) {
        facebookId = id;
        userName = name;
        photoUrl = url;
    }

    public void setFacebookId(String id){
        facebookId = id;
    }

    public void setUserName(String name){
        userName = name;
    }

    public void setPhotoUrl(String url) {
        photoUrl = url;
    }

    public String getId(){
        return facebookId;
    }

    public String getUserName(){
        return userName;
    }

    public String getPhotoUrl(){
        return photoUrl;
    }

    public String toString(){
        return userName;
    }
}
