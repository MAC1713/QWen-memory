package AI;

import javax.swing.*;

/**
 *
 * @author MAC1713
 * @email 1172820376@qq.com
 * @date 2024-07-23 03:57:27
 */
public class MainApplication {
    public static void main(String[] args) {
        // Set JVM arguments programmatically
        System.setProperty("java.awt.headless", "false");
        System.setProperty("sun.java2d.noddraw", "true");
        System.setProperty("sun.java2d.d3d", "false");
        System.setProperty("sun.java2d.opengl", "false");
        System.setProperty("sun.java2d.pmoffscreen", "false");

        // Increase CodeCache size
        System.setProperty("ReservedCodeCacheSize", "256m");

        // Enable CodeCache flushing
        System.setProperty("UseCodeCacheFlushing", "true");

        // Launch the application on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            ChatGUI chatGUI = new ChatGUI();
            chatGUI.setVisible(true);
        });
    }
}
