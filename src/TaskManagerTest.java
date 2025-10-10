import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void testCreateAndGetTaskById() {
        Task task = new Task(0, "Task", "Description", TaskStatus.NEW);
        manager.createTask(task);

        Task saved = manager.getTask(task.getId());
        assertNotNull(saved);
        assertEquals(task.getId(), saved.getId());
        assertEquals("Task", saved.getTitle());
    }

    @Test
    void testCreateAndGetEpicAndSubtasks() {
        Epic epic = new Epic(0, "Epic", "Epic Desc");
        manager.createEpic(epic);

        Subtask sub1 = new Subtask(0, "Sub1", "Desc1", TaskStatus.NEW, epic.getId());
        Subtask sub2 = new Subtask(0, "Sub2", "Desc2", TaskStatus.NEW, epic.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        ArrayList<Subtask> subtasks = manager.getEpicSubtasks(epic.getId());
        assertEquals(2, subtasks.size());
        assertTrue(subtasks.stream().anyMatch(s -> s.getTitle().equals("Sub1")));
        assertTrue(subtasks.stream().anyMatch(s -> s.getTitle().equals("Sub2")));
    }

    @Test
    void testSubtaskEpicIdValidation() {
        int nonexistentEpicId = 9999; // Эпика с таким ID нет в менеджере

        Subtask subtask = new Subtask(0, "Test subtask", "Description", TaskStatus.NEW, nonexistentEpicId);
        manager.createSubtask(subtask);

        // Проверяем, что подзадачи с таким ID нет в менеджере
        assertNull(manager.getSubtask(subtask.getId()), "Подзадача с несуществующим epicId не должна быть создана");
    }

    @Test
    void testTaskEqualityById() {
        Task task1 = new Task(0, "Task1", "Desc", TaskStatus.NEW);
        Task task2 = new Task(0, "Task2", "Desc", TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        Task copy = new Task(0, "Copy", "Copy Desc", TaskStatus.NEW);
        copy.setId(task1.getId());

        assertEquals(task1, copy);
        assertEquals(task1.hashCode(), copy.hashCode());
    }

    @Test
    void testTaskSubclassesEqualityById() {
        Epic epic1 = new Epic(0, "Epic1", "Desc1");
        Epic epic2 = new Epic(0, "Epic2", "Desc2");
        manager.createEpic(epic1);
        manager.createEpic(epic2);

        Subtask sub1 = new Subtask(0, "Sub1", "Desc1", TaskStatus.NEW, epic1.getId());
        Subtask sub2 = new Subtask(0, "Sub2", "Desc2", TaskStatus.NEW, epic1.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        Epic epicCopy = new Epic(0, "CopyEpic", "CopyDesc");
        epicCopy.setId(epic1.getId());
        assertEquals(epic1, epicCopy);
        assertEquals(epic1.hashCode(), epicCopy.hashCode());

        Subtask subCopy = new Subtask(0, "CopySub", "CopyDesc", TaskStatus.NEW, epic1.getId());
        subCopy.setId(sub1.getId());
        assertEquals(sub1, subCopy);
        assertEquals(sub1.hashCode(), subCopy.hashCode());
    }

    @Test
    void testUpdateTask() {
        Task task = new Task(0, "Old Title", "Old Desc", TaskStatus.NEW);
        manager.createTask(task);

        task.setTitle("New Title");
        manager.updateTask(task);

        Task updated = manager.getTask(task.getId());
        assertEquals("New Title", updated.getTitle());
    }

    @Test
    void testDeleteTask() {
        Task task = new Task(0, "Task", "Desc", TaskStatus.NEW);
        manager.createTask(task);

        manager.deleteTaskById(task.getId());
        assertNull(manager.getTask(task.getId()));
    }

    @Test
    void testDeleteEpicAlsoDeletesSubtasks() {
        Epic epic = new Epic(0, "Epic", "Desc");
        manager.createEpic(epic);

        Subtask subtask = new Subtask(0, "Sub", "Desc", TaskStatus.NEW, epic.getId());
        manager.createSubtask(subtask);

        manager.deleteEpicById(epic.getId());

        assertNull(manager.getEpic(epic.getId()));
        assertNull(manager.getSubtask(subtask.getId()));
    }

    @Test
    void testHistoryManagerNoDuplicates() {
        // Создаём 12 задач
        for (int i = 1; i <= 12; i++) {
            manager.createTask(new Task(0, "Task" + i, "Desc" + i, TaskStatus.NEW));
        }

        // Просматриваем задачи 1..12
        for (int i = 1; i <= 12; i++) {
            manager.getTask(i);
        }

        ArrayList<Task> history = manager.getHistory();

        // Проверяем, что все задачи есть и нет дубликатов
        assertEquals(12, history.size(), "История должна содержать все просмотры без ограничений");

        // Проверяем уникальность
        long distinctCount = history.stream().map(Task::getId).distinct().count();
        assertEquals(12, distinctCount, "В истории не должно быть повторов");
    }


    @Test
    void testEpicCannotContainItselfAsSubtask() {
        Epic epic = new Epic(0, "Epic", "Desc");
        manager.createEpic(epic);

        Subtask subtask = new Subtask(0, "Sub", "Desc", TaskStatus.NEW, epic.getId());
        manager.createSubtask(subtask);

        epic.addSubtask(new Subtask(0, "FakeSub", "Desc", TaskStatus.NEW, epic.getId()) {{
            setId(epic.getId());
        }});

        // Проверка, что нельзя добавить эпик в самого себя
        assertFalse(epic.getSubtaskIds().contains(epic.getId()));
    }

    @Test
    void testSubtaskCannotBeItsOwnEpic() {
        Subtask subtask = new Subtask(0, "Subtask name", "Description", TaskStatus.NEW, 0);
        int ownId = 123;
        subtask.setId(ownId);

        // Попытка установить epicId равным собственному id
        subtask.setEpicId(ownId);

        // Проверяем, что epicId не равен id подзадачи
        assertNotEquals(ownId, subtask.getEpicId(), "epicId не должен быть равен id подзадачи");
    }

    @Test
    void testManagersReturnsInitializedInstances() {
        TaskManager taskManager = Managers.getDefault();
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(taskManager, "TaskManager должен быть проинициализирован");
        assertNotNull(historyManager, "HistoryManager должен быть проинициализирован");

        // Проверим, что они готовы к работе (например, можно добавить задачу)
        Task testTask = new Task(0, "Test", "Desc", TaskStatus.NEW);
        taskManager.createTask(testTask);
        assertNotNull(taskManager.getTask(testTask.getId()));
    }

    @Test
    void testTaskManagerAddsDifferentTypesAndFindsById() {
        Task task = new Task(0, "Task", "Desc", TaskStatus.NEW);
        Epic epic = new Epic(0, "Epic", "Desc");
        Subtask subtask = new Subtask(0, "Sub", "Desc", TaskStatus.NEW, epic.getId());

        manager.createTask(task);
        manager.createEpic(epic);
        subtask.setEpicId(epic.getId());
        manager.createSubtask(subtask);

        // Проверяем, что все добавлены и доступны по id
        assertEquals(task, manager.getTask(task.getId()));
        assertEquals(epic, manager.getEpic(epic.getId()));
        assertEquals(subtask, manager.getSubtask(subtask.getId()));

        // Проверяем типы
        assertInstanceOf(Task.class, manager.getTask(task.getId()));
        assertInstanceOf(Epic.class, manager.getEpic(epic.getId()));
        assertInstanceOf(Subtask.class, manager.getSubtask(subtask.getId()));
    }

    @Test
    void testIdConflictsBetweenUserSetAndGenerated() {
        TaskManager manager = new InMemoryTaskManager();

        // Создаём задачу с вручную заданным ID
        Task manualTask = new Task(0, "Manual Task", "Desc", TaskStatus.NEW);
        manualTask.setId(100);  // вручную заданный ID
        manager.createTask(manualTask);

        // Создаём задачу без ID (или с id=0), чтобы менеджер сгенерировал ID
        Task autoTask = new Task(0, "Auto Task", "Desc", TaskStatus.NEW);
        // id по умолчанию 0 или не задан
        manager.createTask(autoTask);

        int manualId = manualTask.getId();
        int autoId = autoTask.getId();

        assertNotEquals(manualId, autoId, "Сгенерированный ID не должен совпадать с вручную заданным");
    }

    @Test
    void testTaskImmutabilityWhenAddedToManager() {
        Task originalTask = new Task(0, "Original Title", "Original Description", TaskStatus.NEW);
        String originalTitle = originalTask.getTitle();
        String originalDescription = originalTask.getDescription();
        int originalId = originalTask.getId();

        manager.createTask(originalTask);

        assertNotEquals(originalId, originalTask.getId(), "id должен измениться после добавления");
        assertEquals(originalTitle, originalTask.getTitle(), "title не должен измениться");
        assertEquals(originalDescription, originalTask.getDescription(), "description не должен измениться");

        // Проверим через getTask
        Task retrieved = manager.getTask(originalTask.getId());
        assertEquals(originalTitle, retrieved.getTitle());
        assertEquals(originalDescription, retrieved.getDescription());
    }

    @Test
    void testHistoryManagerKeepsOnlyLatestVersion() {
        HistoryManager historyManager = new InMemoryHistoryManager();

        Task task = new Task(0, "Original Name", "Original Description", TaskStatus.NEW);
        task.setId(1);

        // Первый просмотр
        historyManager.add(task);

        // Меняем данные и "просматриваем" заново
        task.setTitle("Updated Name");
        task.setDescription("Updated Description");
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "В истории должна быть только последняя версия задачи");

        Task latest = history.get(0);
        assertEquals("Updated Name", latest.getTitle(), "Имя должно быть обновлённым");
        assertEquals("Updated Description", latest.getDescription(), "Описание должно быть обновлённым");
    }
}
