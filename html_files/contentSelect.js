/**
 * contentSelect.js
 * This file manages the interaction between the three main iframes of the application:
 * - formFrame: Contains the configuration form and deployment controls
 * - graphFrame: Displays the computational graph visualization
 * - outputFrame: Shows the computation results
 */

// Wait for the DOM to be fully loaded before initializing
window.addEventListener("DOMContentLoaded", () => {
  // Get reference to the form iframe
  const formFrame = document.getElementById("formFrame");

  // Check deployment state from localStorage
  const isDeployed = localStorage.getItem("isDeployed") === "true";
  updateFramesContent(isDeployed);

  // Set up event handling once the form iframe is fully loaded
  formFrame.onload = () => {
    // Access the document inside the form iframe
    const formDoc =
      formFrame.contentDocument || formFrame.contentWindow.document;

    // Get reference to the deploy button inside the form
    const deployBtn = formDoc.getElementById("deployBtn");

    // Add click handler to the deploy button if it exists
    if (deployBtn) {
      deployBtn.addEventListener("click", () => {
        // Set deployment state in localStorage
        localStorage.setItem("isDeployed", "true");
        updateFramesContent(true);
      });
    }
  };
});

/**
 * Updates the content of all iframes based on deployment state
 * @param {boolean} isDeployed - Whether the application is in deployed state
 */
function updateFramesContent(isDeployed = false) {
  // Get references to the graph and output iframes
  const graphFrame = document.getElementById("graphFrame");
  const outputFrame = document.getElementById("outputFrame");

  if (isDeployed) {
    // In deployed state, show the active graph visualization
    graphFrame.src = "generated_graph.html";

    // Sample graph data structure (to be replaced with actual data)
    // This represents a simple computational graph with:
    // - Two input nodes (A=2.0, B=3.5)
    // - A plus agent that adds A and B to produce C (5.5)
    // - A times agent with inputs C and D (1.0)
    const graphData = {
      nodes: [
        { id: "A", label: "A", type: "topic", value: 2.0 },
        { id: "B", label: "B", type: "topic", value: 3.5 },
        { id: "plus", label: "plus", type: "agent" },
        { id: "C", label: "C", type: "topic", value: 5.5 }, // Result of A + B
        { id: "times", label: "times", type: "agent" },
        { id: "D", label: "D", type: "topic", value: 1.0 },
      ],
      edges: [
        { source: "A", target: "plus" },
        { source: "B", target: "plus" },
        { source: "plus", target: "C" },
        { source: "C", target: "times" },
        { source: "D", target: "times" },
      ],
    };

    // Add a small delay to ensure frames are ready before sending data
    setTimeout(() => {
      // Load the results page
      outputFrame.src = "results.html";

      // Once results page is loaded, send the graph data
      outputFrame.onload = () => {
        outputFrame.contentWindow.postMessage(
          {
            type: "updateResults",
            data: graphData,
          },
          "*" // Allow cross-origin communication between frames
        );
      };
    }, 100);
  } else {
    // In undeployed state, show the initial placeholder pages
    graphFrame.src = "graph.html";
    outputFrame.src = "results.html";
  }
}

// Handle messages from child frames
window.addEventListener("message", (event) => {
  // When results frame indicates it's ready to receive data
  if (event.data.type === "resultsFrameReady") {
    const outputFrame = document.getElementById("outputFrame");
    // Send null data to show loading state in results frame
    outputFrame.contentWindow.postMessage(
      {
        type: "updateResults",
        data: null,
      },
      "*" // Allow cross-origin communication between frames
    );
  }
});
