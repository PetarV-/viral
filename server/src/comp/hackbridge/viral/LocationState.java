package comp.hackbridge.viral;

import java.util.ArrayList;
import java.util.HashMap;


public class LocationState {
    private final int MAX_NODES = 1000; // TODO
    private final int array_capacity = 100;
    private final ArrayList<ArrayList<Double>> state; //2D state array
    private HashMap<Long, Node> nodes;  // Map from node ID to Node
    private HashMap<Long, Integer> state_position;  // Map from node ID to array pos

    private int node_ctr = 0;  // Counts nodes to add to position

    private long num_nodes = 0;

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

    /**
     * Called upon addition of a new node.
     */
    public StartMessage OnConnect() {
        double rand_aware = GetRandomNumber(1.0);
        double rand_physical = GetRandomNumber(1.0);

        Node new_node = new Node(
                GetRandomNumber() < INITIAL_INFECTED_PERC ?
                        PhysicalState.INFECTED : PhysicalState.SUSCEPTIBLE,
                GetRandomNumber() < INITIAL_AWARENESS_PERC ?
                        AwarenessState.AWARE : AwarenessState.UNAWARE);
        nodes.put(new_node.getID(), new_node);
        state_position.put(new_node.getID(), node_ctr);
        node_ctr++;



        // TODO: random factor for susceptible, infected
        return new StartMessage(new_node.getID(),
                new_node.getPhysicalState(), new_node.getAwarenessState());
    }

    /**
     * Called upon connection of an existing node.
     * @param id
     * @return
     */
    public StartMessage OnConnect(long id) {

        if (num_nodes > array_capacity) {

        }
        System.out.println("Connected");
        return null;
    }

    /**
     * Called upon disconnection of an existing node.
     */
    public boolean OnDisconnect(long id) {
        System.out.println("Disonnected");
        return false;
    }

    /**
     * @param nodeID
     */
    public void OnLocationChange(long nodeID) {
        System.out.println("Location Changed");
    }

}
