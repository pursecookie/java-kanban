package tracker.model;

import java.time.Instant;

public class Subtask extends Task {

    int epicId;

    public Subtask(int id, String title, Status status, String description, long duration, Instant startTime,
                   int epicId) {
        super(id, title, status, description, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public Type getType() {
        return Type.SUBTASK;
    }
}