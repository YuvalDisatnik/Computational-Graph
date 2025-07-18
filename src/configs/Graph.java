package configs;

import java.util.ArrayList;
import java.util.HashMap;

import graph.Agent;
import graph.Topic;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

public class Graph extends ArrayList<Node>{
    
    public boolean hasCycles() {
        for (Node node : this) {
            if (node.hasCycles()) {
                return true;
            }
        }
        return false;
    }

    public void createFromTopics(){
        TopicManager tm = TopicManagerSingleton.get();
        
        HashMap<String, Node> nodes = new HashMap<>();

        for (Topic t : tm.getTopics()) {
            String topicNodeName = "T" + t.name;
            Node topicNode = nodes.get(topicNodeName);
            if (topicNode == null) {
                topicNode = new Node(topicNodeName);
                nodes.put(topicNodeName, topicNode);
                this.add(topicNode);
            }

            // For each subscriber agent, add an edge from the topic node to the agent node
            for (Agent a : t.getSubs()) {
                String agentNodeName = "A" + a.getName();
                Node agentNode = nodes.get(agentNodeName);
                if (agentNode == null) {
                    agentNode = new Node(agentNodeName);
                    nodes.put(agentNodeName, agentNode);
                    this.add(agentNode);
                }
                topicNode.addEdge(agentNode);
            }

            for (Agent a : t.getPubs()) {
                String agentNodeName = "A" + a.getName();
                Node agentNode = nodes.get(agentNodeName);
                if (agentNode == null) {
                    agentNode = new Node(agentNodeName);
                    nodes.put(agentNodeName, agentNode);
                    this.add(agentNode);
                }
                agentNode.addEdge(topicNode);
            }
        }
        logGraphData();
    }

    /**
     * Logs all node names and all edges in the graph.
     */
    public void logGraphData() {
        // Log all node names
        StringBuilder nodeNamesLog = new StringBuilder("Graph nodes: [");
        for (Node node : this) {
            nodeNamesLog.append(node.getName()).append(", ");
        }
        if (this.size() > 0) nodeNamesLog.setLength(nodeNamesLog.length() - 2);
        nodeNamesLog.append("]");
        // Log all edges
        StringBuilder edgesLog = new StringBuilder("Graph edges: [");
        boolean hasEdges = false;
        for (Node node : this) {
            for (Node target : node.getEdges()) {
                edgesLog.append("(").append(node.getName()).append(" -> ").append(target.getName()).append(")").append(", ");
                hasEdges = true;
            }
        }
        if (hasEdges) edgesLog.setLength(edgesLog.length() - 2);
        edgesLog.append("]");
    }
}

