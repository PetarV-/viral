package com.hackbridge.viral;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by PetarV on 25/03/2016.
 */
public class StateLog {
    private Map<Long, Node> nodes;
    private Map<Long, Integer> positionMap;
    private List<ArrayList<Double>> state;

    public Map<Long, Node> getNodes() {
        return nodes;
    }

    public Map<Long, Integer> getPositionMap() {
        return positionMap;
    }

    public List<ArrayList<Double>> getState() {
        return state;
    }

    public StateLog(Map<Long, Node> nodes, Map<Long, Integer> positionMap, List<ArrayList<Double>> state) {
        this.nodes = new HashMap<Long, Node>();
        this.nodes.putAll(nodes);
        this.positionMap = new HashMap<Long, Integer>();
        this.positionMap.putAll(positionMap);
        this.state = new ArrayList<ArrayList<Double>>();
        int i = 0;
        for (ArrayList<Double> row : state) {
            this.state.add(new ArrayList<Double>());
            this.state.get(i).addAll(row);
            i++;
        }
    }

    /**
     * Logs the current state with the following format:
     *
     * START followed by two integers N, M representing the number of nodes and dimension of the
     * distance matrix. This is followed by N lines in the format "ID mID A P", where ID is the
     * nodeID, mID is the node's index in the distance matrix, A is the awareness state (A for aware,
     * U for unaware), and P is the physical state (I for infected, V for vaccinated, S for susceptible).
     * This is followed by a M*M matrix of node distance floats, followed by END.
     */
    public void writeToFile(String filename) {
        try {
            BufferedWriter logfile = new BufferedWriter(new FileWriter(filename));
            logfile.write("START\n");
            logfile.write(String.format("%d %d\n", nodes.size(), nodes.size()));
            for (Node node : nodes.values()) {
                logfile.write(String.format(
                        "%d %d %s %s\n", node.getID(), positionMap.get(node.getID()),
                        node.getAwarenessState().toString().charAt(0),
                        node.getPhysicalState().toString().charAt(0)));
            }
            for (ArrayList<Double> row : state) {
                String line = "";
                for (Double d : row) {
                    line += String.format("%.7f ", d);
                }
                logfile.write(line + "\n");
            }
            logfile.write("END\n");
            logfile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
