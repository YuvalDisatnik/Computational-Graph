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
    }
}

