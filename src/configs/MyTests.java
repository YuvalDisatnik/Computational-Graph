package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import graph.Agent;
import graph.Message;
import graph.ParallelAgent;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

public class MyTests {

    public static void main(String[] args) {
        testEmptyGraph();
        testSelfLoopGraph();
        testMultipleNodesGraph();
        testBinOpAgentEdgeCases();
        testParallelAgent();
        testTopicPublish();
        System.out.println("All tests completed.");
    }

    // עזר: בודק אם ברשימת קודקודים יש מעגל
    public static boolean graphHasCycles(List<Node> nodes) {
        for (Node node : nodes) {
            if (node.hasCycles()) {
                return true;
            }
        }
        return false;
    }

    // בדיקה: גרף ריק – אין מעגל
    public static void testEmptyGraph() {
        Graph g = new Graph();
        if (g.hasCycles()) {
            System.out.println("Empty graph: hasCycles returned true (-5)");
        }
    }

    // בדיקה: קודקוד עם לולאה עצמית צריך לזהות מעגל
    public static void testSelfLoopGraph() {
        Node selfNode = new Node("SelfLoop");
        selfNode.addEdge(selfNode);
        List<Node> graph = new ArrayList<>();
        graph.add(selfNode);
        if (!graphHasCycles(graph)) {
            System.out.println("Self loop: hasCycles failed to detect cycle (-5)");
        }
    }

    // בדיקה: גרף של מספר קודקודים – ללא מעגל ואז עם מעגל
    public static void testMultipleNodesGraph() {
        // ללא מעגל
        Node a = new Node("A");
        Node b = new Node("B");
        Node c = new Node("C");
        a.addEdge(b);
        b.addEdge(c);
        List<Node> graph = Arrays.asList(a, b, c);
        if (graphHasCycles(graph)) {
            System.out.println("Graph without cycle: hasCycles returned true (-5)");
        }
        // הוספת מעגל: c -> a
        c.addEdge(a);
        if (!graphHasCycles(graph)) {
            System.out.println("Graph with cycle: hasCycles failed to detect cycle (-5)");
        }
    }

    // בדיקה לסוכן BinOpAgent: קלט לא-נומרי ונומרי
    public static void testBinOpAgentEdgeCases() {
        TopicManager tm = TopicManagerSingleton.get();
        tm.clear();
        Config config = new MathExampleConfig();
        config.create();

        // בדיקה: קלט לא נומרי – אמור שלא לחשב תוצאה (כלומר לא לשנות את msg)
        GetAgent ga = new GetAgent("R3");
        tm.getTopic("A").publish(new Message("Not a number"));
        tm.getTopic("B").publish(new Message("Not a number"));
        if (ga.msg != null && !Double.isNaN(ga.msg.asDouble)) {
            System.out.println("BinOpAgent did not handle non-numeric input correctly (-5)");
        }

        // בדיקה: קלט נומרי – תוצאה אמורה להיות (a+b)*(a-b)
        Random r = new Random();
        int x = 1 + r.nextInt(100);
        int y = 1 + r.nextInt(100);
        tm.getTopic("A").publish(new Message(x));
        tm.getTopic("B").publish(new Message(y));
        double expected = (x + y) * (x - y);
        if (ga.msg == null || Math.abs(expected - ga.msg.asDouble) > 0.05) {
            System.out.println("BinOpAgent computation error (-10): Expected " + expected + ", got " + (ga.msg != null ? ga.msg.asDouble : "null"));
        }
    }

    // בדיקה לסוכן מקביל ParallelAgent: שליחת מספר הודעות ובדיקה שהן מתקבלות בסדר הנכון
    public static void testParallelAgent() {
        DummyAgent dummy = new DummyAgent("dummy");
        ParallelAgent pa = new ParallelAgent(dummy, 10);
        int messagesCount = 5;
        for (int i = 0; i < messagesCount; i++) {
            pa.callback("testTopic", new Message(i));
        }
        // לחכות מעט כדי לתת לסוכן המקביל לעבד את ההודעות
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        if (dummy.received.size() != messagesCount) {
            System.out.println("ParallelAgent did not process all messages (-5)");
        } else {
            for (int i = 0; i < messagesCount; i++) {
                if ((int) dummy.received.get(i).asDouble != i) {
                    System.out.println("ParallelAgent message order incorrect (-5)");
                    break;
                }
            }
        }
        pa.close();
    }

    // בדיקה: מנוי, פרסום וקבלת הודעות דרך מערכת Topic
    public static void testTopicPublish() {
        TopicManager tm = TopicManagerSingleton.get();
        tm.clear();
        DummyAgent dummySub = new DummyAgent("subscriber");
        tm.getTopic("Topic1").subscribe(dummySub);
        tm.getTopic("Topic1").publish(new Message("Hello"));
        // לחכות מעט לעיבוד ההודעה
        try { Thread.sleep(50); } catch (InterruptedException e) {}
        if (dummySub.received.size() != 1 || !dummySub.received.get(0).asText.equals("Hello")) {
            System.out.println("Topic publish/subscribe failed (-5)");
        }
    }
}

// סוכן לדמה שאוסף הודעות (משמש למבחנים)
class DummyAgent implements Agent {
    private String name;
    public List<Message> received = new ArrayList<>();

    public DummyAgent(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void reset() {
        received.clear();
    }

    @Override
    public void callback(String topic, Message msg) {
        received.add(msg);
    }

    @Override
    public void close() {}
}

// מחלקה לקבלת הודעה מסויימת, דומה לזו שבקובץ MainTrain
class GetAgent implements Agent {
    public Message msg;
    public GetAgent(String topic) {
        TopicManagerSingleton.get().getTopic(topic).subscribe(this);
    }
    @Override
    public String getName() { return "GetAgent"; }
    @Override
    public void reset() {}
    @Override
    public void callback(String topic, Message msg) {
        this.msg = msg;
    }
    @Override
    public void close() {}
}
