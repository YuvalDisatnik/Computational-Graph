package configs;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;

public class DivAgent implements Agent {

    private double x = 0;
    private double y = 0;
    private final String[] subs;
    private final String[] pubs;
    private int id;
    private static int divCounter = 0;

    public DivAgent(String[] subs, String[] pubs){
        this.subs = subs;
        this.pubs = pubs;
        this.id = divCounter;
        divCounter++;
        if(subs.length >= 2){
            TopicManagerSingleton.get().getTopic(subs[0]).subscribe(this);
            TopicManagerSingleton.get().getTopic(subs[1]).subscribe(this);
        }
        else{
            throw new IllegalArgumentException("DivAgent requires at least 2 subscriptions");
        }

        // Register as publisher for output topics
        if (pubs.length > 0) {
            TopicManagerSingleton.get().getTopic(pubs[0]).addPublisher(this);
        }
    }

    private void updateX(Message msg){
        if(y != 0){
            x = msg.asDouble;
            publishResult();
        }
        else{
            throw new IllegalArgumentException("Can't divide by 0");
        }
    }

    private void updateY(Message msg){
        if(msg.asDouble != 0){
            y = msg.asDouble;
            publishResult();
        }
        else{
            throw new IllegalArgumentException("Can't divide by 0");
        }
    }

    private void publishResult(){
        System.out.println("trying to do" + x + "/" + y);
        if(pubs.length > 0){
            double result = x / y;
            TopicManagerSingleton.get().getTopic(pubs[0]).publish(new Message(result));
        }
    }

    @Override
    public String getName() {
        return "DivAgent" + id;
    }

    @Override
    public void reset() {
        x=0;
        y=0;
    }

    @Override
    public void callback(String topic, Message msg) {
        try {
            double value = Double.parseDouble(msg.asText);
        } catch (NumberFormatException e) {
            return;
        }
        if (topic.equals(TopicManagerSingleton.get().getTopic(subs[0]).name)) {
            updateX(msg);
        } else if (topic.equals(TopicManagerSingleton.get().getTopic(subs[1]).name)) {
            updateY(msg);
        }
    }

    @Override
    public void close() {
        this.reset();
    }
}
