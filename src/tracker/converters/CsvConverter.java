package tracker.converters;

import tracker.managers.FileBackedTasksManager;
import tracker.managers.HistoryManager;
import tracker.models.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CsvConverter {
    public String toString(Task task) {
        if (task.getType() == TaskType.TASK || task.getType() == TaskType.EPIC) {
            return task.getId() + "," +
                    task.getType() + "," +
                    task.getTitle() + "," +
                    task.getStatus() + "," +
                    task.getDescription() + "," +
                    task.getDuration() + "," +
                    task.getStartTime() + "," + "";
        }
        Subtask subtask = (Subtask) task;
        return subtask.getId() + "," +
                subtask.getType() + "," +
                subtask.getTitle() + "," +
                subtask.getStatus() + "," +
                subtask.getDescription() + "," +
                task.getDuration() + "," +
                task.getStartTime() + "," +
                subtask.getEpicId();
    }

    public static Task fromString(String value, FileBackedTasksManager manager) {
        String[] fields = value.split(",");

        int id = Integer.parseInt(fields[0]);
        TaskType taskType = TaskType.valueOf(fields[1]);
        String title = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        long duration = Long.parseLong(fields[5]);
        LocalDateTime startTime;

        if (Objects.equals(fields[6], "null")) {
            startTime = null;
        } else {
            startTime = LocalDateTime.parse(fields[6]);
        }

        int epicId = 0;
        if (fields.length > 7) {
            epicId = Integer.parseInt(fields[7]);
        }

        switch (taskType) {
            case TASK:
                return new Task(id, title, status, description, duration, startTime);
            case EPIC:
                ArrayList<Integer> subtaskIds = new ArrayList<>();
                for (Subtask subtask : manager.getSubtaskList()) {
                    if (subtask.getEpicId() == id) {
                        subtaskIds.add(subtask.getId());
                    }
                }
                return new Epic(id, title, status, description, duration, startTime, subtaskIds);
            case SUBTASK:
                return new Subtask(id, title, status, description, duration, startTime, epicId);
        }
        return null;
    }

    public static String historyToString(HistoryManager historyManager) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Task task : historyManager.getHistory()) {
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