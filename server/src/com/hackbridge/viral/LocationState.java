package com.hackbridge.viral;

import java.util.ArrayList;
import java.util.HashMap;


public class LocationState {
    private final int MAX_NODES = 1000; // TODO
    private final int array_capacity = 400;
    private final ArrayList<ArrayList<Double>> state; //2D state array
    private HashMap<Long, Node> nodes;  // Map from node ID to Node
    private HashMap<Long, Integer> state_position;  // Map from node ID to array pos

    private int node_ctr = 0;  // Counts nodes to add to position

    private long num_connected_nodes = 0; // Number of active nodes

    // TODO: move to separate class perhaps.
    // TODO: tune parameters.
    private final double INITIAL_INFECTED_PERC = 0.05;
    private final double INITIAL_AWARENESS_PERC = 0.05;


    public LocationState() {
        state = new ArrayList<ArrayList<Double>>();
        nodes = new HashMap<Long, Node>();
        state_position = new HashMap<Long, Integer>();
        for (int i = 0; i < array_capacity; ++i) {
            state.add(new ArrayList<Double>());
        }
    }

    /**
     * Default: (0.0, 1.0)
     * @return
     */
    private double GetRandomNumber(double max) {
        return Math.random() * max;
    }

    private double GetRandomNumber() {
        return Math.random();
    }

    private void IncreaseStateArrayCapacity() {
        // TODO
    }

    /**
     * Called upon addition of a new node.
     * @return StartMessage with PhysicalState and AwarenessState initialized at random
     */
    public StartMessage OnConnect() {
        Node new_node = new Node(
                GetRandomNumber() < INITIAL_INFECTED_PERC ?
                        PhysicalState.INFECTED : PhysicalState.SUSCEPTIBLE,
                GetRandomNumber() < INITIAL_AWARENESS_PERC ?
                        AwarenessState.AWARE : AwarenessState.UNAWARE);

        nodes.put(new_node.getID(), new_node);
        state_position.put(new_node.getID(), node_ctr);

        if (node_ctr >= array_capacity) {
            IncreaseStateArrayCapacity();
        }

        node_ctr++;
        num_connected_nodes++; // TODO: starts counting in calculations even when no location exists

        System.out.println("New node connected. " + new_node);

        return new StartMessage(new_node.getID(),
                new_node.getPhysicalState(), new_node.getAwarenessState());
    }

    /**
     * Called upon connection of an existing node.
     * @param id
     * @return
     */
    public StartMessage OnConnect(long id) {
        Node node = nodes.get(id);
        if (node == null) {
            System.err.println("Error: failured to reconnect node " + id);
        }

        System.out.println("Reconnected " + id);
        return null;
    }

    /**
     * Called upon disconnection of an existing node.
     * @return True if suscessfull
     */
    public boolean OnDisconnect(long id) {
        Node node = nodes.get(id);
        if (node == null) {
            // TODO exception
            System.err.println("Error: failed to disconnect " + id);
            return false;
        }
        node.setConnected(false);
        num_connected_nodes--;
        System.out.println("Disconnected node " + id);
        return true;
    }

    /**
     * @param nodeID
     */
    public void OnLocationChange(long nodeID) {
        System.out.println("Location Changed");
    }

}
