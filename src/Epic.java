import java.util.ArrayList;

public class Epic extends Task {
    // Список id подзадач
    private final ArrayList<Integer> subtaskIds = new ArrayList<>();

    public Epic(int id, String title, String description) {
        // Статус эпика задаётся менеджером, при создании NEW
        super(id, title, description, TaskStatus.NEW);
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtask(int subtaskId) {
        if (!subtaskIds.contains(subtaskId)) {
            subtaskIds.add(subtaskId);
        }
    }

    public void removeSubtask(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }
}
