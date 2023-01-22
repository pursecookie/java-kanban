package tracker.service;

import tracker.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Manager {
    int counter = 1;
    HashMap<Integer, Task> taskList = new HashMap<>();
    HashMap<Integer, Epic> epicList = new HashMap<>();
    HashMap<Integer, Subtask> subtaskList = new HashMap<>();

    public void createTask(String title, String description) {
        Task task = new Task(title, description, counter, Status.NEW);
        counter++;
        taskList.put(task.getId(), task);
    }

    public void createEpic(String title, String description) {
        Epic epic = new Epic(title, description, counter, Status.NEW, new ArrayList<>());
        counter++;
        epicList.put(epic.getId(), epic);
    }

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

    public List<Task> getTaskList() {
        return new ArrayList<>(this.taskList.values());
    }

    public List<Subtask> getSubtaskList() {
        return new ArrayList<>(this.subtaskList.values());
    }

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

    public Task getTaskById(int id) {
        return taskList.get(id);
    }

    public Epic getEpicById(int id) {
        return epicList.get(id);
    }

    public Subtask getSubtaskById(int id) {
        return subtaskList.get(id);
    }

    /* В методах по обновлению задач я решила менять именно значения полей объекта класса, т.к. мне это показалось
    безопаснее и более гибко, чем если мы по ID напрямую заменим значение (объект) в мапе. Но я пока не разбираюсь,
    что сколько памяти занимает, поэтому могу ошибаться. */

    public void updateTask(Task task) {
        if (task == null) {
            return;
        }
        task = taskList.get(task.getId());

        task.setTitle(task.getTitle());
        task.setDescription(task.getDescription());
        task.setStatus(task.getStatus());
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

    public void updateEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        epic = epicList.get(epic.getId());

        epic.setTitle(epic.getTitle());
        epic.setDescription(epic.getDescription());

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

    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }
        subtask = subtaskList.get(subtask.getId());

        subtask.setTitle(subtask.getTitle());
        subtask.setDescription(subtask.getDescription());
        subtask.setStatus(subtask.getStatus());
        updateEpic(epicList.get(subtask.getEpicId()));
    }

    public void deleteTaskById(int id) {
        taskList.remove(id);
    }

    /* Спасибо вам большое за эти подсказки! Действительно, в некоторых местах перемудрила саму себя. Впредь буду
    перечитывать код несколько раз подряд, чтобы улучшать его и делать чище :) Все изменения зафиксировала в своей
    голове. */

    public void deleteEpicById(int id) {
        Epic epic = epicList.remove(id);
        for (Integer subtaskId : epic.getSubtasksIds()) {
            subtaskList.remove(subtaskId);
        }
    }

    public void deleteSubtaskById(int id) {
        Epic epic = epicList.get(subtaskList.get(id).getEpicId());
        epic.removeSubtask(id);
        subtaskList.remove(id);
        updateEpic(epic);
    }

    public void deleteAllTasks() {
        taskList.clear();
    }

    public void deleteAllEpics() {
        subtaskList.clear();
        epicList.clear();
    }

    public void deleteAllSubtasks() {
        for (Integer epicId : epicList.keySet()) {
            Epic epic = epicList.get(epicId);
            epic.cleanSubtaskIds();
        }
        subtaskList.clear();
    }

}