import org.junit.jupiter.api.*;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void testCreateAndGetTaskById() {
        Task task = new Task("Task", "Description");
        manager.createTask(task);

        Task saved = manager.getTask(task.getId());
        assertNotNull(saved);
        assertEquals(task.getId(), saved.getId());
        assertEquals("Task", saved.getTitle());
    }

    @Test
    void testCreateAndGetEpicAndSubtasks() {
        Epic epic = new Epic("Epic", "Epic Desc");
        manager.createEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc1", epic.getId());
        Subtask sub2 = new Subtask("Sub2", "Desc2", epic.getId());
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

        Subtask subtask = new Subtask("Test subtask", "Description", nonexistentEpicId);
        manager.createSubtask(subtask);

        // Проверяем, что подзадачи с таким ID нет в менеджере
        assertNull(manager.getSubtask(subtask.getId()), "Подзадача с несуществующим epicId не должна быть создана");
    }

    @Test
    void testTaskEqualityById() {
        Task task1 = new Task("Task1", "Desc");
        Task task2 = new Task("Task2", "Desc");
        manager.createTask(task1);
        manager.createTask(task2);

        Task copy = new Task("Copy", "Copy Desc");
        copy.setId(task1.getId());

        assertEquals(task1, copy);
        assertEquals(task1.hashCode(), copy.hashCode());
    }

    @Test
    void testTaskSubclassesEqualityById() {
        Epic epic1 = new Epic("Epic1", "Desc1");
        Epic epic2 = new Epic("Epic2", "Desc2");
        manager.createEpic(epic1);
        manager.createEpic(epic2);

        Subtask sub1 = new Subtask("Sub1", "Desc1", epic1.getId());
        Subtask sub2 = new Subtask("Sub2", "Desc2", epic1.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        Epic epicCopy = new Epic("CopyEpic", "CopyDesc");
        epicCopy.setId(epic1.getId());
        assertEquals(epic1, epicCopy);
        assertEquals(epic1.hashCode(), epicCopy.hashCode());

        Subtask subCopy = new Subtask("CopySub", "CopyDesc", epic1.getId());
        subCopy.setId(sub1.getId());
        assertEquals(sub1, subCopy);
        assertEquals(sub1.hashCode(), subCopy.hashCode());
    }

    @Test
    void testUpdateTask() {
        Task task = new Task("Old Title", "Old Desc");
        manager.createTask(task);

        task.setTitle("New Title");
        manager.updateTask(task);

        Task updated = manager.getTask(task.getId());
        assertEquals("New Title", updated.getTitle());
    }

    @Test
    void testDeleteTask() {
        Task task = new Task("Task", "Desc");
        manager.createTask(task);

        manager.deleteTaskById(task.getId());
        assertNull(manager.getTask(task.getId()));
    }

    @Test
    void testDeleteEpicAlsoDeletesSubtasks() {
        Epic epic = new Epic("Epic", "Desc");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Sub", "Desc", epic.getId());
        manager.createSubtask(subtask);

        manager.deleteEpicById(epic.getId());

        assertNull(manager.getEpic(epic.getId()));
        assertNull(manager.getSubtask(subtask.getId()));
    }

    @Test
    void testHistoryLimitAndDuplicates() {
        // Создаем 12 задач
        for (int i = 1; i <= 12; i++) {
            manager.createTask(new Task("Task" + i, "Desc" + i));
        }

        // Просматриваем задачи 1..12
        for (int i = 1; i <= 12; i++) {
            manager.getTask(i);
        }

        ArrayList<Task> history = manager.getHistory();
        assertEquals(10, history.size(), "История должна содержать максимум 10 элементов");

        // Проверяем, что это последние 10 (с 3 по 12)
        for (int i = 0; i < 10; i++) {
            assertEquals(i + 3, history.get(i).getId());
        }
    }

    @Test
    void testEpicCannotContainItselfAsSubtask() {
        Epic epic = new Epic("Epic", "Desc");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Sub", "Desc", epic.getId());
        manager.createSubtask(subtask);

        epic.addSubtask(new Subtask("FakeSub", "Desc", epic.getId()) {{
            setId(epic.getId());
        }});

        // Проверка, что нельзя добавить эпик в самого себя
        assertFalse(epic.getSubtaskIds().contains(epic.getId()));
    }

    @Test
    void testSubtaskCannotBeItsOwnEpic() {
        Subtask subtask = new Subtask("Subtask name", "Description", 0);
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
        Task testTask = new Task("Test", "Desc");
        taskManager.createTask(testTask);
        assertNotNull(taskManager.getTask(testTask.getId()));
    }

    @Test
    void testTaskManagerAddsDifferentTypesAndFindsById() {
        Task task = new Task("Task", "Desc");
        Epic epic = new Epic("Epic", "Desc");
        Subtask subtask = new Subtask("Sub", "Desc", 0);

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
        Task manualTask = new Task("Manual Task", "Desc");
        manualTask.setId(100);  // вручную заданный ID
        manager.createTask(manualTask);

        // Создаём задачу без ID (или с id=0), чтобы менеджер сгенерировал ID
        Task autoTask = new Task("Auto Task", "Desc");
        // id по умолчанию 0 или не задан
        manager.createTask(autoTask);

        int manualId = manualTask.getId();
        int autoId = autoTask.getId();

        assertNotEquals(manualId, autoId, "Сгенерированный ID не должен совпадать с вручную заданным");
    }

    @Test
    void testTaskImmutabilityWhenAddedToManager() {
        Task originalTask = new Task("Original Title", "Original Description");
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
    void testHistoryManagerPreservesTaskVersions() {
        HistoryManager historyManager = new InMemoryHistoryManager();

        String originalName = "Original Name";
        String originalDescription = "Original Description";

        Task task = new Task(originalName, originalDescription);
        task.setId(1);

        historyManager.add(task);

        task.setTitle("Updated Name");
        task.setDescription("Updated Description");

        historyManager.add(task);

        ArrayList<Task> history = historyManager.getHistory();

        assertFalse(history.isEmpty(), "История должна содержать задачи после добавления");
        assertEquals(2, history.size(), "В истории должно быть 2 версии задачи");

        Task firstVersion = history.get(0);

        assertEquals(originalName, firstVersion.getTitle(), "Имя первой версии должно быть оригинальным");
        assertEquals(originalDescription, firstVersion.getDescription(), "Описание первой версии должно быть оригинальным");

        Task secondVersion = history.get(1);

        assertEquals("Updated Name", secondVersion.getTitle(), "Имя второй версии должно быть обновлённым");
        assertEquals("Updated Description", secondVersion.getDescription(), "Описание второй версии должно быть обновлённым");
    }
}
