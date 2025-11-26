package com.tcon.webid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class WebidApplication {

    public static void main(String[] args) {
        // Determine port: prefer application property or environment variable, fallback to 8080
        String portEnv = System.getProperty("server.port");
        if (portEnv == null || portEnv.trim().isEmpty()) {
            portEnv = System.getenv("PORT");
        }

        int port = 8080;
        try {
            if (portEnv != null && !portEnv.trim().isEmpty()) {
                port = Integer.parseInt(portEnv.trim());
            }
        } catch (NumberFormatException ignored) { }

        int chosenPort = findAvailablePort(port);
        if (chosenPort != port) {
            System.out.println("Port " + port + " is in use; switching to available port " + chosenPort);
        }

        Map<String, Object> props = new HashMap<>();
        props.put("server.port", String.valueOf(chosenPort));
        SpringApplication app = new SpringApplication(WebidApplication.class);
        app.setDefaultProperties(props);
        app.run(args);
    }

    private static int findAvailablePort(int preferred) {
        // Try the preferred port, then fall back to ephemeral port if in use
        try (ServerSocket socket = new ServerSocket(preferred)) {
            socket.setReuseAddress(true);
            return preferred;
        } catch (Exception e) {
            // preferred port not available - ask OS for a free port
            try (ServerSocket socket = new ServerSocket(0)) {
                socket.setReuseAddress(true);
                return socket.getLocalPort();
            } catch (Exception ex) {
                // If we can't find any port, fall back to 8080 (may still fail)
                return 8080;
            }
        }
    }
}
