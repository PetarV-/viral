package com.hackbridge.viral;

public class Node {
    private static long GLOBAL_ID = 1;
    private final long nodeID;

    private PhysicalState physical_state;
    private AwarenessState awareness_state;

    // TEMPORARY
    public double lat;
    public double lng;

    private boolean connected;
    private boolean location_set;

    public Node(PhysicalState physical_s, AwarenessState awareness_s) {
        nodeID = GLOBAL_ID;
        GLOBAL_ID++;
        this.physical_state = physical_s;
        this.awareness_state = awareness_s;
        connected = true;

        // TODO
        lat = Math.random() * 100;
        lng = Math.random() * 100;
        location_set = false;
    }

    public long getID() {
        return nodeID;
    }

    public AwarenessState getAwarenessState() {
        return awareness_state;
    }

    void setAwarenessState(AwarenessState as) {
       this.awareness_state = as;
    }

    public PhysicalState getPhysicalState() {
        return physical_state;
    }

    void setPhysicalState(PhysicalState ps) {
        this.physical_state = ps;
    }

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

    void setLocationSet(boolean location_set) {
        this.location_set = location_set;
    }

    @Override
    public String toString() {
        return String.format("{NodeId : %d, Awareness : %s, Physical : %s," + " Latitude : %.5f, Longitude : %.5f}",
                nodeID, awareness_state, physical_state, getLatitude(), getLongitude());
    }

    public boolean setLocation(double lat, double lng) {
        location_set = true;
        // TODO: CHECKs
        this.lat = lat;
        this.lng = lng;
        return true;
    }

    public double getLatitude() {
        return lat;
    }

    public double getLongitude() {
        return lng;
    }

    public boolean isActive() {
        return connected && location_set;
    }

    public double getDistanceFrom(Node o) {
        return gps2m(getLatitude(), getLongitude(), o.getLatitude(), o.getLongitude());
    }

    /**
     * Calculate distance from on longitude and latitude.
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
