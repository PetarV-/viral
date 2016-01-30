package com.hackbridge.viral;

/**
 * Created by stella on 1/30/16.
 */
public class LocationStateTest {
    public LocationStateTest() {
        connectionTest();
        locationChangeTest();
    }

    private void connectionTest() {
        System.out.println("Running connection test");
        LocationState location_state = new LocationState();
        StartMessage start_msg = location_state.onConnect();
        location_state.onDisconnect(start_msg.getId());
        location_state.onDisconnect(0);
        location_state.onConnect(0);
    }

    private void locationChangeTest() {
        System.out.println("Running location change test");
        LocationState location_state = new LocationState();
        StartMessage start1 = location_state.onConnect();
        StartMessage start2 = location_state.onConnect();
        StartMessage start3 = location_state.onConnect();
        //location_state.onLocationChange(start1.getId(), 10.0000000, 15.000000);
        //location_state.onLocationChange(start2.getId(), 10.000001, 15.0000001);
        //location_state.onLocationChange(start3.getId(), 10.000000, 15.1);
    }

    private void edgeTest() {

    }

}
