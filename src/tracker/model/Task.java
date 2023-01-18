package tracker.model;

public class Task {

    private String title;
    private String subject;
    private final int id;
    private Status status;

    public Task(String name, String subject, int id, Status status) {
        this.title = name;
        this.subject = subject;
        this.id = id;
        this.status = status;
    }

    @Override
    public String toString() {
        return "Task{" +
                "title='" + title + '\'' +
                ", subject='" + subject + '\'' +
                ", id=" + id +
                ", status='" + status + '\'' +
                '}';
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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

}