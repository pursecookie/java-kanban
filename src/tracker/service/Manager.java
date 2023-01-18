package tracker.service;

import tracker.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Manager {
    int counter = 1;
    HashMap<Integer, Task> taskList = new HashMap<>();
    HashMap<Integer, Epic> epicList = new HashMap<>();
    HashMap<Integer, Subtask> subtaskList = new HashMap<>();

    /* В описании ТЗ говорится о том, что функционал приложения должен быть прописан в единственном классе -
    Manager. При этом, каждый тип задачи должен иметь свой метод, и иногда бывает такое, что эти
    методы схожи, просто нуждаются в переопределении (дублирование кода). Было бы правильнее сделать свой Manager под
    каждый тип задачи? Например, EpicManager и SubtaskManager, которые наследовали бы от TaskManager. И, если сигнатура
    метода отличается, мы бы просто делали новый метод для данного типа задачи. Иначе создается впечатление, что в кучу
    смешались люди и кони, как и методы для разных типов задач. Разделить эти методы по классам было бы структурнее,
    мне кажется. */

    public void createTask(String title, String subject) {
        Task task = new Task(title, subject, counter, Status.NEW);
        counter++;
        taskList.put(task.getId(), task);
    }

    public void createEpic(String title, String subject) {
        Epic epic = new Epic(title, subject, counter, Status.NEW, new ArrayList<>());
        counter++;
        epicList.put(epic.getId(), epic);
    }

    public void createSubtask(String title, String subject, int epicId) {
        Subtask subtask = new Subtask(title, subject, counter, Status.NEW, epicId);
        counter++;
        subtaskList.put(subtask.getId(), subtask);
        epicList.get(epicId).getSubtasks().add(subtask.getId());
    }

    public void getTaskList() {
        if (!taskList.isEmpty()) {
            System.out.println("Задачи:");
            for (Integer id : taskList.keySet()) {
                System.out.println(taskList.get(id));
            }
        } else {
            System.out.println("Вы ещё не внесли ни одной задачи");
        }
    }

    public void getEpicList() {
        if (!epicList.isEmpty()) {
            for (Integer epicId : epicList.keySet()) {
                System.out.println("Эпик:\r\n" + epicList.get(epicId));
                getSubtaskListByEpic(epicId);
            }
        } else {
            System.out.println("Вы ещё не внесли ни одного эпика");
        }
    }

    public void getSubtaskListByEpic(int epicId) {
        if (epicList.get(epicId).getSubtasks().isEmpty()) {
            System.out.println("Вы ещё не внесли подзадачи для данного эпика");
        } else {
            System.out.println("Подзадачи эпика:");
            for (Integer subtaskId : subtaskList.keySet()) {
                if (subtaskList.get(subtaskId).getEpicId() == epicId) {
                    System.out.println(subtaskList.get(subtaskId));
                }
            }
        }
    }

    public void getTaskById(int id) {
        System.out.println(taskList.get(id));
    }

    public void getEpicById(int id) {
        System.out.println(epicList.get(id));
    }

    public void getSubtaskById(int id) {
        System.out.println(subtaskList.get(id));
    }

    public void updateTask(int id, String title, String subject, Status status) {
        taskList.get(id).setTitle(title);
        taskList.get(id).setSubject(subject);
        taskList.get(id).setStatus(status);
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

    public void updateEpic(int id, String title, String subject) {
        epicList.get(id).setTitle(title);
        epicList.get(id).setSubject(subject);

        ArrayList<Status> subtaskStatus = new ArrayList<>();

        for (Integer subtaskId : epicList.get(id).getSubtasks()) {
            subtaskStatus.add(subtaskList.get(subtaskId).getStatus());
        }

        if (epicList.get(id).getSubtasks().isEmpty() || checkIsNew(subtaskStatus)) {
            epicList.get(id).setStatus(Status.NEW);
        } else if (checkIsDone(subtaskStatus)) {
            epicList.get(id).setStatus(Status.DONE);
        } else {
            epicList.get(id).setStatus(Status.IN_PROGRESS);
        }
    }

    public void updateSubtask(int id, String title, String subject, Status status) {
        subtaskList.get(id).setTitle(title);
        subtaskList.get(id).setSubject(subject);
        subtaskList.get(id).setStatus(status);
    }

    public void deleteTaskById(int id) {
        taskList.remove(id);
    }

    public void deleteEpicById(int id) {
        deleteSubtaskByEpic(id);
        epicList.remove(id);
    }

    public void deleteSubtaskById(int id) {
        int epicId = subtaskList.get(id).getEpicId();
        epicList.get(epicId).getSubtasks().remove(Integer.valueOf(id));
        subtaskList.remove(id);
    }

    public void deleteSubtaskByEpic(int epicId) {
        ArrayList<Integer> subtaskToRemove = new ArrayList<>();

        for (Integer id : epicList.keySet()) {
            if (id == epicId) {
                subtaskToRemove.addAll(epicList.get(id).getSubtasks());
            }
        }
        epicList.get(epicId).getSubtasks().removeAll(subtaskToRemove);
        for (Integer subtaskId : subtaskToRemove) {
            subtaskList.keySet().removeAll(Collections.singletonList(subtaskId));
        }
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
            epicList.get(epicId).getSubtasks().clear();
        }
        subtaskList.clear();
    }

}