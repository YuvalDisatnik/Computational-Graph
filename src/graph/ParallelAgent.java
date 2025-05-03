package graph;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ParallelAgent implements Agent {
    // ParallelAgent has an Agent field
    private final Agent agent;
    private final BlockingQueue<MessageWrapper> blockingQueue;
    private final Thread queueHandler;
    private volatile boolean stop = false;

    public ParallelAgent(Agent agent, int capacity){
        this.agent = agent;
        this.blockingQueue = new ArrayBlockingQueue<>(capacity);

        this.queueHandler = new Thread(()->{
            try{
                while (!stop || !blockingQueue.isEmpty()){
                    MessageWrapper next = blockingQueue.take();
                    agent.callback(next.topic, next.message);
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        });

        queueHandler.start();
    }

    @Override
    public String getName() {
        return agent.getName();
    }

    @Override
    public void reset() {
        agent.reset();
    }

    @Override
    public void callback(String topic, Message msg) {
        try{
            blockingQueue.put(new MessageWrapper(topic, msg));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        stop = true;
        queueHandler.interrupt();
    }

    private static class MessageWrapper{
        final String topic;
        final Message message;

        public MessageWrapper(String topic, Message message){
            this.topic = topic;
            this.message = message;
        }
    }
}
