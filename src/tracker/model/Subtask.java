package tracker.model;

public class Subtask extends Task {

    int epicId;

    public Subtask(String title, String subject, int id, Status status, int epicId) {
        super(title, subject, id, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }
}