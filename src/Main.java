public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        Task task1 = new Task(0, "Задача 1", "Описание 1", TaskStatus.NEW);
        Task task2 = new Task(0, "Задача 2", "Описание 2", TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        Epic epic1 = new Epic(0, "Эпик 1", "С эпиком 1");
        manager.createEpic(epic1);

        Subtask sub1 = new Subtask(0, "Подзадача 1", "Описание", TaskStatus.NEW, epic1.getId());
        Subtask sub2 = new Subtask(0, "Подзадача 2", "Описание", TaskStatus.NEW, epic1.getId());
        Subtask sub3 = new Subtask(0, "Подзадача 3", "Описание", TaskStatus.NEW, epic1.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);
        manager.createSubtask(sub3);

        Epic epic2 = new Epic(0, "Эпик 2", "Без подзадач");
        manager.createEpic(epic2);

        // Просмотры в разном порядке
        manager.getTask(task1.getId());
        manager.getEpic(epic1.getId());
        manager.getSubtask(sub2.getId());
        manager.getTask(task1.getId()); // повторный просмотр — дубль не появится
        manager.getSubtask(sub1.getId());
        manager.getEpic(epic2.getId());

        printHistory(manager);

        // Удаляем задачу и эпик
        manager.deleteTaskById(task1.getId());
        manager.deleteEpicById(epic1.getId());

        System.out.println("\nПосле удаления:");
        printHistory(manager);
    }

    private static void printHistory(TaskManager manager) {
        System.out.println("\nИстория просмотров:");
        for (Task t : manager.getHistory()) {
            System.out.println(t);
        }
    }
}
