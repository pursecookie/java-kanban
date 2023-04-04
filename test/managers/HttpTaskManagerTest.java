package managers;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tracker.managers.*;
import tracker.models.Epic;
import tracker.models.Status;
import tracker.models.Subtask;
import tracker.models.Task;
import tracker.servers.KVServer;
import tracker.servers.KVTaskClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerTest {
    private KVServer kvServer;
    private KVTaskClient kvClient;
    public static final String URL = "http://localhost:";
    public static final int PORT = 8078;
    public static final String KEY = "testSaving";
    private TaskManager httpTaskManager;
    private Gson gson;
    Task savedTaskId1;
    Epic savedEpicId2;
    Subtask savedSubtaskId3;
    Subtask savedSubtaskId4;
    Task savedTaskId5;

    @BeforeEach
    void beforeEach() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
        httpTaskManager = Managers.getDefault(URL, PORT, KEY);
        kvClient = new KVTaskClient(URL, PORT);

        gson = Managers.getGson((HttpTaskManager) httpTaskManager);

        savedTaskId1 = new Task(1, "Сделать зарядку", Status.NEW, "описание задачи1",
                15, LocalDateTime.of(2023, Month.MARCH, 22, 8, 0));
        savedEpicId2 = new Epic(2, "Уборка", Status.NEW, "описание задачи2",
                0, null, new ArrayList<>());
        savedSubtaskId3 = new Subtask(3, "Вымыть пол", Status.NEW, "описание задачи3",
                20, LocalDateTime.of(2023, Month.MARCH, 22, 10, 0), 2);
        savedSubtaskId4 = new Subtask(4, "Вынести мусор", Status.NEW, "описание задачи4",
                10, LocalDateTime.of(2023, Month.MARCH, 22, 9, 0), 2);
        savedTaskId5 = new Task(5, "Позвонить маме", Status.NEW, "описание задачи5",
                40, null);
    }

    @AfterEach
    void tearDown() {
        kvServer.stop();
    }

    @DisplayName("Проверка работы по сохранению и восстановлению состояния")
    @Test
    public void shouldCorrectlySaveAndLoadCondition() {
        httpTaskManager.createTask(savedTaskId1);
        httpTaskManager.createEpic(savedEpicId2);
        httpTaskManager.createSubtask(savedSubtaskId3);
        httpTaskManager.createSubtask(savedSubtaskId4);

        httpTaskManager.getTaskById(1);
        httpTaskManager.getEpicById(2);

        String loadedSaving = kvClient.load(KEY);
        String expected = gson.toJson(httpTaskManager);

        assertEquals(expected, loadedSaving, "Менеджеры задач различаются");
    }

    @DisplayName("Проверка работы по сохранению и восстановлению состояния, если нет задач")
    @Test
    public void shouldCorrectlySaveAndLoadConditionIfTasksDoesNotExist() {
        httpTaskManager.createTask(savedTaskId1);
        httpTaskManager.deleteTaskById(1);

        String loadedSaving = kvClient.load(KEY);
        String expected = gson.toJson(httpTaskManager);

        assertEquals(expected, loadedSaving, "Менеджеры задач различаются");
    }

    @DisplayName("Проверка работы по сохранению и восстановлению состояния, если у эпика нет подзадач")
    @Test
    public void shouldCorrectlySaveAndLoadConditionIfEpicHasNotSubtasks() {
        httpTaskManager.createEpic(savedEpicId2);

        String loadedSaving = kvClient.load(KEY);
        String expected = gson.toJson(httpTaskManager);

        assertEquals(expected, loadedSaving, "Менеджеры задач различаются");
    }

    @DisplayName("Проверка работы по сохранению и восстановлению состояния, если нет истории просмотров")
    @Test
    public void shouldCorrectlySaveAndLoadConditionIfHistoryIsEmpty() {
        httpTaskManager.createTask(savedTaskId1);
        httpTaskManager.createEpic(savedEpicId2);
        httpTaskManager.createSubtask(savedSubtaskId3);

        String loadedSaving = kvClient.load(KEY);
        String expected = gson.toJson(httpTaskManager);

        assertEquals(expected, loadedSaving, "Менеджеры задач различаются");
    }
}
