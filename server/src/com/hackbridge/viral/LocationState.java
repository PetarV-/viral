package com.hackbridge.viral;

import java.util.ArrayList;
import java.util.HashMap;


public class LocationState {
    private final int MAX_NODES = 1000; // TODO
    private final int array_capacity = 400;
    private final ArrayList<ArrayList<Double>> state; //2D state array
    private HashMap<Long, Node> nodes;  // Map from node ID to Node
    private HashMap<Long, Integer> state_position;  // Map from node ID to array pos
    private HashMap<Integer, Long> arrayPos_to_node; // (Bad): maps position in state array to nodeID

    private int node_ctr = 0;  // Counts nodes to add to position
    private long num_connected_nodes = 0; // Number of active nodes; will probably be deprecated.

    // TODO: move to separate class perhaps.
    // TODO: tune parameters.
    private final double INITIAL_INFECTED_PROB = 0.05;
    private final double INITIAL_AWARENESS_PROB = 0.05;
    private final double ACTIVATE_EDGE_PROB = 0.2;

    public LocationState() {
        state = new ArrayList<ArrayList<Double>>();
        nodes = new HashMap<Long, Node>();
        state_position = new HashMap<Long, Integer>();
        arrayPos_to_node = new HashMap<Integer, Long>();
        for (int i = 0; i < array_capacity; ++i) {
            ArrayList<Double> new_array = new ArrayList<Double>();
            for (int j = 0; j < array_capacity; ++j) {
                new_array.add(0.0);
            }
            state.add(new_array);
        }
    }

    /** Called upon addition of a new node.
     *
     *  @return StartMessage with PhysicalState and AwarenessState initialized at random
     */
    public StartMessage onConnect() {
        Node new_node = new Node(
                getRandomNumber() < INITIAL_INFECTED_PROB ?
                        PhysicalState.INFECTED : PhysicalState.SUSCEPTIBLE,
                getRandomNumber() < INITIAL_AWARENESS_PROB ?
                        AwarenessState.AWARE : AwarenessState.UNAWARE);

        nodes.put(new_node.getID(), new_node);
        state_position.put(new_node.getID(), node_ctr);
        arrayPos_to_node.put(node_ctr, new_node.getID());

        if (node_ctr >= array_capacity) {
            increaseStateArrayCapacity();
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
    public StartMessage onConnect(long id) {
        Node node = nodes.get(id);
        if (node == null) {
            System.err.println("Error: failured to reconnect node " + id);
            return null;
        }
        System.out.println("Reconnected " + node);
        return new StartMessage(
                node.getID(), node.getPhysicalState(), node.getAwarenessState());
    }

    /**
     * Called upon disconnection of an existing node.
     * @return True if suscessfull
     */
    public boolean onDisconnect(long id) {
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
     * TODO: argument should be location object
     * @param nodeID
     */
    public void onLocationChange(long nodeID, double lat, double lon) {
        // Get lat/long from node
        Node node = nodes.get(nodeID);
        if (node == null) {
            // TODO exception
            System.err.println("Error: failed to update location " + node + ". Node does not exist");
            return;
        }
        node.setLocation(lat, lon);
        recomputeDistances(nodeID);

        // With some probability, activate edge
        if (getRandomNumber() < ACTIVATE_EDGE_PROB) {
            activateRandomEdge();
        }
    }

    /**
     * Recompute distance for this nodeID
     * @param nodeID
     */
    private void recomputeDistances(long nodeID) {
        Node thisNode = nodes.get(nodeID);
        if (thisNode == null) {
            System.err.println("Node id " + nodeID + " does not exist.");
            return;
        }
        if (!thisNode.isActive()) {
            System.err.println("Node id " + nodeID + " is not active.");
            return;
        }
        int arrayPos = state_position.get(nodeID);

        for (int i = 0; i < node_ctr; ++i) {
            if (i == arrayPos) {
                continue;
            }
            Node node = nodes.get(arrayPos_to_node.get(i));
            if (!node.isActive()) {
                continue;
            }
            double distance = thisNode.getDistanceFrom(node);
            setArrayDistance(arrayPos, i, distance);

            //System.out.println("Cur node: " + thisNode + " " + "ONode: " + node + " Dist: " + distance);
        }

        System.out.println("Outputting current state:");
        outputArray();
    }

    private void setArrayDistance(int i, int j, double dist) {
        try {
            state.get(i).set(j, dist);
            state.get(j).set(i, dist);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /**
     * Output the current distance array, includes inactive nodes.
     */
    private void outputArray() {
        for (int i = 0; i < node_ctr; ++i) {
            String line = "";
            for (int j = 0; j < node_ctr; ++j) {
                line += String.format("%.5f\t", state.get(i).get(j));
            }
            System.out.println(line);
        }
    }

    private void activateRandomEdge() {
        // TODO
    }

    /**
     * Default: (0.0, 1.0)
     * @return
     */
    private double getRandomNumber(double max) {
        return Math.random() * max;
    }

    private double getRandomNumber() {
        return Math.random();
    }

    private void increaseStateArrayCapacity() {
        // TODO
    }


}
