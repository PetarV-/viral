package com.hackbridge.viral;

import java.util.HashMap;
import java.util.Map;

/**
 * The EdgeSelector randomly selects an edge using a method to be overridden by implementing
 * classes.
 */
public abstract class EdgeSelector {

    protected final Map<Long, Node> nodes;  // Map from node ID to Node
    protected final NetworkParameters parameters;
    protected Map<Integer, Node> posToNode; // Maps node index from nodeCtr to node
    protected int nodeCtr = 0;

    EdgeSelector(Map<Long, Node> nodes, NetworkParameters parameters) {
        this.nodes = nodes;
        this.parameters = parameters;
        posToNode = new HashMap<Integer, Node>();
    }

    /**
     * Adds a new node.
     *
     * @param node
     */
    public void addNode(Node node) {
        posToNode.put(nodeCtr, node);
        nodeCtr++;
    }

    /**
     * Action taken when nodeID's location has been updated.
     * Does nothing unless overridden.
     *
     * @param nodeID
     */
    public void updateDistances(long nodeID) {

    }

    /**
     * Action taken to reset the selector.
     * Does nothing unless overridden.
     */
    public void reset() {
    }

    /**
     * Select a random edge.
     *
     * @return
     */
    public abstract Edge selectRandomEdge();
}
