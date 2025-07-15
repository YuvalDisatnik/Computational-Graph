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

  // Set deployment state to false on initial load
  localStorage.setItem("isDeployed", "false");

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
    // In deployed state, show a temporary page first
    graphFrame.src = "loader.html";

    // Add a delay (e.g., 2000ms) before loading the main content
    setTimeout(() => {
      // After delay, show the active graph visualization
      graphFrame.src = "generated_graph.html";

      // Fetch graph data from the server
      fetch("/graph-data")
        .then((response) => {
          if (!response.ok) {
            throw new Error("Graph data not available yet.");
          }
          return response.json();
        })
        .then((graphData) => {
          // Load the results page
          outputFrame.src = "results.html";

          // Once results page is loaded, send the graph data
          outputFrame.onload = () => {
            if (outputFrame.contentWindow) {
              outputFrame.contentWindow.postMessage(
                {
                  type: "updateResults",
                  data: graphData,
                },
                "*" // Allow cross-origin communication between frames
              );
            }
          };
        })
        .catch((error) => {
          console.error("Error fetching graph data:", error);
          // Handle error, e.g., show a message in the output frame
          outputFrame.src = "results.html";
          outputFrame.onload = () => {
            if (outputFrame.contentWindow) {
              outputFrame.contentWindow.postMessage(
                {
                  type: "updateResults",
                  data: null, // or some error indicator
                },
                "*"
              );
            }
          };
        });
    }, 2000); // 2-second delay
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

  // When iframes are reloaded after publishing a message
  if (event.data.type === "refreshData") {
    console.log("Refreshing data after message publish");
    refreshGraphData();
  }
});

/**
 * Fetches fresh graph data and updates the iframes
 */
function refreshGraphData() {
  const outputFrame = document.getElementById("outputFrame");

  // Fetch fresh graph data from the server
  fetch("/graph-data")
    .then((response) => {
      if (!response.ok) {
        throw new Error("Graph data not available yet.");
      }
      return response.json();
    })
    .then((graphData) => {
      console.log("Fresh graph data received:", graphData);

      // Send the fresh data to the results iframe
      if (outputFrame.contentWindow) {
        outputFrame.contentWindow.postMessage(
          {
            type: "updateResults",
            data: graphData,
          },
          "*"
        );
      }
    })
    .catch((error) => {
      console.error("Error fetching fresh graph data:", error);
      // Send null data to show loading state
      if (outputFrame.contentWindow) {
        outputFrame.contentWindow.postMessage(
          {
            type: "updateResults",
            data: null,
          },
          "*"
        );
      }
    });
}
