package com.hackbridge.viral;

import java.util.ArrayList;
import java.util.HashMap;

public final class LocationState {
    private final boolean DEBUG = true;

    private int array_capacity = 512;
    private final ArrayList<ArrayList<Double>> state; // 2D state array, modify only through setArrayDistance
    private ArrayList<Double> distance_sums;  // Internal calculation: sum of a row of state
    private HashMap<Long, Node> nodes;  // Map from node ID to Node
    private HashMap<Long, Integer> state_position;  // Map from node ID to array pos
    private HashMap<Integer, Node> position_to_node; // Maps position in state array to node

    private double distance_total = 0;  // Total distances (= 2 * value of all biredirectional edges)
    private int node_ctr = 0;  // Counts nodes to add to position

    // TODO: tune parameters.
    private final double INITIAL_INFECTED_PROB = DEBUG ? 0.30 : 0.20;
    private final double INITIAL_AWARENESS_PROB = DEBUG ? 0.50 : 0.10;
    private final double INFECTED_IF_VACCINATED_PROB = DEBUG ? 0.10 : 0.01;
    private final double ACTIVATE_EDGE_PROB = DEBUG ? 1.0 : 0.05;
    private final double LAMBDA_FACTOR = 0.15;

    public LocationState() {
        state = new ArrayList<ArrayList<Double>>();
        nodes = new HashMap<Long, Node>();
        state_position = new HashMap<Long, Integer>();
        position_to_node = new HashMap<Integer, Node>();
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
     *  @return StartMessage with PhysicalState and AwarenessState initialized at random.
     */
    public StartMessage onConnect() {
        PhysicalState new_ps =
                getRandomNumber() < INITIAL_INFECTED_PROB ?
                        PhysicalState.INFECTED : PhysicalState.SUSCEPTIBLE;
        AwarenessState new_as;
        if (new_ps == PhysicalState.INFECTED) {
            new_as = getRandomNumber() < INITIAL_AWARENESS_PROB ?
                    AwarenessState.AWARE : AwarenessState.UNAWARE;
        } else {
            new_as = AwarenessState.UNAWARE;
        }

        Node new_node = new Node(new_ps, new_as);

        nodes.put(new_node.getID(), new_node);
        state_position.put(new_node.getID(), node_ctr);
        position_to_node.put(node_ctr, new_node);

        if (node_ctr >= array_capacity) {
            increaseStateArrayCapacity();
        }

        node_ctr++;

        System.out.println("New node connected. " + new_node);

        return new StartMessage(new_node.getID(),
                new_node.getPhysicalState(), new_node.getAwarenessState());
    }

    /**
     * Called upon reconnection of an existing node.
     * @param id - unique node identifier returned when node was first created.
     * @return StartMessage with state, null if node with id did not exist previously.
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
        System.out.println("Disconnected node " + id);
        return true;
    }

    /**
     * Called upon a change in location of a node.
     * Currently, repeated calls to this method will increase the probability of an edge being activated.
     * @param nodeID
     */
    public void onLocationChange(long nodeID, LocationWrapper location) {
        // Get lat/long from node
        Node node = nodes.get(nodeID);
        if (node == null) {
            // TODO exception
            System.err.println("Error: failed to update location " + nodeID + ". Node does not exist");
            return;
        }
        node.setLocation(location);
        recomputeDistances(nodeID);

        // With some probability, activate edge
        if (getRandomNumber() < ACTIVATE_EDGE_PROB) {
            activateRandomEdge();
        }
    }

    /**
     * Called when a node is vaccinated.
     * @param nodeID
     */
    public boolean onVaccinate(long nodeID) {
        Node node = nodes.get(nodeID);
        if (node == null) {
            // TODO exception
            System.err.println("Error: failed to vaccinate " + nodeID + ". Node does not exist.");
            return false;
        }
        node.setPhysicalState(PhysicalState.VACCINATED);
        Main.changeState(new ChangeMessage(node.getPhysicalState(), node.getAwarenessState()), nodeID);
        return true;
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

        int arrayPos = state_position.get(nodeID);

        for (int i = 0; i < node_ctr; ++i) {
            if (i == arrayPos) {
                continue;
            }
            Node node = position_to_node.get(i);
            if (!node.isActive()) {
                continue;
            }
            double distance = Math.exp(-LAMBDA_FACTOR * thisNode.getDistanceFrom(node));
            setArrayDistance(arrayPos, i, distance);
        }

        if (DEBUG) {
            outputArray();
        }
    }


    /**
     * The state matrix should only be modified through this method.
     * @param i
     * @param j
     * @param dist
     */
    private void setArrayDistance(int i, int j, double dist) {
        try {
            distance_sums.set(i, distance_sums.get(i) + dist - state.get(i).get(j));
            distance_total += dist - state.get(i).get(j);
            state.get(i).set(j, dist);

            distance_sums.set(j, distance_sums.get(j) + dist - state.get(j).get(i));
            distance_total += dist - state.get(j).get(i);
            state.get(j).set(i, dist);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    // Resets all state other than nodeIDs
    public void reset() {
        array_capacity = 512;
        state.clear();
        distance_sums.clear();
        distance_total = 0;

        for (int i = 0; i < array_capacity; ++i) {
            ArrayList<Double> new_array = new ArrayList<Double>();
            distance_sums.add(0.0);
            for (int j = 0; j < array_capacity; ++j) {
                new_array.add(0.0);
            }
            state.add(new_array);
        }

        for (Node node : nodes.values()) {
            PhysicalState new_ps =
                getRandomNumber() < INITIAL_INFECTED_PROB ?
                        PhysicalState.INFECTED : PhysicalState.SUSCEPTIBLE;
            AwarenessState new_as;
            if (new_ps == PhysicalState.INFECTED) {
                new_as = getRandomNumber() < INITIAL_AWARENESS_PROB ?
                        AwarenessState.AWARE : AwarenessState.UNAWARE;
            } else {
                new_as = AwarenessState.UNAWARE;
            }
            node.reset(new_ps, new_as);
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

        if (DEBUG) {
            System.out.format("Ack edge. Rand: %.5f total: %.5f Nodes: %d %d\n", rand*distance_total, total_so_far, i, j);
            outputNodes();
            System.out.format("Edge %d <-> %d activated.\n", i, j);
        }
        activateEdge(i, j);
    }

    private void outputNodes() {
        for (int i = 0; i < node_ctr; ++i) {
            System.out.println(position_to_node.get(i));
        }
    }

    /**
     * Changes the state of the input node to infected, setting awareness based on INITIAL_AWARENESS_PROB.
     * This method calls Main.changeState.
     * @param node
     */
    private void infectNode(Node node) {
        System.out.format("Node %d is now infected.", node.getID());
        node.setPhysicalState(PhysicalState.INFECTED);
        AwarenessState new_as = getRandomNumber() < INITIAL_AWARENESS_PROB ?
                AwarenessState.AWARE : AwarenessState.UNAWARE;
        node.setAwarenessState(new_as);
        Main.changeState(new ChangeMessage(PhysicalState.INFECTED, new_as), node.getID());
    }

    /**
     * Edge i-j is activated. If i is INFECTED and j is SUSCEPTIBLE, then j is INFECTED (and vice versa).
     * Or if is is INFECTED and j is VACCINATED, then j is INFECTED with probability INFECTED_IF_VACCINATED_PROB.
     *
     * This method calls Main.changeState if a node changes state.
     * @param i
     * @param j
     */
    private void activateEdge(int i, int j) {
        try {
            Node ni = position_to_node.get(i);
            Node nj = position_to_node.get(j);
            if (!ni.isActive() || !nj.isActive()) {
                return;
            }

            if (ni.getPhysicalState() == PhysicalState.INFECTED) {
                if ((nj.getPhysicalState() == PhysicalState.SUSCEPTIBLE) ||
                        ((nj.getPhysicalState() == PhysicalState.VACCINATED) &&
                                getRandomNumber() < INFECTED_IF_VACCINATED_PROB)) {
                    infectNode(nj);
                }
            } else if (nj.getPhysicalState() == PhysicalState.INFECTED) {
                if ((ni.getPhysicalState() == PhysicalState.SUSCEPTIBLE) ||
                        ((ni.getPhysicalState() == PhysicalState.VACCINATED) &&
                                getRandomNumber() < INFECTED_IF_VACCINATED_PROB)) {
                    infectNode(ni);
                }
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
        int new_capacity = array_capacity*2;
        for (int i = 0; i < array_capacity; ++i) {
            for (int j = array_capacity; j < new_capacity; ++j) {
                state.get(i).add(0.0);
            }
        }

        for (int i = array_capacity; i < new_capacity; ++i) {
            ArrayList<Double> new_array = new ArrayList<Double>();
            distance_sums.add(0.0);
            for (int j = 0; j < new_capacity; ++j) {
               new_array.add(0.0);
            }
            state.add(new_array);
        }
        array_capacity = new_capacity;
    }
}