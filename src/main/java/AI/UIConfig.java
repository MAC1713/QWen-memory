package AI;

import javax.swing.*;
import java.awt.*;

/**
 * @author mac
 */
public class UIConfig {
    private static boolean isDarkMode = false;

    public static void applyInitialTheme(JFrame frame) {
        applyTheme(frame, new JTextArea(), new JTextField());
    }

    public static void toggleTheme(JFrame frame, JTextArea chatArea, JTextField inputField) {
        isDarkMode = !isDarkMode;
        applyTheme(frame, chatArea, inputField);
    }

    public static void applyTheme(JFrame frame, JTextArea chatArea, JTextField inputField) {
        Color bgColor = isDarkMode ? new Color(50, 50, 50) : new Color(255, 248, 220);
        Color fgColor = isDarkMode ? Color.WHITE : Color.BLACK;

        frame.getContentPane().setBackground(bgColor);
        chatArea.setBackground(isDarkMode ? new Color(70, 70, 70) : new Color(255, 250, 220));
        chatArea.setForeground(fgColor);
        inputField.setBackground(bgColor);
        inputField.setForeground(fgColor);

        // 更新所有组件的UI
        SwingUtilities.updateComponentTreeUI(frame);
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }
}