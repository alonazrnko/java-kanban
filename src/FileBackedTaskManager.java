import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void restoreEpic(Epic epic) {
        super.restoreEpic(epic);
        save();
    }

    @Override
    public void restoreSubtask(Subtask subtask) {
        super.restoreSubtask(subtask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public Optional<Task> getTask(int id) {
        Optional<Task> opt = super.getTask(id);
        opt.ifPresent(task -> save());
        return opt;
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        Optional<Epic> opt = super.getEpic(id);
        opt.ifPresent(epic -> save());
        return opt;
    }

    @Override
    public Optional<Subtask> getSubtask(int id) {
        Optional<Subtask> opt = super.getSubtask(id);
        opt.ifPresent(sub -> save());
        return opt;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            throw new ManagerSaveException("Файл недоступен: " + file);
        }

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.size() < 2) return manager;

            Map<Integer, Epic> epicsMap = new HashMap<>();
            Map<Integer, Task> allTasks = new HashMap<>();

            // Создаем все эпики
            lines.stream().skip(1) // пропускаем заголовок
                    .takeWhile(line -> !line.isEmpty()).map(line -> fromString(line, epicsMap)).forEach(task -> {
                        allTasks.put(task.getId(), task);
                        if (task instanceof Epic) {
                            manager.restoreEpic((Epic) task);
                            epicsMap.put(task.getId(), (Epic) task);
                        }
                    });

            // Создаем задачи и подзадачи
            allTasks.values().stream().filter(task -> !(task instanceof Epic)).forEach(task -> {
                if (task instanceof Subtask subtask) {
                    Epic epic = epicsMap.get(subtask.getEpicId());
                    if (epic != null) {
                        manager.restoreSubtask(subtask);
                    }
                } else {
                    manager.createTask(task);
                }
            });

            // Восстанавливаем историю
            int historyIndex = lines.indexOf("");
            if (historyIndex >= 0 && historyIndex + 1 < lines.size()) {
                Arrays.stream(lines.get(historyIndex + 1).split(",")).filter(s -> !s.isEmpty()).map(Integer::parseInt).map(allTasks::get).filter(Objects::nonNull).forEach(manager.historyManager::add);
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла " + file.getAbsolutePath(), e);
        }

        return manager;
    }

    // Преобразуем задачу в строку CSV
    private String toString(Task task) {
        String type = task instanceof Epic ? "EPIC" : task instanceof Subtask ? "SUBTASK" : "TASK";
        String epicId = task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "";
        String durationMinutes = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";
        String start = task.getStartTime() != null ? task.getStartTime().toString() : "";
        return String.join(",", String.valueOf(task.getId()), type, task.getTitle(), task.getStatus().name(), task.getDescription(), epicId, durationMinutes, start);
    }

    private String historyToString(List<Task> history) {
        List<String> ids = new ArrayList<>();
        for (Task task : history) {
            ids.add(String.valueOf(task.getId()));
        }
        return String.join(",", ids);
    }

    private static Task fromString(String value, Map<Integer, Epic> epicsMap) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String title = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];
        Duration duration = parts.length > 6 && !parts[6].isEmpty() ? Duration.ofMinutes(Long.parseLong(parts[6])) : null;
        LocalDateTime startTime = parts.length > 7 && !parts[7].isEmpty() ? LocalDateTime.parse(parts[7]) : null;

        switch (type) {
            case "TASK":
                return new Task(id, title, description, status, duration, startTime);
            case "EPIC":
                return new Epic(id, title, description);
            case "SUBTASK":
                int epicId = parts.length > 5 && !parts[5].isEmpty() ? Integer.parseInt(parts[5]) : 0;
                Subtask subtask = new Subtask(id, title, description, status, epicId, duration, startTime);
                Epic epic = epicsMap.get(epicId);
                if (epic != null) epic.addSubtask(subtask);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    private void save() {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("id,type,name,status,description,epic,duration,startTime");
            for (Task task : getAllTasks()) {
                writer.println(toString(task));
            }
            writer.println();
            writer.println(historyToString(historyManager.getHistory()));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл " + file.getAbsolutePath(), e);
        }
    }
}
