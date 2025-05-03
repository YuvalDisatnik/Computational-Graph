package graph;

import java.util.HashSet;
import java.util.Set;

// a class to represent a Topic through which messages can be published to all subscribers
public class Topic {
	public final String name;
	private final Set<Agent> subs;
	private final Set<Agent> pubs;

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
		for (Agent agent : subs) {
			agent.callback(name, m);
		}
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
