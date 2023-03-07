package tracker.model;

public class Subtask extends Task {

    int epicId;

    public Subtask(int id, Type type, String title, Status status, String description, int epicId) {
        super(id, type, title, status, description);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }
}