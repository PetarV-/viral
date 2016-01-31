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
    private RoleState role;
    private String code;
    private long id;
    private boolean isRunning;

    public StartMessage(long id, PhysicalState infected, AwarenessState aware, RoleState role) {
        this.id = id;
        this.infected = infected;
        this.aware = aware;
        this.role = role;
        this.code = "";
        this.isRunning = false;
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

    public boolean isRunning() {
        return isRunning;
    }

    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public RoleState getRole() {
        return role;
    }
}
