package com.hackbridge.viral;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public final class StateManager {
    private final boolean DEBUG = false;
    private final boolean LOG = true;

    // The configurable parameters of the multiplex network.
    private final NetworkParameters parameters;

    // Fields for maintaining the internal network state.
    private final List<ArrayList<Double>> state; // 2D state array, modify only through setArrayDistance
    private List<Double> distanceSums;  // Internal calculation: sum of a row of state
    private Map<Long, Node> nodes;  // Map from node ID to Node
    private Map<Long, Integer> nodeToMatrixPos;  // Map from node ID to row index in state matrix
    private Map<Integer, Node> matrixPosToNode; // Maps position in state matrix to node
    private double distanceTotal = 0;  // Total distances (= 2 * value of all bidirectional edges)
    private int nodeCtr = 0;  // Counts the number of nodes: equivalent to the next available row index in the state matrix
    private int arrayCapacity = 512;
    private boolean runTikzer;

    // Fields for logging.
    private Tikzer tikzer = null;
    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private String logfileName;

    public StateManager() {
        this(new NetworkParameters(), false, 0);
    }

    public StateManager(NetworkParameters parameters) {
        this(parameters, false, 0);
    }

    public StateManager(NetworkParameters parameters, boolean runTikzer, int tikzerPort) {
        this.parameters = parameters;
        Date today = Calendar.getInstance().getTime();
        logfileName = dateFormat.format(today) + ".log";

        this.runTikzer = runTikzer;

        if (runTikzer) {
            tikzer = new Tikzer(tikzerPort);
        }

        state = new ArrayList<ArrayList<Double>>();
        nodes = new HashMap<Long, Node>();
        nodeToMatrixPos = new HashMap<Long, Integer>();
        matrixPosToNode = new HashMap<Integer, Node>();
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

    /**
     * This method should be called on addition of a new node to the network. If a node is
     * reconnecting to the network, onConnect(long id) should be called instead.
     *
     * A new node in the network is created with its PhysicalState, AwarenessState, and
     * RoleState initialized at random based on StateManager's probability configuration.
     *
     * @return StartMessage with PhysicalState, AwarenessState, and RoleState initialized at
     * random.
     */
    public StartMessage onConnect() {
        PhysicalState physicalState = getRandomPhysicalState();
        AwarenessState awarenessState = getRandomAwarenessState();
        RoleState roleState = getRandomRoleState();

        Node newNode = new Node(physicalState, awarenessState, roleState);

        nodes.put(newNode.getID(), newNode);
        nodeToMatrixPos.put(newNode.getID(), nodeCtr);
        matrixPosToNode.put(nodeCtr, newNode);

        if (nodeCtr >= arrayCapacity) {
            increaseStateArrayCapacity();
        }

        nodeCtr++;

        Logger.log(3, "New node connected. " + newNode);

        return new StartMessage(newNode.getID(),
                newNode.getPhysicalState(), newNode.getAwarenessState(), newNode.getRoleState());
    }

    /**
     * Called upon reconnection of an existing node.
     *
     * This method registers the reconnection and returns a StartMessage with the node's current
     * state.
     *
     * @param id - unique node identifier returned when node was first created.
     * @return StartMessage with state, null if a node with id did not exist previously.
     */
    public StartMessage onConnect(long id) {
        Node node = nodes.get(id);
        if (node == null) {
            Logger.logError(2, "Failed to reconnect node " + id + ".");
            return null;
        }
        Logger.log(3, "Reconnected: " + node);
        return new StartMessage(node.getID(),
                node.getPhysicalState(), node.getAwarenessState(), node.getRoleState());
    }

    /**
     * Called upon disconnection of an existing node.
     *
     * This disconnects the node but preserves the PhysicalState, AwarenessState, and RoleState
     * of the node.
     *
     * @return True if disconnect was successful, false otherwise.
     */
    public boolean onDisconnect(long id) {
        Node node = nodes.get(id);
        if (node == null) {
            Logger.logError(3, "Failed to disconnect node " + id + ".");
            return false;
        }
        node.setConnected(false);
        Logger.log(3, "Disconnected node " + id + ".");
        return true;
    }

    /**
     * Called upon a change in the location of a node.
     *
     * This updates the node's location and recalculates its distance from its neighbouring nodes.
     * The StateManager may then activate an edge based on ACTIVATE_EDGE_PROB.
     *
     * Currently, repeated calls to this method will increase the probability of an edge being
     * activated.
     *
     * @param nodeID
     */
    public void onLocationChange(long nodeID, LocationWrapper location) {
        Node node = nodes.get(nodeID);
        if (node == null) {
            Logger.logError(2, "Failed to update location of node '" + nodeID +
                            "'. The node does not exist.");
            return;
        }
        node.setLocation(location);

        recomputeNeighbouringDistances(nodeID);

        // With probability ACTIVATE_EDGE_PROB, activate a random edge of the graph.
        if (getRandomNumber() < parameters.getActivateEdgeProbability()) {
            activateRandomEdge();
        }

        // If the node is infected with the disease, with probability SPONTANEOUS_RECOVERY_PROB, the
        // node will spontaneously recover from the infection.
        if (node.hasDisease()) {
            if (getRandomNumber() < parameters.getSpontaneousRecoveryProbability()) {
                Logger.log(3, "Node " + node.getID() + " has recovered and is now susceptible.");
                node.setPhysicalState(PhysicalState.SUSCEPTIBLE);
                Main.changeState(new ChangeMessage(
                        node.getPhysicalState(), node.getAwarenessState()), node.getID());
            }
        }

        if (LOG) {
            logState(logfileName);
        }
    }

    /**
     * This method is called when a node is vaccinated.
     *
     * If the node is susceptible, its state changes to vaccinated and the node is now aware of
     * the virus.
     *
     * If the node were previously unaware of the virus, it is now aware.
     *
     * @param nodeID
     * @return True if node was vaccinated, false otherwise.
     */
    public boolean onVaccinate(long nodeID) {
        Node node = nodes.get(nodeID);
        if (node == null) {
            Logger.logError(3, "Failed to vaccinate node '" + nodeID + "'. The node does not " +
                    "exist.");
            return false;
        }
        if (node.getPhysicalState() == PhysicalState.SUSCEPTIBLE) {
            node.setPhysicalState(PhysicalState.VACCINATED);
            node.setAwarenessState(AwarenessState.AWARE);
            Main.changeState(new ChangeMessage(
                    node.getPhysicalState(), AwarenessState.AWARE), nodeID);
            return true;
        }

        if (node.getAwarenessState() != AwarenessState.AWARE) {
            node.setAwarenessState(AwarenessState.AWARE);
            Main.changeState(new ChangeMessage(
                    node.getPhysicalState(), AwarenessState.AWARE), nodeID);
            return false;
        }
        return false;
    }

    /**
     * Returns the RoleState of the node.
     *
     * @param nodeID
     * @return RoleState, or null if the nodeID does not exist.
     */
    public RoleState getRoleState(long nodeID) {
        Node node = nodes.get(nodeID);
        if (node == null) {
            Logger.logError(3, "Failed to get the role state of node '" + nodeID + "'. The node " +
                    "does not exist.");
            return null;
        }
        return node.getRoleState();
    }

    public PhysicalState getPhysicalState(long nodeID) {
        Node node = nodes.get(nodeID);
        if (node == null) {
            Logger.logError(3, "Failed to get the physical state of node '" + nodeID + "'. The " +
                    "node does not exist.");
            return null;
        }
        return node.getPhysicalState();
    }

    /**
     * Returns the percentage of active nodes (those that are connected and have sent at least one
     * location update) that carry the disease agent (PhysicalState.INFECTED or
     * PhysicalState.CARRIER)
     * @return
     */
    public double getPercentageInfected() {
        int numInfectedNodes = 0;
        int totalActiveNodes = 0;
        for (Node node : nodes.values()) {
            if (!node.getConnected()) {
                continue;
            }
            if (node.hasDisease()) {
                numInfectedNodes++;
            }
            totalActiveNodes++;
        }
        if (totalActiveNodes == 0) {
            return 0.0;
        }
        return (double) numInfectedNodes / (double) totalActiveNodes;
    }

    /**
     * Recompute the distances from the node's neighbouring nodes.
     *
     * @param nodeID
     */
    private void recomputeNeighbouringDistances(long nodeID) {
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
            Node node = matrixPosToNode.get(i);
            if (!node.isActive()) {
                continue;
            }
            double distance =
                    parameters.getExponentialMultiplier()*Math.exp(
                            -parameters.getLambdaFactor()* thisNode.getDistanceFrom(node));

            debugPrint("Actual distance: " + thisNode.getDistanceFrom(node) + ", Exponentiated: " +
                    distance);
            setArrayDistance(matrixPos, i, distance);
        }

        if (DEBUG) {
            outputArray();
        }
    }

    private void debugPrint(String s) {
        if (DEBUG) {
            Logger.log(1, s);
        }
    }

    /**
     * Sets the distance between nodes i and j to dist. Note that i and j refer to internal
     * indices of the state matrix and do not necessarily correspond to the node's identifiers.
     *
     * The state matrix should only be modified through this method.
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
     * Resets the network's state by resetting the state matrices and each node's state. The
     * nodes' unique ids are preserved.
     */
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

        for (Node node : nodes.values()) {
            node.reset(getRandomPhysicalState(), getRandomAwarenessState(), getRandomRoleState());
        }
    }

    /**
     * Creates a StateLog and writes the log to filename.
     *
     * This method also adds the log to tikzer.
     * @param filename
     */
    private void logState(String filename) {
        StateLog log = new StateLog(nodes, nodeToMatrixPos, state);
        log.writeToFile(filename, true);
        if (runTikzer) {
            tikzer.addLog(log);
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

    /**
     * Activates a random edge in the network.
     */
    private void activateRandomEdge() {
        if (distanceTotal <= 0 || nodeCtr <= 0) {
            return;
        }

        // Pick an edge based on a random number and the accumulative normalized edge distances.
        double rand = getRandomNumber();

        // Tracks the accumulate distance at the current row/column in the state matrix.
        double totalDistSoFar = 0.0;
        int i = 0, j = 0;

        // Add row distances.
        while (totalDistSoFar + distanceSums.get(i) <= rand * distanceTotal && i < nodeCtr) {
            totalDistSoFar += distanceSums.get(i);
            i++;
        }

        // Look at column distances.
        while (totalDistSoFar + state.get(i).get(j) <= rand* distanceTotal && j < nodeCtr) {
            totalDistSoFar += state.get(i).get(j);
            j++;
        }

        if (DEBUG) {
            System.out.format("Ack edge. Rand: %.5f total: %.5f Nodes: %d %d\n",
                    rand * distanceTotal, totalDistSoFar, i, j);
            outputNodes();
            System.out.format("Edge %d <-> %d activated.\n", i, j);
        }
        activateEdge(i, j);
    }

    /**
     * Activates edge i-j as follows. All operations are commutative. The edge is activated only
     * if both i and j are active nodes.
     *
     * The PhysicalStates (A,B) of i and j change as follows:
     * - (INFECTED/CARRIER, SUSCEPTIBLE) -> (INFECTED/CARRIER, INFECTED/CARRIER)
     * - (INFECTED/CARRIER, VACCINATED) -> (INFECTED/CARRIER, INFECTED/CARRIER) if a random
     * number is greater than INFECTED_IF_VACCINATED_PROB
     *
     * This method calls Main.changeState if a node changes state.
     * @param i
     * @param j
     */
    private void activateEdge(int i, int j) {
        if (i == j) {
            return;
        }
        try {
            Node ni = matrixPosToNode.get(i);
            Node nj = matrixPosToNode.get(j);

            if (ni == null || nj == null) {
                Logger.logError(3, "Tried to active null nodes " + i + " " + j);
                return;
            }

            if (!ni.isActive() || !nj.isActive()) {
                return;
            }

            if (ni.getPhysicalState() == PhysicalState.CARRIER) {
                if (getRandomNumber() < parameters.getDevelopSymptomsProbability()) {
                    ni.setPhysicalState(PhysicalState.INFECTED);
                }
            }

            if (nj.getPhysicalState() == PhysicalState.CARRIER) {
                if (getRandomNumber() < parameters.getDevelopSymptomsProbability()) {
                    nj.setPhysicalState(PhysicalState.INFECTED);
                }
            }

            if (ni.hasDisease()) {
                if ((nj.getPhysicalState() == PhysicalState.SUSCEPTIBLE) ||
                        ((nj.getPhysicalState() == PhysicalState.VACCINATED) &&
                                getRandomNumber() < parameters.getInfectedIfVaccinatedProbability())) {
                    infectNode(nj);
                }
            } else if (nj.hasDisease()) {
                if ((ni.getPhysicalState() == PhysicalState.SUSCEPTIBLE) ||
                        ((ni.getPhysicalState() == PhysicalState.VACCINATED) &&
                                getRandomNumber() < parameters.getInfectedIfVaccinatedProbability())) {
                    infectNode(ni);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void outputNodes() {
        for (int i = 0; i < nodeCtr; ++i) {
            System.out.println(matrixPosToNode.get(i));
        }
    }

    /**
     * Changes the state of the input node to infected, setting awareness based on
     * INITIAL_AWARENESS_PROB.
     *
     * A node can be infected and not aware.
     *
     * This method calls Main.changeState if the node is newly infected.
     * @param node
     */
    private void infectNode(Node node) {
        if (node == null) {
            return;
        }
        if (node.hasDisease()) {
            return;
        }
        Logger.log(3, String.format("Node %d is now infected.", node.getID()));
        node.setPhysicalState(getRandomDiseasedPhysicalState());
        node.setAwarenessState(getRandomAwarenessState());

        Main.changeState(new ChangeMessage(
                node.getPhysicalState(), node.getAwarenessState()), node.getID());
    }

    private double getRandomNumber() {
        return Math.random();
    }

    /**
     * Returns a random PhysicalState based on StateManager's configuration of
     * INITIAL_INFECTED_PROB.
     *
     * @return
     */
    private PhysicalState getRandomPhysicalState() {
        return getRandomNumber() < parameters.getInitialInfectedProbability()?
                getRandomDiseasedPhysicalState() : PhysicalState.SUSCEPTIBLE;
    }

    /**
     * Returns PhysicalState.INFECTED or PhysicalState.CARRIER based on INITIAL_SYMPTOMATIC_PROB.
     * @return
     */
    private PhysicalState getRandomDiseasedPhysicalState() {
        return getRandomNumber() < parameters.getInitialSymptomaticProbability()?
                PhysicalState.INFECTED : PhysicalState.CARRIER;
    }

     /**
     * Returns a random AwarenessState based on StateManager's configuration of
     * INITIAL_AWARENESS_PROB.
     *
     * @return
     */
    private AwarenessState getRandomAwarenessState() {
        return getRandomNumber() < parameters.getInitialAwareProbability()?
                AwarenessState.AWARE : AwarenessState.UNAWARE;
    }

     /**
     * Returns a random RoleState based on StateManager's configuration of EVIL_PROB.
     *
     * @return
     */
    private RoleState getRandomRoleState() {
        return getRandomNumber() < parameters.getInfectorProbability()?
                RoleState.INFECTOR : RoleState.HUMAN;
    }

    private void increaseStateArrayCapacity() {
        int new_capacity = arrayCapacity *2;
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
}