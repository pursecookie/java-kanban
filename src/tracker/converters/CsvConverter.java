package tracker.converters;

import tracker.managers.FileBackedTasksManager;
import tracker.managers.HistoryManager;
import tracker.model.*;

import java.util.ArrayList;
import java.util.List;

public class CsvConverter {
    public String toString(Task task) {
        if (task.getType() == Type.TASK || task.getType() == Type.EPIC) {
            return task.getId() + "," +
                    task.getType() + "," +
                    task.getTitle() + "," +
                    task.getStatus() + "," +
                    task.getDescription() + "," + "";
        }
        Subtask subtask = (Subtask) task;
        return subtask.getId() + "," +
                subtask.getType() + "," +
                subtask.getTitle() + "," +
                subtask.getStatus() + "," +
                subtask.getDescription() + "," +
                subtask.getEpicId();
    }

    public static Task fromString(String value, FileBackedTasksManager manager) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        Type type = Type.valueOf(fields[1]);
        String title = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        int epicId = 0;
        if (fields.length > 5) {
            epicId = Integer.parseInt(fields[5]);
        }

        switch (type) {
            case TASK:
                return new Task(id, type, title, status, description);
            case EPIC:
                ArrayList<Integer> subtaskIds = new ArrayList<>();
                for (Subtask subtask : manager.getSubtaskList()) {
                    if (subtask.getEpicId() == id) {
                        subtaskIds.add(subtask.getId());
                    }
                }
                return new Epic(id, type, title, status, description, subtaskIds);
            case SUBTASK:
                return new Subtask(id, type, title, status, description, epicId);
        }
        return null;
    }

    public static String historyToString(HistoryManager manager) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Task task : manager.getHistory()) {
            stringBuilder.append(task.getId()).append(",");
        }
        return String.valueOf(stringBuilder);
    }

    public static List<Integer> historyFromString(String value) {
        List<Integer> historyId = new ArrayList<>();
        String[] idList = value.split(",");
        for (String id : idList) {
            historyId.add(Integer.valueOf(id));
        }
        return historyId;
    }
}