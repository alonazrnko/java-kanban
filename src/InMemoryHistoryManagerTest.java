import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task(1, "Task 1", "Desc 1", TaskStatus.NEW);
        task2 = new Task(2, "Task 2", "Desc 2", TaskStatus.IN_PROGRESS);
        task3 = new Task(3, "Task 3", "Desc 3", TaskStatus.DONE);
    }

    @Test
    void shouldAddTasksToHistory() {
        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "История должна содержать 2 задачи");
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void shouldRemoveDuplicateWhenTaskViewedAgain() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1); // повторный просмотр task1

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "Повторный просмотр не должен дублироваться");
        assertEquals(task2, history.get(0));
        assertEquals(task1, history.get(1), "Последний просмотр должен быть в конце");
    }

    @Test
    void shouldRemoveTaskFromHistoryById() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertFalse(history.contains(task2), "Удалённая задача не должна остаться в истории");
    }

    @Test
    void shouldReturnEmptyListWhenHistoryIsEmpty() {
        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть пустой при старте");
    }

    @Test
    void shouldNotThrowWhenRemovingNonexistentTask() {
        assertDoesNotThrow(() -> historyManager.remove(999), "Удаление несуществующей задачи не должно вызывать ошибку");
    }
}
