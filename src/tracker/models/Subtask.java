package tracker.models;

import java.time.LocalDateTime;

public class Subtask extends Task {

    int epicId;

    public Subtask(int id, String title, Status status, String description, long duration, LocalDateTime startTime,
                   int epicId) {
        super(id, title, status, description, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }
}