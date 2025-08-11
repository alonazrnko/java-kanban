import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // Создадим задачу
        Task task1 = new Task(0, "Задача 1", "Описание задачи 1", TaskStatus.NEW);
        Task task2 = new Task(0, "Задача 2", "Описание задачи 2", TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        // Создадим эпик
        Epic epic1 = new Epic(0, "Эпик 1", "Описание эпика 1");
        manager.createEpic(epic1);

        // Получим id созданного эпика
        int epic1Id = manager.getAllEpics().get(0).getId();

        // Создадим подзадачи для эпика
        Subtask epic1Sub1 = new Subtask(0, "Подзадача 1.1", "Описание подзадачи 1.1", TaskStatus.NEW, epic1Id);
        Subtask epic1Sub2 = new Subtask(0, "Подзадача 1.2", "Описание подзадачи 1.2", TaskStatus.NEW, epic1Id);
        manager.createSubtask(epic1Sub1);
        manager.createSubtask(epic1Sub2);

        Epic epic2 = new Epic(0, "Эпик 2", "Описание эпика 2");
        manager.createEpic(epic2);
        int epic2Id = manager.getAllEpics().get(1).getId();

        // Добавляем одну подзадачу ко второму эпику
        Subtask epic2Sub1 = new Subtask(0, "Подзадача 2.1", "Описание подзадачи 2.1", TaskStatus.NEW, epic2Id);
        manager.createSubtask(epic2Sub1);

        // Печатаем списки задач, эпиков и подзадач
        System.out.println("Все задачи");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("Все эпики");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic + " с подзадачами: " + epic.getSubtaskIds());
        }

        System.out.println("Все подзадачи");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        // Изменяем статусы задач
        ArrayList<Task> allTasks = manager.getAllTasks();
        Task updatedTask1 = new Task(allTasks.get(0).getId(), allTasks.get(0).getTitle(), allTasks.get(0).getDescription(), TaskStatus.DONE);
        manager.updateTask(updatedTask1);

        Task updatedTask2 = new Task(allTasks.get(1).getId(), allTasks.get(1).getTitle(), allTasks.get(1).getDescription(), TaskStatus.IN_PROGRESS);
        manager.updateTask(updatedTask2);

        // Изменяем статусы подзадач первого эпика
        ArrayList<Subtask> epic1Subs = manager.getSubtasksByEpicId(epic1Id);
        Subtask updatedEpic1Sub1 = new Subtask(epic1Subs.get(0).getId(), epic1Subs.get(0).getTitle(), epic1Subs.get(0).getDescription(), TaskStatus.DONE, epic1Id);
        Subtask updatedEpic1Sub2 = new Subtask(epic1Subs.get(1).getId(), epic1Subs.get(1).getTitle(), epic1Subs.get(1).getDescription(), TaskStatus.DONE, epic1Id);
        manager.updateSubtask(updatedEpic1Sub1);
        manager.updateSubtask(updatedEpic1Sub2);

        // Изменяем статус подзадачи второго эпика
        ArrayList<Subtask> epic2Subs = manager.getSubtasksByEpicId(epic2Id);
        Subtask updatedEpic2Sub1 = new Subtask(epic2Subs.get(0).getId(), epic2Subs.get(0).getTitle(), epic2Subs.get(0).getDescription(), TaskStatus.IN_PROGRESS, epic2Id);
        manager.updateSubtask(updatedEpic2Sub1);

        // Печатаем обновленные объекты
        System.out.println("Обновленные задачи");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("Обновленные эпики");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic + " с подзадачами: " + epic.getSubtaskIds());
        }

        System.out.println("Обновленные подзадачи");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        // Проверяем, что статусы эпиков пересчитались по подзадачам
        System.out.println("Статус эпика 1 (должен быть DONE): " + manager.getEpicById(epic1Id).getStatus());
        System.out.println("Статус эпика 2 (должен быть IN_PROGRESS): " + manager.getEpicById(epic2Id).getStatus());

        // Удаляем одну задачу (task2)
        manager.deleteTaskById(updatedTask2.getId());

        // Удаляем один эпик (эпик 1)
        manager.deleteEpicById(epic1Id);

        // Печатаем списки после удаления
        System.out.println("Списки после удаления");
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("Эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic + " с подзадачами: " + epic.getSubtaskIds());
        }

        System.out.println("Подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }
    }
}
