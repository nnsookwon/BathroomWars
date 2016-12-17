package io.nnsookwon.bathroom_wars;

/**
 * Created by nnsoo on 12/13/2016.
 */

public class RestroomVisit {

    private Restroom restroom;
    private long timeMilli;

    public RestroomVisit() {

    }

    public RestroomVisit(Restroom mRestroom, long mTimeMilli){
        restroom = mRestroom;
        timeMilli = mTimeMilli;
    }

    public void setRestroom(Restroom mRestroom){
        restroom = mRestroom;
    }

    public void setTimeMilli(long mTimeMilli){
        timeMilli = mTimeMilli;
    }

    public Restroom getRestroom(){
        return restroom;
    }

    public long getTimeMilli(){
        return timeMilli;
    }

}
