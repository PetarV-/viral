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

    /**
     * Returns true if the node has the disease. A node has been infected if it's PhysicalState
     * is INFECTED or CARRIER.
     * @return
     */
    public boolean hasDisease() {
        return physicalState == PhysicalState.INFECTED || physicalState == PhysicalState.CARRIER;
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
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     */
    private double computeDistanceAndBearing(double lat1, double lon1,
                                                  double lat2, double lon2) {
        // Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
        // using the "Inverse Formula" (section 4)
        int MAXITERS = 20;
        // Convert lat/long to radians
        lat1 *= Math.PI / 180.0;
        lat2 *= Math.PI / 180.0;
        lon1 *= Math.PI / 180.0;
        lon2 *= Math.PI / 180.0;
        double a = 6378137.0; // WGS84 major axis
        double b = 6356752.3142; // WGS84 semi-major axis
        double f = (a - b) / a;
        double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);
        double L = lon2 - lon1;
        double A = 0.0;
        double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
        double U2 = Math.atan((1.0 - f) * Math.tan(lat2));
        double cosU1 = Math.cos(U1);
        double cosU2 = Math.cos(U2);
        double sinU1 = Math.sin(U1);
        double sinU2 = Math.sin(U2);
        double cosU1cosU2 = cosU1 * cosU2;
        double sinU1sinU2 = sinU1 * sinU2;
        double sigma = 0.0;
        double deltaSigma = 0.0;
        double cosSqAlpha = 0.0;
        double cos2SM = 0.0;
        double cosSigma = 0.0;
        double sinSigma = 0.0;
        double cosLambda = 0.0;
        double sinLambda = 0.0;
        double lambda = L; // initial guess
        for (int iter = 0; iter < MAXITERS; iter++) {
            double lambdaOrig = lambda;
            cosLambda = Math.cos(lambda);
            sinLambda = Math.sin(lambda);
            double t1 = cosU2 * sinLambda;
            double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
            double sinSqSigma = t1 * t1 + t2 * t2; // (14)
            sinSigma = Math.sqrt(sinSqSigma);
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
            sigma = Math.atan2(sinSigma, cosSigma); // (16)
            double sinAlpha = (sinSigma == 0) ? 0.0 :
                    cosU1cosU2 * sinLambda / sinSigma; // (17)
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
            cos2SM = (cosSqAlpha == 0) ? 0.0 :
                    cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha; // (18)
            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
            A = 1 + (uSquared / 16384.0) * // (3)
                    (4096.0 + uSquared *
                            (-768 + uSquared * (320.0 - 175.0 * uSquared)));
            double B = (uSquared / 1024.0) * // (4)
                    (256.0 + uSquared *
                            (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
            double C = (f / 16.0) *
                    cosSqAlpha *
                    (4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
            double cos2SMSq = cos2SM * cos2SM;
            deltaSigma = B * sinSigma * // (6)
                    (cos2SM + (B / 4.0) *
                            (cosSigma * (-1.0 + 2.0 * cos2SMSq) -
                                    (B / 6.0) * cos2SM *
                                            (-3.0 + 4.0 * sinSigma * sinSigma) *
                                            (-3.0 + 4.0 * cos2SMSq)));
            lambda = L +
                    (1.0 - C) * f * sinAlpha *
                            (sigma + C * sinSigma *
                                    (cos2SM + C * cosSigma *
                                            (-1.0 + 2.0 * cos2SM * cos2SM))); // (11)
            double delta = (lambda - lambdaOrig) / lambda;
            if (Math.abs(delta) < 1.0e-12) {
                break;
            }
        }
        float distance = (float) (b * A * (sigma - deltaSigma));
        return distance;
    }
    private double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
        double pk = 180.0/Math.PI;

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
