# Computational Graph Web Application

A modular simulation of a computational graph system using a publisher/subscriber architecture, implemented as part of the Advanced Programming course. This project provides a complete web-based interface for creating, visualizing, and interacting with computational graphs.

## 🌟 Features

- **Interactive Web Interface** - Modern, responsive UI with iframe-based architecture
- **Configuration Management** - Upload and manage computational graph configurations
- **Real-time Visualization** - Dynamic graph rendering with Cytoscape.js
- **Message Publishing** - Send messages to topics in real-time
- **AI-Powered Config Generation** - Generate configurations from natural language descriptions
- **RESTful API** - Complete backend with servlet-based endpoints
- **Static File Serving** - Built-in web server for HTML/CSS/JS assets

## 🏗️ Architecture

### Backend Components
- **HTTP Server** (`MyHTTPServer`) - Custom HTTP server implementation
- **Servlet Framework** - Request routing and handling
- **Graph Engine** - Publisher/subscriber computational graph system
- **Configuration Parser** - Processes `.conf` files into executable graphs

### Frontend Components
- **Main Interface** (`index.html`) - Iframe-based layout
- **Configuration Panel** (`form.html`) - File upload and message publishing
- **Graph Visualization** (`graph.html`) - Interactive graph display
- **Results Display** (`results.html`) - Output and status information

### Key Servlets
- `ConfLoader` - Handles configuration uploads (`/upload`) and generation (`/generate-config`)
- `TopicDisplayer` - Manages message publishing (`/publish`)
- `HtmlLoader` - Serves static web assets (`/app/`)

## 🚀 Quick Start

### Prerequisites
- Java 8 or higher
- Web browser (Chrome, Firefox, Safari, Edge)

### Running the Application

1. **Clone and Navigate**
   ```bash
   git clone <repository-url>
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

## 📖 Usage Guide

### 1. Upload Configuration
- Click "Choose File" in the Configuration section
- Select a `.conf` file (try `config_files/simple.conf`)
- Click "Deploy" to activate the configuration

### 2. Send Messages
- Enter a topic name in the "Topic Name" field
- Enter your message in the "Message" field
- Click "Send" or press Enter

### 3. Generate Configuration
- Describe your desired graph structure in natural language
- Click "Generate" to create a downloadable `.conf` file
- Upload the generated file to deploy it

### 4. View Graph
- The graph visualization updates automatically when configurations are deployed
- Nodes represent agents and topics
- Edges show data flow connections

## 🔌 API Endpoints

| Method | Endpoint | Description | Content-Type |
|--------|----------|-------------|--------------|
| `GET` | `/app/*` | Static file serving | `text/html`, `text/css`, `application/javascript` |
| `POST` | `/upload` | Configuration file upload | `application/octet-stream` |
| `POST` | `/publish` | Publish message to topic | `application/json` |
| `POST` | `/generate-config` | Generate config from description | `application/json` |

### Example API Usage

**Publishing a Message:**
```bash
curl -X POST http://localhost:8080/publish \
  -H "Content-Type: application/json" \
  -d '{"topic": "INPUT1", "message": "42"}'
```

**Generating Configuration:**
```bash
curl -X POST http://localhost:8080/generate-config \
  -H "Content-Type: application/json" \
  -d '{"description": "Create a simple addition and increment pipeline"}' \
  --output generated-config.conf
```

## 📁 Project Structure

```
Computational-Graph/
├── src/
│   ├── Main.java                 # Application entry point
│   ├── servlets/                 # HTTP request handlers
│   │   ├── ConfLoader.java       # Configuration management
│   │   ├── TopicDisplayer.java   # Message publishing
│   │   └── HtmlLoader.java       # Static file server
│   ├── server/                   # HTTP server implementation
│   │   ├── MyHTTPServer.java     # Main server class
│   │   ├── RequestParser.java    # HTTP request parsing
│   │   └── Servlet.java          # Servlet interface
│   ├── graph/                    # Computational graph core
│   │   ├── Topic.java            # Publisher/subscriber topics
│   │   ├── Agent.java            # Computation agents
│   │   ├── Message.java          # Message data structure
│   │   └── TopicManagerSingleton.java
│   ├── configs/                  # Configuration management
│   └── views/                    # Visualization components
│       └── HtmlGraphWriter.java  # Graph-to-JSON converter
├── html_files/                   # Web frontend
│   ├── index.html               # Main application page
│   ├── form.html                # Configuration panel
│   ├── graph.html               # Graph visualization
│   ├── results.html             # Results display
│   ├── styles.css               # Application styles
│   └── *.js                     # JavaScript utilities
├── config_files/                # Sample configurations
│   └── simple.conf              # Example configuration
└── out/                         # Compiled Java classes
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

# Next agent
test.IncAgent
C
D
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
javac -cp src src/**/*.java -d out

# Run with custom port
java -cp out -Dport=9090 Main
```

### Adding New Agents
1. Implement the `Agent` interface
2. Add your agent class to the classpath
3. Reference it in configuration files

### Debugging
- Server logs appear in the console
- Browser developer tools for frontend debugging
- Check `config_files/` for uploaded configurations

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is part of an Advanced Programming course assignment.

## 👥 Authors

- **Omri Triki** - Backend development, server implementation
- **Yuval Disatnik** - Frontend development, graph visualization

---

*For questions or issues, please check the source code documentation or create an issue in the repository.*
