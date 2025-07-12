package graph;

import java.util.HashSet;
import java.util.Set;

// a class to represent a Topic through which messages can be published to all subscribers
public class Topic {
	public final String name;
	private final Set<Agent> subs;
	private final Set<Agent> pubs;
	private volatile String lastMessage = null;

	Topic(String name) {
		this.name = name;
		subs = new HashSet<>();
		pubs = new HashSet<>();
	}

	public void subscribe(Agent a) {
        subs.add(a);
	}

	public void unsubscribe(Agent a) {
		subs.remove(a);
	}

	public void publish(Message m) {
		//System.out.println("    Topic.publish() called for topic '" + name + "' with message: " + m.asText);
		//System.out.println("    Previous lastMessage: '" + this.lastMessage + "'");
		this.lastMessage = m.asText;
		//System.out.println("    New lastMessage: '" + this.lastMessage + "'");
		for (Agent agent : subs) {
			agent.callback(name, m);
		}
	}

	public String getLastMessage() {
		//System.out.println("    Topic.getLastMessage() called for topic '" + name + "', returning: '" + this.lastMessage + "'");
		return this.lastMessage;
	}

	public void addPublisher(Agent a) {
        pubs.add(a);
	}

	public void removePublisher(Agent a) {
		pubs.remove(a);
	}

	public Set<Agent> getSubs() {
		return subs;
	}

	public Set<Agent> getPubs() {
		return pubs;
	}
}
