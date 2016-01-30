package com.hackbridge.viral;

/**
 * Created by PetarV on 30/01/2016.
 */
/*
    Internal message for the server to put in the event queue.
    Has no information except the id of the node that disconnected.
 */
public class DisconnectMessage extends Message {
    private long id;

    public DisconnectMessage(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
