package AI.Windows;

import AI.*;
import AI.Global.CurrentUserMessage;
import AI.Manager.AIConstantsManager;
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

import static AI.Constants.AIChatConstants.*;

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
    private ConfigurationWindow configPanel;
    private NotebookWindow notebookWindow;
    private AIConstantsEditorWindow constantsEditorWindow;
    private AIConstantsManager constantsManager;
    private SidebarMenu sidebarMenu;

    public ChatWindow() {
        super("AI Chat");
        initComponents();
        setupLayout();
        setupEventListeners();
    }

    private void initComponents() {
        aiNotebook = new AINotebook();
        aiChat = new AIChat(aiNotebook);
        configPanel = new ConfigurationWindow();
        notebookWindow = new NotebookWindow(aiNotebook);
        constantsManager = new AIConstantsManager();
        constantsEditorWindow = new AIConstantsEditorWindow(constantsManager);
        sidebarMenu = new SidebarMenu(this);

        chatArea = new JTextArea();
        inputField = new JTextField();
        sendButton = new JButton("发送");
    }

    private void setupLayout() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createChatPanel(), BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(createMenuButton());
        mainPanel.add(topPanel, BorderLayout.NORTH);

        add(mainPanel, BorderLayout.CENTER);
        add(sidebarMenu, BorderLayout.WEST);

        UIConfig.applyInitialTheme(this);
    }

    private JButton createMenuButton() {
        JButton menuButton = new JButton("☰");
        menuButton.setFocusPainted(false);
        menuButton.setBorderPainted(false);
        menuButton.setContentAreaFilled(false);
        menuButton.setPreferredSize(new Dimension(30, 30));
        menuButton.addActionListener(e -> sidebarMenu.toggleSidebar());
        return menuButton;
    }

    private JPanel createChatPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout());

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(255, 248, 220));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
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

    /**
     * 打开记事本窗口
     */
    public void openNotebookWindow() {
        notebookWindow.setVisible(true);
    }

    /**
     * 打开提示词窗口
     */
    public void openConstantsEditorWindow() {
        constantsEditorWindow.setVisible(true);
    }

    public void openConfigWindow() {
        configPanel.setVisible(true);
    }

    public void toggleTheme() {
        UIConfig.toggleTheme(this, chatArea, inputField);
        notebookWindow.applyTheme(UIConfig.isDarkMode());
        constantsEditorWindow.applyTheme(UIConfig.isDarkMode());
        sidebarMenu.applyTheme(UIConfig.isDarkMode());
        configPanel.applyTheme(UIConfig.isDarkMode());
    }

    public ConfigurationWindow getConfigPanel() {
        return configPanel;
    }
}

