package tracker.model;

import java.util.ArrayList;

public class Epic extends Task {
    ArrayList<Integer> subtasksIds;

    public Epic(String title, String description, int id, Status status, ArrayList<Integer> subtasksIds) {
        super(title, description, id, status);
        this.subtasksIds = subtasksIds;
    }

    public Epic(String title, String description, int id) {
        super(title, description, id);
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