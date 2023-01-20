package tracker.service;

import tracker.model.*;

import java.util.ArrayList;
import java.util.Collections;
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

        /* Подскажите, зачем нужно добавлять проверку на NPE? По логике представленного в ТЗ интерфейса мы не можем
        создать подзадачу без эпика. Т.е. передаваемый в кач-ве аргумента ID эпика априори существует. Иначе я не
        понимаю, как мне выполнить эту проверку без System.out.println (если их все нужно удалить из данного класса).
        Можно было рассмотреть вариант, как в предыдущем ТЗ, по отлавливанию I0 Exception, но мы этого еще не
        проходили, да и опять же - там используется System.out.println*/

        Epic epic = epicList.get(epicId);
        epic.addSubtaskId(subtask.getId());
        updateEpic(epic);
    }

    public List<Task> getTaskList() {
        List<Task> getTasks = new ArrayList<>();
        for (Integer id : taskList.keySet()) {
            getTasks.add(taskList.get(id));
        }
        return getTasks;
    }

    public List<Subtask> getSubtaskList() {
        List<Subtask> getSubtasks = new ArrayList<>();
        for (Integer id : subtaskList.keySet()) {
            getSubtasks.add(subtaskList.get(id));
        }
        return getSubtasks;
    }

    public List<Epic> getEpicList() {
        List<Epic> getEpics = new ArrayList<>();
        for (Integer epicId : epicList.keySet()) {
            getEpics.add(epicList.get(epicId));
        }
        return getEpics;
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

    public void updateTask(Task task) {
        taskList.get(task.getId()).setTitle(task.getTitle());
        taskList.get(task.getId()).setDescription(task.getDescription());
        taskList.get(task.getId()).setStatus(task.getStatus());
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
        epicList.get(epic.getId()).setTitle(epic.getTitle());
        epicList.get(epic.getId()).setDescription(epic.getDescription());

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
        subtaskList.get(subtask.getId()).setTitle(subtask.getTitle());
        subtaskList.get(subtask.getId()).setDescription(subtask.getDescription());
        subtaskList.get(subtask.getId()).setStatus(subtask.getStatus());
        updateEpic(epicList.get(subtask.getEpicId()));
    }

    public void deleteTaskById(int id) {
        taskList.remove(id);
    }

    public void deleteEpicById(int id) {
        deleteSubtaskByEpic(id);
        epicList.remove(id);
    }

    public void deleteSubtaskById(int id) {
        Epic epic = epicList.get(subtaskList.get(id).getEpicId());
        epic.removeSubtask(id);
        subtaskList.remove(id);
        updateEpic(epic);
    }

    public void deleteSubtaskByEpic(int epicId) {
        ArrayList<Integer> subtaskToRemove = new ArrayList<>();

        for (Integer id : epicList.keySet()) {
            if (id == epicId) {
                subtaskToRemove.addAll(epicList.get(epicId).getSubtasksIds());
            }
        }
        epicList.get(epicId).getSubtasksIds().removeAll(subtaskToRemove);
        for (Integer subtaskId : subtaskToRemove) {
            subtaskList.keySet().removeAll(Collections.singletonList(subtaskId));
        }
        updateEpic(epicList.get(epicId));
    }

    public void deleteAllTasks() {
        taskList.clear();
    }

    public void deleteAllEpics() {
        deleteAllSubtasks();
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