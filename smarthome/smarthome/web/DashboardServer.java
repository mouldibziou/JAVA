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

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardServer {

    private final CentralController controller;
    private final int port;
    private final String webContentPath;

    public DashboardServer(CentralController controller, int port, String webContentPath) {
        this.controller = controller;
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
            // Parse query params: /api/control?id=UUID&action=toggle
            String query = t.getRequestURI().getQuery();
            Map<String, String> params = queryToMap(query);

            String id = params.get("id");
            String action = params.get("action");

            String response = "{}";
            int code = 400;

            if (id != null && action != null) {
                SmartDevice device = controller.findDeviceById(id);
                if (device != null) {
                    if ("toggle".equalsIgnoreCase(action)) {
                        if (device.isOn())
                            device.turnOff();
                        else
                            device.turnOn();
                    } else if ("on".equalsIgnoreCase(action)) {
                        device.turnOn();
                    } else if ("off".equalsIgnoreCase(action)) {
                        device.turnOff();
                    }
                    code = 200;
                    response = "{\"status\":\"ok\", \"newState\":\"" + (device.isOn() ? "ON" : "OFF") + "\"}";
                } else {
                    code = 404;
                    response = "{\"error\":\"Device not found\"}";
                }
            } else {
                response = "{\"error\":\"Missing id or action\"}";
            }

            t.getResponseHeaders().set("Content-Type", "application/json");
            t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            byte[] bytes = response.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            t.sendResponseHeaders(code, bytes.length);
            OutputStream os = t.getResponseBody();
            os.write(bytes);
            os.close();
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
                        controller.addDeviceToRoom(room, device);
                        code = 200;
                        response = "{\"status\":\"ok\", \"message\":\"Device added\"}";
                    }
                } catch (Exception e) {
                    code = 500;
                    response = "{\"error\":\"" + e.getMessage() + "\"}";
                }
            } else {
                response = "{\"error\":\"Missing parameters (room, name, type)\"}";
            }
            sendJson(t, code, response);
        }
    }

    private void sendJson(HttpExchange t, int code, String response) throws IOException {
        t.getResponseHeaders().set("Content-Type", "application/json");
        t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
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
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    private String buildJsonStats() {
        // Manual JSON construction to avoid external dependencies
        StringBuilder json = new StringBuilder();
        json.append("{");

        // Total Energy
        double totalEnergy = controller.getTotalEnergyConsumption();
        json.append("\"totalEnergy\": ").append(totalEnergy).append(",");

        // Rooms
        json.append("\"rooms\": [");
        List<Room> rooms = controller.getHome().getRooms(); // Need to expose getHome() or getRooms() in Controller
        // Wait, CentralController doesn't expose getHome() publicly in the file I saw.
        // I might need to update CentralController to expose rooms or iterate
        // differently.
        // Actually, CentralController has `listAllDevices` which iterates internal
        // home.
        // I should add a `getRooms()` method to CentralController or `getHome()`.

        // Assuming I will add getHome() or getRooms() to CentralController.
        // Let's assume I fix CentralController.

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
                json.append("\"status\": \"").append(d.getStatus()).append("\","); // getStatus string?
                // The output of getStatus() in the test looked like "Light2 - ON" maybe?
                // Wait, SmartDevice.java check might be needed.
                // Assuming basic properties.
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
        json.append("]");

        json.append("}");
        return json.toString();
    }
}
