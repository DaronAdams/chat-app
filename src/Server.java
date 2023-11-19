import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private int port;
    private Set<String> users = new HashSet<String>();
    private Set<ChatThread> chatThreads = new HashSet<>();

    public Server(int port) {
        this.port = port;
    }

    public void run() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port: " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New user has connected.");

                ChatThread newUser = new ChatThread(socket, this);
                chatThreads.add(newUser);
                newUser.start();
            }
        } catch (IOException exception) {
            System.out.println("Error in the server: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    /*
    * Sends a message to connected clients except the user who sent the message
     */
    void sendMessageToAllClients(String message, ChatThread excludeUser) {
        for (ChatThread user: chatThreads) {
            if (user != excludeUser) {
                user.sendMessage(message);
            }
        }
    }

    /*
    * Helper method to add users to the list of connected users
     */
    void addUser(String userName) {
        users.add(userName);
    }

    /*
    * Helper method to remove users from the list of connected users
     */
    void deleteUser(String userName, ChatThread user) {
        boolean isDeleted = users.remove(userName);
        if (isDeleted) {
            chatThreads.remove(userName);
            System.out.println(userName + " has disconnected.");
        }
    }

    /*
    * Returns the list of users
     */
    Set<String> getUsers() {
        return this.users;
    }

    /*
    * Returns true if users are connected (not including the current user)
    * Otherwise false
     */
    boolean usersAreConnected() {
        return !this.users.isEmpty();
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java server <port-number>");
            System.exit(0);
        }

        int port = Integer.parseInt(args[0]);

        Server server = new Server(port);
        server.run();
    }


}
