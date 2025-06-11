package configs;

import java.util.function.BinaryOperator;

import graph.Agent;
import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

public class BinOpAgent implements Agent {

    private final String name;
    private final String firstInputTopic;
    private final String secondInputTopic;
    private final String outputTopic;
    private final BinaryOperator<Double> op;
    private Double firstInputValue;
    private Double secondInputValue;

    public BinOpAgent(String name, String firstInputTopic, String secondInputTopic, String outputTopic, BinaryOperator<Double> op) {

        this.name = name;
        this.firstInputTopic = firstInputTopic;
        this.secondInputTopic = secondInputTopic;
        this.outputTopic = outputTopic;
        this.op = op;

        TopicManager tm = TopicManagerSingleton.get();
        Topic firstInput = tm.getTopic(firstInputTopic);
        Topic secondInput = tm.getTopic(secondInputTopic);
        Topic output = tm.getTopic(outputTopic);

        firstInput.subscribe(this);
        secondInput.subscribe(this);
        output.addPublisher(this);

        callback(name, new Message(name));

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void reset() {
        firstInputValue = 0.0;
        secondInputValue = 0.0;
    }

    @Override
    public void callback(String topic, Message msg) {
        if(!Double.isNaN(msg.asDouble)) {
            if(topic.equals(firstInputTopic)) {
                firstInputValue = msg.asDouble;
            } else if(topic.equals(secondInputTopic)) {
                secondInputValue = msg.asDouble;
            }
        }

        if(firstInputValue != null && secondInputValue != null) {
            Double result = op.apply(firstInputValue, secondInputValue);
            Message outputMsg = new Message(result);
            TopicManagerSingleton.get().getTopic(outputTopic).publish(outputMsg);
            reset();
        }
    }

    @Override
    public void close() {

    }
}
