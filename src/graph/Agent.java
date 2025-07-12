package graph;

/**
 * Interface for computational agents in the graph system.
 * 
 * Agents are the core processing units that subscribe to topics, 
 * process incoming messages, and publish results to output topics.
 * Each agent implements a specific computation or transformation.
 * 
 * @author Omri Triki, Yuval Disatnik
 */
public interface Agent {
    
    /**
     * Returns the name of this agent.
     * 
     * @return The agent's name as a string
     */
    String getName();
    
    /**
     * Resets the agent's internal state to initial values.
     * Called when the graph is being reconfigured or reset.
     */
    void reset();
    
    /**
     * Callback method called when a message is published to a topic
     * that this agent is subscribed to.
     * 
     * @param topic The name of the topic that published the message
     * @param msg The message that was published
     */
    void callback(String topic, Message msg);
    
    /**
     * Closes the agent and releases any resources it holds.
     * Called when the agent is being shut down.
     */
    void close();
}
