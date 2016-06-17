package com.hackbridge.viral;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExactRandomEdgeSelector extends EdgeSelector {

    private boolean DEBUG = false;

    private List<ArrayList<Double>> state; // 2D state array, modify only through setArrayDistance
    private List<Double> distanceSums;  // Internal calculation: sum of a row of stat0e
    private double distanceTotal = 0;  // Total distances (= 2 * value of all bidirectional edges)
    private Map<Long, Integer> nodeToMatrixPos;  // Map from node ID to row index in state matrix
    private int nodeCtr = 0;  // Counts the number of nodes: equivalent to the next available row index in the state matrix
    private int arrayCapacity = 512;

    public ExactRandomEdgeSelector(Map<Long, Node> nodes, NetworkParameters parameters) {
        super(nodes, parameters);
        state = new ArrayList<ArrayList<Double>>();
        nodeToMatrixPos = new HashMap<Long, Integer>();
        distanceSums = new ArrayList<Double>();
        for (int i = 0; i < arrayCapacity; ++i) {
            ArrayList<Double> new_array = new ArrayList<Double>();
            distanceSums.add(0.0);
            for (int j = 0; j < arrayCapacity; ++j) {
                new_array.add(0.0);
            }
            state.add(new_array);
        }
    }

    @Override
    public void addNode(Node node) {
        nodeToMatrixPos.put(node.getID(), nodeCtr);
        posToNode.put(nodeCtr, node);

        if (nodeCtr >= arrayCapacity) {
            increaseStateArrayCapacity();
        }

        nodeCtr++;
    }

    /**
     * Recompute the distances from the node's neighbouring nodes.
     *
     * @param nodeID
     */
    @Override
    public void updateDistances(long nodeID) {
        Node thisNode = nodes.get(nodeID);
        if (thisNode == null) {
            Logger.logError(3, "Failed to recompute distances. Node id " + nodeID + " does not " +
                    "exist.");
            return;
        }

        int matrixPos = nodeToMatrixPos.get(nodeID);

        for (int i = 0; i < nodeCtr; ++i) {
            if (i == matrixPos) {
                continue;
            }
            Node node = posToNode.get(i);
            if (!node.isActive()) {
                continue;
            }
            double distance =
                    parameters.getExponentialMultiplier() * Math.exp(
                            -parameters.getLambdaFactor() * thisNode.getDistanceFrom(node));


            /*
            debugPrint("Actual distance: " + thisNode.getDistanceFrom(node) + ", Exponentiated: " +
                    distance);
            */
            setArrayDistance(matrixPos, i, distance);
        }

        if (DEBUG) {
            outputArray();
        }
    }

    @Override
    public void reset() {
        arrayCapacity = 512;
        state.clear();
        distanceSums.clear();
        distanceTotal = 0;

        for (int i = 0; i < arrayCapacity; ++i) {
            ArrayList<Double> newRow = new ArrayList<Double>();
            distanceSums.add(0.0);
            for (int j = 0; j < arrayCapacity; ++j) {
                newRow.add(0.0);
            }
            state.add(newRow);
        }
    }

    /**
     * Selects an edge at random with probability directly and exactly
     * proportional to its distance.
     *
     * @return
     */
    public Edge selectRandomEdge() {
        if (distanceTotal <= 0 || nodeCtr <= 0) {
            return null;
        }

        // Pick an edge based on a random number and the accumulative normalized edge distances.
        double rand = Math.random();

        // Tracks the accumulate distance at the current row/column in the state matrix.
        double totalDistSoFar = 0.0;
        int i = 0, j = 0;

        // Add row distances.
        while (totalDistSoFar + distanceSums.get(i) <= rand * distanceTotal && i < nodeCtr) {
            totalDistSoFar += distanceSums.get(i);
            i++;
        }

        // Look at column distances.
        while (totalDistSoFar + state.get(i).get(j) <= rand * distanceTotal && j < nodeCtr) {
            totalDistSoFar += state.get(i).get(j);
            j++;
        }

        if (DEBUG) {
            System.out.format("Ack edge. Rand: %.5f total: %.5f Nodes: %d %d\n",
                    rand * distanceTotal, totalDistSoFar, i, j);
            outputNodes();
            System.out.format("Edge %d <-> %d activated.\n", i, j);
        }

        if (i == j) {
            return null;
        }

        Node ni = posToNode.get(i);
        Node nj = posToNode.get(j);

        if (ni == null || nj == null) {
            Logger.logError(3, "Tried to active null nodes " + i + " " + j);
            return null;
        }
        return new Edge(ni, nj);
    }

    public Map<Long, Integer> getNodeToMatrixPos() {
        return nodeToMatrixPos;
    }

    public List<ArrayList<Double>> getState() {
        return state;
    }

    private void increaseStateArrayCapacity() {
        int new_capacity = arrayCapacity * 2;
        for (int i = 0; i < arrayCapacity; ++i) {
            for (int j = arrayCapacity; j < new_capacity; ++j) {
                state.get(i).add(0.0);
            }
        }

        for (int i = arrayCapacity; i < new_capacity; ++i) {
            ArrayList<Double> new_array = new ArrayList<Double>();
            distanceSums.add(0.0);
            for (int j = 0; j < new_capacity; ++j) {
                new_array.add(0.0);
            }
            state.add(new_array);
        }
        arrayCapacity = new_capacity;
    }

    /**
     * Sets the distance between nodes i and j to dist. Note that i and j refer to internal
     * indices of the state matrix and do not necessarily correspond to the node's identifiers.
     * <p>
     * The state matrix should only be modified through this method.
     *
     * @param i
     * @param j
     * @param dist
     */
    private void setArrayDistance(int i, int j, double dist) {
        try {
            distanceSums.set(i, distanceSums.get(i) + dist - state.get(i).get(j));
            distanceTotal += dist - state.get(i).get(j);
            state.get(i).set(j, dist);

            distanceSums.set(j, distanceSums.get(j) + dist - state.get(j).get(i));
            distanceTotal += dist - state.get(j).get(i);
            state.get(j).set(i, dist);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /**
     * Output the current distance array, including inactive nodes.
     */
    private void outputArray() {
        Logger.log(3, "===============\nOutputting current stage");
        for (int i = 0; i < nodeCtr; ++i) {
            String line = "";
            for (int j = 0; j < nodeCtr; ++j) {
                line += String.format("%.5f\t", state.get(i).get(j));
            }

            line += "\t | \t RowDistance: " + String.format("%.5f", distanceSums.get(i));
            Logger.log(3, line);
        }
        Logger.log(3, String.format("Total distance: %.5f", distanceTotal));
        Logger.log(3, "====================");
    }

    private void outputNodes() {
        for (int i = 0; i < nodeCtr; ++i) {
            System.out.println(posToNode.get(i));
        }
    }
}
