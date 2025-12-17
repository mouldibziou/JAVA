package smarthome.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import smarthome.core.CentralController;
import smarthome.core.Home;
import smarthome.core.Room;
import smarthome.devices.SmartDevice;
import smarthome.devices.Light2;
import smarthome.devices.Thermostat;
import smarthome.devices.MotionSensor;
import smarthome.devices.SmartTV;
import smarthome.devices.SmartAlarm;

import smarthome.automation.AutomationEngine;
import smarthome.automation.AutomationRule;
import smarthome.automation.Condition;
import smarthome.automation.Action;
import smarthome.automation.DeviceAction;
import smarthome.automation.GroupStateCondition;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalTime;

public class DashboardServer {

    private final CentralController controller;
    private final AutomationEngine automationEngine;
    private final int port;
    private final String webContentPath;

    public DashboardServer(CentralController controller, int port, String webContentPath) {
        this.controller = controller;
        this.automationEngine = new AutomationEngine(controller);
        this.port = port;
        this.webContentPath = webContentPath;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // API Endpoint
        server.createContext("/api/stats", new StatsHandler());
        server.createContext("/api/control", new ControlHandler());
        server.createContext("/api/rooms/add", new AddRoomHandler());
        server.createContext("/api/devices/add", new AddDeviceHandler());
        server.createContext("/api/rules/add", new AddRuleHandler());
        server.createContext("/api/rules/list", new ListRulesHandler());
        server.createContext("/api/schedule/check", new ScheduleHandler());
        server.createContext("/api/devices/search", new SearchHandler());
        server.createContext("/api/devices/remove", new RemoveDeviceHandler());
        server.createContext("/api/bulk/on", new BulkOnHandler());
        server.createContext("/api/bulk/off", new BulkOffHandler());

        // Static File Handler
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Dashboard server started on http://localhost:" + port);
    }

    private class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            try {
                // Trigger automation evaluation on stats refresh (simulation step)
                automationEngine.evaluateRules();

