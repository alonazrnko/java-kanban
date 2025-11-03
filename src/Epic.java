import java.util.ArrayList;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Map;

public class Epic extends Task {
    private final ArrayList<Integer> subtaskIds = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(int id, String title, String description) {
        super(id, title, description, TaskStatus.NEW, Duration.ZERO, null);
        this.endTime = null;
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

    public void recalculateTimeAndDuration(Map<Integer, Subtask> allSubtasksMap) {
        ArrayList<Subtask> subtasks = subtaskIds.stream()
                .map(allSubtasksMap::get) // получаем Subtask по id
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));


        setDuration(subtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus));

        setStartTime(subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null));

        if (subtasks.isEmpty()) {
            this.endTime = null;
            return;
        }

        this.endTime = subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public Duration getDuration() {
        return super.getDuration();
    }

    @Override
    public LocalDateTime getStartTime() {
        return super.getStartTime();
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }
}
