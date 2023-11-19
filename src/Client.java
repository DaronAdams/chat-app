package client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private int port;
    private String userName;
    private String hostname;

    public Client(int port, String hostname) {
        this.port = port;
        this.hostname = hostname;
    }

    public void run() throws IOException {
        try {
            final Socket socket = new Socket(hostname, port);

            System.out.println("Connected to the chat room.");

            new ReadThread(socket, this).start();
            new WriteThread(socket, this).start();
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("IOException: " + ex.getMessage());
        }
    }

    /*
    * Sets the current username
     */
    void setUserName(String userName) {
        this.userName = userName;
    }

    /*
    * Returns the current username
     */
    String getUserName() {
        return this.userName;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        Client client = new Client(port, hostname);
        client.run();
    }
}
