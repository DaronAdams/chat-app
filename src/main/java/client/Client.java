package client;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
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
import java.util.HashMap;
import java.util.Map;

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
        frame = new JFrame(title);

        frame.setSize(800, 600);
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

                if (isCommand(message)) {
                    handleClientCommand(message);
                } else {
                    sendMessage(message);
                }
                textField.setText("");
            }
        });
        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                showCommandSuggestions(e);
            }
            // Non-used methods required for documentListener
            public void removeUpdate(DocumentEvent e) {}
            // Non-used methods required for documentListener
            public void changedUpdate(DocumentEvent e) {}
        });
    }

    private void showCommandSuggestions(DocumentEvent e) {
        try {
            String text = textField.getText(0, e.getOffset() + e.getLength());
            if (text.endsWith("/")) {
                JPopupMenu suggestionsMenu = new JPopupMenu();
                suggestionsMenu.add(createSuggestionItem("/list", "Lists all online users"));
                suggestionsMenu.add(createSuggestionItem("/exit", "Exits the chat"));
                suggestionsMenu.add(createSuggestionItem("/clear", "Clears all messages from the chat history"));
                suggestionsMenu.add(createSuggestionItem("/msg", "Sends a private message. Usage: /msg <username> <message>"));
                suggestionsMenu.show(textField, 0, textField.getHeight());
            }
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    private JMenuItem createSuggestionItem(String command, String description) {
        JMenuItem item = new JMenuItem(command);
        item.addActionListener(e -> {
            textField.setText(command + " ");
            textField.requestFocus();
        });
        return item;
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

        // Sending the entered username to the server
        out.println(username);

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

    private void sendServerCommand(String command) {
        out.println(command);
    }

    private void sendMessage(String message) {
        String timestamp = dateFormatter.format(new Date());
        out.println(username + " [" + timestamp + "]: " + message);
    }

    private void handleClientCommand(String command) {
        String[] parts = command.split(" ", 2);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "/clear":
                messageArea.setText("");
                break;
            case "/exit":
                exitClient();
                break;
            case "/list":
                sendServerCommand(command);
                break;
            case "/msg":
                out.println(command);
                break;
            default:
                messageArea.append("Unknown command: " + cmd + "\n");
                break;
        }
    }

    private boolean isCommand(String message) {
        return message.startsWith("/");
    }

    private void exitClient() {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            // Setting up the theme
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Client client = new Client("Chat Application");
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.runClient();
    }


}