package client;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import java.awt.*;
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
    private final JFrame frame;
    private final JTextPane messagePane;
    private final JTextField textField;
    private final String username;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");

    public Client(String title) {
        this.title = "Chat Application";
        frame = new JFrame(title);

        frame.setSize(800, 600);
        textField = new JTextField(40);
        messagePane = new JTextPane();
        messagePane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messagePane);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        this.username = JOptionPane.showInputDialog(
                frame,
                "Enter your username: ",
                "User",
                JOptionPane.PLAIN_MESSAGE
        );

        textField.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.pack();

        textField.addActionListener(e -> {
            String message = textField.getText();

            if (isCommand(message)) {
                handleClientCommand(message);
            } else {
                sendMessage(message);
            }
            textField.setText("");
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

    /*
    * Allows you to append stylized text to the message area
     */
    private void appendText(String text, Color color) {
        StyledDocument document = messagePane.getStyledDocument();
        Style style = messagePane.addStyle("Style", null);

        try {
            document.insertString(document.getLength(), text + "\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void displayMessage(String message, boolean isPrivate) {
        if (isPrivate) {
            appendText(message, Color.BLUE);
        } else {
            appendText(message, Color.BLACK);
        }
    }

    /*
    * Adds a command to the list of suggestions when a user types "/"
     */
    private void showCommandSuggestions(DocumentEvent e) {
        try {
            String text = textField.getText(0, e.getOffset() + e.getLength());
            if (text.endsWith("/")) {
                JPopupMenu suggestionsMenu = new JPopupMenu();
                suggestionsMenu.add(createSuggestionItem("/list"));
                suggestionsMenu.add(createSuggestionItem("/exit"));
                suggestionsMenu.add(createSuggestionItem("/clear"));
                suggestionsMenu.add(createSuggestionItem("/msg"));
                suggestionsMenu.show(textField, 0, textField.getHeight());
            }
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    /*
    * Helper method to add a suggestion item to the JPane menu
     */
    private JMenuItem createSuggestionItem(String command) {
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

    /*
    * Runs the client and connects to the server using socket connections
     */
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
                        displayMessage(message, message.startsWith("PM"));
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
                messagePane.setText("");
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
                appendText("Unknown command: " + cmd, Color.RED);
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
            // Setting the theme
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
