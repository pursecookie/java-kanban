package tracker.servers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import tracker.adapters.DurationAdapter;
import tracker.adapters.HistoryManagerAdapter;
import tracker.adapters.LocalDateTimeAdapter;
import tracker.managers.HistoryManager;
import tracker.managers.HttpTaskManager;
import tracker.models.Endpoint;
import tracker.models.Epic;
import tracker.models.Subtask;
import tracker.models.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpTaskServer {
    public static final int PORT = 8080;
    private final HttpServer httpServer;
    private final Gson gson;
    private final HttpTaskManager httpTaskManager;

    public HttpTaskServer(HttpTaskManager httpTaskManager) throws IOException {
        this.httpTaskManager = httpTaskManager;
        gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(HistoryManager.class, new HistoryManagerAdapter(httpTaskManager))
                .create();
        httpServer = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        httpServer.createContext("/tasks/", this::handleTasks);
    }

    private void handleTasks(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        String idParam = httpExchange.getRequestURI().getQuery();
        String requestMethod = httpExchange.getRequestMethod();
        Endpoint endpoint = getEndpoint(path, idParam, requestMethod);

        if (idParam == null) {
            switch (endpoint) {
                case GET_ALL_TASKS:
                    writeResponse(httpExchange, gson.toJson(httpTaskManager.getTaskList()), 200);
                    break;
                case GET_HISTORY:
                    writeResponse(httpExchange, gson.toJson(httpTaskManager.getHistory()), 200);
                    break;
                case GET_PRIORITIZED:
                    writeResponse(httpExchange, gson.toJson(httpTaskManager.getPrioritizedTasks()), 200);
                    break;
                case GET_ALL_SUBTASKS:
                    writeResponse(httpExchange, gson.toJson(httpTaskManager.getSubtaskList()), 200);
                    break;
                case GET_ALL_EPICS:
                    writeResponse(httpExchange, gson.toJson(httpTaskManager.getEpicList()), 200);
                    break;
                case POST_TASK:
                    String taskBody = new String(httpExchange.getRequestBody().readAllBytes(), UTF_8);
                    Task outputTask = gson.fromJson(taskBody, Task.class);

                    if (outputTask == null) {
                        break;
                    }

                    if (httpTaskManager.getTaskList().size() != 0) {
                        for (Task task : httpTaskManager.getTaskList()) {
                            if (task.getId() == outputTask.getId()) {
                                httpTaskManager.updateTask(outputTask);
                                writeResponse(httpExchange, "Задача успешно обновлена!", 200);
                            }
                        }
                    } else {
                        httpTaskManager.createTask(outputTask);
                        writeResponse(httpExchange, "Задача успешно создана!", 200);
                    }
                    break;
                case POST_EPIC:
                    String epicBody = new String(httpExchange.getRequestBody().readAllBytes(), UTF_8);
                    Epic outputEpic = gson.fromJson(epicBody, Epic.class);

                    if (outputEpic == null) {
                        break;
                    }

                    if (httpTaskManager.getEpicList().size() != 0) {
                        for (Epic epic : httpTaskManager.getEpicList()) {
                            if (epic.getId() == outputEpic.getId()) {
                                httpTaskManager.updateEpicInfo(outputEpic);
                                writeResponse(httpExchange, "Эпик успешно обновлен!", 200);
                            }
                        }
                    } else {
                        httpTaskManager.createEpic(outputEpic);
                        writeResponse(httpExchange, "Эпик успешно создан!", 200);
                    }
                    break;
                case POST_SUBTASK:
                    String subtaskBody = new String(httpExchange.getRequestBody().readAllBytes(), UTF_8);
                    Subtask outputSubtask = gson.fromJson(subtaskBody, Subtask.class);

                    if (outputSubtask == null) {
                        break;
                    }

                    if (httpTaskManager.getSubtaskList().size() != 0) {
                        for (Subtask subtask : httpTaskManager.getSubtaskList()) {
                            if (subtask.getId() == outputSubtask.getId()) {
                                httpTaskManager.updateSubtask(outputSubtask);
                                writeResponse(httpExchange, "Подзадача успешно обновлена!", 200);
                            }
                        }
                    } else {
                        httpTaskManager.createSubtask(outputSubtask);
                        writeResponse(httpExchange, "Подзадача успешно создана!", 200);
                    }
                    break;
                case DELETE_ALL_TASKS:
                    httpTaskManager.deleteAllTasks();
                    writeResponse(httpExchange, "Все задачи успешно удалены!", 200);
                    break;
                case DELETE_ALL_EPICS:
                    httpTaskManager.deleteAllEpics();
                    writeResponse(httpExchange, "Все эпики успешно удалены!", 200);
                    break;
                case DELETE_ALL_SUBTASKS:
                    httpTaskManager.deleteAllSubtasks();
                    writeResponse(httpExchange, "Все подзадачи успешно удалены!", 200);
                    break;
                default:
                    writeResponse(httpExchange, "Такого эндпоинта не существует", 404);
            }
        } else {
            String[] ids = idParam.split("=");
            int id = parsePathId(ids[1]);

            if (id != -1) {
                if (httpTaskManager.taskList.containsKey(id) ||
                        httpTaskManager.epicList.containsKey(id) ||
                        httpTaskManager.subtaskList.containsKey(id)) {
                    switch (endpoint) {
                        case GET_ONE_TASK:
                            writeResponse(httpExchange, gson.toJson(httpTaskManager.getTaskById(id)),
                                    200);
                            break;
                        case GET_EPIC_SUBTASKS:
                            writeResponse(httpExchange, gson.toJson(httpTaskManager.getSubtaskListByEpic(id)),
                                    200);
                            break;
                        case GET_ONE_SUBTASK:
                            writeResponse(httpExchange, gson.toJson(httpTaskManager.getSubtaskById(id)),
                                    200);
                            break;
                        case GET_ONE_EPIC:
                            writeResponse(httpExchange, gson.toJson(httpTaskManager.getEpicById(id)),
                                    200);
                            break;
                        case DELETE_ONE_TASK:
                            httpTaskManager.deleteTaskById(id);
                            writeResponse(httpExchange, "Задача с ID" + id + " успешно удалена!",
                                    200);
                            break;
                        case DELETE_ONE_EPIC:
                            httpTaskManager.deleteEpicById(id);
                            writeResponse(httpExchange, "Эпик с ID" + id + " успешно удален!",
                                    200);
                            break;
                        case DELETE_ONE_SUBTASK:
                            httpTaskManager.deleteSubtaskById(id);
                            writeResponse(httpExchange, "Подзадача с ID" + id + " успешно удалена!",
                                    200);
                            break;
                        default:
                            writeResponse(httpExchange, "Такого эндпоинта не существует", 404);
                    }
                } else {
                    writeResponse(httpExchange, "Получен неверный ID - " + ids[1], 405);
                }
            } else {
                writeResponse(httpExchange, "Получен неверный ID - " + ids[1], 405);
            }
        }
    }

    private Endpoint getEndpoint(String requestPath, String idParam, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2) {
            return Endpoint.GET_PRIORITIZED;
        } else if (pathParts.length == 3) {
            switch (requestMethod) {
                case "GET":
                    if (idParam == null) {
                        switch (pathParts[2]) {
                            case "task":
                                return Endpoint.GET_ALL_TASKS;
                            case "subtask":
                                return Endpoint.GET_ALL_SUBTASKS;
                            case "epic":
                                return Endpoint.GET_ALL_EPICS;
                            case "history":
                                return Endpoint.GET_HISTORY;
                        }
                    } else {
                        switch (pathParts[2]) {
                            case "task":
                                return Endpoint.GET_ONE_TASK;
                            case "subtask":
                                return Endpoint.GET_ONE_SUBTASK;
                            case "epic":
                                return Endpoint.GET_ONE_EPIC;
                        }
                    }
                case "POST":
                    switch (pathParts[2]) {
                        case "task":
                            return Endpoint.POST_TASK;
                        case "subtask":
                            return Endpoint.POST_SUBTASK;
                        case "epic":
                            return Endpoint.POST_EPIC;
                    }
                case "DELETE":
                    if (idParam == null) {
                        switch (pathParts[2]) {
                            case "task":
                                return Endpoint.DELETE_ALL_TASKS;
                            case "subtask":
                                return Endpoint.DELETE_ALL_SUBTASKS;
                            case "epic":
                                return Endpoint.DELETE_ALL_EPICS;
                        }
                    } else {
                        switch (pathParts[2]) {
                            case "task":
                                return Endpoint.DELETE_ONE_TASK;
                            case "subtask":
                                return Endpoint.DELETE_ONE_SUBTASK;
                            case "epic":
                                return Endpoint.DELETE_ONE_EPIC;
                        }
                    }
            }
        } else if (pathParts.length == 4) {
            return Endpoint.GET_EPIC_SUBTASKS;
        }
        return Endpoint.UNKNOWN;
    }

    private int parsePathId(String path) {
        try {
            return Integer.parseInt(path);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private void writeResponse(HttpExchange exchange, String responseString, int responseCode) throws IOException {
        if (responseString.isBlank()) {
            exchange.sendResponseHeaders(responseCode, 0);
        } else {
            byte[] bytes = responseString.getBytes(UTF_8);
            exchange.sendResponseHeaders(responseCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
        exchange.close();
    }

    public void start() {
        System.out.println("Запускаем сервер на порту " + PORT);
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("Остановили сервер на порту " + PORT);
    }

}