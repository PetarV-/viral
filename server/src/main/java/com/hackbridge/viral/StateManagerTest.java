package com.hackbridge.viral;

/**
 * Created by stella on 1/30/16.
 */
public class StateManagerTest {
    public StateManagerTest() {
        //  connectionTest();
        // locationChangeTest();
        // locationValueTest();
         stepTest();
        // resetTest();
    }

    private void connectionTest() {
        System.out.println("Running connection test");
        StateManager stateManager = new StateManager();
        StartMessage start_msg = stateManager.onConnect();
        stateManager.onDisconnect(start_msg.getId());
        stateManager.onDisconnect(0);
        stateManager.onConnect(0);
    }

    private void locationValueTest() {
        System.out.println("Running location value test");
        StateManager stateManager = new StateManager();
        StartMessage start1 = stateManager.onConnect();
        StartMessage start2 = stateManager.onConnect();
        StartMessage start3 = stateManager.onConnect();
        stateManager.onLocationChange(start1.getId(), new LocationWrapper(52.2008, 00.1198373,0.0));
        stateManager.onLocationChange(start2.getId(), new LocationWrapper(52.2040, 0.1198373, 0.0));
        stateManager.onLocationChange(start2.getId(), new LocationWrapper(52.2040, 0.1198373, 0.0));
        stateManager.onLocationChange(start2.getId(), new LocationWrapper(52.2040, 0.1198373, 0.0));
        stateManager.onLocationChange(start2.getId(), new LocationWrapper(52.2040, 0.1198373, 0.0));

    }

    private void locationChangeTest() {
        System.out.println("Running location change test");
        StateManager stateManager = new StateManager();
        StartMessage start1 = stateManager.onConnect();
        StartMessage start2 = stateManager.onConnect();
        StartMessage start3 = stateManager.onConnect();
        stateManager.onLocationChange(start1.getId(), new LocationWrapper(10.0000000, 15.000000, 0.0));
        stateManager.onLocationChange(start2.getId(), new LocationWrapper(10.000001, 15.0000001, 0.0));
        stateManager.onLocationChange(start3.getId(), new LocationWrapper(10.000000, 15.1, 0.0));
    }

    private void stepTest() {
        System.out.println("Running location step test");
        StateManager stateManager = new StateManager();
        int nMsgs = 10;
        StartMessage sms[] = new StartMessage[nMsgs];
        for (int i = 0; i < nMsgs; ++i) {
            sms[i] = stateManager.onConnect();
        }

        for (int i = 0; i < 100; ++i) {
            int rand = (int) Math.floor(Math.random() * nMsgs);
            stateManager.onLocationChange(sms[rand].getId(), new LocationWrapper(10.000000, 15.1, 0.0));
            try {
                System.in.read();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Percentage infected: " + stateManager.getPercentageInfected());
        }
    }

    private void resetTest() {
        System.out.println("Running reset test");
        StateManager stateManager = new StateManager();
        StartMessage start_msg = stateManager.onConnect();
        stateManager.onConnect(start_msg.getId());
        stateManager.reset();
        StartMessage sm2 = stateManager.onConnect(start_msg.getId());
        System.out.println(sm2.getId());
    }

    public static void main(String[] args) {
        new StateManagerTest();
    }

}
