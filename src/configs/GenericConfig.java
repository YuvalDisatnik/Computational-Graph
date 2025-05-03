package test;


import graph.Agent;
import graph.ParallelAgent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class GenericConfig implements Config {
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
    public void create(){
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Config name cannot be null or empty");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(name))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line.trim());}
            if (lines.size() % 3 != 0) {
                throw new IllegalArgumentException("Invalid config file: " + name);
            }
            for (int i = 0; i < lines.size(); i += 3) {
                String className = lines.get(i);
                String[] subs = lines.get(i + 1).split(",");
                String[] pubs = lines.get(i + 2).split(",");

                Class<?> clazz = Class.forName(className);
                Object agentInstance = clazz.getConstructor(String[].class, String[].class)
                        .newInstance((Object) subs, (Object) pubs);
                if (agentInstance instanceof Agent) {
                    ParallelAgent parallelAgent = new ParallelAgent((Agent) agentInstance, 10);
                    agents.add(parallelAgent);
                } else {
                    throw new IllegalArgumentException("Class " + className + " does not implement Agent interface");
                }

            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading config file: " + e.getMessage(), e);
        }
    }
}
