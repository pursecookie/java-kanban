package tracker.model;

import java.time.Instant;
import java.util.ArrayList;

public class Epic extends Task {
    ArrayList<Integer> subtasksIds;
    protected Instant endTime;

    public Epic(int id, String title, Status status, String description, long duration, Instant startTime,
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

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    @Override
    public Type getType() {
        return Type.EPIC;
    }

}