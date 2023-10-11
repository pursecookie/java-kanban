package servers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tracker.managers.Managers;
import tracker.managers.TaskManager;
import tracker.managers.impl.HttpTaskManager;
import tracker.models.Epic;
import tracker.models.Status;
import tracker.models.Subtask;
import tracker.models.Task;
import tracker.servers.HttpTaskServer;
import tracker.servers.KVServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskServerTest {
    private HttpTaskServer httpTaskServer;
    private KVServer kvServer;
    private HttpClient client;
    public static final String URL = "http://localhost:";
    public static final int PORT = 8078;
    public static final String KEY = "testSaving";
    private TaskManager httpTaskManager;
    private Gson gson;
    private Task savedTaskId1;
    private Epic savedEpicId2;
    private Subtask savedSubtaskId3;
    private Subtask savedSubtaskId4;
    private Task savedTaskId5;
    private Epic savedEpicId6;

    @BeforeEach
    void beforeEach() throws IOException {
        kvServer = new KVServer();
        kvServer.start();

        httpTaskManager = Managers.getDefault(URL, PORT, KEY);
        httpTaskServer = new HttpTaskServer((HttpTaskManager) httpTaskManager);
        httpTaskServer.start();

        client = HttpClient.newHttpClient();

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
                40, LocalDateTime.of(2023, Month.MARCH, 22, 18, 0));
        savedEpicId6 = new Epic(6, "Переезд", Status.NEW, "описание задачи6",
                0, null, new ArrayList<>());
    }

    @AfterEach
    void tearDown() {
        httpTaskServer.stop();
        kvServer.stop();
    }

    @DisplayName("Проверка обработки запросов с неверным ID")
    @Test
    void shouldNotDoAnythingIfIdDoesNotExists() {
        URI url = URI.create("http://localhost:8080/tasks/task?id=3");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = getResponse(request);

        assertEquals("Получен неверный ID - 3", response.body());
    }

    @DisplayName("Проверка обработки запросов с несуществующим эндпоинтом")
    @Test
    void shouldNotDoAnythingIfEndpointDoesNotExists() {
        URI url = URI.create("http://localhost:8080/tasks/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = getResponse(request);

        assertEquals("Такого эндпоинта не существует", response.body());
    }

    @DisplayName("Проверка обработки GET-запроса для получения задачи по ID")
    @Test
    void shouldReturnTaskById() {
        httpTaskManager.createTask(savedTaskId1);

        URI url = URI.create("http://localhost:8080/tasks/task?id=1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = getResponse(request);

        assertEquals(HTTP_OK, response.statusCode());

        Task testTask = gson.fromJson(response.body(), Task.class);

        assertNotNull(testTask, "Ошибка в десериализации задачи");
        assertEquals(savedTaskId1, testTask, "Задачи не совпадают");
    }

    @DisplayName("Проверка обработки GET-запроса для получения эпика по ID")
    @Test
    void shouldReturnEpicById() {
        httpTaskManager.createEpic(savedEpicId2);

        URI url = URI.create("http://localhost:8080/tasks/epic?id=2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = getResponse(request);

        assertEquals(HTTP_OK, response.statusCode());

        Epic testEpic = gson.fromJson(response.body(), Epic.class);

        assertNotNull(testEpic, "Ошибка в десериализации эпика");
        assertEquals(savedEpicId2, testEpic, "Эпики не совпадают");
    }

    @DisplayName("Проверка обработки GET-запроса для получения подзадачи по ID")
    @Test
    void shouldReturnSubtaskById() {
        httpTaskManager.createEpic(savedEpicId2);
        httpTaskManager.createSubtask(savedSubtaskId3);

        URI url = URI.create("http://localhost:8080/tasks/subtask?id=3");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = getResponse(request);

        assertEquals(HTTP_OK, response.statusCode());

        Subtask testSubtask = gson.fromJson(response.body(), Subtask.class);

        assertNotNull(testSubtask, "Ошибка в десериализации подзадачи");
        assertEquals(savedSubtaskId3, testSubtask, "Подзадачи не совпадают");
    }

    @DisplayName("Проверка обработки GET-запроса для получения всех задач")
    @Test
    void shouldReturnAllTasks() {
        httpTaskManager.createTask(savedTaskId1);
        httpTaskManager.createTask(savedTaskId5);

        URI url = URI.create("http://localhost:8080/tasks/task");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = getResponse(request);

        assertEquals(HTTP_OK, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        List<Task> testTaskList = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            testTaskList.add(gson.fromJson(jsonArray.get(i), Task.class));
        }

        assertNotNull(testTaskList, "Ошибка в десериализации задач");
        assertEquals(List.of(savedTaskId1, savedTaskId5), testTaskList, "Задачи не совпадают");
    }

    @DisplayName("Проверка обработки GET-запроса для получения всех эпиков")
    @Test
    void shouldReturnAllEpics() {
        httpTaskManager.createEpic(savedEpicId2);
        httpTaskManager.createEpic(savedEpicId6);

        URI url = URI.create("http://localhost:8080/tasks/epic");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = getResponse(request);

        assertEquals(HTTP_OK, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        List<Epic> testEpicList = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            testEpicList.add(gson.fromJson(jsonArray.get(i), Epic.class));
        }

        assertNotNull(testEpicList, "Ошибка в десериализации эпиков");
        assertEquals(List.of(savedEpicId2, savedEpicId6), testEpicList, "Эпики не совпадают");
    }

    @DisplayName("Проверка обработки GET-запроса для получения всех подзадач")
    @Test
    void shouldReturnAllSubtasks() {
        httpTaskManager.createEpic(savedEpicId2);
        httpTaskManager.createSubtask(savedSubtaskId3);
        httpTaskManager.createSubtask(savedSubtaskId4);

        URI url = URI.create("http://localhost:8080/tasks/subtask");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = getResponse(request);

        assertEquals(HTTP_OK, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        List<Subtask> testSubtaskList = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            testSubtaskList.add(gson.fromJson(jsonArray.get(i), Subtask.class));
        }

        assertNotNull(testSubtaskList, "Ошибка в десериализации подзадач");
        assertEquals(List.of(savedSubtaskId3, savedSubtaskId4), testSubtaskList, "Подзадачи не совпадают");
    }

    @DisplayName("Проверка обработки GET-запроса для получения всех подзадач конкретного эпика")
    @Test
    void shouldReturnAllSubtasksByEpicId() {
        httpTaskManager.createEpic(savedEpicId2);
        httpTaskManager.createSubtask(savedSubtaskId3);
        httpTaskManager.createSubtask(savedSubtaskId4);

        URI url = URI.create("http://localhost:8080/tasks/subtask/epic?id=2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = getResponse(request);

        assertEquals(HTTP_OK, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        List<Subtask> testSubtaskList = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            testSubtaskList.add(gson.fromJson(jsonArray.get(i), Subtask.class));
        }

        assertNotNull(testSubtaskList, "Ошибка в десериализации подзадач");
        assertEquals(List.of(savedSubtaskId3, savedSubtaskId4), testSubtaskList, "Подзадачи не совпадают");
    }

    @DisplayName("Проверка обработки GET-запроса для получения истории просмотра")
    @Test
    void shouldReturnHistory() {
        httpTaskManager.createTask(savedTaskId5);
        httpTaskManager.createEpic(savedEpicId2);
        httpTaskManager.createSubtask(savedSubtaskId3);
        httpTaskManager.createTask(savedTaskId1);

        httpTaskManager.getTaskById(1);
        httpTaskManager.getSubtaskById(3);

        URI url = URI.create("http://localhost:8080/tasks/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = getResponse(request);

        assertEquals(HTTP_OK, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();

        assertEquals(2, jsonArray.size(), "Неверный размер истории просмотра");
    }

    @DisplayName("Проверка обработки GET-запроса для получения списка задач по приоритету")
    @Test
    void shouldReturnPrioritizedTaskList() {
        httpTaskManager.createTask(savedTaskId5);
        httpTaskManager.createEpic(savedEpicId2);
        httpTaskManager.createSubtask(savedSubtaskId3);
        httpTaskManager.createTask(savedTaskId1);

        URI url = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = getResponse(request);

        assertEquals(HTTP_OK, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();

        assertEquals(3, jsonArray.size(), "Неверный размер списка задач");
    }

    @DisplayName("Проверка обработки POST-запроса для создания задачи")
    @Test
    void shouldCreateTask() {
        URI url = URI.create("http://localhost:8080/tasks/task");
        String json = gson.toJson(savedTaskId1);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = getResponse(request);

        assertEquals(HTTP_OK, response.statusCode());
        assertEquals("Задача успешно создана!", response.body(), "Задача не создалась");
    }

    @DisplayName("Проверка обработки POST-запроса для создания эпика")
    @Test
    void shouldCreateEpic() {
        URI url = URI.create("http://localhost:8080/tasks/epic");
        String json = gson.toJson(savedEpicId2);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = getResponse(request);

        assertEquals(HTTP_OK, response.statusCode());
        assertEquals("Эпик успешно создан!", response.body(), "Эпик не создался");
    }

    @DisplayName("Проверка обработки POST-запроса для создания подзадачи")
    @Test
    void shouldCreateSubtask() {
        httpTaskManager.createEpic(savedEpicId2);

        URI url = URI.create("http://localhost:8080/tasks/subtask");
        String json = gson.toJson(savedSubtaskId3);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = getResponse(request);

        assertEquals(HTTP_OK, response.statusCode());
        assertEquals("Подзадача успешно создана!", response.body(), "Подзадача не создалась");
    }

    @DisplayName("Проверка обработки POST-запроса для обновления задачи")
    @Test
    void shouldUpdateTaskStatus() {
        httpTaskManager.createTask(savedTaskId1);
        savedTaskId1.setStatus(Status.IN_PROGRESS);

        URI url = URI.create("http://localhost:8080/tasks/task");
        String json = gson.toJson(savedTaskId1);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = getResponse(request);

        assertEquals("Задача успешно обновлена!", response.body(), "Статус задачи не обновился");

    }

    @DisplayName("Проверка обработки POST-запроса для обновления эпика")
    @Test
    void shouldUpdateEpicDescription() {
        httpTaskManager.createEpic(savedEpicId2);
        savedEpicId2.setDescription("");

        URI url = URI.create("http://localhost:8080/tasks/epic");
        String json = gson.toJson(savedEpicId2);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = getResponse(request);

        assertEquals("Эпик успешно обновлен!", response.body(), "Описание эпика не обновилось");
    }

    @DisplayName("Проверка обработки POST-запроса для обновления подзадачи")
    @Test
    void shouldUpdateSubtaskStatus() {
        httpTaskManager.createEpic(savedEpicId2);
        httpTaskManager.createSubtask(savedSubtaskId3);
        savedSubtaskId3.setStatus(Status.IN_PROGRESS);

        URI url = URI.create("http://localhost:8080/tasks/subtask");
        String json = gson.toJson(savedSubtaskId3);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = getResponse(request);

        assertEquals("Подзадача успешно обновлена!", response.body(), "Описание эпика не обновилось");
    }

    @DisplayName("Проверка обработки DELETE-запроса для удаления задачи по ID")
    @Test
    void shouldDeleteTaskById() {
        httpTaskManager.createTask(savedTaskId1);

        URI url = URI.create("http://localhost:8080/tasks/task?id=1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = getResponse(request);

        assertEquals("Задача с ID1 успешно удалена!", response.body());
        assertEquals(0, httpTaskManager.getTaskList().size(), "Задача не удалилась");
        assertEquals(0, httpTaskManager.getPrioritizedTasks().size(),
                "Задача не удалилась из списка задач по приоритету");
    }

    @DisplayName("Проверка обработки DELETE-запроса для удаления эпика по ID")
    @Test
    void shouldDeleteEpicById() {
        httpTaskManager.createEpic(savedEpicId2);

        URI url = URI.create("http://localhost:8080/tasks/epic?id=2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = getResponse(request);

        assertEquals("Эпик с ID2 успешно удален!", response.body());
        assertEquals(0, httpTaskManager.getEpicList().size(), "Эпик не удалился");
        assertEquals(0, httpTaskManager.getSubtaskList().size(), "Подзадачи данного эпика не удалились");
    }

    @DisplayName("Проверка обработки DELETE-запроса для удаления подзадачи по ID")
    @Test
    void shouldDeleteSubtaskById() {
        Epic testEpic = httpTaskManager.createEpic(savedEpicId2);
        httpTaskManager.createSubtask(savedSubtaskId3);

        URI url = URI.create("http://localhost:8080/tasks/subtask?id=3");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = getResponse(request);

        assertEquals("Подзадача с ID3 успешно удалена!", response.body());
        assertEquals(0, httpTaskManager.getSubtaskList().size(), "Подзадача не удалилась");
        assertEquals(0, httpTaskManager.getPrioritizedTasks().size(),
                "Задача не удалилась из списка задач по приоритету");
        assertEquals(0, testEpic.getSubtasksIds().size(), "У эпика не удалилась данная подзадача");
    }

    @DisplayName("Проверка обработки DELETE-запроса для удаления всех задач")
    @Test
    void shouldDeleteAllTasks() {
        httpTaskManager.createTask(savedTaskId1);
        httpTaskManager.createTask(savedTaskId5);

        URI url = URI.create("http://localhost:8080/tasks/task");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = getResponse(request);

        assertEquals("Все задачи успешно удалены!", response.body());
        assertEquals(0, httpTaskManager.getTaskList().size(), "Удалились не все задачи");
        assertEquals(0, httpTaskManager.getPrioritizedTasks().size(),
                "Задачи не удалились из списка задач по приоритету");
    }

    @DisplayName("Проверка обработки DELETE-запроса для удаления всех эпиков")
    @Test
    void shouldDeleteAllEpics() {
        httpTaskManager.createEpic(savedEpicId2);
        httpTaskManager.createEpic(savedEpicId6);

        URI url = URI.create("http://localhost:8080/tasks/epic");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = getResponse(request);

        assertEquals("Все эпики успешно удалены!", response.body());
        assertEquals(0, httpTaskManager.getEpicList().size(), "Удалились не все эпики");
        assertEquals(0, httpTaskManager.getSubtaskList().size(), "Подзадачи всех эпиков не удалились");
    }

    @DisplayName("Проверка обработки DELETE-запроса для удаления всех подзадач")
    @Test
    void shouldDeleteAllSubtasks() {
        Epic testEpic = httpTaskManager.createEpic(savedEpicId2);
        httpTaskManager.createSubtask(savedSubtaskId3);
        httpTaskManager.createSubtask(savedSubtaskId4);

        URI url = URI.create("http://localhost:8080/tasks/subtask");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = getResponse(request);

        assertEquals("Все подзадачи успешно удалены!", response.body());
        assertEquals(0, httpTaskManager.getSubtaskList().size(), "Удалились не все подзадачи");
        assertEquals(0, httpTaskManager.getPrioritizedTasks().size(),
                "Подзадачи не удалились из списка задач по приоритету");
        assertEquals(0, testEpic.getSubtasksIds().size(), "У эпика не очистился список его подзадач");
    }

    private HttpResponse<String> getResponse(HttpRequest request) {
        HttpResponse<String> response;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Во время выполнения запроса возникла ошибка. " +
                    "Проверьте, пожалуйста, URL-адрес и повторите попытку.");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Введённый вами адрес не соответствует формату URL. " +
                    "Попробуйте, пожалуйста, снова.");
        }
        return response;
    }
}