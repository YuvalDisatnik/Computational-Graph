package configs;


import graph.Agent;
import graph.ParallelAgent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GenericConfig implements Config {
    private static final Logger LOGGER = Logger.getLogger(GenericConfig.class.getName());
    private String name;
    private int version;
    private final List<ParallelAgent> agents = new ArrayList<>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public void close() {
        //        System.out.println("[GenericConfig] Closing configuration");
        //        System.out.println("[GenericConfig] Number of agents to close: " + agents.size());
        
        for (ParallelAgent agent : agents) {
            //            System.out.println("[GenericConfig] Closing agent: " + agent.getClass().getSimpleName());
            agent.close();
        }

        agents.clear();
        //        System.out.println("[GenericConfig] All agents closed and list cleared");
    }

    public void setConfFile(String confFile) {
        //        System.out.println("[GenericConfig] Setting configuration file: " + confFile);
        this.name = confFile;
    }

    @Override
    public void create() {
        //        System.out.println("[GenericConfig] === Starting configuration creation ===");
        
        if (name == null || name.isEmpty()) {
            //            System.out.println("[GenericConfig] Error: Config name is null or empty");
            throw new IllegalArgumentException("Config name cannot be null or empty");
        }
        //        System.out.println("[GenericConfig] Using configuration file: " + name);

        try (BufferedReader reader = new BufferedReader(new FileReader(name))) {
            //        System.out.println("[GenericConfig] Reading configuration file");
            List<String> lines = new ArrayList<>();
            String line;
            int lineCount = 0;
            
            while ((line = reader.readLine()) != null) {
                lineCount++;
                String trimmedLine = line.trim();
                //                System.out.println("[GenericConfig] Reading line " + lineCount + ": " + trimmedLine);
                lines.add(trimmedLine);
            }
            
            //        System.out.println("[GenericConfig] Total lines read: " + lines.size());
            
            if (lines.size() % 3 != 0) {
                //            System.out.println("[GenericConfig] Error: Invalid number of lines (" + lines.size() + "). Must be divisible by 3.");
                throw new IllegalArgumentException("Invalid config file: " + name);
            }

            int agentCount = lines.size() / 3;
            //        System.out.println("[GenericConfig] Creating " + agentCount + " agents");
            
            for (int i = 0; i < lines.size(); i += 3) {
                int agentIndex = (i / 3) + 1;
                //                System.out.println("[GenericConfig] === Processing Agent " + agentIndex + " ===");
                
                String className = lines.get(i);
                String[] subs = lines.get(i + 1).split(",");
                String[] pubs = lines.get(i + 2).split(",");

                //                System.out.println("[GenericConfig] Agent " + agentIndex + " details:");
                //                System.out.println("[GenericConfig] - Class: " + className);
                //                System.out.println("[GenericConfig] - Subscriptions: " + String.join(", ", subs));
                //                System.out.println("[GenericConfig] - Publications: " + String.join(", ", pubs));

                try {
                    //                    System.out.println("[GenericConfig] Loading class: " + className);
                    Class<?> clazz = Class.forName(className);
                    
                    //                    System.out.println("[GenericConfig] Creating agent instance");
                    Object agentInstance = clazz.getConstructor(String[].class, String[].class)
                            .newInstance((Object) subs, (Object) pubs);
                    
                    if (agentInstance instanceof Agent) {
                        //                        System.out.println("[GenericConfig] Creating parallel agent wrapper");
                        ParallelAgent parallelAgent = new ParallelAgent((Agent) agentInstance, 10);
                        agents.add(parallelAgent);
                        //                        System.out.println("[GenericConfig] Agent " + agentIndex + " created successfully");
                    } else {
                        //                        System.out.println("[GenericConfig] Error: Class " + className + " does not implement Agent interface");
                        throw new IllegalArgumentException("Class " + className + " does not implement Agent interface");
                    }
                } catch (ClassNotFoundException e) {
                    //                    System.out.println("[GenericConfig] Error: Class not found - " + className);
                    throw new RuntimeException("Class not found: " + className, e);
                } catch (Exception e) {
                    //                    System.out.println("[GenericConfig] Error creating agent: " + e.getMessage());
                    throw new RuntimeException("Error creating agent: " + e.getMessage(), e);
                }
            }
            
            //        System.out.println("[GenericConfig] === Configuration creation completed ===");
            //        System.out.println("[GenericConfig] Total agents created: " + agents.size());
            
        } catch (Exception e) {
            //        System.out.println("[GenericConfig] Error reading config file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error reading config file: " + e.getMessage(), e);
        }
    }

    /**
     * Logs all node names and all edges in the given graph.
     */
    public static void logGraphData(configs.Graph graph) {
        if (graph == null) {
            LOGGER.info("Graph is null");
            return;
        }
        // Log all node names
        StringBuilder nodeNamesLog = new StringBuilder("Graph nodes: [");
        for (configs.Node node : graph) {
            nodeNamesLog.append(node.getName()).append(", ");
        }
        if (graph.size() > 0) nodeNamesLog.setLength(nodeNamesLog.length() - 2);
        nodeNamesLog.append("]");
        LOGGER.info(nodeNamesLog.toString());
        // Log all edges
        StringBuilder edgesLog = new StringBuilder("Graph edges: [");
        boolean hasEdges = false;
        for (configs.Node node : graph) {
            for (configs.Node target : node.getEdges()) {
                edgesLog.append("(").append(node.getName()).append(" -> ").append(target.getName()).append(")").append(", ");
                hasEdges = true;
            }
        }
        if (hasEdges) edgesLog.setLength(edgesLog.length() - 2);
        edgesLog.append("]");
        LOGGER.info(edgesLog.toString());
    }
}
