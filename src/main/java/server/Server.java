package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final int PORT = 8080;
    private static Set<Handler> clients = new HashSet<>();
    private static final int MAX_CHAT_HISTORY = 50; // Store last 50 messages
    private static Queue<String> chatHistory = new LinkedList<>();
    private static Map<String, Handler> clientMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println("The chat server is running.");
        try (ServerSocket listener = new ServerSocket(PORT)) {
            while (true) {
                new Handler(listener.accept()).start();
            }
        }
    }

    private static class Handler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private String username;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String input;

                username = in.readLine();
                synchronized (clientMap) {
                    clientMap.put(username, this);
                    clients.add(this);
                }

                synchronized (chatHistory) {
                    for (String historyMsg : chatHistory) {
                        out.println(historyMsg);
                    }
                }

                while ((input = in.readLine()) != null) {
                    if (input.startsWith("/")) {
                        handleServerCommand(input, this);
                    } else {
                        broadcastMessage(input);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (username != null && !username.isEmpty()) {
                    synchronized (clients) {
                        clients.remove(this);
                    }
                    synchronized (clientMap) {
                        clientMap.remove(username);
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void broadcastMessage(String message) {
            synchronized (chatHistory) {
                chatHistory.add(message);
                if (chatHistory.size() > MAX_CHAT_HISTORY) {
                    chatHistory.remove();
                }
                for (Handler client : clients) {
                    client.out.println(message);
                }
            }
        }

        private void handleServerCommand(String command, Handler sender) {
            if (command.startsWith("/list")) {
                StringBuilder userList = new StringBuilder();
                synchronized (clientMap) {
                    for (String user: clientMap.keySet()) {
                        userList.append(user).append(", ");
                    }
                }
                if (userList.length() > 0) {
                    userList.setLength(userList.length() - 2);
                }
                out.println("Connected users: " + userList);
            }

        }
    }
}
