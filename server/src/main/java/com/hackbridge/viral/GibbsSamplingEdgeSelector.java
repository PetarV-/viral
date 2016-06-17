package com.hackbridge.viral;

import java.util.Map;

/**
 * This class selects an edge using Gibbs sampling as follows:
 * 1. Initialize with an arbitrary node (x, y).
 * 2. Choose an (x', y') uniformly at random such that either x=x' or y=y'.
 * 3. If distance d(x',y') <= d(x,y), accept.
 * 4. Otherwise accept with probability d(x,y) / d(x',y').
 * 5. Repeat 2-4 for N steps, where N is specified in parameters.
 * 6. Output (x,y).
 */
public class GibbsSamplingEdgeSelector extends EdgeSelector {

    GibbsSamplingEdgeSelector(Map<Long, Node> nodes, NetworkParameters parameters) {
        super(nodes, parameters);
    }

    public Edge selectRandomEdge() {
        if (nodeCtr < 2) {
            return null;
        }

        int x1 = 0;
        int y1 = 1;
        Node curX = posToNode.get(x1);
        Node curY = posToNode.get(y1);

        double prevDistance = curX.getDistanceFrom(curY);
        for (int i = 0; i < parameters.getNumStepsForGibbsSampling(); i++) {
            int x2, y2;
            if (Math.random() < 0.50) {
                // (x,y) -> (x',y)
                x2 = getRandomNode();
                y2 = y1;
            } else {
                // (x,y) -> (x, y')
                x2 = x1;
                y2 = getRandomNode();
            }

            if (x2 == y2) continue;

            Node nextX = posToNode.get(x2);
            Node nextY = posToNode.get(y2);
            if (nextX == null || nextY == null) {
                continue;
            }
            double nextDistance = nextX.getDistanceFrom(nextY);
            if (nextDistance <= prevDistance) {
                curX = nextX;
                curY = nextY;
            } else {
                double p = prevDistance / nextDistance;
                if (Math.random() <= p) {
                    curX = nextX;
                    curY = nextY;
                }
            }
        }
        return new Edge(curX, curY);
    }

    private int getRandomNode() {
        return (int) Math.floor(Math.random() * (nodeCtr));
    }
}
