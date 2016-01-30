package com.hackbridge.viral;

/**
 * Created by stella on 1/30/16.
 */
public class LocationStateTest {
    public LocationStateTest() {
        // TODO: add asserts
        LocationState location_state = new LocationState();
        StartMessage start_msg = location_state.OnConnect();
        location_state.OnDisconnect(start_msg.getId());
        location_state.OnDisconnect(0);
        location_state.OnConnect(0);
        location_state.OnConnect(1);

    }

}
