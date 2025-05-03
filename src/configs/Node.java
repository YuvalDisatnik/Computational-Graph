package test;

import graph.Message;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Node {
    private String name;
    private List<Node> edges;
    private Message msg;

    public Node(String name) {
        this.name = name;
        this.edges = new ArrayList<>();
    }

    public Message getMsg() {
        return msg;
    }

    public String getName() {
        return name;
    }

    public List<Node> getEdges() {
        return edges;
    }

    public void setMsg(Message msg) {
        this.msg = msg;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEdges(List<Node> edges) {
        this.edges = edges;
    }

    public void addEdge(Node node) {
        edges.add(node);
    }

    public boolean hasCycles() {
        return hasCyclesHelper(new HashSet<>(), new HashSet<>());
    }

    private boolean hasCyclesHelper(Set<Node> visited, Set<Node> recStack){
        if (recStack.contains(this)){
            return true;
        }
        if (visited.contains(this)){
            return false;
        }
        visited.add(this);
        recStack.add(this);
        for (Node neighbor : edges) {
            if (neighbor.hasCyclesHelper(visited, recStack)){
                return true;
            }
        }
        recStack.remove(this);
        return false;
    }
}