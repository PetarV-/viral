package com.hackbridge.viral;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class StateManager {
    private final boolean DEBUG = false;
    private final boolean LOG = true;

    // The configurable parameters of the multiplex network.
    private final NetworkParameters parameters;

    private final Map<Long, Node> nodes;  // Map from node ID to Node

    // Selects an edge to activate using algorithm specified by parameters.
    private EdgeSelector edgeSelector;
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

        nodes = new HashMap<Long, Node>();

        if (parameters.getEdgeSelectionAlgorithm() ==
                NetworkParameters.EdgeSelectionAlgorithm.ExactRandom) {
            edgeSelector = new ExactRandomEdgeSelector(nodes, parameters);
        } else {
            edgeSelector = new GibbsSamplingEdgeSelector(nodes, parameters);
        }
    }

    /**
     * This method should be called on addition of a new node to the network. If a node is
     * reconnecting to the network, onConnect(long id) should be called instead.
     * <p>
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
        edgeSelector.addNode(newNode);

        Logger.log(3, "New node connected. " + newNode);

        return new StartMessage(newNode.getID(),
                newNode.getPhysicalState(), newNode.getAwarenessState(), newNode.getRoleState());
    }

    /**
     * Called upon reconnection of an existing node.
     * <p>
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
     * <p>
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
     * <p>
     * This updates the node's location and recalculates its distance from its neighbouring nodes.
     * The StateManager may then activate an edge based on ACTIVATE_EDGE_PROB.
     * <p>
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

        // Recompute neighbouring distances if necessary.
        edgeSelector.updateDistances(nodeID);

        // With probability ACTIVATE_EDGE_PROB, activate a random edge of the graph.
        if (getRandomNumber() < parameters.getActivateEdgeProbability()) {
            Edge edge = edgeSelector.selectRandomEdge();
            if (edge != null) {
                activateEdge(edge.getFirst(), edge.getSecond());
            }
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

        // Log state and generate visualization only if using exact inference.
        if (LOG && parameters.getEdgeSelectionAlgorithm() ==
                NetworkParameters.EdgeSelectionAlgorithm.ExactRandom) {
            logState(logfileName);
        }
    }

    /**
     * This method is called when a node is vaccinated.
     * <p>
     * If the node is susceptible, its state changes to vaccinated and the node is now aware of
     * the virus.
     * <p>
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
     *
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

    private void debugPrint(String s) {
        if (DEBUG) {
            Logger.log(1, s);
        }
    }

    /**
     * Resets the network's state by resetting the state matrices and each node's state. The
     * nodes' unique ids are preserved.
     */
    public void reset() {
        edgeSelector.reset();

        for (Node node : nodes.values()) {
            node.reset(getRandomPhysicalState(), getRandomAwarenessState(), getRandomRoleState());
        }
    }

    /**
     * Creates a StateLog and writes the log to filename.
     * <p>
     * Currently this does something only if using the ExactRandomEdgeSelector.
     * This method also adds the log to tikzer.
     *
     * @param filename
     */
    private void logState(String filename) {
        if (edgeSelector instanceof ExactRandomEdgeSelector) {
            StateLog log = new StateLog(nodes,
                    ((ExactRandomEdgeSelector) edgeSelector).getNodeToMatrixPos(),
                    ((ExactRandomEdgeSelector) edgeSelector).getState());
            log.writeToFile(filename, true);
            if (runTikzer) {
                tikzer.addLog(log);
            }
        }
    }

    /**
     * Activates edge i-j as follows. All operations are commutative. The edge is activated only
     * if both i and j are active nodes.
     * <p>
     * The PhysicalStates (A,B) of i and j change as follows:
     * - (INFECTED/CARRIER, SUSCEPTIBLE) -> (INFECTED/CARRIER, INFECTED/CARRIER)
     * - (INFECTED/CARRIER, VACCINATED) -> (INFECTED/CARRIER, INFECTED/CARRIER) if a random
     * number is greater than INFECTED_IF_VACCINATED_PROB
     * <p>
     * This method calls Main.changeState if a node changes state.
     */
    private void activateEdge(Node ni, Node nj) {
        try {
            if (ni == null || nj == null) {
                Logger.logError(3, "Tried to active null nodes");
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

    /**
     * Changes the state of the input node to infected, setting awareness based on
     * INITIAL_AWARENESS_PROB.
     * <p>
     * A node can be infected and not aware.
     * <p>
     * This method calls Main.changeState if the node is newly infected.
     *
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
        return getRandomNumber() < parameters.getInitialInfectedProbability() ?
                getRandomDiseasedPhysicalState() : PhysicalState.SUSCEPTIBLE;
    }

    /**
     * Returns PhysicalState.INFECTED or PhysicalState.CARRIER based on INITIAL_SYMPTOMATIC_PROB.
     *
     * @return
     */
    private PhysicalState getRandomDiseasedPhysicalState() {
        return getRandomNumber() < parameters.getInitialSymptomaticProbability() ?
                PhysicalState.INFECTED : PhysicalState.CARRIER;
    }

    /**
     * Returns a random AwarenessState based on StateManager's configuration of
     * INITIAL_AWARENESS_PROB.
     *
     * @return
     */
    private AwarenessState getRandomAwarenessState() {
        return getRandomNumber() < parameters.getInitialAwareProbability() ?
                AwarenessState.AWARE : AwarenessState.UNAWARE;
    }

    /**
     * Returns a random RoleState based on StateManager's configuration of EVIL_PROB.
     *
     * @return
     */
    private RoleState getRandomRoleState() {
        return getRandomNumber() < parameters.getInfectorProbability() ?
                RoleState.INFECTOR : RoleState.HUMAN;
    }
}