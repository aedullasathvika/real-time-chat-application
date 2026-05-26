package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5000;

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? parsePort(args[1]) : DEFAULT_PORT;

        new ChatClient().start(host, port);
    }

    public void start(String host, int port) {
        System.out.println("Connecting to chat server at " + host + ":" + port + "...");

        try (
            Socket socket = new Socket(host, port);
            BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter serverOutput = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader keyboardInput = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Connected. Type /quit to exit.");

            Thread listener = new Thread(() -> listenForServerMessages(serverInput), "server-listener");
            listener.setDaemon(true);
            listener.start();

            String userInput;
            while ((userInput = keyboardInput.readLine()) != null) {
                serverOutput.println(userInput);

                if ("/quit".equalsIgnoreCase(userInput.trim())) {
                    break;
                }
            }
        } catch (IOException error) {
            System.err.println("Client error: " + error.getMessage());
        }
    }

    private static void listenForServerMessages(BufferedReader serverInput) {
        try {
            String message;
            while ((message = serverInput.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException error) {
            System.out.println("Disconnected from server.");
        }
    }

    private static int parsePort(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException error) {
            System.err.println("Invalid port '" + value + "'. Using default port " + DEFAULT_PORT + ".");
            return DEFAULT_PORT;
        }
    }
}
