package com.hackbridge.viral;

/**
 * Created by PetarV on 30/01/2016.
 */
/*
    Server -> Phone. Message sent when a phone's state changes.
    (new_phys_state, new_awareness_state)
 */
public class ChangeMessage extends Message {
    private PhysicalState infected;
    private AwarenessState aware;

    public ChangeMessage(PhysicalState infected, AwarenessState aware) {
        this.infected = infected;
        this.aware = aware;
    }

    public PhysicalState getInfected() {
        return infected;
    }

    public AwarenessState getAware() {
        return aware;
    }
}
