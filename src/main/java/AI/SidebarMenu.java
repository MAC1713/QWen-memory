package AI;

import AI.Windows.ChatWindow;

import javax.swing.*;
import java.awt.*;

public class SidebarMenu extends JPanel {
    private ChatWindow chatWindow;
    private JPanel sidebarContent;
    private boolean isOpen = false;

    public SidebarMenu(ChatWindow chatWindow) {
        this.chatWindow = chatWindow;
        setLayout(new BorderLayout());
        initComponents();
        // 初始时隐藏侧边栏
        setVisible(false);
    }

    private void initComponents() {
        sidebarContent = new JPanel();
        sidebarContent.setLayout(new BoxLayout(sidebarContent, BoxLayout.Y_AXIS));

        addSidebarItems();

        JScrollPane scrollPane = new JScrollPane(sidebarContent);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addSidebarItems() {
        addButton("参数配置", e -> chatWindow.openConfigWindow());
        addButton("打开记事本", e -> chatWindow.openNotebookWindow());
        addButton("切换暗黑模式", e -> chatWindow.toggleTheme());
        addButton("编辑AI关键词", e -> chatWindow.openConstantsEditorWindow());
    }

    private void addButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(listener);

        sidebarContent.add(button);
        sidebarContent.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    public void toggleSidebar() {
        isOpen = !isOpen;
        setVisible(isOpen);
        chatWindow.revalidate();
        chatWindow.repaint();
    }

    private void showConfigPanel() {
        JDialog dialog = new JDialog(chatWindow, "参数配置", true);
        dialog.setLayout(new BorderLayout());
        dialog.add(chatWindow.getConfigPanel(), BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(chatWindow);
        dialog.setVisible(true);
    }

    public void applyTheme(boolean isDarkMode) {
        Color bgColor = isDarkMode ? new Color(60, 60, 60) : new Color(240, 240, 220);
        Color buttonBgColor = isDarkMode ? new Color(80, 80, 80) : new Color(220, 220, 220);

        setBackground(bgColor);
        sidebarContent.setBackground(bgColor);
        for (Component comp : sidebarContent.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                button.setBackground(buttonBgColor);
            }
        }
    }
}