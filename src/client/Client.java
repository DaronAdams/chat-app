package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Client {
    private BufferedReader in;
    private PrintWriter out;
    private String title;
    private JFrame frame;
    private JTextArea messageArea;
    private JTextField textField;
    private String username;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");

    public Client(String title) {
        this.title = "Chat Application";

        // Initializing JFrame
        frame = new JFrame(title);
        textField = new JTextField(40);
        this.username = JOptionPane.showInputDialog(
                frame,
                "Enter your username: ",
                "User",
                JOptionPane.PLAIN_MESSAGE
        );
        messageArea = new JTextArea(8, 40);

        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = textField.getText();
                String timestamp = dateFormatter.format(new Date());
                out.println(username + " [" + timestamp + "]: " + message);
                textField.setText("");
            }
        });
    }

    /*
    * UI prompt for user to enter server address
     */
    private String getServerAddress() {
        return JOptionPane.showInputDialog(
                frame,
                "Enter IP Address of the Server:",
                JOptionPane.QUESTION_MESSAGE);
    }

    private void runClient() throws IOException {
        String address = getServerAddress();
        Socket socket = new Socket(address, 8080);

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        SwingUtilities.invokeLater(() -> {
            textField.setEditable(true);
        });

        Thread readThread = new Thread(() -> {
            try {
                while (true) {
                    String message = in.readLine();
                    if (message == null) break;
                    SwingUtilities.invokeLater(() -> {
                        messageArea.append(message + "\n");
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Optionally update the UI or notify the user of the error here
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        readThread.start();
    }

    private void handleCommand(String command) {
        if (command.equals("/clear")) {
            messageArea.setText("");
        }
        // Add more client-side commands here
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client("Chat Application");
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.runClient();
    }


}
