package com.hackbridge.viral;

/**
 * Created by stella on 1/30/16.
 */
public class LocationStateTest {
    public LocationStateTest() {
        //  connectionTest();
        // locationChangeTest();
        locationValueTest();
        //stepTest();
        // resetTest();
    }

    private void connectionTest() {
        System.out.println("Running connection test");
        LocationState location_state = new LocationState();
        StartMessage start_msg = location_state.onConnect();
        location_state.onDisconnect(start_msg.getId());
        location_state.onDisconnect(0);
        location_state.onConnect(0);
    }

    private void locationValueTest() {
        System.out.println("Running location value test");
        LocationState location_state = new LocationState();
        StartMessage start1 = location_state.onConnect();
        StartMessage start2 = location_state.onConnect();
        StartMessage start3 = location_state.onConnect();
        location_state.onLocationChange(start1.getId(), new LocationWrapper(52.2040783, 0.1198373, 0.0));
        location_state.onLocationChange(start2.getId(), new LocationWrapper(52.203878, 0.1203195, 0.0));
        location_state.onLocationChange(start2.getId(), new LocationWrapper(52.203878, 0.1203195, 0.0));
        location_state.onLocationChange(start2.getId(), new LocationWrapper(52.203878, 0.1203195, 0.0));
        location_state.onLocationChange(start2.getId(), new LocationWrapper(52.203878, 0.1203195, 0.0));

    }

    private void locationChangeTest() {
        System.out.println("Running location change test");
        LocationState location_state = new LocationState();
        StartMessage start1 = location_state.onConnect();
        StartMessage start2 = location_state.onConnect();
        StartMessage start3 = location_state.onConnect();
        location_state.onLocationChange(start1.getId(), new LocationWrapper(10.0000000, 15.000000, 0.0));
        location_state.onLocationChange(start2.getId(), new LocationWrapper(10.000001, 15.0000001, 0.0));
        location_state.onLocationChange(start3.getId(), new LocationWrapper(10.000000, 15.1, 0.0));
    }

    private void stepTest() {
        System.out.println("Running location step test");
        LocationState location_state = new LocationState();
        int nMsgs = 10;
        StartMessage sms[] = new StartMessage[nMsgs];
        for (int i = 0; i < 10; ++i) {
            sms[i] = location_state.onConnect();
        }

        for (int i = 0; i < 100; ++i) {
            int rand = (int) Math.floor(Math.random() * nMsgs);
            location_state.onLocationChange(sms[rand].getId(), new LocationWrapper(10.000000, 15.1, 0.0));
            try {
                System.in.read();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void resetTest() {
        System.out.println("Running reset test");
        LocationState location_state = new LocationState();
        StartMessage start_msg = location_state.onConnect();
        location_state.onConnect(start_msg.getId());
        location_state.reset();
        StartMessage sm2 = location_state.onConnect(start_msg.getId());
        System.out.println(sm2.getId());
    }

}
