package com.hackbridge.viral;

/**
 * Created by PetarV on 30/01/2016.
 */
/*
    Server -> Phone. Sent when a round stops. Contains information on whether the round was won.
 */
public class StopMessage extends Message {
    private boolean hasWon;

    public StopMessage(boolean hasWon) {
        this.hasWon = hasWon;
    }

    public boolean isHasWon() {
        return hasWon;
    }
}
