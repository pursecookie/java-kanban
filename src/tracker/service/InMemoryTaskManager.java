package tracker.service;

import tracker.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static tracker.service.Managers.getDefaultHistory;

public class InMemoryTaskManager implements TaskManager {
    int counter = 1;
    HashMap<Integer, Task> taskList = new HashMap<>();
    HashMap<Integer, Epic> epicList = new HashMap<>();
    HashMap<Integer, Subtask> subtaskList = new HashMap<>();
    HistoryManager historyManager = getDefaultHistory();

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public void createTask(String title, String description) {
        Task task = new Task(title, description, counter, Status.NEW);
        counter++;
        taskList.put(task.getId(), task);
    }

    @Override
    public void createEpic(String title, String description) {
        Epic epic = new Epic(title, description, counter, Status.NEW, new ArrayList<>());
        counter++;
        epicList.put(epic.getId(), epic);
    }

    @Override
    public void createSubtask(String title, String description, int epicId) {
        Subtask subtask = new Subtask(title, description, counter, Status.NEW, epicId);
        counter++;
        subtaskList.put(subtask.getId(), subtask);

        Epic epic = epicList.get(epicId);
        if (epic == null) {
            return;
        }
        epic.addSubtaskId(subtask.getId());
        updateEpic(epic);
    }

    @Override
    public List<Task> getTaskList() {
        return new ArrayList<>(this.taskList.values());
    }

    @Override
    public List<Subtask> getSubtaskList() {
        return new ArrayList<>(this.subtaskList.values());
    }

    @Override
    public List<Epic> getEpicList() {
        return new ArrayList<>(this.epicList.values());
    }

    public List<Subtask> getSubtaskListByEpic(int epicId) {
        List<Subtask> getEpicSubtasks = new ArrayList<>();
        for (Integer subtaskId : subtaskList.keySet()) {
            if (subtaskList.get(subtaskId).getEpicId() == epicId) {
                getEpicSubtasks.add(subtaskList.get(subtaskId));
            }
        }
        return getEpicSubtasks;
    }

    @Override
    public Task getTaskById(int id) {
        historyManager.add(taskList.get(id));
        return taskList.get(id);
    }

    @Override
    public Epic getEpicById(int id) {
        historyManager.add(epicList.get(id));
        return epicList.get(id);
    }

    @Override
    public Subtask getSubtaskById(int id) {
        historyManager.add(subtaskList.get(id));
        return subtaskList.get(id);
    }

    @Override
    public void updateTask(Task task) {
        Task updatedTask = taskList.get(task.getId());
        if (updatedTask == null) {
            return;
        }
        updatedTask.setTitle(task.getTitle());
        updatedTask.setDescription(task.getDescription());
        updatedTask.setStatus(task.getStatus());
    }

    public boolean checkIsNew(ArrayList<Status> subtaskStatus) {
        for (Status status : subtaskStatus) {
            if (status != Status.NEW) {
                return false;
            }
        }
        return true;
    }

    public boolean checkIsDone(ArrayList<Status> subtaskStatus) {
        for (Status status : subtaskStatus) {
            if (status != Status.DONE) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic updatedEpic = epicList.get(epic.getId());
        if (updatedEpic == null) {
            return;
        }
        updatedEpic.setTitle(epic.getTitle());
        updatedEpic.setDescription(epic.getDescription());

        ArrayList<Status> subtaskStatus = new ArrayList<>();

        for (Integer subtaskId : epicList.get(epic.getId()).getSubtasksIds()) {
            subtaskStatus.add(subtaskList.get(subtaskId).getStatus());
        }
        if (epicList.get(epic.getId()).getSubtasksIds().isEmpty() || checkIsNew(subtaskStatus)) {
            epicList.get(epic.getId()).setStatus(Status.NEW);
        } else if (checkIsDone(subtaskStatus)) {
            epicList.get(epic.getId()).setStatus(Status.DONE);
        } else {
            epicList.get(epic.getId()).setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask updatedSubtask = subtaskList.get(subtask.getId());
        if (updatedSubtask == null) {
            return;
        }
        updatedSubtask.setTitle(subtask.getTitle());
        updatedSubtask.setDescription(subtask.getDescription());
        updatedSubtask.setStatus(subtask.getStatus());
        updateEpic(epicList.get(subtask.getEpicId()));
    }

    @Override
    public void deleteTaskById(int id) {
        taskList.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epicList.remove(id);
        historyManager.remove(id);
        for (Integer subtaskId : epic.getSubtasksIds()) {
            subtaskList.remove(subtaskId);
            historyManager.remove(subtaskId);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Epic epic = epicList.get(subtaskList.get(id).getEpicId());
        epic.removeSubtask(id);
        subtaskList.remove(id);
        historyManager.remove(id);
        updateEpic(epic);
    }

    @Override
    public void deleteAllTasks() {
        for (Task task : taskList.values()) {
            historyManager.remove(task.getId());
        }
        taskList.clear();
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epicList.values()) {
            historyManager.remove(epic.getId());
            for (Integer subtaskId : epic.getSubtasksIds()) {
                historyManager.remove(subtaskId);
            }
        }
        subtaskList.clear();
        epicList.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask subtask : subtaskList.values()) {
            historyManager.remove(subtask.getId());
        }
        for (Integer epicId : epicList.keySet()) {
            Epic epic = epicList.get(epicId);
            epic.cleanSubtaskIds();
        }
        subtaskList.clear();
    }

}