                String response = buildJsonStats();
                byte[] bytes = response.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // Allow CORS for dev
                t.sendResponseHeaders(200, bytes.length);
                OutputStream os = t.getResponseBody();
                os.write(bytes);
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
                t.sendResponseHeaders(500, 0);
                t.close();
            }
        }
    }

    private class ControlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            // Parse query params: /api/control?id=UUID&action=toggle&value=xxx
            String query = t.getRequestURI().getQuery();
            Map<String, String> params = queryToMap(query);

            String id = params.get("id");
            String action = params.get("action");
            String value = params.get("value");

            String response = "{}";
            int code = 400;

            if (id != null && action != null) {
                SmartDevice device = controller.findDeviceById(id);
                if (device != null) {
                    try {
                        if ("toggle".equalsIgnoreCase(action)) {
                            if (device.isOn())
                                device.turnOff();
                            else
                                device.turnOn();
                            response = "{\"status\":\"ok\", \"newState\":\"" + (device.isOn() ? "ON" : "OFF") + "\"}";
                        } else if ("on".equalsIgnoreCase(action)) {
                            device.turnOn();
                            response = "{\"status\":\"ok\", \"newState\":\"ON\"}";
                        } else if ("off".equalsIgnoreCase(action)) {
                            device.turnOff();
                            response = "{\"status\":\"ok\", \"newState\":\"OFF\"}";
                        } else if ("setBrightness".equalsIgnoreCase(action) && device instanceof Light2 light) {
                            if (value != null) {
                                int b = Integer.parseInt(value);
                                light.setBrightness(b);
                                response = "{\"status\":\"ok\", \"message\":\"Brightness set to " + b + "\"}";
                            }
                        } else if ("setTargetTemperature".equalsIgnoreCase(action)
                                && device instanceof Thermostat thermostat) {
                            if (value != null) {
                                double temp = Double.parseDouble(value);
                                thermostat.setTargetTemperature(temp);
                                response = "{\"status\":\"ok\", \"message\":\"Target temp set to " + temp + "\"}";
                            }
                        } else {
                            response = "{\"error\":\"Invalid action or device type\"}";
                            code = 400;
                        }
                        if (code == 400 && response.contains("status"))
                            code = 200; // Fix code if status ok
                    } catch (Exception e) {
                        code = 500;
                        response = "{\"error\":\"" + e.getMessage() + "\"}";
                    }
                } else {
                    code = 404;
                    response = "{\"error\":\"Device not found\"}";
                }
            } else {
                response = "{\"error\":\"Missing id or action\"}";
            }

            sendJson(t, code, response);
        }
    }

    private class ScheduleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            Map<String, String> params = queryToMap(query);
            String timeStr = params.get("time");

            if (timeStr != null) {
                try {
                    LocalTime time = LocalTime.parse(timeStr);
                    controller.checkSchedules(time);
                    sendJson(t, 200, "{\"status\":\"ok\", \"message\":\"Processed schedule for " + timeStr + "\"}");
                } catch (Exception e) {
                    sendJson(t, 500, "{\"error\":\"Invalid time format (HH:mm)\"}");
                }
            } else {
                sendJson(t, 400, "{\"error\":\"Missing time parameter\"}");
            }
        }
    }

    private class AddRoomHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            Map<String, String> params = queryToMap(query);
            String name = params.get("name");

            String response = "{}";
            int code = 400;

            if (name != null && !name.trim().isEmpty()) {
                try {
                    controller.addRoom(new Room(name));
                    code = 200;
                    response = "{\"status\":\"ok\", \"message\":\"Room added\"}";
                } catch (Exception e) {
                    code = 500;
                    response = "{\"error\":\"" + e.getMessage() + "\"}";
                }
            } else {
                response = "{\"error\":\"Missing room name\"}";
            }
            sendJson(t, code, response);
        }
    }

    private class AddDeviceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            Map<String, String> params = queryToMap(query);

            String room = params.get("room");
            String name = params.get("name");
            String type = params.get("type");

            System.out.println("=== ADD DEVICE REQUEST ===");
            System.out.println("Room: " + room);
            System.out.println("Name: " + name);
            System.out.println("Type: " + type);

            String response = "{}";
            int code = 400;

            if (room != null && name != null && type != null) {
                try {
                    SmartDevice device = null;
                    switch (type.toLowerCase()) {
                        case "light" -> device = new Light2(name, 0);
                        case "thermostat" -> device = new Thermostat(name);
                        case "motionsensor" -> device = new MotionSensor(name, 5);
                        case "smarttv" -> device = new SmartTV(name);
                        case "smartalarm" -> device = new SmartAlarm(name, 1234); // Default PIN
                        default -> throw new IllegalArgumentException("Unknown device type: " + type);
                    }

                    if (device != null) {
                        System.out.println("Device created: " + device.getName() + " (ID: " + device.getId() + ")");
                        System.out.println("Adding to room: " + room);
                        controller.addDeviceToRoom(room, device);
                        System.out.println("Device added successfully!");
                        code = 200;
                        response = "{\"status\":\"ok\", \"message\":\"Device added\"}";
                    }
                } catch (Exception e) {
                    System.err.println("ERROR adding device: " + e.getMessage());
                    e.printStackTrace();
                    code = 500;
                    response = "{\"error\":\"" + e.getMessage() + "\"}";
                }
            } else {
                System.out.println("Missing parameters!");
                response = "{\"error\":\"Missing parameters (room, name, type)\"}";
            }
            System.out.println("Response code: " + code);
            System.out.println("Response: " + response);
            System.out.println("=========================");
            sendJson(t, code, response);
        }
    }

    private class AddRuleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            // POST request with JSON
            if ("POST".equals(t.getRequestMethod())) {
                InputStream is = t.getRequestBody();
                String body = new BufferedReader(new InputStreamReader(is))
                        .lines().collect(Collectors.joining("\n"));

                // Parse simple JSON manually (assuming format)
                Map<String, String> json = parseSimpleJson(body);

                String name = json.get("name");
                String triggerId = json.get("triggerDevice");
                String triggerState = json.get("triggerState"); // ON/OFF
                String targetId = json.get("targetDevice");
                String actionStr = json.get("action"); // turnOn/turnOff

                if (name != null && triggerId != null && targetId != null) {
                    try {
                        // Create condition: Check if trigger device is in the required state
                        // For simplicity, we'll use the device's type and check ANY device of that type
                        SmartDevice triggerDevice = controller.findDeviceById(triggerId);
                        if (triggerDevice == null) {
                            sendJson(t, 404, "{\"error\":\"Trigger device not found\"}");
                            return;
                        }

                        boolean state = "ON".equalsIgnoreCase(triggerState);
                        String deviceType = triggerDevice.getClass().getSimpleName();
                        Condition condition = new GroupStateCondition(deviceType, state, false); // ANY device of this
                                                                                                 // type

                        // Create Action - convert "turnOn"/"turnOff" to "ON"/"OFF"
                        String command = actionStr.equalsIgnoreCase("turnOn") ? "ON" : "OFF";
                        Action action = new DeviceAction(command, targetId, false); // false = target by ID

                        AutomationRule rule = new AutomationRule(name, condition, action);
                        automationEngine.addRule(rule);

                        sendJson(t, 200, "{\"status\":\"ok\", \"message\":\"Rule created\"}");
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendJson(t, 500, "{\"error\":\"" + e.getMessage() + "\"}");
                    }
                } else {
                    sendJson(t, 400, "{\"error\":\"Missing fields\"}");
                }
            } else {
                sendJson(t, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    private class ListRulesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            List<AutomationRule> rules = automationEngine.getRules();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < rules.size(); i++) {
                AutomationRule r = rules.get(i);
                json.append("{")
                        .append("\"name\":\"").append(r.getName()).append("\",")
                        .append("\"active\":").append(r.isActive())
                        .append("}");
                if (i < rules.size() - 1)
                    json.append(",");
            }
            json.append("]");
            sendJson(t, 200, json.toString());
        }
    }

    private class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            Map<String, String> params = queryToMap(query);
            String q = params.get("q");

            if (q != null && !q.trim().isEmpty()) {
                q = q.toLowerCase();
                // Search by ID or Type
                List<SmartDevice> result = new java.util.ArrayList<>();

                // 1. Try finding by ID directly
                // 1. Try finding by ID directly (Logic integrated into loop below)
                // SmartDevice byId = controller.findDeviceById(q);

                // Better: iterate all rooms and find matches
                for (Room r : controller.getHome().getRooms()) {
                    for (SmartDevice d : r.getDevices()) {
                        if (d.getId().toLowerCase().contains(q) || d.getType().toLowerCase().contains(q)
                                || d.getName().toLowerCase().contains(q)) {
                            result.add(d);
                        }
                    }
                }

                // Build JSON response
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < result.size(); i++) {
                    SmartDevice d = result.get(i);
                    json.append("{");
                    json.append("\"id\":\"").append(d.getId()).append("\",");
                    json.append("\"name\":\"").append(d.getName()).append("\",");
                    json.append("\"type\":\"").append(d.getType()).append("\",");
                    json.append("\"room\":\"").append(d.getRoomName()).append("\"");
                    json.append("}");
                    if (i < result.size() - 1)
                        json.append(",");
                }
                json.append("]");
                sendJson(t, 200, json.toString());
            } else {
                sendJson(t, 400, "{\"error\":\"Missing query parameter 'q'\"}");
            }
        }
    }

    private class RemoveDeviceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            Map<String, String> params = queryToMap(query);
            String id = params.get("id");

            if (id != null) {
                SmartDevice device = controller.findDeviceById(id);
                if (device != null) {
                    try {
                        controller.removeDeviceFromRoom(device.getRoomName(), device);
                        sendJson(t, 200, "{\"status\":\"ok\", \"message\":\"Device removed\"}");
                    } catch (Exception e) {
                        sendJson(t, 500, "{\"error\":\"" + e.getMessage() + "\"}");
                    }
                } else {
                    sendJson(t, 404, "{\"error\":\"Device not found\"}");
                }
            } else {
                sendJson(t, 400, "{\"error\":\"Missing id\"}");
            }
        }
    }

    private class BulkOnHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            controller.turnOnAllDevices();
            sendJson(t, 200, "{\"status\":\"ok\", \"message\":\"All devices turned ON\"}");
        }
    }

    private class BulkOffHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            controller.turnOffAllDevices();
            sendJson(t, 200, "{\"status\":\"ok\", \"message\":\"All devices turned OFF\"}");
        }
    }

    private Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.replace("{", "").replace("}", "").replace("\"", "");
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] entry = pair.split(":");
            if (entry.length == 2) {
                map.put(entry[0].trim(), entry[1].trim());
            }
        }
        return map;
    }

    private void sendJson(HttpExchange t, int code, String response) throws IOException {
        t.getResponseHeaders().set("Content-Type", "application/json");
        t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        t.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
        t.getResponseHeaders().set("Pragma", "no-cache");
        t.getResponseHeaders().set("Expires", "0");
        byte[] bytes = response.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        t.sendResponseHeaders(code, bytes.length);
        OutputStream os = t.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String uri = t.getRequestURI().toString();
            if (uri.equals("/"))
                uri = "/index.html";

            Path path = Paths.get(webContentPath + uri);
            if (Files.exists(path) && !Files.isDirectory(path)) {
                String contentType = determineContentType(path);
                t.getResponseHeaders().set("Content-Type", contentType);
                t.sendResponseHeaders(200, Files.size(path));
                OutputStream os = t.getResponseBody();
                Files.copy(path, os);
                os.close();
            } else {
                String response = "404 (Not Found)\n";
                t.sendResponseHeaders(404, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    private String determineContentType(Path path) {
        String s = path.toString().toLowerCase();
        if (s.endsWith(".html"))
            return "text/html";
        if (s.endsWith(".css"))
            return "text/css";
        if (s.endsWith(".js"))
            return "application/javascript";
        return "text/plain";
    }

    private Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null)
            return result;
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                try {
                    result.put(entry[0], java.net.URLDecoder.decode(entry[1], "UTF-8"));
                } catch (java.io.UnsupportedEncodingException e) {
                    result.put(entry[0], entry[1]);
                }
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    private String buildJsonStats() {
        // ... (existing implementation) ...
        // Manual JSON construction to avoid external dependencies
        StringBuilder json = new StringBuilder();
        json.append("{");

        // Total Energy
        double totalEnergy = controller.getTotalEnergyConsumption();
        json.append("\"totalEnergy\": ").append(totalEnergy).append(",");

        // Rooms
        json.append("\"rooms\": [");
        List<Room> rooms = controller.getHome().getRooms();

        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            json.append("{");
            json.append("\"name\": \"").append(r.getName()).append("\",");
            json.append("\"devices\": [");

            List<SmartDevice> devices = r.getDevices();
            for (int j = 0; j < devices.size(); j++) {
                SmartDevice d = devices.get(j);
                json.append("{");
                json.append("\"id\": \"").append(d.getId()).append("\",");
                json.append("\"name\": \"").append(d.getName()).append("\",");
                json.append("\"type\": \"").append(d.getType()).append("\",");
                json.append("\"status\": \"").append(d.getStatus()).append("\",");
                json.append("\"energy\": ").append(d.getEnergyConsumption());

                json.append("}");
                if (j < devices.size() - 1)
                    json.append(",");
            }
            json.append("]");
            json.append("}");
            if (i < rooms.size() - 1)
                json.append(",");
        }
        json.append("],");

        // Rules
        json.append("\"rules\": [");
        List<AutomationRule> rules = automationEngine.getRules();
        for (int i = 0; i < rules.size(); i++) {
            AutomationRule r = rules.get(i);
            json.append("{")
                    .append("\"name\":\"").append(r.getName()).append("\",")
                    .append("\"active\":").append(r.isActive())
                    .append("}");
            if (i < rules.size() - 1)
                json.append(",");
        }
        json.append("]");

        json.append("}");
        return json.toString();
    }
}
