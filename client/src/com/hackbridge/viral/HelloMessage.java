package com.hackbridge.viral;

/**
 * Created by PetarV on 30/01/2016.
 */
/*
    Phone -> Server. Sent when a phone with an ID connects.
    (id)
 */
public class HelloMessage extends Message {
    private long id;

    public HelloMessage(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
