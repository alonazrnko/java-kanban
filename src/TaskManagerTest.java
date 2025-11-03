import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.time.*;
import java.util.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    @Test
    void shouldCreateAndRetrieveTaskById() {
        Task task = new Task(0, "Task", "Desc", TaskStatus.NEW);
        manager.createTask(task);

        Task saved = manager.getTask(task.getId()).orElseThrow();
        assertEquals(task.getTitle(), saved.getTitle());
    }

    @Test
    void shouldUpdateTask() {
        Task task = new Task(0, "Old", "Desc", TaskStatus.NEW);
        manager.createTask(task);

        task.setTitle("Updated");
        manager.updateTask(task);

        assertEquals("Updated", manager.getTask(task.getId()).orElseThrow().getTitle());
    }

    @Test
    void shouldDeleteTaskById() {
        Task task = new Task(0, "Delete", "Desc", TaskStatus.NEW);
        manager.createTask(task);
        manager.deleteTaskById(task.getId());

        assertTrue(manager.getTask(task.getId()).isEmpty());
    }

    @Test
    void shouldHandleEmptyHistory() {
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void shouldPreventEpicFromContainingItself() {
        Epic epic = new Epic(0, "Epic", "Desc");
        manager.createEpic(epic);

        Subtask sub = new Subtask(0, "Sub", "Desc", TaskStatus.NEW, epic.getId());
        sub.setId(epic.getId());

        assertThrows(IllegalArgumentException.class, () -> manager.createSubtask(sub));
    }

    // Тесты для расчёта статуса Epic

    @Test
    void shouldSetEpicStatusNewWhenAllSubtasksNew() {
        Epic epic = new Epic(0, "Epic", "Desc");
        manager.createEpic(epic);
        manager.createSubtask(new Subtask(0, "S1", "Desc", TaskStatus.NEW, epic.getId()));
        manager.createSubtask(new Subtask(0, "S2", "Desc", TaskStatus.NEW, epic.getId()));

        assertEquals(TaskStatus.NEW, manager.getEpic(epic.getId()).orElseThrow().getStatus());
    }

    @Test
    void shouldSetEpicStatusDoneWhenAllSubtasksDone() {
        Epic epic = new Epic(0, "Epic", "Desc");
        manager.createEpic(epic);
        manager.createSubtask(new Subtask(0, "S1", "Desc", TaskStatus.DONE, epic.getId()));
        manager.createSubtask(new Subtask(0, "S2", "Desc", TaskStatus.DONE, epic.getId()));

        assertEquals(TaskStatus.DONE, manager.getEpic(epic.getId()).orElseThrow().getStatus());
    }

    @Test
    void shouldSetEpicStatusInProgressWhenMixedStatuses() {
        Epic epic = new Epic(0, "Epic", "Desc");
        manager.createEpic(epic);
        manager.createSubtask(new Subtask(0, "S1", "Desc", TaskStatus.NEW, epic.getId()));
        manager.createSubtask(new Subtask(0, "S2", "Desc", TaskStatus.DONE, epic.getId()));

        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epic.getId()).orElseThrow().getStatus());
    }

    @Test
    void shouldSetEpicStatusInProgressWhenSubtaskInProgress() {
        Epic epic = new Epic(0, "Epic", "Desc");
        manager.createEpic(epic);
        manager.createSubtask(new Subtask(0, "S1", "Desc", TaskStatus.IN_PROGRESS, epic.getId()));

        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epic.getId()).orElseThrow().getStatus());
    }

    // Проверка пересечения интервалов

    @Test
    void shouldPreventTaskOverlap() {
        Task t1 = new Task(0, "T1", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 10, 28, 10, 0));
        Task t2 = new Task(0, "T2", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 10, 28, 10, 15));

        manager.createTask(t1);

        assertThrows(IllegalArgumentException.class, () -> manager.createTask(t2),
                "Ожидалось исключение при пересечении задач по времени");
    }

    @Test
    void shouldSaveAndLoadTaskWithAllFields() {
        File file = new File("test_tasks.csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = new Task(0, "Test", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 10, 28, 14, 0));
        manager.createTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        Optional<Task> loadedOpt = loaded.getTask(task.getId());
        assertTrue(loadedOpt.isPresent(), "Задача не была загружена из файла");
        Task loadedTask = loadedOpt.get();

        assertEquals(task.getStartTime(), loadedTask.getStartTime());
        assertEquals(task.getDuration(), loadedTask.getDuration());
        assertEquals(task.getEndTime(), loadedTask.getEndTime());
    }
}
