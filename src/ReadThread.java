import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ReadThread extends Thread {
    private BufferedReader reader;
    final private Socket socket;
    final private Client client;

    public ReadThread(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;

        try {
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException e) {
            System.out.println("Input stream error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void start() {
        while (true) {
            try {
                String response = reader.readLine();
                System.out.println("\n" + response);

                // Printing the username after the server message
                if (client.getUserName() != null) {
                    System.out.println("[" + client.getUserName() + "]: ");
                }
            } catch (IOException e) {
                System.out.println("Error reading from the server: " + e.getMessage());
                e.printStackTrace();
                break;
            }
        }
    }
}
