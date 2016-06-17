package com.hackbridge.viral;

public class Edge {
    private Node a, b;

    Edge(Node a, Node b) {
        this.a = a;
        this.b = b;
    }

    public Node getFirst() {
        return a;
    }

    public Node getSecond() {
        return b;
    }
}
