package xyz.melnychuk.niimbotprint.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import xyz.melnychuk.niimbotprint.model.Sticker;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

@Slf4j
public class EditorHistoryService {

    private static final int LIMIT = 50;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Deque<Sticker> undoStack = new ArrayDeque<>();
    private final Deque<Sticker> redoStack = new ArrayDeque<>();

    private Sticker sticker;
    private Sticker pending;

    private Consumer<Boolean> undoListener = u -> {};
    private Consumer<Boolean> redoListener = r -> {};

    public EditorHistoryService(Sticker sticker) {
        this.sticker = sticker;
    }

    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
        pending = null;
        fireListeners();
    }

    public void beginEdit() {
        if (sticker == null) {
            return;
        }
        pending = snapshot();
    }

    public void endEdit() {
        if (pending == null || sticker == null) {
            return;
        }
        if (!serialize(pending).equals(serialize(sticker))) {
            undoStack.push(pending);
            if (undoStack.size() > LIMIT) {
                undoStack.removeLast();
            }
            redoStack.clear();
            pending = null;
            fireListeners();
        } else {
            pending = null;
        }
    }

    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }
        redoStack.push(snapshot());
        restore(undoStack.pop());
        fireListeners();
        return true;
    }

    public boolean redo() {
        if (redoStack.isEmpty()) {
            return false;
        }
        undoStack.push(snapshot());
        restore(redoStack.pop());
        fireListeners();
        return true;
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void setUndoListener(Consumer<Boolean> undoListener) {
        this.undoListener = undoListener == null ? u -> {} : undoListener;
        fireListeners();
    }

    public void setRedoListener(Consumer<Boolean> redoListener) {
        this.redoListener = redoListener == null ? r -> {} : redoListener;
        fireListeners();
    }

    private Sticker snapshot() {
        return mapper.convertValue(mapper.convertValue(sticker, Object.class), Sticker.class);
    }

    private String serialize(Sticker s) {
        try {
            return mapper.writeValueAsString(s);
        } catch (Exception e) {
            log.error("Exception in serialize().", e);
            return "";
        }
    }

    private void restore(Sticker snapshot) {
        sticker.getElements().clear();
        sticker.getElements().addAll(snapshot.getElements());
        sticker.setWidth(snapshot.getWidth());
        sticker.setHeight(snapshot.getHeight());
    }

    private void fireListeners() {
        undoListener.accept(canUndo());
        redoListener.accept(canRedo());
    }
}