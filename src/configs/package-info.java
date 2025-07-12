/**
 * Configuration management package for computational graphs.
 * 
 * This package provides the infrastructure for defining, loading, and managing
 * computational graph configurations:
 * 
 * <ul>
 *   <li><strong>Config</strong> - Interface for configuration objects</li>
 *   <li><strong>GenericConfig</strong> - File-based configuration loader</li>
 *   <li><strong>Graph</strong> - Graph representation and cycle detection</li>
 *   <li><strong>Node</strong> - Graph node implementation</li>
 *   <li><strong>Agent implementations</strong> - PlusAgent, IncAgent, BinOpAgent</li>
 * </ul>
 * 
 * Configurations can be loaded from text files or created programmatically,
 * supporting both simple and complex computational graph topologies.
 * 
 * @author Omri Triki, Yuval Disatnik
 */
package configs; 