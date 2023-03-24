package tracker.model;

import java.time.Instant;
import java.util.Objects;

public class Task {

    protected String title;
    protected String description;
    protected final int id;
    protected Status status;
    protected long duration;
    protected Instant startTime;

    public Task(int id, String title, Status status, String description, long duration, Instant startTime) {
        this.title = title;
        this.description = description;
        this.id = id;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return getType() + "{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                ", duration='" + duration + '\'' +
                ", startTime='" + startTime + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(title, task.title) && Objects.equals(description, task.description) &&
                status == task.status;
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

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Type getType() {
        return Type.TASK;
    }

    public Instant getEndTime() {
        return startTime.plusSeconds(duration * 60);
    }

}