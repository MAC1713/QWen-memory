package org.example;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.time.LocalDateTime;

/**
 * @author mac
 */
public class AINotebook {
    private List<Note> notes;
    private static final String NOTEBOOK_FILE = "ai_notebook.ser";

    public AINotebook() {
        this.notes = loadNotebook();
    }

    public void addNote(String content, String tag, double importance) {
        notes.add(new Note(content, tag, importance));
        saveNotebook();
    }

    public List<Note> getNotes() {
        return new ArrayList<>(notes);
    }

    public String getFormattedNotes() {
        StringBuilder sb = new StringBuilder();
        sb.append("AI Notebook:\n");
        for (Note note : notes) {
            sb.append("- [").append(note.tag).append("] (Importance: ").append(note.importance)
                    .append(") ").append(note.content).append(" (Added: ").append(note.timestamp).append(")\n");
        }
        return sb.toString();
    }

    public void cleanupNotes() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        notes.removeIf(note -> !note.isPermanent && note.importance < 0.5 && note.timestamp.isBefore(oneMonthAgo));
        saveNotebook();
    }

    private void saveNotebook() {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(NOTEBOOK_FILE)))) {
            oos.writeObject(notes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Note> loadNotebook() {
        File file = new File(NOTEBOOK_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file.toPath()))) {
                return (List<Note>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    private static class Note implements Serializable {
        private static final long serialVersionUID = 1L;
        String content;
        String tag;
        double importance;
        LocalDateTime timestamp;
        boolean isPermanent;

        Note(String content, String tag, double importance) {
            this.content = content;
            this.tag = tag;
            this.importance = importance;
            this.timestamp = LocalDateTime.now();
            this.isPermanent = "Name".equalsIgnoreCase(tag) || "Identity".equalsIgnoreCase(tag);
        }
    }
}