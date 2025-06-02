window.addEventListener("DOMContentLoaded", () => {
  const formFrame = document.getElementById("formFrame");

  // Ensure the form iframe is fully loaded
  formFrame.onload = () => {
    const formDoc =
      formFrame.contentDocument || formFrame.contentWindow.document;
    const deployBtn = formDoc.getElementById("deployBtn");

    if (deployBtn) {
      deployBtn.addEventListener("click", () => {
        // Override iframe sources
        document.getElementById("graphFrame").src = "graph.html";
        document.getElementById("outputFrame").src = "graph.html";
      });
    }
  };
});
