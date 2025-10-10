import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void shouldDeleteSubtasksWhenEpicRemoved() {
        Epic epic = new Epic(0, "Epic 1", "Desc");
        manager.createEpic(epic);

        Subtask sub1 = new Subtask(0, "Sub 1", "Desc", TaskStatus.NEW, epic.getId());
        Subtask sub2 = new Subtask(0, "Sub 2", "Desc", TaskStatus.NEW, epic.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        manager.deleteEpicById(epic.getId());

        assertTrue(manager.getSubtasks().isEmpty(), "После удаления эпика подзадачи должны удалиться");
        assertTrue(manager.getEpics().isEmpty(), "Эпик должен удалиться");
    }

    @Test
    void shouldNotKeepOldSubtaskIdsInEpic() {
        Epic epic = new Epic(0, "Epic", "Desc");
        manager.createEpic(epic);

        Subtask sub1 = new Subtask(0, "Sub 1", "Desc", TaskStatus.NEW, epic.getId());
        manager.createSubtask(sub1);
        manager.deleteSubtaskById(sub1.getId());

        assertTrue(manager.getEpicSubtasks(epic.getId()).isEmpty(),
                "После удаления подзадачи эпик не должен хранить её ID");
    }

    @Test
    void shouldUpdateEpicStatusWhenSubtasksChange() {
        Epic epic = new Epic(0, "Epic", "Desc");
        manager.createEpic(epic);

        Subtask sub1 = new Subtask(0, "Sub 1", "Desc", TaskStatus.NEW, epic.getId());
        Subtask sub2 = new Subtask(0, "Sub 2", "Desc", TaskStatus.DONE, epic.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        // Проверяем, что статус эпика пересчитан
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus());
    }
}

