package AI;

import org.example.AINotebook;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * @author mac
 */
public class NotebookPanel extends JPanel {
    private JTextArea notebookArea;
    private AINotebook aiNotebook;

    public NotebookPanel(AINotebook aiNotebook) {
        this.aiNotebook = aiNotebook;
        setLayout(new BorderLayout());
        setBackground(new Color(255, 248, 220));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "AI 笔记本",
                TitledBorder.CENTER, TitledBorder.TOP));

        initializeComponents();
    }

    private void initializeComponents() {
        notebookArea = new JTextArea();
        notebookArea.setEditable(false);
        notebookArea.setLineWrap(true);
        notebookArea.setWrapStyleWord(true);
        notebookArea.setBackground(new Color(255, 250, 240));

        JScrollPane scrollPane = new JScrollPane(notebookArea);
        add(scrollPane, BorderLayout.CENTER);

        JButton cleanupButton = new JButton("清理笔记");
        cleanupButton.addActionListener(e -> cleanupNotes());
        add(cleanupButton, BorderLayout.SOUTH);

        updateNotebookContent();
    }

    public void updateNotebookContent() {
        SwingUtilities.invokeLater(() -> {
            notebookArea.setText(aiNotebook.getFormattedNotes());
            notebookArea.setCaretPosition(0); // 滚动到顶部
        });
    }

    private void cleanupNotes() {
        int option = JOptionPane.showConfirmDialog(this,
                "确定要清理不重要的旧笔记吗？", "清理笔记",
                JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            aiNotebook.cleanupNotes();
            updateNotebookContent();
            JOptionPane.showMessageDialog(this, "笔记已清理完毕。");
        }
    }
}
