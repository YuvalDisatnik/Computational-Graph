package configs;

/**
 * Interface for configuration objects that define computational graphs.
 * 
 * Configurations specify how agents are created, connected, and initialized
 * in the computational graph system. Each configuration can be created,
 * managed, and cleaned up through this interface.
 * 
 * @author Omri Triki, Yuval Disatnik
 */
public interface Config {
    
    /**
     * Creates and initializes the computational graph based on this configuration.
     * This method should instantiate all agents and establish their connections.
     */
    void create();
    
    /**
     * Returns the name of this configuration.
     * 
     * @return The configuration name
     */
    String getName();
    
    /**
     * Returns the version number of this configuration.
     * 
     * @return The configuration version
     */
    int getVersion();
    
    /**
     * Closes the configuration and releases all associated resources.
     * This should clean up all agents and their connections.
     */
    void close();
}
