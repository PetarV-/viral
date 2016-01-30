package com.hackbridge.viral;

public class Node {
    private static long GLOBAL_ID = 1;
    private final long nodeID;

    private PhysicalState physical_state;
    private AwarenessState awareness_state;

    private boolean connected;

    public Node(PhysicalState physical_s, AwarenessState awareness_s) {
        nodeID = GLOBAL_ID;
        GLOBAL_ID++;
        this.physical_state = physical_s;
        this.awareness_state = awareness_s;
        connected = true;
    }

    public long getID() {
        return nodeID;
    }

    public AwarenessState getAwarenessState() {
        return awareness_state;
    }

    public PhysicalState getPhysicalState() {
        return physical_state;
    }

    public boolean getConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public String toString() {
        return String.format("Node id: Awareness: %d, %s, Physical: %s",
                nodeID, awareness_state, physical_state);
    }
}
