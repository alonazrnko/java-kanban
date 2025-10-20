import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subtaskIds = new ArrayList<>();

    public Epic(int id, String title, String description) {
        super(id, title, description, TaskStatus.NEW);
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public boolean addSubtask(Subtask subtask) {
        if (subtask == null) {
            return false;
        }
        if (subtask.getId() == this.getId()) {
            return false;
        }
        if (!subtaskIds.contains(subtask.getId())) {
            subtaskIds.add(subtask.getId());
            return true;
        }
        return false;
    }


    public void removeSubtask(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }
}
