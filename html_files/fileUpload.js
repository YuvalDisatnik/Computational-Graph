/**
 * fileUpload.js
 * This file handles the file selection functionality for the configuration file upload.
 * It manages the interaction between the visible "Choose File" button and the hidden file input,
 * as well as updating the UI to show the selected file name.
 */

// Add click handler to the visible "Choose File" button
document.getElementById("chooseFileBtn").addEventListener("click", function () {
  // When the visible button is clicked, trigger a click on the hidden file input
  // This creates a better-looking custom file upload interface while maintaining native functionality
  document.getElementById("configFile").click();
});

// Add change handler to the hidden file input
document.getElementById("configFile").addEventListener("change", function () {
  // Get the name of the selected file, or "No file chosen" if no file is selected
  // this.files[0] refers to the first (and only) file in the selection
  const fileName = this.files[0] ? this.files[0].name : "No file chosen";

  // Update the UI to display the selected file name
  // This provides visual feedback to the user about their file selection
  document.getElementById("fileName").textContent = fileName;
});
