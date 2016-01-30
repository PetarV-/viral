package com.hackbridge.viral;

import java.util.ArrayList;
import java.util.HashMap;


public class LocationState {
    private final boolean DEBUG = true;

    private final int MAX_NODES = 1000; // TODO
    private final int array_capacity = 512;
    private final ArrayList<ArrayList<Double>> state; //2D state array, modify only through method
    private ArrayList<Double> distance_sums;
    private HashMap<Long, Node> nodes;  // Map from node ID to Node
    private HashMap<Long, Integer> state_position;  // Map from node ID to array pos
    private HashMap<Integer, Long> arrayPos_to_node; // (Bad): maps position in state array to nodeID

    private double distance_total = 0;
    private int node_ctr = 0;  // Counts nodes to add to position
    private long num_connected_nodes = 0; // Number of active nodes; will probably be deprecated.

    // TODO: move to separate class perhaps.
    // TODO: tune parameters.
    private final double INITIAL_INFECTED_PROB = 0.05;
    private final double INITIAL_AWARENESS_PROB = 0.05;
    private final double ACTIVATE_EDGE_PROB = 1.0;

    public LocationState() {
        state = new ArrayList<ArrayList<Double>>();
        nodes = new HashMap<Long, Node>();
        state_position = new HashMap<Long, Integer>();
        arrayPos_to_node = new HashMap<Integer, Long>();
        distance_sums = new ArrayList<Double>();
        for (int i = 0; i < array_capacity; ++i) {
            ArrayList<Double> new_array = new ArrayList<Double>();
            distance_sums.add(0.0);
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
     * TODO: argument should be PositionMessage
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
     * Recompute distance for this nodeID, c
     * @param nodeID
     */
    private void recomputeDistances(long nodeID) {
        Node thisNode = nodes.get(nodeID);
        if (thisNode == null) {
            System.err.println("Node id " + nodeID + " does not exist.");
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

        if (DEBUG) {
            outputArray();
        }
    }

    /**
     * Array should only be set through this method.
     * @param i
     * @param j
     * @param dist
     */
    private void setArrayDistance(int i, int j, double dist) {
        try {
            distance_sums.set(i, distance_sums.get(i) + dist - state.get(i).get(j));
            distance_total += dist - state.get(i).get(j);
            state.get(i).set(j, dist);

            distance_total += dist - state.get(j).get(i);
            distance_sums.set(j, distance_sums.get(j) + dist - state.get(j).get(j));
            state.get(j).set(i, dist);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /**
     * Output the current distance array, includes inactive nodes (TODO)
     */
    private void outputArray() {
        System.out.println("===============\nOuputting current stage");
        for (int i = 0; i < node_ctr; ++i) {
            String line = "";
            for (int j = 0; j < node_ctr; ++j) {
                line += String.format("%.5f\t", state.get(i).get(j));
            }

            line += "\t | \t RowDistance: " + String.format("%.5f", distance_sums.get(i));
            System.out.println(line);
        }
        System.out.format("Total distance: %.5f\n", distance_total);
        System.out.println("====================");
    }

    private void activateRandomEdge() {
        if (distance_total <= 0 || node_ctr <= 0) {
            // TODO
            return;
        }
        double rand = getRandomNumber();
        double total_so_far = 0.0;
        int i = 0, j = 0;

        // Let's pretend we're being efficient...
        while (total_so_far + distance_sums.get(i) <= rand*distance_total && i < node_ctr) {
            total_so_far += distance_sums.get(i);
            i++;
        }

        while (total_so_far + state.get(i).get(j) <= rand*distance_total && j < node_ctr) {
            total_so_far += state.get(i).get(j);
            j++;
        }
        System.out.format("EdgeL %d <-> %d activated.\n", i, j);

        if (DEBUG) {
            System.out.format("Ack edge. Rand: %.5f total: %.5f Nodes: %d %d", rand*distance_total, total_so_far, i, j);
        }

        // Check if action needs to be taken, ie one node is healthy and one isn't.

    }

    private void activateEdge(int i, int j) {
        try {
            Node ni = nodes.get(arrayPos_to_node.get(i));
            Node nj = nodes.get(arrayPos_to_node.get(j));
            if (ni.getPhysicalState() == PhysicalState.INFECTED &&
                    nj.getPhysicalState() == PhysicalState.SUSCEPTIBLE) {
                // TODO: update awareness state?
                Main.changeState(new ChangeMessage(PhysicalState.INFECTED, nj.getAwarenessState()), nj.getID());
            } else if (ni.getPhysicalState() == PhysicalState.SUSCEPTIBLE &&
                    nj.getPhysicalState() == PhysicalState.INFECTED) {
                Main.changeState(new ChangeMessage(PhysicalState.INFECTED, ni.getAwarenessState()), ni.getID());
            }
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }
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
