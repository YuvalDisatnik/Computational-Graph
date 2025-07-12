/**
 * Core graph engine package for the computational graph system.
 * 
 * This package contains the fundamental components of the publisher/subscriber
 * architecture that powers the computational graph:
 * 
 * <ul>
 *   <li><strong>Agent</strong> - Interface for computational processing units</li>
 *   <li><strong>Message</strong> - Immutable data containers for inter-agent communication</li>
 *   <li><strong>Topic</strong> - Communication channels between agents</li>
 *   <li><strong>ParallelAgent</strong> - Thread-safe wrapper for agents</li>
 *   <li><strong>TopicManagerSingleton</strong> - Central topic registry</li>
 * </ul>
 * 
 * The graph engine provides a robust foundation for building complex
 * computational pipelines with automatic message routing and parallel processing.
 * 
 * @author Omri Triki, Yuval Disatnik
 */
package graph; 