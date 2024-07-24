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
    private JButton openNotebookButton;
    private NotebookWindow notebookWindow;
    private JSlider cleanupSlider;
    private JToggleButton themeToggle;
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

        // Add cleanup slider
        cleanupSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        cleanupSlider.setMajorTickSpacing(25);
        cleanupSlider.setMinorTickSpacing(5);
        cleanupSlider.setPaintTicks(true);
        cleanupSlider.setPaintLabels(true);

        JButton cleanupButton = new JButton("Cleanup Notes");
        cleanupButton.addActionListener(e -> cleanupNotes());

        JPanel cleanupPanel = new JPanel(new BorderLayout());
        cleanupPanel.add(new JLabel("Importance Threshold:"), BorderLayout.WEST);
        cleanupPanel.add(cleanupSlider, BorderLayout.CENTER);
        cleanupPanel.add(cleanupButton, BorderLayout.EAST);

        mainPanel.add(cleanupPanel, BorderLayout.SOUTH);

        // Load existing notes
        if (!aiNotebook.getNotes().isEmpty()) {
            chatArea.append("Loaded existing notes:\n" + aiNotebook.getFormattedNotes() + "\n\n");
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
        notebookWindow.loadNotes();
    }

    private void openNotebookWindow() {
        notebookWindow.setVisible(true);
    }

    private void cleanupNotes() {
        double threshold = cleanupSlider.getValue() / 100.0;
        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete all notes with importance below " + threshold + "?",
                "Cleanup Notes", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            aiNotebook.cleanupNotes(threshold);
            notebookWindow.loadNotes();
            JOptionPane.showMessageDialog(this, "Notes cleaned up successfully.");
        }
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

        // Update button colors
        Component[] components = getContentPane().getComponents();
        updateComponentColors(components, bgColor, fgColor);

        // Update NotebookWindow theme
        notebookWindow.applyTheme(isDarkMode);

        SwingUtilities.updateComponentTreeUI(this);
    }

    private void updateComponentColors(Component[] components, Color bgColor, Color fgColor) {
        for (Component component : components) {
            if (component instanceof JButton) {
                component.setBackground(bgColor);
                component.setForeground(fgColor);
            } else if (component instanceof JPanel) {
                component.setBackground(bgColor);
                updateComponentColors(((JPanel) component).getComponents(), bgColor, fgColor);
            }
        }
    }
}

