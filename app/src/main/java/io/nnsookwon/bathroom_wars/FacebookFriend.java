package io.nnsookwon.bathroom_wars;

/**
 * Created by nnsoo on 12/16/2016.
 */

public class FacebookFriend {
    private String facebookId;
    private String userName;

    public FacebookFriend(){

    }
    public FacebookFriend(String id, String name){
        facebookId = id;
        userName = name;
    }

    public void setFacebookId(String id){
        facebookId = id;
    }

    public void setUserName(String name){
        userName = name;
    }

    public String getId(){
        return facebookId;
    }

    public String getUserName(){
        return userName;
    }

    public String toString(){
        return userName;
    }
}
