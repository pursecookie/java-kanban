package tracker.servers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import tracker.managers.HttpTaskManager;
import tracker.managers.Managers;
import tracker.models.Epic;
import tracker.models.Subtask;
import tracker.models.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import static java.net.HttpURLConnection.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpTaskServer {
    public static final int PORT = 8080;
    private final HttpServer httpServer;
    private final Gson gson;
    private final HttpTaskManager httpTaskManager;

    public HttpTaskServer(HttpTaskManager httpTaskManager) throws IOException {
        this.httpTaskManager = httpTaskManager;
        gson = Managers.getGson(httpTaskManager);
        httpServer = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        httpServer.createContext("/tasks/", this::handle);
    }

    private void handle(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        String idParam = httpExchange.getRequestURI().getQuery();
        String requestMethod = httpExchange.getRequestMethod();

        String[] pathParts = path.split("/");

        if (pathParts.length == 2) {
            writeResponse(httpExchange, gson.toJson(httpTaskManager.getPrioritizedTasks()), HTTP_OK);
        }

        switch (pathParts[2]) {
            case "task":
                handleTasks(httpExchange, idParam, requestMethod);
                break;
            case "epic":
                handleEpics(httpExchange, idParam, requestMethod);
                break;
            case "subtask":
                handleSubtasks(httpExchange, idParam, requestMethod, pathParts);
                break;
            case "history":
                handleHistory(httpExchange);
                break;
            default:
                writeResponse(httpExchange, "Такого эндпоинта не существует", HTTP_NOT_FOUND);
        }
    }

    private void handleTasks(HttpExchange httpExchange, String idParam, String requestMethod) throws IOException {
        if (idParam == null) {
            switch (requestMethod) {
                case "GET":
                    writeResponse(httpExchange, gson.toJson(httpTaskManager.getTaskList()), HTTP_OK);
                    break;
                case "POST":
                    String taskBody = new String(httpExchange.getRequestBody().readAllBytes(), UTF_8);
                    Task outputTask = gson.fromJson(taskBody, Task.class);

                    if (outputTask == null) {
                        break;
                    }

                    if (httpTaskManager.getTaskList().size() != 0) {
                        for (Task task : httpTaskManager.getTaskList()) {
                            if (task.getId() == outputTask.getId()) {
                                httpTaskManager.updateTask(outputTask);
                                writeResponse(httpExchange, "Задача успешно обновлена!", HTTP_OK);
                            }
                        }
                    } else {
                        httpTaskManager.createTask(outputTask);
                        writeResponse(httpExchange, "Задача успешно создана!", HTTP_OK);
                    }
                    break;
                case "DELETE":
                    httpTaskManager.deleteAllTasks();
                    writeResponse(httpExchange, "Все задачи успешно удалены!", HTTP_OK);
                    break;
            }
        } else {
            String[] ids = idParam.split("=");
            int id = parsePathId(ids[1]);

            if (id != -1) {
                if (httpTaskManager.taskList.containsKey(id)) {
                    switch (requestMethod) {
                        case "GET":
                            writeResponse(httpExchange, gson.toJson(httpTaskManager.getTaskById(id)), HTTP_OK);
                            break;
                        case "DELETE":
                            httpTaskManager.deleteTaskById(id);
                            writeResponse(httpExchange, "Задача с ID" + id + " успешно удалена!", HTTP_OK);
                            break;
                    }
                } else {
                    writeResponse(httpExchange, "Получен неверный ID - " + ids[1], HTTP_BAD_METHOD);
                }
            } else {
                writeResponse(httpExchange, "Получен неверный ID - " + ids[1], HTTP_BAD_METHOD);
            }
        }
    }

    private void handleEpics(HttpExchange httpExchange, String idParam, String requestMethod) throws IOException {
        if (idParam == null) {
            switch (requestMethod) {
                case "GET":
                    writeResponse(httpExchange, gson.toJson(httpTaskManager.getEpicList()), HTTP_OK);
                    break;
                case "POST":
                    String epicBody = new String(httpExchange.getRequestBody().readAllBytes(), UTF_8);
                    Epic outputEpic = gson.fromJson(epicBody, Epic.class);

                    if (outputEpic == null) {
                        break;
                    }

                    if (httpTaskManager.getEpicList().size() != 0) {
                        for (Epic epic : httpTaskManager.getEpicList()) {
                            if (epic.getId() == outputEpic.getId()) {
                                httpTaskManager.updateEpicInfo(outputEpic);
                                writeResponse(httpExchange, "Эпик успешно обновлен!", HTTP_OK);
                            }
                        }
                    } else {
                        httpTaskManager.createEpic(outputEpic);
                        writeResponse(httpExchange, "Эпик успешно создан!", HTTP_OK);
                    }
                    break;
                case "DELETE":
                    httpTaskManager.deleteAllEpics();
                    writeResponse(httpExchange, "Все эпики успешно удалены!", HTTP_OK);
                    break;
            }
        } else {
            String[] ids = idParam.split("=");
            int id = parsePathId(ids[1]);

            if (id != -1) {
                if (httpTaskManager.epicList.containsKey(id)) {
                    switch (requestMethod) {
                        case "GET":
                            writeResponse(httpExchange, gson.toJson(httpTaskManager.getEpicById(id)), HTTP_OK);
                            break;
                        case "DELETE":
                            httpTaskManager.deleteEpicById(id);
                            writeResponse(httpExchange, "Эпик с ID" + id + " успешно удален!", HTTP_OK);
                            break;
                    }
                } else {
                    writeResponse(httpExchange, "Получен неверный ID - " + ids[1], HTTP_BAD_METHOD);
                }
            } else {
                writeResponse(httpExchange, "Получен неверный ID - " + ids[1], HTTP_BAD_METHOD);
            }
        }
    }

    private void handleSubtasks(HttpExchange httpExchange, String idParam, String requestMethod,
                                String[] pathParts) throws IOException {
        if (idParam == null) {
            switch (requestMethod) {
                case "GET":
                    writeResponse(httpExchange, gson.toJson(httpTaskManager.getSubtaskList()), HTTP_OK);
                    break;
                case "POST":
                    String subtaskBody = new String(httpExchange.getRequestBody().readAllBytes(), UTF_8);
                    Subtask outputSubtask = gson.fromJson(subtaskBody, Subtask.class);

                    if (outputSubtask == null) {
                        break;
                    }

                    if (httpTaskManager.getSubtaskList().size() != 0) {
                        for (Subtask subtask : httpTaskManager.getSubtaskList()) {
                            if (subtask.getId() == outputSubtask.getId()) {
                                httpTaskManager.updateSubtask(outputSubtask);
                                writeResponse(httpExchange, "Подзадача успешно обновлена!", HTTP_OK);
                            }
                        }
                    } else {
                        httpTaskManager.createSubtask(outputSubtask);
                        writeResponse(httpExchange, "Подзадача успешно создана!", HTTP_OK);
                    }
                    break;
                case "DELETE":
                    httpTaskManager.deleteAllSubtasks();
                    writeResponse(httpExchange, "Все подзадачи успешно удалены!", HTTP_OK);
                    break;
            }
        } else {
            String[] ids = idParam.split("=");
            int id = parsePathId(ids[1]);

            if (id != -1) {
                if (pathParts.length == 4) {
                    if (httpTaskManager.epicList.containsKey(id)) {
                        writeResponse(httpExchange, gson.toJson(httpTaskManager.getSubtaskListByEpic(id)), HTTP_OK);
                    } else {
                        writeResponse(httpExchange, "Получен неверный ID эпика - " + id, HTTP_BAD_METHOD);
                    }
                }
                if (httpTaskManager.subtaskList.containsKey(id)) {
                    switch (requestMethod) {
                        case "GET":
                            writeResponse(httpExchange, gson.toJson(httpTaskManager.getSubtaskById(id)), HTTP_OK);
                            break;
                        case "DELETE":
                            httpTaskManager.deleteSubtaskById(id);
                            writeResponse(httpExchange, "Подзадача с ID" + id + " успешно удалена!", HTTP_OK);
                            break;
                    }
                } else {
                    writeResponse(httpExchange, "Получен неверный ID - " + ids[1], HTTP_BAD_METHOD);
                }
            } else {
                writeResponse(httpExchange, "Получен неверный ID - " + ids[1], HTTP_BAD_METHOD);
            }
        }
    }

    private void handleHistory(HttpExchange httpExchange) throws IOException {
        writeResponse(httpExchange, gson.toJson(httpTaskManager.getHistory()), HTTP_OK);
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