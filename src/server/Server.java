package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private static final int PORT = 8080;
    private static Set<Handler> clients = new HashSet<>();

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

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                clients.add(this);

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String input;
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
                if (out != null) {
                    clients.remove(this);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void broadcastMessage(String message) {
            for (Handler client : clients) {
                client.out.println(message);
            }
        }

        private void handleServerCommand(String command, Handler sender) {
            // Server-side command processing logic
            if (command.startsWith("/list")) {

            }
            // Add more server-side commands here
        }
    }
}
