import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private int nextId = 1;

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    // Генерация нового уникального ID
    private int generateId() {
        return nextId++;
    }

    // --- Методы для Task ---

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public void createTask(Task task) {
        int id = generateId();
        Task newTask = new Task(id, task.getTitle(), task.getDescription(), task.getStatus());
        tasks.put(id, newTask);
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    // --- Методы для Epic ---

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public void deleteAllEpics() {
        // При удалении эпиков удаляем и их подзадачи
        for (Epic epic : epics.values()) {
            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
        epics.clear();
        subtasks.clear();
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public void createEpic(Epic epic) {
        int id = generateId();
        Epic newEpic = new Epic(id, epic.getTitle(), epic.getDescription());
        epics.put(id, newEpic);
    }

    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            // Статус эпика нельзя менять извне, сохраняем старый статус
            TaskStatus oldStatus = epics.get(epic.getId()).getStatus();

            Epic updatedEpic = new Epic(epic.getId(), epic.getTitle(), epic.getDescription());
            // Копируем подзадачи из старого эпика
            for (int subtaskId : epics.get(epic.getId()).getSubtaskIds()) {
                updatedEpic.addSubtask(subtaskId);
            }
            // Статус пересчитаем по подзадачам
            recalculateEpicStatus(updatedEpic);

            epics.put(epic.getId(), updatedEpic);
        }
    }

    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            // Удаляем все подзадачи эпика
            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    // --- Методы для Subtask ---

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void deleteAllSubtasks() {
        // Удаляем подзадачи из эпиков
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            recalculateEpicStatus(epic);
        }
        subtasks.clear();
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public void createSubtask(Subtask subtask) {
        int id = generateId();
        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            System.out.println("Эпик с id " + epicId + " не найден");
        }
        Subtask newSubtask = new Subtask(id, subtask.getTitle(), subtask.getDescription(), subtask.getStatus(), epicId);
        subtasks.put(id, newSubtask);
        epic.addSubtask(id);
        recalculateEpicStatus(epic);
    }

    public void updateSubtask(Subtask subtask) {
        int id = subtask.getId();
        if (!subtasks.containsKey(id)) {
            return;
        }
        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            System.out.println("Эпик с id " + epicId + " не найден");
        }
        subtasks.put(id, subtask);
        recalculateEpicStatus(epic);
    }

    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                recalculateEpicStatus(epic);
            }
        }
    }

    // Получение всех подзадач эпика по его id
    public ArrayList<Subtask> getSubtasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>(subtasks.values());
        }
        ArrayList<Subtask> result = new ArrayList<>();
        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                result.add(subtask);
            }
        }
        return result;
    }

    // Пересчёт статуса эпика согласно статусам подзадач
    private void recalculateEpicStatus(Epic epic) {
        ArrayList<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) continue;

            TaskStatus status = subtask.getStatus();
            if (status != TaskStatus.NEW) {
                allNew = false;
            }
            if (status != TaskStatus.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}
