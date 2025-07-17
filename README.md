# Computational Graph Web Application

A modular, extensible simulation of a computational graph system using a publisher/subscriber architecture. Developed as part of the Advanced Programming course, this project demonstrates modern software engineering practices, concurrent message-passing, and web-based visualization. The system is designed for educational purposes, allowing users to create, visualize, and interact with computational graphs in real time.

## 🧠 Background & Motivation

Computational graphs are widely used in data processing, machine learning, and distributed systems. This project models a computational graph as a set of agents (processing units) and topics (communication channels), using a publisher/subscriber pattern for message routing. The architecture is modular, supporting easy extension with new agent types and graph topologies. The web interface provides an accessible way to experiment with graph-based computation, configuration management, and real-time feedback.

## 🌟 Features

- **Interactive Web Interface** – Modern, responsive UI with iframe-based architecture and seamless state management
- **Configuration Management** – Upload and manage computational graph configurations with real-time validation
- **Real-time Visualization** – Dynamic graph rendering with Cytoscape.js and coordinated iframe updates
- **Message Publishing** – Send messages to topics in real time with immediate feedback
- **RESTful API** – Complete backend with servlet-based endpoints returning JSON responses
- **Static File Serving** – Built-in web server for HTML/CSS/JS assets
- **Robust Error Handling** – Comprehensive error reporting and troubleshooting guidance

## 🏗️ Architecture

### Backend Components

- **HTTP Server** (`MyHTTPServer`) – Custom HTTP server implementation
- **Servlet Framework** – Request routing and handling
- **Graph Engine** – Publisher/subscriber computational graph system
- **Configuration Parser** – Processes `.conf` files into executable graphs

### Frontend Components

- **Main Interface** (`index.html`) – Iframe-based layout with coordinated updates
- **Configuration Panel** (`form.html`) – File upload and message publishing
- **Graph Visualization** (`graph.html`/`graph_temp.html`/`generated_graph.html`) – Interactive graph display using Cytoscape.js
- **Results Display** (`results.html`) – Real-time output and computation results
- **Content Coordinator** (`contentSelect.js`) – Manages iframe communication and state synchronization

> **Note:** All web interface files are located in the `html_files/` directory and are served via the `/app/` endpoint (e.g., `/app/index.html`).

### Key Servlets

- `ConfLoader` – Handles configuration uploads (`/upload`) with JSON response support
- `TopicDisplayer` – Manages message publishing (`/publish`) with real-time feedback
- `HtmlLoader` – Serves static web assets (`/app/`) with proper MIME type handling
- `GraphDataServlet` – (if enabled) Provides graph data for visualization

## 🚀 Quick Start

### Prerequisites

- Java 8 or higher
- Web browser (Chrome, Firefox, Safari, Edge)

### Installation & Running

1. **Clone and Navigate**

   ```bash
   git clone <https://github.com/YuvalDisatnik/Computational-Graph.git>
   cd Computational-Graph
   ```

2. **Compile the Project**

   ```bash
   javac -cp src src/Main.java src/servlets/*.java src/server/*.java src/graph/*.java src/configs/*.java src/views/*.java -d out
   ```

3. **Start the Server**

   ```bash
   java -cp out Main
   ```

4. **Access the Application**
   Open your browser and go to: `http://localhost:8080/app/index.html`

5. **Stop the Server**
   Press `Enter` in the terminal or `Ctrl+C`

> **Troubleshooting:**
>
> - If you change files in `html_files/`, clear your browser cache or do a hard refresh to see updates.
> - If port 8080 is in use, edit the port in `Main.java` and recompile.

## 📁 Project Structure

```
Computational-Graph/
├── src/
│   ├── Main.java                 # Application entry point
│   ├── servlets/                 # HTTP request handlers (ConfLoader, TopicDisplayer, HtmlLoader, GraphDataServlet)
│   ├── server/                   # HTTP server implementation (MyHTTPServer, RequestParser, Servlet)
│   ├── graph/                    # Computational graph core (Agent, Topic, Message, ParallelAgent, TopicManagerSingleton)
│   ├── configs/                  # Configuration management (Config, GenericConfig, Graph, Node, agent implementations)
│   └── views/                    # Visualization components (HtmlGraphWriter)
├── html_files/                   # Web frontend (index.html, form.html, graph.html, results.html, styles.css, *.js)
├── config_files/                 # Sample configurations (simple.conf, cycle.conf)
├── out/                          # Compiled Java classes
└── README.md
```

