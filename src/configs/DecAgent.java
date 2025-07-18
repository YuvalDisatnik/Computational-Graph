package configs;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;

public class DecAgent implements Agent {

    private double x = 0;
    private final String[] subs;
    private final String[] pubs;
    private int id;
    private static int decCounter = 0;

    public DecAgent(String[] subs, String[] pubs){
        this.subs = subs;
        this.pubs = pubs;
        this.id = decCounter;
        decCounter++;
        if(subs.length >= 1){
            TopicManagerSingleton.get().getTopic(subs[0]).subscribe(this);
        }
        else{
            throw new IllegalArgumentException("DecAgent requires at least 1 subscriptions");
        }

        // Register as publisher for output topics
        if (pubs.length > 0) {
            TopicManagerSingleton.get().getTopic(pubs[0]).addPublisher(this);
        }
    }

    private void updateX(String topic, Message msg){
        if(!Double.isNaN(msg.asDouble)){
            x = msg.asDouble;
        }
        else{
            x = 0;
        }
        publishResult();
    }

    private void publishResult(){
        if(pubs.length > 0){
            double result = x - 1;
            TopicManagerSingleton.get().getTopic(pubs[0]).publish(new Message(result));
        }
    }

    @Override
    public String getName() {
        return "DecAgent" + id;
    }

    @Override
    public void reset() {
        x=0;
    }

    @Override
    public void callback(String topic, Message msg) {
        if (topic.equals(TopicManagerSingleton.get().getTopic(subs[0]).name)) {
            updateX(topic, msg);
        }
    }

    @Override
    public void close() {
        this.reset();
    }
}
