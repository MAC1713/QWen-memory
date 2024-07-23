package AI;

import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.example.AINotebook;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author MAC1713
 * @email 1172820376@qq.com
 * @date 2024-07-23 03:59:06
 */
public class ChatGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private AIChat aiChat;
    private AINotebook aiNotebook;
    private ConfigurationPanel configPanel;
    private NotebookPanel notebookPanel;

    public ChatGUI() {
        super("AI Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLayout(new BorderLayout());

        // Set background color
        getContentPane().setBackground(new Color(255, 248, 220));

        // Initialize components
        aiNotebook = new AINotebook();
        aiChat = new AIChat(aiNotebook);
        configPanel = new ConfigurationPanel();
        notebookPanel = new NotebookPanel(aiNotebook);

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(255, 248, 220));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Add config panel to the left
        mainPanel.add(configPanel, BorderLayout.WEST);

        // Create and add chat panel to the center
        JPanel chatPanel = createChatPanel();
        mainPanel.add(chatPanel, BorderLayout.CENTER);

        // Add notebook panel to the right
        mainPanel.add(notebookPanel, BorderLayout.EAST);

        add(mainPanel, BorderLayout.CENTER);

        // Load existing notes
        if (!aiNotebook.getNotes().isEmpty()) {
            chatArea.append("Loaded existing notes:\n" + aiNotebook.getFormattedNotes() + "\n\n");
            notebookPanel.updateNotebookContent();
        }
    }

    private JPanel createChatPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(new Color(255, 248, 220));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(255, 250, 240));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(new Color(255, 248, 220));
        inputField = new JTextField();
        sendButton = new JButton("发送");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(255, 248, 220));
        buttonPanel.add(sendButton);

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        return chatPanel;
    }

    private void sendMessage() {
        String userInput = inputField.getText();
        if (!userInput.trim().isEmpty()) {
            chatArea.append("You: " + userInput + "\n");
            inputField.setText("");

            try {
                List<Message> messages = new ArrayList<>();
                messages.add(Message.builder().role(Role.USER.getValue()).content(userInput).build());
                String aiResponse = aiChat.generateResponse(userInput, configPanel.createGenerationParam(messages));
                chatArea.append("Emma: " + aiResponse + "\n\n");

                checkAndUpdateNotebook(aiResponse);
            } catch (ApiException | NoApiKeyException | InputRequiredException ex) {
                chatArea.append("Error: " + ex.getMessage() + "\n\n");
            }
        }
    }

    private void checkAndUpdateNotebook(String aiResponse) {
        List<AINotebook.Note> newNotes = aiChat.extractNotesFromResponse(aiResponse);
        for (AINotebook.Note note : newNotes) {
            aiNotebook.addNote(note.getContent(), note.getTag(), note.getImportance());
            chatArea.append("Added note to AI Notebook: [" + note.getTag() + "] " + note.getContent() +
                    " (Importance: " + note.getImportance() + ")\n\n");
        }
        notebookPanel.updateNotebookContent();
    }

}