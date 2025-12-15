import org.junit.jupiter.api.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @Override
    protected FileBackedTaskManager createManager() {
        return new FileBackedTaskManager(new File("test_tasks.csv"));
    }

    @Test
    void shouldThrowOnInvalidFilePath() {
        File invalidFile = new File("невалидный_путь/файл.txt");

        ManagerSaveException exception = assertThrows(
                ManagerSaveException.class,
                () -> FileBackedTaskManager.loadFromFile(invalidFile)
        );

        assertTrue(exception.getMessage().contains("Файл недоступен"));
    }

    @Test
    void shouldNotThrowOnValidFile() {
        assertDoesNotThrow(() -> {
            File file = File.createTempFile("tasks", ".csv");
            new FileBackedTaskManager(file);
        });
    }
}
