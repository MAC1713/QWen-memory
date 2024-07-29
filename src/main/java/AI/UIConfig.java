package AI;

import javax.swing.*;
import java.awt.*;

public class UIConfig {
    private static boolean isDarkMode = false;

    public static void applyInitialTheme(JFrame frame) {
        applyTheme(frame, new JTextArea(), new JTextField(), new JPanel());
    }

    public static void toggleTheme(JFrame frame, JTextArea chatArea, JTextField inputField, JPanel configPanel) {
        isDarkMode = !isDarkMode;
        applyTheme(frame, chatArea, inputField, configPanel);
    }

    public static void applyTheme(JFrame frame, JTextArea chatArea, JTextField inputField, JPanel configPanel) {
        Color bgColor = isDarkMode ? new Color(50, 50, 50) : new Color(255, 248, 220);
        Color fgColor = isDarkMode ? Color.WHITE : Color.BLACK;

        frame.getContentPane().setBackground(bgColor);
        chatArea.setBackground(isDarkMode ? new Color(70, 70, 70) : new Color(255, 250, 240));
        chatArea.setForeground(fgColor);
        inputField.setBackground(bgColor);
        inputField.setForeground(fgColor);
        configPanel.setBackground(bgColor);
        configPanel.setForeground(fgColor);

        SwingUtilities.updateComponentTreeUI(frame);
    }

    public static JButton createOpenNotebookButton(Runnable action) {
        JButton button = new JButton("Open Notebook");
        button.addActionListener(e -> action.run());
        return button;
    }

    public static JToggleButton createThemeToggle(Runnable action) {
        JToggleButton toggle = new JToggleButton("Dark Mode");
        toggle.addActionListener(e -> action.run());
        return toggle;
    }

    public static JButton createOpenConstantsEditorButton(Runnable action) {
        JButton button = new JButton("Edit AI Constants");
        button.addActionListener(e -> action.run());
        return button;
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }
}