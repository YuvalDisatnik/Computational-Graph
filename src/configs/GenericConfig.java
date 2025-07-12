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
        
        for (ParallelAgent agent : agents) {
            agent.close();
        }

        agents.clear();
    }

    public void setConfFile(String confFile) {
        this.name = confFile;
    }

    @Override
    public void create() {
        
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Config name cannot be null or empty");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(name))) {
            List<String> lines = new ArrayList<>();
            String line;
            
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                lines.add(trimmedLine);
            }
            
            
            if (lines.size() % 3 != 0) {
                throw new IllegalArgumentException("Invalid config file: " + name);
            }
            
            for (int i = 0; i < lines.size(); i += 3) {
                
                String className = lines.get(i);
                String[] subs = lines.get(i + 1).split(",");
                String[] pubs = lines.get(i + 2).split(",");

                try {
                    Class<?> clazz = Class.forName(className);
                    
                    Object agentInstance = clazz.getConstructor(String[].class, String[].class)
                            .newInstance((Object) subs, (Object) pubs);
                    
                    if (agentInstance instanceof Agent) {
                        ParallelAgent parallelAgent = new ParallelAgent((Agent) agentInstance, 10);
                        agents.add(parallelAgent);
                    } else {
                        throw new IllegalArgumentException("Class " + className + " does not implement Agent interface");
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Class not found: " + className, e);
                } catch (Exception e) {
                    throw new RuntimeException("Error creating agent: " + e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
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
