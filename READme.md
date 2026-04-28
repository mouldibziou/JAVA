# Smart Home System — Java

A Java-based Smart Home simulation system that models a connected home with multiple rooms, smart devices, an automation engine, and a live web dashboard.

---

## Table of Contents

- [Overview](#overview)
- [Project Structure](#project-structure)
- [Features](#features)
- [Architecture](#architecture)
  - [Core](#core)
  - [Devices](#devices)
  - [Automation](#automation)
  - [Web Dashboard](#web-dashboard)
  - [Exceptions](#exceptions)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Compile](#compile)
  - [Run](#run)
- [Usage Examples](#usage-examples)
- [Web Dashboard](#web-dashboard-1)

---

## Overview

The Smart Home System lets you:

- Model a home with multiple rooms and smart devices
- Control devices individually or in bulk (turn on/off all, per-room)
- Monitor and optimise energy consumption
- Define automation rules that trigger device actions based on sensor readings, time, or thresholds
- Manage everything through a browser-based dashboard served directly from the Java application

---

## Project Structure

```
JAVA/
├── CentralController.java          # Entry-point copy (see smarthome/core/)
└── smarthome/
    ├── smarthome/
    │   ├── SmartHomeTest.java       # Main demo / server launcher
    │   ├── AutomationTest.java      # Automation engine demo
    │   ├── core/
    │   │   ├── Home.java            # Home entity
    │   │   ├── Room.java            # Room entity
    │   │   ├── CentralController.java
    │   │   └── HomeSize.java        # Smart-scoring enum
    │   ├── devices/
    │   │   ├── SmartDevice.java     # Abstract base class
    │   │   ├── Light2.java
    │   │   ├── Thermostat.java
    │   │   ├── MotionSensor.java
    │   │   ├── SmartTV.java
    │   │   ├── SmartAlarm.java
    │   │   ├── Controllable.java    # Interface
    │   │   ├── EnergyConsumer.java  # Interface
    │   │   └── Schedulable.java     # Interface
    │   ├── automation/
    │   │   ├── AutomationEngine.java
    │   │   ├── AutomationRule.java
    │   │   ├── Action.java / DeviceAction.java
    │   │   ├── Condition.java
    │   │   ├── SensorCondition.java
    │   │   ├── ThresholdCondition.java
    │   │   ├── TimeCondition.java
    │   │   └── GroupStateCondition.java
    │   ├── exceptions/
    │   │   ├── DeviceNotFoundException.java
    │   │   └── InvalidDeviceOperationException.java
    │   └── web/
    │       └── DashboardServer.java # Built-in HTTP server (port 8085)
    └── web-content/
        ├── index.html               # Dashboard UI
        ├── style.css
        └── app.js
```

---

## Features

| Feature | Description |
|---|---|
| Room management | Add / remove rooms; enforce unique names and max-room limits |
| Device management | Add / remove devices to rooms; search by ID or type |
| Bulk control | Turn all devices on or off globally or per room |
| Energy monitoring | Query real-time total energy consumption across all devices |
| Energy optimisation | Automatically dim lights, activate ECO mode, and adjust thermostats |
| Smart scoring | Classify the home (SMALL / MEDIUM / LARGE / SMART) based on device count |
| Automation engine | Rule-based engine with sensor, threshold, time, and group-state conditions |
| Web dashboard | Live browser UI for monitoring, control, and rule management |

---

## Architecture

### Core

| Class | Responsibility |
|---|---|
| `Home` | Owns rooms; tracks owner, address, and capacity |
| `Room` | Contains a list of `SmartDevice` instances |
| `CentralController` | Facade over `Home`; exposes all control and query operations |
| `HomeSize` | Enum that maps device count to a smart-scoring category |

### Devices

All devices extend the abstract `SmartDevice` class, which implements the `Controllable` and `EnergyConsumer` interfaces.

| Device | Notable behaviour |
|---|---|
| `Light2` | Adjustable brightness; dims to ≤ 40 % during energy optimisation |
| `Thermostat` | Tracks current & target temperature |
| `MotionSensor` | Fires motion-detection events |
| `SmartTV` | Channel and volume control |
| `SmartAlarm` | Arm / disarm with PIN |

### Automation

The `AutomationEngine` evaluates a list of `AutomationRule` objects on demand. Each rule pairs a **Condition** with an **Action**.

| Condition type | Triggers when… |
|---|---|
| `SensorCondition` | A named sensor is in the expected on/off state |
| `ThresholdCondition` | A device metric (e.g. temperature) crosses a threshold |
| `TimeCondition` | The current time matches the configured hour/minute |
| `GroupStateCondition` | All devices in a group share a common state |

Actions are represented by `DeviceAction`, which sends an `ON` or `OFF` command to a target device by its UUID.

### Web Dashboard

`DashboardServer` embeds Java's built-in `com.sun.net.httpserver.HttpServer` and exposes a REST-style API consumed by the single-page frontend in `web-content/`.

| Endpoint | Purpose |
|---|---|
| `GET /stats` | Energy stats and room/device list |
| `POST /control` | Turn a device on or off |
| `POST /addRoom` | Add a new room |
| `POST /addDevice` | Add a device to a room |
| `POST /addRule` | Create an automation rule |
| `GET /rules` | List active automation rules |
| `GET /` | Serves the static dashboard files |

### Exceptions

| Exception | Thrown when |
|---|---|
| `DeviceNotFoundException` | A room or device lookup returns no result |
| `InvalidDeviceOperationException` | An operation is illegal for the device's current state |

---

## Getting Started

### Prerequisites

- Java 17 or later (uses pattern-matching `instanceof`)
- No external build tool required (plain `javac`)

### Compile

From the `smarthome/` directory:

```bash
javac -d out $(find smarthome -name "*.java")
```

### Run

**Full demo + dashboard server:**

```bash
java -cp out smarthome.SmartHomeTest
```

The application will run the device demo and then start the dashboard at **http://localhost:8085**.

**Automation engine demo only:**

```bash
java -cp out smarthome.AutomationTest
```

---

## Usage Examples

```java
// Create a home and controller
Home home = new Home(1, "Alice", 5, "Tunis");
CentralController controller = new CentralController(home);

// Add rooms
controller.addRoom(new Room("Living Room"));
controller.addRoom(new Room("Kitchen"));

// Add devices
SmartDevice light = new Light2("Ceiling Light", 80);
controller.addDeviceToRoom("Living Room", light);

// Control devices
controller.turnOnAllDevices();
System.out.println("Energy: " + controller.getTotalEnergyConsumption() + " W");
controller.optimizeEnergy();
controller.turnOffAllDevices();
```

**Automation rule example:**

```java
AutomationEngine engine = new AutomationEngine(controller);

Condition motionDetected = new SensorCondition("MotionSensor", true);
Action turnOnLight      = new DeviceAction("ON", light.getId(), false);
engine.addRule(new AutomationRule("Motion Lights", motionDetected, turnOnLight));

// Trigger evaluation (call this on a schedule or event)
engine.evaluateRules();
```

---

## Web Dashboard

Once the server is running, open **http://localhost:8085** in your browser to:

- View total energy consumption and all rooms with their devices
- Toggle individual devices on or off
- Add rooms and devices dynamically
- Create and view automation rules
- Run time-based schedule simulations
