package AI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AIConstantsEditorWindow extends JFrame {
    private JTextArea initialPromptArea;
    private JTextArea simplifiedPromptArea;
    private JButton saveButton;
    private AIConstantsManager constantsManager;

    public AIConstantsEditorWindow(AIConstantsManager manager) {
        super("AI Constants Editor");
        this.constantsManager = manager;
        setSize(600, 400);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initialPromptArea = new JTextArea(constantsManager.getInitialSystemPrompt());
        simplifiedPromptArea = new JTextArea(constantsManager.getSimplifiedSystemPrompt());

        mainPanel.add(createPromptPanel("Initial System Prompt", initialPromptArea));
        mainPanel.add(createPromptPanel("Simplified System Prompt", simplifiedPromptArea));

        add(mainPanel, BorderLayout.CENTER);

        saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> saveChanges());
        add(saveButton, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private JPanel createPromptPanel(String title, JTextArea textArea) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void saveChanges() {
        constantsManager.saveConstants(initialPromptArea.getText(), simplifiedPromptArea.getText());
        JOptionPane.showMessageDialog(this, "Changes saved successfully!");
    }

    public void applyTheme(boolean isDarkMode) {
        Color bgColor = isDarkMode ? new Color(50, 50, 50) : new Color(255, 248, 220);
        Color fgColor = isDarkMode ? Color.WHITE : Color.BLACK;

        this.getContentPane().setBackground(bgColor);
        initialPromptArea.setBackground(isDarkMode ? new Color(70, 70, 70) : new Color(255, 250, 240));
        initialPromptArea.setForeground(fgColor);
        simplifiedPromptArea.setBackground(isDarkMode ? new Color(70, 70, 70) : new Color(255, 250, 240));
        simplifiedPromptArea.setForeground(fgColor);
        saveButton.setBackground(bgColor);
        saveButton.setForeground(fgColor);

        updateComponentColors(this.getContentPane().getComponents(), bgColor, fgColor);

        SwingUtilities.updateComponentTreeUI(this);
    }

    private void updateComponentColors(Component[] components, Color bgColor, Color fgColor) {
        for (Component component : components) {
            if (component instanceof JPanel) {
                component.setBackground(bgColor);
                updateComponentColors(((JPanel) component).getComponents(), bgColor, fgColor);
            } else if (component instanceof JScrollPane) {
                component.setBackground(bgColor);
                ((JScrollPane) component).getViewport().setBackground(bgColor);
            }
        }
    }
}