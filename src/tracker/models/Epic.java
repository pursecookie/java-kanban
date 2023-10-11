package tracker.models;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subtasksIds;
    private LocalDateTime endTime;

    public Epic(int id, String title, Status status, String description, long duration, LocalDateTime startTime,
                ArrayList<Integer> subtasksIds) {
        super(id, title, status, description, duration, startTime);
        this.subtasksIds = subtasksIds;
    }

    public ArrayList<Integer> getSubtasksIds() {
        return subtasksIds;
    }

    public void addSubtaskId(int id) {
        subtasksIds.add(id);
    }

    public void cleanSubtaskIds() {
        subtasksIds.clear();
    }

    public void removeSubtask(Integer id) {
        subtasksIds.remove(id);
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }
}