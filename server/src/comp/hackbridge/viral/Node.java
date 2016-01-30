package comp.hackbridge.viral;

public class Node {
    private static long GLOBAL_ID = 1;
    private final long nodeID;

    private PhysicalState physical_state;
    private AwarenessState awareness_state;

    public Node(PhysicalState physical_s, AwarenessState awareness_s) {
        nodeID = GLOBAL_ID;
        GLOBAL_ID++;
        this.physical_state = physical_s;
        this.awareness_state = awareness_s;
    }

    public long getID() {
        return nodeID;
    }

    public AwarenessState getAwarenessState() {
        return awareness_state;
    }

    public PhysicalState getPhysicalState() {
        return physical_state;
    }
}
