package comp.hackbridge.viral;

import java.util.ArrayList;
import java.util.HashMap;


public class LocationState {
    private final int MAX_NODES = 1000; // TODO
    private final int array_capacity = 100;
    private final ArrayList<ArrayList<Double>> state; //2D state array
    private HashMap<Long, Node> nodes;  // Map from node ID to Node
    private HashMap<Long, Integer> state_position;  // Map from node ID to array pos

    private int num_nodes = 0;

    public LocationState() {
        state = new ArrayList<ArrayList<Double>>();
        nodes = new HashMap<Long, Node>();
        state_position = new HashMap<Long, Integer>();
        for (int i = 0; i < array_capacity; ++i) {
            state.add(new ArrayList<Double>());
        }
    }

    /**
     * Called upon addition of a new node.
     */
    public StartMessage OnConnect() {
        return null;
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
