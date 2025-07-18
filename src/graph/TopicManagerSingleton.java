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
			Topic topic = map.computeIfAbsent(name, Topic::new);
			return topic;
		}

		public Collection<Topic> getTopics() {
			return map.values();
		}

		public void clear() {
			map.clear();
		}

		public boolean topicExists(String name){
			return map.containsKey(name);
		}
	}

}
