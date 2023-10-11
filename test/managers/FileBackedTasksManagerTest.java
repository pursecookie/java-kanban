package managers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tracker.managers.impl.FileBackedTasksManager;
import tracker.models.Epic;
import tracker.models.Status;
import tracker.models.Subtask;
import tracker.models.Task;

import java.io.File;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {
    private File file;

    @BeforeEach
    void beforeEach() {
        file = new File("managerData.csv");
        taskManager = new FileBackedTasksManager();
    }

    @AfterEach
    void afterEach() {
        file.delete();
    }

    @DisplayName("Проверка работы по сохранению и восстановлению состояния")
    @Test
    public void shouldCorrectlySaveAndLoadCondition() {
        Task testTask = taskManager.createTask(savedTaskId1);
        Epic testEpic = taskManager.createEpic(savedEpicId2);
        Subtask testSubtask1 = taskManager.createSubtask(savedSubtaskId3);
        Subtask testSubtask2 = taskManager.createSubtask(savedSubtaskId4);

        taskManager.getTaskById(1);
        taskManager.getEpicById(2);

        FileBackedTasksManager taskManager2 = FileBackedTasksManager.loadFromFile(file);

        assertEquals(List.of(testTask), taskManager2.getTaskList(), "Задача восстановилась некорректно");
        assertEquals(List.of(testEpic), taskManager2.getEpicList(), "Эпик восстановился некорректно");
        assertEquals(List.of(testSubtask1, testSubtask2), taskManager2.getSubtaskList(),
                "Подзадачи восстановились некорректно");

        Task testTask2 = taskManager.createTask(new Task(counter.count(), "Заказать доставку", Status.NEW,
                "описание задачи5", 5,
                LocalDateTime.of(2023, Month.MARCH, 22, 18, 0)));

        assertEquals(5, testTask2.getId(), "Не совпадает новое значение счетчика для ID");

        assertEquals(List.of(testTask, testEpic), taskManager2.getHistory(), "Не совпадает история просмотров");
    }

    @DisplayName("Проверка работы по сохранению и восстановлению состояния, если нет задач")
    @Test
    public void shouldCorrectlySaveAndLoadConditionIfTasksDoesNotExist() {
        taskManager.createTask(savedTaskId1);
        taskManager.deleteTaskById(1);

        FileBackedTasksManager taskManager2 = FileBackedTasksManager.loadFromFile(file);

        assertEquals(0, taskManager2.getTaskList().size(), "Задачи восстановились");
    }

    @DisplayName("Проверка работы по сохранению и восстановлению состояния, если у эпика нет подзадач")
    @Test
    public void shouldCorrectlySaveAndLoadConditionIfEpicHasNotSubtasks() {
        Epic testEpic = taskManager.createEpic(savedEpicId2);

        FileBackedTasksManager taskManager2 = FileBackedTasksManager.loadFromFile(file);

        assertEquals(List.of(testEpic), taskManager2.getEpicList(), "Некорректное восстановление эпика");
    }

    @DisplayName("Проверка работы по сохранению и восстановлению состояния, если нет истории просмотров")
    @Test
    public void shouldCorrectlySaveAndLoadConditionIfHistoryIsEmpty() {
        taskManager.createTask(savedTaskId1);
        taskManager.createEpic(savedEpicId2);
        taskManager.createSubtask(savedSubtaskId3);

        FileBackedTasksManager taskManager2 = FileBackedTasksManager.loadFromFile(file);

        assertEquals(0, taskManager2.getHistory().size(), "История просмотра не пустая");
    }

    @DisplayName("Проверка работы по сохранению и восстановлению списка задач по приоритету")
    @Test
    public void shouldCorrectlySaveAndLoadPrioritizedTasks() {
        taskManager.createEpic(savedEpicId2);
        Subtask testSubtask1 = taskManager.createSubtask(savedSubtaskId3);
        Subtask testSubtask2 = taskManager.createSubtask(savedSubtaskId4);
        Task testTask1 = taskManager.createTask(new Task(counter.count(), "Заказать доставку", Status.NEW,
                "описание задачи5", 15, null));
        Task testTask2 = taskManager.createTask(savedTaskId1);

        FileBackedTasksManager taskManager2 = FileBackedTasksManager.loadFromFile(file);

        List<Task> sortedList = new ArrayList<>(taskManager2.getPrioritizedTasks());

        assertEquals(List.of(testTask2, testSubtask2, testSubtask1, testTask1), sortedList,
                "Список не восстановился");
    }

}