package managers;

import org.junit.jupiter.api.BeforeEach;
import tracker.managers.InMemoryTaskManager;

import static tracker.managers.Managers.getDefault;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    void beforeEach() {
        taskManager = (InMemoryTaskManager) getDefault();
    }
}