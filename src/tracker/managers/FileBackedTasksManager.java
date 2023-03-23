package tracker.managers;

import tracker.converters.CsvConverter;
import tracker.exceptions.*;
import tracker.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTaskManager {
    File data;
    static CsvConverter csvConverter = new CsvConverter();

    void save() {
        try {
            data = new File("managerData.csv");
            if (!data.exists()) {
                data.createNewFile();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось создать файл автосохранения");
        }

        try (PrintWriter printWriter = new PrintWriter(data, StandardCharsets.UTF_8)) {
            printWriter.println("id,type,name,status,description,duration,startTime,epic");

            for (Task task : taskList.values()) {
                printWriter.println(csvConverter.toString(task));
            }
            for (Subtask subtask : subtaskList.values()) {
                printWriter.println(csvConverter.toString(subtask));
            }
            for (Epic epic : epicList.values()) {
                printWriter.println(csvConverter.toString(epic));
            }

            printWriter.println();
            printWriter.print(CsvConverter.historyToString(historyManager));

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка записи данных в файл автосохранения");
        }
    }

    public static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager loadedManager = new FileBackedTasksManager();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            List<String> lines = new ArrayList<>();
            int maxId = 0;

            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            lines.remove(0);

            for (String row : lines) {
                if (row.isEmpty()) {
                    continue;
                }
                if (row.contains("SUBTASK")) {
                    Subtask subtask = (Subtask) CsvConverter.fromString(row, loadedManager);
                    if (subtask != null) {
                        loadedManager.subtaskList.put(subtask.getId(), subtask);
                        if (subtask.getId() > maxId) {
                            maxId = subtask.getId();
                        }
                    }
                } else if (row.contains("EPIC")) {
                    Epic epic = (Epic) CsvConverter.fromString(row, loadedManager);
                    if (epic != null) {
                        loadedManager.epicList.put(epic.getId(), epic);
                        if (epic.getId() > maxId) {
                            maxId = epic.getId();
                        }
                    }
                } else if (row.contains("TASK")) {
                    Task task = CsvConverter.fromString(row, loadedManager);
                    if (task != null) {
                        loadedManager.taskList.put(task.getId(), task);
                        if (task.getId() > maxId) {
                            maxId = task.getId();
                        }
                    }
                } else {
                    for (Integer id : CsvConverter.historyFromString(row)) {
                        Task task = null;
                        if (loadedManager.taskList.containsKey(id)) {
                            task = loadedManager.taskList.get(id);
                        } else if (loadedManager.epicList.containsKey(id)) {
                            task = loadedManager.epicList.get(id);
                        } else if (loadedManager.subtaskList.containsKey(id)) {
                            task = loadedManager.subtaskList.get(id);
                        }
                        loadedManager.historyManager.add(task);
                    }
                }
            }
            loadedManager.counter.counter = maxId;
        } catch (IOException e) {
            throw new ManagerLoadException("Ошибка при чтении файла");
        }
        return loadedManager;
    }

    @Override
    public Task createTask(Task task) {
        Task task1 = super.createTask(task);
        save();
        return task1;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic epic1 = super.createEpic(epic);
        save();
        return epic1;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask subtask1 = super.createSubtask(subtask);
        save();
        return subtask1;
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = super.getSubtaskById(id);
        save();
        return subtask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }
}