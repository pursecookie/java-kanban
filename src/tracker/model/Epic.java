package tracker.model;

import java.util.ArrayList;

public class Epic extends Task {
    ArrayList<Integer> subtasksIds;

    public Epic(int id, Type type, String title, Status status, String description, ArrayList<Integer> subtasksIds) {
        super(id, type, title, status, description);
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

    public void removeSubtask(int id) {
        subtasksIds.remove(id);
    }

}