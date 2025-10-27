import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private File file;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        file = File.createTempFile("tasks_test", ".csv");
        manager = new FileBackedTaskManager(file);
    }

    @AfterEach
    void tearDown() {
        file.delete(); // удаляем временный файл после теста
    }

    @Test
    void saveAndLoadEmptyManager() {
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loaded.getTasks().isEmpty(), "Задачи должны быть пустыми");
        assertTrue(loaded.getEpics().isEmpty(), "Эпики должны быть пустыми");
        assertTrue(loaded.getSubtasks().isEmpty(), "Подзадачи должны быть пустыми");
        assertTrue(loaded.getHistory().isEmpty(), "История должна быть пустой");
    }

    @Test
    void saveAndLoadWithTasks() {
        Task task1 = new Task(0, "Task1", "Desc1", TaskStatus.NEW);
        Task task2 = new Task(0, "Task2", "Desc2", TaskStatus.IN_PROGRESS);
        manager.createTask(task1);
        manager.createTask(task2);

        Epic epic = new Epic(0, "Epic1", "EpicDesc");
        manager.createEpic(epic);

        Subtask sub1 = new Subtask(0, "Sub1", "SubDesc1", TaskStatus.NEW, epic.getId());
        Subtask sub2 = new Subtask(0, "Sub2", "SubDesc2", TaskStatus.DONE, epic.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        // Просмотр задач для истории
        manager.getTask(task1.getId());
        manager.getEpic(epic.getId());
        manager.getSubtask(sub2.getId());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        // Проверка задач
        assertEquals(2, loaded.getTasks().size(), "Количество задач должно совпадать");
        assertEquals(1, loaded.getEpics().size(), "Количество эпиков должно совпадать");
        assertEquals(2, loaded.getSubtasks().size(), "Количество подзадач должно совпадать");

        // Проверка истории
        ArrayList<Task> history = loaded.getHistory();
        assertEquals(3, history.size(), "История просмотров должна содержать 3 элемента");
        assertEquals(task1.getTitle(), history.get(0).getTitle());
        assertEquals(epic.getTitle(), history.get(1).getTitle());
        assertEquals(sub2.getTitle(), history.get(2).getTitle());
    }

    @Test
    void managerStatePersistsAfterReload() {
        Task task = new Task(0, "PersistentTask", "Desc", TaskStatus.NEW);
        manager.createTask(task);
        Epic epic = new Epic(0, "PersistentEpic", "EpicDesc");
        manager.createEpic(epic);

        Subtask sub = new Subtask(0, "PersistentSub", "SubDesc", TaskStatus.NEW, epic.getId());
        manager.createSubtask(sub);

        // Загружаем менеджер из файла
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        // Проверка соответствия
        assertEquals(manager.getTasks().size(), loaded.getTasks().size());
        assertEquals(manager.getEpics().size(), loaded.getEpics().size());
        assertEquals(manager.getSubtasks().size(), loaded.getSubtasks().size());

        // Проверка связей эпик-подзадача
        ArrayList<Subtask> epicSubs = loaded.getEpicSubtasks(epic.getId());
        assertEquals(1, epicSubs.size(), "Эпик должен содержать одну подзадачу");
        assertEquals(sub.getTitle(), epicSubs.get(0).getTitle());
    }
}
