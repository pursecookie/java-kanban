package tracker.model;

public class Task {

    private String title;
    private String description;
    private final int id;
    private Status status;
    private final Type type;

    public Task(int id, Type type, String title, Status status, String description) {
        this.title = title;
        this.description = description;
        this.id = id;
        this.status = status;
        this.type = type;
    }

    @Override
    public String toString() {
        return type + "{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Type getType() {
        return type;
    }
}