import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Task {
    private int id;
    private String title;
    private String description;
    private TaskStatus status;
    private Duration duration;
    private LocalDateTime startTime;

    public Task(int id, String title, String description, TaskStatus status, Duration duration, LocalDateTime startTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task(int id, String title, String description, TaskStatus status) {
        this(id, title, description, status, Duration.ZERO, null);
    }

    public Task(Task other) {
        this.id = other.id;
        this.title = other.title;
        this.description = other.description;
        this.status = other.status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public LocalDateTime getEndTime() {
        return (startTime != null && duration != null) ? startTime.plus(duration) : null;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", duration=" + (duration != null ? duration.toMinutes() + " min" : "null") +
                ", startTime=" + startTime +
                ", endTime=" + getEndTime() +
                '}';
    }
}
