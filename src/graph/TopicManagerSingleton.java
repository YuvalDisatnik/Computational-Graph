package graph;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class TopicManagerSingleton {

	public static TopicManager get() {
		return TopicManager.instance;
	}

	public static class TopicManager {

		private static final TopicManager instance = new TopicManager();
		private final ConcurrentHashMap<String, Topic> map;

		private TopicManager() {
			this.map = new ConcurrentHashMap<>();
		}

		public Topic getTopic(String name) {
			System.out.println("    TopicManager.getTopic() called with name: '" + name + "'");
			Topic topic = map.computeIfAbsent(name, Topic::new);
			System.out.println("    TopicManager.getTopic() returning topic: '" + topic.name + "'");
			return topic;
		}

		public Collection<Topic> getTopics() {
			System.out.println("    TopicManager.getTopics() called, returning " + map.size() + " topics");
			return map.values();
		}

		public void clear() {
			map.clear();
		}
	}

}
