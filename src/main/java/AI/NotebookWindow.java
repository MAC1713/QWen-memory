package AI;

import org.example.AINotebook;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.*;
import java.io.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class NotebookWindow extends JFrame {
    private AINotebook aiNotebook;
    private JTextPane notebookArea;
    private JButton saveButton;
    private JButton cancelButton;
    private JTextField searchField;
    private JButton exportButton;
    private JButton importButton;
    private List<AINotebook.Note> originalNotes;

    public NotebookWindow(AINotebook aiNotebook) {
        super("AI Notebook");
        this.aiNotebook = aiNotebook;
        setSize(600, 400);
        setLayout(new BorderLayout());
        setResizable(true);

        initComponents();
        loadNotes();
    }

    private void initComponents() {
        notebookArea = new JTextPane();
        notebookArea.setEditable(true);
        JScrollPane scrollPane = new JScrollPane(notebookArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        exportButton = new JButton("Export Notes");
        importButton = new JButton("Import Notes");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(importButton);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        searchField = new JTextField(20);
        bottomPanel.add(searchField, BorderLayout.NORTH);

        add(bottomPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> saveNotes());
        cancelButton.addActionListener(e -> loadNotes());
        exportButton.addActionListener(e -> exportNotes());
        importButton.addActionListener(e -> importNotes());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { search(); }
            public void removeUpdate(DocumentEvent e) { search(); }
            public void insertUpdate(DocumentEvent e) { search(); }
        });
    }

    public void loadNotes() {
        originalNotes = new ArrayList<>(aiNotebook.getNotes());
        updateNotebookContent();
    }

    private void updateNotebookContent() {
        StringBuilder content = new StringBuilder();
        for (AINotebook.Note note : originalNotes) {
            content.append(formatNote(note)).append("\n");
        }
        notebookArea.setText(content.toString());
        applyColorFormatting();
    }

    private String formatNote(AINotebook.Note note) {
        return String.format("[%s] (Importance: %.1f) %s (Added: %s)",
                note.getTag(), note.getImportance(), note.getContent(), note.getTimestamp());
    }

    private void saveNotes() {
        aiNotebook.clearNotes();
        String[] lines = notebookArea.getText().split("\n");
        for (String line : lines) {
            AINotebook.Note note = parseNote(line);
            if (note != null) {
                aiNotebook.addNote(note.getContent(), note.getTag(), note.getImportance());
            }
        }
        JOptionPane.showMessageDialog(this, "Notes saved successfully!");
    }

    private AINotebook.Note parseNote(String line) {
        String regex = "\\[(.*?)] \\(Importance: (\\d+\\.\\d+)\\) (.*) \\(Added: (.*)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String tag = matcher.group(1);
            double importance = Double.parseDouble(matcher.group(2));
            String content = matcher.group(3);
            return new AINotebook.Note(content, tag, importance);
        }
        return null;
    }

    private void search() {
        String searchText = searchField.getText().toLowerCase();
        List<AINotebook.Note> filteredNotes = originalNotes.stream()
                .filter(note -> note.getContent().toLowerCase().contains(searchText) ||
                        note.getTag().toLowerCase().contains(searchText))
                .collect(java.util.stream.Collectors.toList());

        StringBuilder content = new StringBuilder();
        for (AINotebook.Note note : filteredNotes) {
            content.append(formatNote(note)).append("\n");
        }
        notebookArea.setText(content.toString());
        applyColorFormatting();
    }

    private void applyColorFormatting() {
        String text = notebookArea.getText();
        StyledDocument doc = notebookArea.getStyledDocument();

        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        Style tagStyle = notebookArea.addStyle("tagStyle", defaultStyle);
        Style importanceStyle = notebookArea.addStyle("importanceStyle", defaultStyle);
        Style contentStyle = notebookArea.addStyle("contentStyle", defaultStyle);
        Style timestampStyle = notebookArea.addStyle("timestampStyle", defaultStyle);

        StyleConstants.setForeground(tagStyle, Color.BLUE);
        StyleConstants.setForeground(importanceStyle, Color.RED);
        StyleConstants.setForeground(contentStyle, Color.BLACK);
        StyleConstants.setForeground(timestampStyle, Color.GRAY);

        try {
            doc.remove(0, doc.getLength());
            String[] lines = text.split("\n");
            String regex = "\\[(.*?)] \\(Importance: (\\d+\\.\\d+)\\) (.*) \\(Added: (.*)\\)";
            for (String line : lines) {
                Matcher matcher = Pattern.compile(regex).matcher(line);
                if (matcher.find()) {
                    doc.insertString(doc.getLength(), "[" + matcher.group(1) + "] ", tagStyle);
                    doc.insertString(doc.getLength(), "(Importance: " + matcher.group(2) + ") ", importanceStyle);
                    doc.insertString(doc.getLength(), matcher.group(3) + " ", contentStyle);
                    doc.insertString(doc.getLength(), "(Added: " + matcher.group(4) + ")\n", timestampStyle);
                } else {
                    doc.insertString(doc.getLength(), line + "\n", defaultStyle);
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void exportNotes() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Notes");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                file = new File(file.getParentFile(), file.getName() + ".txt");
            }
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.print(notebookArea.getText());
                JOptionPane.showMessageDialog(this, "Notes exported successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error exporting notes: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importNotes() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Notes");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                notebookArea.setText(content.toString());
                applyColorFormatting();
                JOptionPane.showMessageDialog(this, "Notes imported successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error importing notes: " + ex.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void applyTheme(boolean isDarkMode) {
        Color bgColor = isDarkMode ? new Color(50, 50, 50) : new Color(255, 248, 220);
        Color fgColor = isDarkMode ? Color.WHITE : Color.BLACK;

        getContentPane().setBackground(bgColor);
        notebookArea.setBackground(isDarkMode ? new Color(70, 70, 70) : new Color(255, 250, 240));
        notebookArea.setForeground(fgColor);
        searchField.setBackground(bgColor);
        searchField.setForeground(fgColor);

        // Update button colors
        Component[] components = getContentPane().getComponents();
        updateComponentColors(components, bgColor, fgColor);

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