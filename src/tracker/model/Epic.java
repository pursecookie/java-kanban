package tracker.model;

import java.util.ArrayList;

public class Epic extends Task {
    ArrayList<Integer> subtasks;

    public Epic(String title, String subject, int id, Status status, ArrayList<Integer> subtasks) {
        super(title, subject, id, status);
        this.subtasks = subtasks;
    }

    public ArrayList<Integer> getSubtasks() {
        return subtasks;
    }

}