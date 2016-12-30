package io.nnsookwon.bathroom_wars;

/**
 * Created by nnsoo on 12/13/2016.
 */

public class RestroomVisit {
    private String key;
    private Restroom restroom;
    private long timeMilli;

    public RestroomVisit() {

    }

    public RestroomVisit(String mKey, Restroom mRestroom, long mTimeMilli){
        key = mKey;
        restroom = mRestroom;
        timeMilli = mTimeMilli;
    }

    public void setKey(String mKey){
        key = mKey;
    }
    public void setRestroom(Restroom mRestroom){
        restroom = mRestroom;
    }

    public void setTimeMilli(long mTimeMilli){
        timeMilli = mTimeMilli;
    }

    public String getKey(){
        return key;
    }
    public Restroom getRestroom(){
        return restroom;
    }

    public long getTimeMilli(){
        return timeMilli;
    }


}
