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

import static AI.AIChatConstants.*;

/**
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
    private JButton openNotebookButton;
    private NotebookWindow notebookWindow;
    private JToggleButton themeToggle;
    private JButton openConstantsEditorButton;
    private AIConstantsEditorWindow constantsEditorWindow;
    private AIConstantsManager constantsManager;
    private boolean isDarkMode = false;

    public ChatGUI() {
        super("AI Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLayout(new BorderLayout());
        setResizable(true);

        // Set background color
        getContentPane().setBackground(new Color(255, 248, 220));

        // Initialize components
        aiNotebook = new AINotebook();
        aiChat = new AIChat(aiNotebook);
        configPanel = new ConfigurationPanel();
        notebookWindow = new NotebookWindow(aiNotebook);
        // Initialize AIConstantsManager
        constantsManager = new AIConstantsManager();

        // Initialize AIConstantsEditorWindow
        constantsEditorWindow = new AIConstantsEditorWindow(constantsManager);

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(255, 248, 220));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Add config panel to the left
        mainPanel.add(configPanel, BorderLayout.WEST);

        // Create and add chat panel to the center
        JPanel chatPanel = createChatPanel();
        mainPanel.add(chatPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Load existing notes
        if (!aiNotebook.getNotes().isEmpty()) {
            notebookWindow.loadNotes();
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

        // Add open notebook button
        openNotebookButton = new JButton("Open Notebook");
        openNotebookButton.addActionListener(e -> openNotebookWindow());
        buttonPanel.add(openNotebookButton);

        // Add theme toggle button
        themeToggle = new JToggleButton("Dark Mode");
        themeToggle.addActionListener(e -> toggleTheme());
        buttonPanel.add(themeToggle);

        // Add open constants editor button
        openConstantsEditorButton = new JButton("Edit AI Constants");
        openConstantsEditorButton.addActionListener(e -> openConstantsEditorWindow());
        buttonPanel.add(openConstantsEditorButton);

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        return chatPanel;
    }

    private void sendMessage() {
        String userInput = inputField.getText();
        CurrentUserMessage.getInstance().setMessage(userInput);
        int messageCount = CurrentUserMessage.getInstance().getMessageCount();
        if (messageCount == 1) {
            sendSpecialMessage(Role.USER, REPEAT_NOTEBOOK);
        }
        if (messageCount % TIME_TO_COLLATION == 0 && messageCount > 0) {
            sendSpecialMessage(Role.USER, COLLATION);
        }
        if (!userInput.trim().isEmpty()) {
            chatArea.append("You: " + userInput + "\n");
            inputField.setText("");
            try {
                List<Message> messages = new ArrayList<>();
                messages.add(Message.builder().role(Role.USER.getValue()).content(userInput).build());
                String aiResponse = aiChat.generateResponse(userInput, configPanel.createGenerationParam(messages));
                //存入notebook
                checkAndUpdateNotebook(aiResponse);
                //去除note指令部分
                aiResponse = aiChat.removeNoteTags(aiResponse);
                chatArea.append("Emma: " + aiResponse + "\n\n");
            } catch (ApiException | NoApiKeyException | InputRequiredException ex) {
                chatArea.append("Error: " + ex.getMessage() + "\n\n");
            }
        }
    }

    private void sendSpecialMessage(Role role, String collation) {
        try {
            List<Message> messages = new ArrayList<>();
            messages.add(Message.builder().role(role.getValue()).content(collation).build());
            String aiResponse = aiChat.generateResponse(collation, configPanel.createGenerationParam(messages));
            checkAndUpdateNotebook(aiResponse);
        } catch (ApiException | NoApiKeyException | InputRequiredException ex) {
            chatArea.append("Error: " + ex.getMessage() + "\n\n");
        }
    }

    void checkAndUpdateNotebook(String aiResponse) {
        System.out.println("aiResponse = " + aiResponse);
        List<AINotebook.Note> newNotes = aiChat.extractNotesFromResponse(aiResponse);
        for (AINotebook.Note note : newNotes) {
            aiNotebook.addNote(note.getContent(), note.getTag(), note.getImportance());
        }
        notebookWindow.loadNotes();
    }

    private void openNotebookWindow() {
        notebookWindow.setVisible(true);
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }

    private void applyTheme() {
        Color bgColor = isDarkMode ? new Color(50, 50, 50) : new Color(255, 248, 220);
        Color fgColor = isDarkMode ? Color.WHITE : Color.BLACK;

        getContentPane().setBackground(bgColor);
        chatArea.setBackground(isDarkMode ? new Color(70, 70, 70) : new Color(255, 250, 240));
        chatArea.setForeground(fgColor);
        inputField.setBackground(bgColor);
        inputField.setForeground(fgColor);
        configPanel.setBackground(bgColor);
        configPanel.setForeground(fgColor);

        // Update NotebookWindow theme
        notebookWindow.applyTheme(isDarkMode);
        constantsEditorWindow.applyTheme(isDarkMode);

        SwingUtilities.updateComponentTreeUI(this);
    }

    private void openConstantsEditorWindow() {
        constantsEditorWindow.setVisible(true);
    }

}

