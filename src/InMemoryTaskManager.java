import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    private int nextId = 1;

    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private int generateId() {
        return nextId++;
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
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) historyManager.add(subtask);
        return subtask;
    }

    @Override
    public void createTask(Task task) {
        if (task == null) return;
        task.setId(generateId());
        tasks.put(task.getId(), task);
    }

    @Override
    public void createEpic(Epic epic) {
        if (epic == null) return;
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        if (subtask == null) return;
        // Проверка, что эпик существует и не равен подзадаче
        if (!epics.containsKey(subtask.getEpicId()) || subtask.getEpicId() == subtask.getId()) return;

        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subtask);
            recalculateEpicStatus(epic); // <-- добавляем пересчёт
        }
    }

    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) return;
        tasks.put(task.getId(), task);
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
        subtasks.put(subtask.getId(), subtask);
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
                recalculateEpicStatus(epic);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        ArrayList<Subtask> result = new ArrayList<>();
        if (epic != null) {
            for (int subId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.get(subId);
                if (subtask != null) result.add(subtask);
            }
        }
        return result;
    }

    @Override
    public ArrayList<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistory());
    }

    private void recalculateEpicStatus(Epic epic) {
        List<Integer> subIds = epic.getSubtaskIds();
        if (subIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int id : subIds) {
            TaskStatus status = subtasks.get(id).getStatus();
            if (status != TaskStatus.NEW) allNew = false;
            if (status != TaskStatus.DONE) allDone = false;
        }

        if (allNew) epic.setStatus(TaskStatus.NEW);
        else if (allDone) epic.setStatus(TaskStatus.DONE);
        else epic.setStatus(TaskStatus.IN_PROGRESS);
    }
}
