package com.hackbridge.viral;

public class Node {
    private static long GLOBAL_ID = 1;  // The next available unique id.

    private final long nodeID;  // The node's unique id.

    private PhysicalState physicalState;
    private AwarenessState awarenessState;
    private RoleState roleState;

    private LocationWrapper location;  // The last known location of the node.

    private boolean connected;  // True if the node is currently connected to the network.
    private boolean hasLocation;  // True if a location was ever known for the node.

    public Node(PhysicalState physicalState, AwarenessState awarenessState, RoleState roleState) {
        nodeID = GLOBAL_ID;
        GLOBAL_ID++;

        this.physicalState = physicalState;
        this.awarenessState = awarenessState;
        this.roleState = roleState;

        connected = true;
        location = new LocationWrapper(0.0,0.0,0.0);
        hasLocation = false;
    }

    /**
     * Resets the node's state, but preserves its unique id.
     * @param physicalState
     * @param awarenessState
     * @param roleState
     */
    public void reset(PhysicalState physicalState,
                      AwarenessState awarenessState, RoleState roleState) {
        connected = false;
        hasLocation = false;
        location = new LocationWrapper(0.0,0.0,0.0);
        this.physicalState = physicalState;
        this.awarenessState = awarenessState;
        this.roleState = roleState;
    }

    /**
     * Returns the node's unique id.
     * @return
     */
    public long getID() {
        return nodeID;
    }

    public AwarenessState getAwarenessState() {
        return awarenessState;
    }

    void setAwarenessState(AwarenessState awarenessState) {
       this.awarenessState = awarenessState;
    }

    public PhysicalState getPhysicalState() {
        return physicalState;
    }

    void setPhysicalState(PhysicalState physicalState) {
        this.physicalState = physicalState;
    }

    public RoleState getRoleState() {
        return roleState;
    }

    /**
     * Returns true if the node is currently connected to the network.
     * @return
     */
    public boolean getConnected() {
        return connected;
    }

    /**
     * True if node is connected to the server.
     * @param connected
     */
    void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public String toString() {
        return String.format(
                "{NodeId : %d, Awareness : %s, Physical : %s," +
                " Latitude : %.5f, Longitude : %.5f}",
                nodeID, awarenessState, physicalState,
                hasLocation ? getLatitude() :  0.0, hasLocation ? getLongitude() : 0.0);
    }

    public boolean setLocation(LocationWrapper location) {
        hasLocation = true;
        this.location = location;
        return true;
    }

    public double getLatitude() {
        if (!hasLocation) {
            Logger.logError(3, "Location for " + nodeID + " not set.");
            return 0.0;
        }
        return location.getLatitude();
    }

    public double getLongitude() {
        if (!hasLocation) {
            Logger.logError(3, "Location for " + nodeID + " not set.");
            return 0.0;
        }
        return location.getLongitude();
    }

    /**
     * Returns true if the node is active. A node is active in the network if it is both
     * connected to the network and has reported its location.
     *
     * @return
     */
    public boolean isActive() {
        return connected && hasLocation;
    }

    /**
     * Gets the distance (in metres) from another node.
     * @param o
     * @return
     */
    public double getDistanceFrom(Node o) {
        return gps2m(getLatitude(), getLongitude(), o.getLatitude(), o.getLongitude());
    }

    /**
     * Calculate the distance between two points specified by longitude and latitude.
     *
     * Taken from:
     * http://stackoverflow.com/questions/8049612/calculating-distance-between-two-geographic-locations
     * @param lat_a
     * @param lng_a
     * @param lat_b
     * @param lng_b
     * @return
     */
    private double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
        double pk = (double) (180.0/Math.PI);

        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;

        double t1 = Math.cos(a1)*Math.cos(a2)*Math.cos(b1)*Math.cos(b2);
        double t2 = Math.cos(a1)*Math.sin(a2)*Math.cos(b1)*Math.sin(b2);
        double t3 = Math.sin(a1)*Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000*tt;
    }
}
