import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime, Comparator.nullsLast(LocalDateTime::compareTo)).thenComparing(Task::getId));

    private int nextId = 1;

    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    private int generateId() {
        return nextId++;
    }

    @Override
    public ArrayList<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Optional<Task> getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
            return Optional.of(task);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
            return Optional.of(epic);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Subtask> getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
            return Optional.of(subtask);
        }
        return Optional.empty();
    }

    @Override
    public void createTask(Task task) {
        task.setId(generateId());
        checkIntersection(task);
        if (hasIntersection(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
        }

        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) prioritizedTasks.add(task);
    }

    @Override
    public void createEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId()) || subtask.getEpicId() == subtask.getId()) {
            throw new IllegalArgumentException("Epic с id=" + subtask.getEpicId() + " не найден");
        }

        subtask.setId(generateId());
        checkIntersection(subtask);
        if (hasIntersection(subtask)) {
            throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
        }

        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (subtask.getStartTime() != null) prioritizedTasks.add(subtask);
        epic.addSubtask(subtask);
        epic.recalculateTimeAndDuration(subtasks);
        recalculateEpicStatus(epic);
    }

    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) return;
        checkIntersection(task);
        if (hasIntersection(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
        }
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) prioritizedTasks.add(task);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) return;
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) return;
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) return;
        checkIntersection(subtask);
        if (hasIntersection(subtask)) {
            throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
        }

        subtasks.put(subtask.getId(), subtask);
        epic.recalculateTimeAndDuration(subtasks);
        recalculateEpicStatus(epic);
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Integer subId : epic.getSubtaskIds()) {
                subtasks.remove(subId);
                historyManager.remove(subId);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                epic.recalculateTimeAndDuration(subtasks);
                recalculateEpicStatus(epic);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        return subtasks.values().stream().filter(subtask -> subtask.getEpicId() == epicId).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistory());
    }

    public static boolean isIntersecting(Task t1, Task t2) {
        if (t1.getStartTime() == null || t1.getEndTime() == null || t2.getStartTime() == null || t2.getEndTime() == null) {
            return false;
        }
        return !t1.getEndTime().isBefore(t2.getStartTime()) && !t1.getStartTime().isAfter(t2.getEndTime());
    }

    private boolean hasIntersection(Task task) {
        return getPrioritizedTasks().stream().filter(t -> t.getId() != task.getId()).anyMatch(t -> isIntersecting(t, task));
    }

    public List<Task> getAllTasks() {
        List<Task> all = new ArrayList<>();
        all.addAll(tasks.values());
        all.addAll(epics.values());
        all.addAll(subtasks.values());
        return all;
    }

    public void restoreEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        prioritizedTasks.add(epic); // если нужно
    }

    public void restoreSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subtask);
            epic.recalculateTimeAndDuration(subtasks);
            recalculateEpicStatus(epic);
        }
    }

    private void recalculateEpicStatus(Epic epic) {
        List<Integer> subIds = epic.getSubtaskIds();
        if (subIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        List<TaskStatus> statuses = subIds.stream().map(subtasks::get)        // получаем Subtask по id
                .filter(Objects::nonNull).map(Task::getStatus).collect(Collectors.toList());

        boolean allNew = statuses.stream().allMatch(s -> s == TaskStatus.NEW);
        boolean allDone = statuses.stream().allMatch(s -> s == TaskStatus.DONE);

        if (allNew) epic.setStatus(TaskStatus.NEW);
        else if (allDone) epic.setStatus(TaskStatus.DONE);
        else epic.setStatus(TaskStatus.IN_PROGRESS);
    }

    private void checkIntersection(Task task) {
        for (Task t : prioritizedTasks) {
            if (isIntersecting(t, task)) {
                throw new IllegalArgumentException("Задача пересекается по времени с другой задачей: " + t);
            }
        }
    }
}