- **src/** – All Java source code (backend, graph engine, servlets, config, visualization)
- **html_files/** – All static web assets (served via `/app/`)
- **config_files/** – Example configuration files for testing
- **out/** – Compiled Java classes (output directory)

## 📖 Usage Guide

### 1. Upload Configuration

- Click "Choose File" in the Configuration section
- Select a `.conf` file (try `config_files/simple.conf`)
- Click "Deploy" to activate the configuration

### 2. Send Messages

- Enter a topic name in the "Topic Name" field
- Enter your message in the "Message" field
- Click "Send" or press Enter

### 3. View Graph

- The graph visualization updates automatically when configurations are deployed
- Nodes represent agents and topics
- Edges show data flow connections

## 🔌 API Endpoints

| Method | Endpoint           | Description                      | Request Content-Type | Response Content-Type                             |
| ------ | ------------------ | -------------------------------- | -------------------- | ------------------------------------------------- |
| `GET`  | `/app/*`           | Static file serving              | -                    | `text/html`, `text/css`, `application/javascript` |
| `POST` | `/upload`          | Configuration file upload        | `text/plain`         | `application/json`                                |
| `POST` | `/publish`         | Publish message to topic         | `application/json`   | `application/json`                                |
| `POST` | `/generate-config` | Generate config from description | `application/json`   | `application/octet-stream`                        |

### Example API Usage

**Uploading Configuration:**

```bash
curl -X POST http://localhost:8080/upload \
  -H "Content-Type: text/plain" \
  --data-binary @config_files/simple.conf
```

**Publishing a Message:**

```bash
curl -X POST http://localhost:8080/publish \
  -H "Content-Type: application/json" \
  -d '{"topic": "INPUT1", "message": "42"}'
```

## 📝 Configuration File Format

Configuration files define computational graphs using a simple text format:

```
# Agent class name
test.PlusAgent
# Subscribed topics (comma-separated)
A,B
# Published topics (comma-separated)
C

test.IncAgent
C
RESULT
```

Each agent requires exactly 3 lines:

1. Fully qualified class name
2. Input topics (subscriptions)
3. Output topics (publications)

## 🎯 Example Configurations

**Simple Addition Pipeline:**

```
test.PlusAgent
INPUT1,INPUT2
SUM

test.IncAgent
SUM
RESULT
```

**Complex Processing Chain:**

```
test.PlusAgent
A,B
AB_SUM

test.PlusAgent
C,D
CD_SUM

test.PlusAgent
AB_SUM,CD_SUM
FINAL_RESULT
```

## 🛠️ Development

### Building

```bash
# Compile all sources
javac -cp src src/Main.java src/servlets/*.java src/server/*.java src/graph/*.java src/configs/*.java src/views/*.java -d out

# Run with custom port
java -cp out -Dport=9090 Main
```

### Adding New Agents

1. Implement the `Agent` interface (see `src/graph/Agent.java`)
2. Add your agent class to the classpath
3. Reference it in configuration files

### Debugging

- Server logs appear in the console
- Browser developer tools for frontend debugging
- Check `config_files/` for uploaded configurations

## 🔧 Troubleshooting

### Common Issues

**Issue: Page reloads/resets after clicking Deploy**

- **Cause**: The deploy functionality now properly returns JSON responses instead of HTML pages.
- **Solution**: Ensure you're using the latest version of the code.

**Issue: Configuration upload fails**

- **Cause**: Invalid configuration format or file encoding issues.
- **Solution**:
  - Ensure your `.conf` file has exactly 3 lines per agent (class name, subscriptions, publications)
  - Use UTF-8 encoding for configuration files
  - Check the console for validation error messages

**Issue: Server won't start on port 8080**

- **Cause**: Port already in use by another application.
- **Solution**:
  - Stop other applications using port 8080
  - Or modify the port in `Main.java` and recompile

**Issue: Graph visualization doesn't update**

- **Cause**: Browser cache or JavaScript errors.
- **Solution**:
  - Clear browser cache or do a hard refresh
  - Check browser developer console for errors
  - Ensure all HTML files are served from the same domain

## 📄 License

This project is part of an Advanced Programming course assignment.

## 👥 Authors

- **Omri Triki**
- **Yuval Disatnik**

---

_For questions or issues, please check the source code documentation or create an issue in the repository._
