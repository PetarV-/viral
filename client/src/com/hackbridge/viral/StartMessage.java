package com.hackbridge.viral;

/**
 * Created by PetarV on 30/01/2016.
 */

/*
    Server -> Phone. Hello message sent on connect, or round start.
    (new_physical_state, new_awareness_state, id)
 */
public class StartMessage extends Message {
    private PhysicalState infected;
    private AwarenessState aware;
    private String code;
    private long id;

    public StartMessage(long id, PhysicalState infected, AwarenessState aware) {
        this.id = id;
        this.infected = infected;
        this.aware = aware;
        this.code = "";
    }

    public PhysicalState getInfected() {
        return infected;
    }

    public AwarenessState getAware() {
        return aware;
    }

    public long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
