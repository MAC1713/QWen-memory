package org.example;

import lombok.Data;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mac
 */
public class AINotebook implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AINotebook.class.getName());
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

    public void clearNotes(){
        notes.clear();
        saveNotebook();
    }

    public void cleanupNotes() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        notes.removeIf(note -> !note.isPermanent && note.importance < 0.5 && note.timestamp.isBefore(oneMonthAgo));
        saveNotebook();
    }

    public void cleanupNotes(double threshold){
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        notes.removeIf(note -> !note.isPermanent && note.importance < threshold && note.timestamp.isBefore(oneMonthAgo));
        saveNotebook();
    }

    private void saveNotebook() {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(NOTEBOOK_FILE)))) {
            oos.writeObject(notes);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving notebook", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Note> loadNotebook() {
        File file = new File(NOTEBOOK_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file.toPath()))) {
                return (List<Note>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Error loading notebook", e);
            }
        }
        return new ArrayList<>();
    }

    @Data
    public static class Note implements Serializable {
        private static final long serialVersionUID = 1L;
        String content;
        String tag;
        double importance;
        LocalDateTime timestamp;
        boolean isPermanent;

        public Note(String content, String tag, double importance) {
            this.content = content;
            this.tag = tag;
            this.importance = importance;
            this.timestamp = LocalDateTime.now();
            this.isPermanent = "Name".equalsIgnoreCase(tag) || "Identity".equalsIgnoreCase(tag);
        }
    }
}