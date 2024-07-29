package AI;

import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.example.AINotebook;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static AI.AIChatConstants.*;

/**
 * @author MAC1713
 * @email 1172820376@qq.com
 * @date 2024-07-23 03:59:06
 */

public class ChatWindow extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private AIChat aiChat;
    private AINotebook aiNotebook;
    private ConfigurationPanel configPanel;
    private NotebookWindow notebookWindow;
    private AIConstantsEditorWindow constantsEditorWindow;
    private AIConstantsManager constantsManager;

    public ChatWindow() {
        super("AI Chat");
        initComponents();
        setupLayout();
        setupEventListeners();
    }

    private void initComponents() {
        aiNotebook = new AINotebook();
        aiChat = new AIChat(aiNotebook);
        configPanel = new ConfigurationPanel();
        notebookWindow = new NotebookWindow(aiNotebook);
        constantsManager = new AIConstantsManager();
        constantsEditorWindow = new AIConstantsEditorWindow(constantsManager);

        chatArea = new JTextArea();
        inputField = new JTextField();
        sendButton = new JButton("发送");
    }

    private void setupLayout() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(configPanel, BorderLayout.WEST);
        mainPanel.add(createChatPanel(), BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        UIConfig.applyInitialTheme(this);
    }

    private JPanel createChatPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout());

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(sendButton);
        buttonPanel.add(UIConfig.createOpenNotebookButton(this::openNotebookWindow));
        buttonPanel.add(UIConfig.createThemeToggle(this::toggleTheme));
        buttonPanel.add(UIConfig.createOpenConstantsEditorButton(this::openConstantsEditorWindow));

        inputPanel.add(buttonPanel, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        return chatPanel;
    }

    private void setupEventListeners() {
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    private void sendMessage() {
        String userInput = inputField.getText();
        CurrentUserMessage.getInstance().setMessage(userInput);
        int messageCount = CurrentUserMessage.getInstance().getMessageCount();
        if (messageCount == 0) {
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
                checkAndUpdateNotebook(aiResponse);
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
        UIConfig.toggleTheme(this, chatArea, inputField, configPanel);
        notebookWindow.applyTheme(UIConfig.isDarkMode());
        constantsEditorWindow.applyTheme(UIConfig.isDarkMode());
    }

    private void openConstantsEditorWindow() {
        constantsEditorWindow.setVisible(true);
    }
}

