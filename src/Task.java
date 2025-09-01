import java.util.Objects;

public class Task {
    protected int id;
    protected String title;
    protected String description;

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Task(Task other) {
        this.id = other.id;
        this.title = other.title;
        this.description = other.description;
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

    @Override
    public String toString() {
        return String.format("Task{id=%d, title='%s'}", id, title);
    }
}
