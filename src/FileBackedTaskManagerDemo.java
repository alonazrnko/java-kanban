import java.io.File;
import java.util.ArrayList;

public class FileBackedTaskManagerDemo {
    public static void main(String[] args) {
        try {
            // Создаём временный файл для хранения менеджера
            File file = File.createTempFile("tasks", ".csv");
            System.out.println("Файл сохранения: " + file.getAbsolutePath());

            // Создаём менеджер с автосохранением
            FileBackedTaskManager manager = new FileBackedTaskManager(file);

            // Создаём задачи
            Task task1 = new Task(0, "Задача 1", "Описание задачи 1", TaskStatus.NEW);
            Task task2 = new Task(0, "Задача 2", "Описание задачи 2", TaskStatus.IN_PROGRESS);
            manager.createTask(task1);
            manager.createTask(task2);

            // Создаём эпик и подзадачи
            Epic epic = new Epic(0, "Эпик 1", "Описание эпика");
            manager.createEpic(epic);

            Subtask sub1 = new Subtask(0, "Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW, epic.getId());
            Subtask sub2 = new Subtask(0, "Подзадача 2", "Описание подзадачи 2", TaskStatus.DONE, epic.getId());
            manager.createSubtask(sub1);
            manager.createSubtask(sub2);

            // Просматриваем задачи, чтобы сформировать историю
            manager.getTask(task1.getId());
            manager.getEpic(epic.getId());
            manager.getSubtask(sub2.getId());

            System.out.println("\n=== Менеджер до перезапуска ===");
            printAll(manager);

            // Загружаем менеджер из файла
            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

            System.out.println("\n=== Менеджер после загрузки из файла ===");
            printAll(loadedManager);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printAll(FileBackedTaskManager manager) {
        System.out.println("Задачи:");
        for (Task t : manager.getTasks()) System.out.println(t);

        System.out.println("\nЭпики:");
        for (Epic e : manager.getEpics()) {
            System.out.println(e);
            ArrayList<Subtask> subs = manager.getEpicSubtasks(e.getId());
            System.out.println("  Подзадачи:");
            for (Subtask s : subs) System.out.println("    " + s);
        }

        System.out.println("\nИстория просмотров:");
        for (Task h : manager.getHistory()) System.out.println(h);
    }
}

