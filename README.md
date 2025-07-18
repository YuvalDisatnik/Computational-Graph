# Computational Graph Web Application

A modular, extensible simulation of a computational graph system using a publisher/subscriber architecture. Designed for educational purposes, this project lets you create, visualize, and interact with computational graphs in real time.

---

## 🌟 Key Features

- **Interactive Web Interface** – Modern, responsive UI for easy experimentation
- **Real-time Visualization** – The presented graph is fully interactive: you can move, scale, and explore nodes and edges dynamically
- **Configuration Management** – Upload and manage computational graph configurations with instant validation
- **Message Publishing** – Send messages to topics in real time and see immediate feedback
- **Supported Math Operations** – Increment, Decrement, Addition, Subtraction, Multiplication, Division

---

## 🚀 Quick Start

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
4. **Open the App**
   - Go to: [http://localhost:8080/app/index.html](http://localhost:8080/app/index.html)

---

## 📝 How to Use

### 1. Upload a Configuration

- Go to the Configuration section
- Select a `.conf` file (see format below)
- Click "Deploy" to activate

### 2. Send Messages

- Enter a topic name and message
- Click "Send" or press Enter
- **Note:** The topic must already exist in the graph (i.e., be defined in your configuration file). You cannot send messages to topics that are not part of the current graph.
- **Note:** The value you send must be valid for the intended mathematical operation (e.g., a number for arithmetic operations, and avoid invalid cases like division by zero).

### 3. View & Interact with the Graph

- The graph updates automatically when configurations are deployed
- **You can move, zoom, and interact with nodes and edges** for better exploration

---

## 📄 Configuration File Format

- Each agent requires **exactly 3 lines**:
  1. Fully qualified class name (e.g., `configs.PlusAgent`)
  2. Input topics (comma-separated)
  3. Output topics (comma-separated)
- **Important:** The configuration file must end with an empty line (a blank line at the end of the file).

**Example:**

```
configs.PlusAgent
A,B
C
configs.IncAgent
C
RESULT

```

---

## ➕ Supported Math Operations

- Increment
- Decrement
- Addition
- Subtraction
- Multiplication
- Division

Reference the corresponding agent classes in your configuration files (e.g., `configs.PlusAgent` for addition).

---

## 🎯 Example Configurations

**Simple Addition Pipeline:**

```
configs.PlusAgent
INPUT1,INPUT2
SUM
configs.IncAgent
SUM
RESULT

```

**Complex Processing Chain:**

```
configs.PlusAgent
A,B
SUM1
configs.MulAgent
SUM1,C
PROD1
configs.SubAgent
PROD1,D
DIFF1
configs.IncAgent
DIFF1
INC1
configs.DivAgent
INC1,E
DIV1
configs.DecAgent
DIV1
DEC1
configs.PlusAgent
DEC1,F
SUM2
configs.MulAgent
SUM2,G
FINAL_RESULT

```

---

## 🔌 API Reference

| Method | Endpoint   | Description              |
| ------ | ---------- | ------------------------ |
| `GET`  | `/app/*`   | Static file serving      |
| `POST` | `/upload`  | Configuration upload     |
| `POST` | `/publish` | Publish message to topic |

---

## 🛠️ Troubleshooting

- Ensure your `.conf` file has exactly 3 lines per agent and ends with an empty line
- Use UTF-8 encoding for configuration files
- If port 8080 is in use, edit the port in `Main.java` and recompile
- **You can only send messages to topics that exist in the current graph.**
- **The value you send must be valid for the operation (e.g., numbers for math, no division by zero, etc.).**

---

## 👥 Authors

- **Omri Triki**
- **Yuval Disatnik**

---

## 📄 License

This project is part of an Advanced Programming course assignment.
