import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    // Метод автосохранения
    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Заголовок CSV
            writer.write("id,type,name,status,description,epic");
            writer.newLine();

            // Сохраняем все задачи
            for (Task task : getTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }
            // Сохраняем все эпики
            for (Epic epic : getEpics()) {
                writer.write(toString(epic));
                writer.newLine();
            }
            // Сохраняем все подзадачи
            for (Subtask sub : getSubtasks()) {
                writer.write(toString(sub));
                writer.newLine();
            }

            writer.newLine();

            // Сохраняем историю просмотров
            String historyLine = historyToString(getHistory());
            writer.write(historyLine);

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл " + file.getAbsolutePath(), e);
        }
    }

    // Преобразуем задачу в строку CSV
    private String toString(Task task) {
        String type = task instanceof Epic ? "EPIC" :
                task instanceof Subtask ? "SUBTASK" : "TASK";
        String epicId = task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "";
        return String.join(",",
                String.valueOf(task.getId()),
                type,
                task.getTitle(),
                task.getStatus().name(),
                task.getDescription(),
                epicId
        );
    }

    // Преобразуем историю в строку
    private String historyToString(List<Task> history) {
        List<String> ids = new ArrayList<>();
        for (Task task : history) {
            ids.add(String.valueOf(task.getId()));
        }
        return String.join(",", ids);
    }

    // Методы create/update/delete переопределяем для автосохранения
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
    public Task getTask(int id) {
        Task task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = super.getEpic(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = super.getSubtask(id);
        save();
        return subtask;
    }

    // Статический метод загрузки из файла
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.size() < 2) return manager; // пустой менеджер

            Map<Integer, Epic> epicsMap = new HashMap<>();
            Map<Integer, Task> allTasks = new HashMap<>();

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) {
                    // История начинается после пустой строки
                    i++;
                    if (i < lines.size()) {
                        String[] historyIds = lines.get(i).split(",");
                        for (String idStr : historyIds) {
                            if (!idStr.isEmpty()) {
                                int id = Integer.parseInt(idStr);
                                Task task = allTasks.get(id);
                                if (task != null) manager.historyManager.add(task);
                            }
                        }
                    }
                    break;
                }
                Task task = fromString(line, epicsMap);
                if (task instanceof Epic) epicsMap.put(task.getId(), (Epic) task);
                allTasks.put(task.getId(), task);

                if (task instanceof Task && !(task instanceof Epic) && !(task instanceof Subtask))
                    manager.createTask(task);
                else if (task instanceof Epic) manager.createEpic((Epic) task);
                else if (task instanceof Subtask) {
                    Subtask sub = (Subtask) task;
                    manager.createSubtask(sub);
                }
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла " + file.getAbsolutePath(), e);
        }
        return manager;
    }

    // Восстановление задачи из строки
    private static Task fromString(String value, Map<Integer, Epic> epicsMap) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String title = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];

        switch (type) {
            case "TASK":
                return new Task(id, title, description, status);
            case "EPIC":
                return new Epic(id, title, description);
            case "SUBTASK":
                int epicId = Integer.parseInt(parts[5]);
                Subtask subtask = new Subtask(id, title, description, status, epicId);
                Epic epic = epicsMap.get(epicId);
                if (epic != null) epic.addSubtask(subtask);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }
}
