package com.hackbridge.viral;

/**
 * Created by PetarV on 30/01/2016.
 */
/*
    Phone -> Server. Sent when a phone's location is updated.
    (Location)
 */
public class PositionMessage extends Message {

    private LocationWrapper loc;
    private long id;

    public PositionMessage(long id, LocationWrapper loc) {
        this.id = id;
        this.loc = loc;
    }

    public LocationWrapper getLocationWrapper() {
        return loc;
    }

    public long getId() {
        return id;
    } 

}
