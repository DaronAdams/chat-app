package server;

import java.io.*;
import java.net.Socket;

public class ChatThread extends Thread {
    private Socket socket;
    private Server server;
    private PrintWriter printWriter;

    public ChatThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void start() {
        try {
            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            OutputStream outputStream = socket.getOutputStream();
            printWriter = new PrintWriter(outputStream, true);

            String userName = reader.readLine();
            server.addUser(userName);

            String message = userName + " has connected.";
            server.sendMessageToAllClients(message, this);

            String clientMessage;

            do {
                clientMessage = reader.readLine();
                message = "[" + userName + "]: " + clientMessage;
                server.sendMessageToAllClients(message, this);
            } while (!clientMessage.equals("quit"));

            server.deleteUser(userName, this);
            socket.close();

            message = userName + "has disconnected.";
            server.sendMessageToAllClients(message, this);

        } catch (IOException e) {
            System.out.println("Error has occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
