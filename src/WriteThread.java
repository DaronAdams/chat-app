import java.io.Console;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class WriteThread {
    private PrintWriter writer;
    private final Socket socket;
    private final Client client;

    public WriteThread(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;

        try {
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
        } catch (IOException e) {
            System.out.println("Error getting output stream: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void start() {
        Console console = System.console();

        String username = console.readLine("\nEnter your username: ");
        client.setUserName(username);
        writer.println(username);

        String text;

        do {
            text = console.readLine("[" + username + "]: ");
            writer.println(text);
        } while (!text.equals("quit"));

        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Error writing to the server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
