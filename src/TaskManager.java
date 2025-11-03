import java.util.ArrayList;
import java.util.Optional;

public interface TaskManager {
    ArrayList<Task> getTasks();

    ArrayList<Task> getPrioritizedTasks();

    ArrayList<Epic> getEpics();

    ArrayList<Subtask> getSubtasks();

    Optional<Task> getTask(int id);

    Optional<Epic> getEpic(int id);

    Optional<Subtask> getSubtask(int id);

    void createTask(Task task);

    void createEpic(Epic epic);

    void createSubtask(Subtask subtask);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    void deleteTaskById(int id);

    void deleteEpicById(int id);

    void deleteSubtaskById(int id);

    void deleteTasks();

    void deleteSubtasks();

    void deleteEpics();

    ArrayList<Subtask> getEpicSubtasks(int epicId);

    ArrayList<Task> getHistory();
}
