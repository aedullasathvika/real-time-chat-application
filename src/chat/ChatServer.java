package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static final int DEFAULT_PORT = 5000;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private final Set<String> usernames = Collections.synchronizedSet(new LinkedHashSet<>());
    private final int port;

    public ChatServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? parsePort(args[0]) : DEFAULT_PORT;
        new ChatServer(port).start();
    }

    public void start() {
        System.out.println("Chat server starting on port " + port + "...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Chat server is running. Waiting for clients...");

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                new Thread(clientHandler, "client-" + socket.getPort()).start();
            }
        } catch (IOException error) {
            System.err.println("Server error: " + error.getMessage());
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

    private void broadcast(String message) {
        String stampedMessage = "[" + LocalTime.now().format(TIME_FORMAT) + "] " + message;
        System.out.println(stampedMessage);

        for (ClientHandler client : clients) {
            client.send(stampedMessage);
        }
    }

    private void sendUserList(ClientHandler receiver) {
        synchronized (usernames) {
            receiver.send("Online users: " + String.join(", ", usernames));
        }
    }

    private String registerUsername(BufferedReader input, PrintWriter output) throws IOException {
        while (true) {
            output.println("Enter your username:");
            String requestedName = input.readLine();

            if (requestedName == null) {
                return null;
            }

            requestedName = requestedName.trim();
            if (requestedName.isEmpty()) {
                output.println("Username cannot be empty.");
                continue;
            }

            synchronized (usernames) {
                if (usernames.contains(requestedName)) {
                    output.println("Username already taken. Try another one.");
                } else {
                    usernames.add(requestedName);
                    return requestedName;
                }
            }
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter output;
        private String username;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
            ) {
                output = writer;
                username = registerUsername(input, output);

                if (username == null) {
                    return;
                }

                output.println("Welcome, " + username + "!");
                output.println("Commands: /users, /quit");
                broadcast(username + " joined the chat.");

                String message;
                while ((message = input.readLine()) != null) {
                    message = message.trim();

                    if (message.isEmpty()) {
                        continue;
                    }

                    if ("/quit".equalsIgnoreCase(message)) {
                        output.println("Goodbye!");
                        break;
                    }

                    if ("/users".equalsIgnoreCase(message)) {
                        sendUserList(this);
                        continue;
                    }

                    broadcast(username + ": " + message);
                }
            } catch (IOException error) {
                System.err.println("Client connection error: " + error.getMessage());
            } finally {
                disconnect();
            }
        }

        void send(String message) {
            if (output != null) {
                output.println(message);
            }
        }

        private void disconnect() {
            clients.remove(this);

            if (username != null) {
                usernames.remove(username);
                broadcast(username + " left the chat.");
            }

            try {
                socket.close();
            } catch (IOException error) {
                System.err.println("Could not close client socket: " + error.getMessage());
            }
        }
    }
}
