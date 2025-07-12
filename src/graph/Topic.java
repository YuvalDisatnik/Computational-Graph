package graph;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a topic in the publisher/subscriber system.
 * 
 * Topics act as communication channels between agents. Agents can subscribe to topics
 * to receive messages and publish to topics to send messages to subscribers.
 * Topics maintain a list of subscribers and publishers, and store the last message
 * that was published to them.
 * 
 * @author Omri Triki, Yuval Disatnik
 */
public class Topic {
    /** The name of this topic */
    public final String name;
    /** Set of agents subscribed to this topic */
    private final Set<Agent> subs;
    /** Set of agents that publish to this topic */
    private final Set<Agent> pubs;
    /** The last message published to this topic (thread-safe) */
    private volatile String lastMessage = null;

	/**
	 * Creates a new topic with the specified name.
	 * 
	 * @param name The name of the topic
	 */
	Topic(String name) {
		this.name = name;
		subs = new HashSet<>();
		pubs = new HashSet<>();
	}

	/**
	 * Subscribes an agent to this topic.
	 * The agent will receive all messages published to this topic.
	 * 
	 * @param a The agent to subscribe
	 */
	public void subscribe(Agent a) {
        subs.add(a);
	}

	/**
	 * Unsubscribes an agent from this topic.
	 * The agent will no longer receive messages from this topic.
	 * 
	 * @param a The agent to unsubscribe
	 */
	public void unsubscribe(Agent a) {
		subs.remove(a);
	}

	/**
	 * Publishes a message to all subscribers of this topic.
	 * Updates the last message and notifies all subscribed agents.
	 * 
	 * @param m The message to publish
	 */
	public void publish(Message m) {
		this.lastMessage = m.asText;
		for (Agent agent : subs) {
			agent.callback(name, m);
		}
	}

	/**
	 * Returns the last message that was published to this topic.
	 * 
	 * @return The last message as a string, or null if no message has been published
	 */
	public String getLastMessage() {
		return this.lastMessage;
	}

	/**
	 * Registers an agent as a publisher to this topic.
	 * 
	 * @param a The agent to register as a publisher
	 */
	public void addPublisher(Agent a) {
        pubs.add(a);
	}

	/**
	 * Removes an agent from the publishers of this topic.
	 * 
	 * @param a The agent to remove as a publisher
	 */
	public void removePublisher(Agent a) {
		pubs.remove(a);
	}

	/**
	 * Returns the set of agents subscribed to this topic.
	 * 
	 * @return A set of subscribed agents
	 */
	public Set<Agent> getSubs() {
		return subs;
	}

	/**
	 * Returns the set of agents that publish to this topic.
	 * 
	 * @return A set of publishing agents
	 */
	public Set<Agent> getPubs() {
		return pubs;
	}
}
