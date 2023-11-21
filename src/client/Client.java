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

public class Client {
    private BufferedReader in;
    private PrintWriter out;
    private String title;
    private JFrame frame;
    private JTextArea messageArea;

    public Client(String title) {
        this.title = title;
        // Initializing JFrame
        frame = new JFrame("title");
        JTextField textField = new JTextField(40);
        messageArea = new JTextArea(8, 40);

        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
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

        // TODO: Implement command system here
        while (true) {
            String message = in.readLine();
            messageArea.append(message + "\n");
        }
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client("Chat Application");
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.runClient();
    }


}
