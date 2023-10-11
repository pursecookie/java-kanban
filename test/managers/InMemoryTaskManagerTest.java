package managers;

import org.junit.jupiter.api.BeforeEach;
import tracker.managers.impl.InMemoryTaskManager;


public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @BeforeEach
    void beforeEach() {
        taskManager = new InMemoryTaskManager();
    }
}