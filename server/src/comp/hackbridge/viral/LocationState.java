package comp.hackbridge.viral;
import java.util.ArrayList;

public class LocationState {
    private final int MAX_NODES = 1000; // TODO
    private final int array_capacity = 100;
    private final ArrayList<ArrayList<Double>> state;

    private int num_nodes = 0;

    public LocationState() {
        state = new ArrayList<ArrayList<Double>>();
        for (int i = 0; i < array_capacity; ++i) {
            state.add(new ArrayList<Double>());
        }
    }

    public void OnConnect() {
        // TODO: add to hashmap of player to num
        // TODO: if new player
        if (num_nodes > array_capacity) {

        }
        System.out.println("Connected");

    }

    public void OnDisconnect() {
        System.out.println("Disonnected");
    }

    // TODO
    public void OnLocationChange() {
        System.out.println("Location Changed");
    }
}
