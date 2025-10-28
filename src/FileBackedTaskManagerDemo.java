import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class FileBackedTaskManagerDemo {
    public static void main(String[] args) {
        try {
            // Создаём временный файл для хранения менеджера
            File file = File.createTempFile("tasks", ".csv");
            System.out.println("Файл сохранения: " + file.getAbsolutePath());

            // Создаём менеджер с автосохранением
            FileBackedTaskManager manager = new FileBackedTaskManager(file);

            Task task1 = new Task(
                    0,
                    "Задача 1",
                    "Описание задачи 1",
                    TaskStatus.NEW,
                    Duration.ofMinutes(60),
                    LocalDateTime.of(2025, 10, 28, 10, 0)
            );

            Task task2 = new Task(
                    0,
                    "Задача 2",
                    "Описание задачи 2",
                    TaskStatus.IN_PROGRESS,
                    Duration.ofMinutes(90),
                    LocalDateTime.of(2025, 10, 28, 12, 0)
            );

            manager.createTask(task1);
            manager.createTask(task2);

            Epic epic = new Epic(0, "Эпик 1", "Описание эпика");
            manager.createEpic(epic);

            Subtask sub1 = new Subtask(
                    0,
                    "Подзадача 1",
                    "Описание подзадачи 1",
                    TaskStatus.NEW,
                    epic.getId(),
                    Duration.ofMinutes(120),
                    LocalDateTime.of(2025, 10, 29, 9, 0)
            );

            Subtask sub2 = new Subtask(
                    0,
                    "Подзадача 2",
                    "Описание подзадачи 2",
                    TaskStatus.DONE,
                    epic.getId(),
                    Duration.ofMinutes(60),
                    LocalDateTime.of(2025, 10, 29, 12, 0)
            );

            manager.createSubtask(sub1);
            manager.createSubtask(sub2);

            manager.getTask(task1.getId());
            manager.getEpic(epic.getId());
            manager.getSubtask(sub2.getId());

            System.out.println("\nМенеджер до перезапуска");
            printAll(manager);

            System.out.println("\nСписок задач по приоритету (по времени старта)");
            manager.getPrioritizedTasks().forEach(System.out::println);

            System.out.println("\nПроверка пересечения задач");
            try {
                Task overlappingTask = new Task(
                        0,
                        "Пересекающаяся задача",
                        "Проверка пересечений",
                        TaskStatus.NEW,
                        Duration.ofMinutes(30),
                        LocalDateTime.of(2025, 10, 28, 10, 30)
                );

                manager.createTask(overlappingTask);
                System.out.println("✅ Задача добавлена (не пересекается)");
            } catch (IllegalArgumentException e) {
                System.out.println("❌ " + e.getMessage());
            }

            // Загружаем менеджер из файла
            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

            System.out.println("\nМенеджер после загрузки из файла");
            printAll(loadedManager);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printAll(FileBackedTaskManager manager) {
        System.out.println("Задачи:");
        manager.getTasks().forEach(System.out::println);

        System.out.println("\nЭпики:");
        manager.getEpics().forEach(epic -> {
            System.out.println(epic);
            ArrayList<Subtask> subs = manager.getEpicSubtasks(epic.getId());
            System.out.println("\nПодзадачи:");
            subs.forEach(sub -> System.out.println("    " + sub));
        });

        System.out.println("\nИстория просмотров:");
        manager.getHistory().forEach(System.out::println);
    }
}



