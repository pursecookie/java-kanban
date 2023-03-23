package tracker.managers;

import tracker.exceptions.ManagerValidateException;
import tracker.model.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static tracker.managers.Managers.getDefaultHistory;

public class InMemoryTaskManager implements TaskManager {
    HashMap<Integer, Task> taskList = new HashMap<>();
    HashMap<Integer, Epic> epicList = new HashMap<>();
    HashMap<Integer, Subtask> subtaskList = new HashMap<>();
    HistoryManager historyManager = getDefaultHistory();
    Counter counter = new Counter();

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public Task createTask(Task task) {
        searchIntersections(task);
        taskList.put(task.getId(), task);

        return taskList.get(task.getId());
    }

    @Override
    public Epic createEpic(Epic epic) {
        epicList.put(epic.getId(), epic);
        return epicList.get(epic.getId());
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (!epicList.containsKey(subtask.getEpicId())) {
            return subtask;
        }

        searchIntersections(subtask);
        subtaskList.put(subtask.getId(), subtask);

        Epic epic = epicList.get(subtask.getEpicId());
        epic.addSubtaskId(subtask.getId());
        updateEpic(epic);
        return subtaskList.get(subtask.getId());
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

    @Override
    public Set<Task> getPrioritizedTasks() {
        List<Task> allTasks = new ArrayList<>();

        allTasks.addAll(taskList.values());
        allTasks.addAll(subtaskList.values());

        List<Task> tasksWithoutStartTime = new ArrayList<>();

        for (Task task : allTasks) {
            if (task.getStartTime() == null) {
                tasksWithoutStartTime.add(task);
            }
        }

        allTasks.removeAll(tasksWithoutStartTime);

        Set<Task> sortedTasks = allTasks.stream()
                .sorted(Comparator.comparing(Task::getStartTime))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        sortedTasks.addAll(tasksWithoutStartTime);

        return sortedTasks;
    }

    public void searchIntersections(Task task) {
        if (task.getStartTime() == null) {
            return;
        }

        List<Task> allTasks = new ArrayList<>(getPrioritizedTasks());

        for (Task taskInList : allTasks) {
            if (taskInList.getStartTime() == null) {
                break;
            }

            boolean isIntersected = task.getStartTime().isBefore(taskInList.getEndTime())
                    && task.getEndTime().isAfter(taskInList.getStartTime());
            if (isIntersected) {
                throw new ManagerValidateException("Время задачи пересекается с другими задачами");
            }
        }
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
    public void updateTask(Task updatedTask) {
        if (!taskList.containsKey(updatedTask.getId())) {
            return;
        }

        Task task = taskList.get(updatedTask.getId());

        if (!updatedTask.getStartTime().equals(task.getStartTime())) {
            searchIntersections(updatedTask);
            task.setStartTime(updatedTask.getStartTime());
        }

        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setStatus(updatedTask.getStatus());
        task.setDuration(updatedTask.getDuration());
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
    public void updateEpic(Epic updatedEpic) {
        if (!epicList.containsKey(updatedEpic.getId())) {
            return;
        }

        Epic epic = epicList.get(updatedEpic.getId());
        epic.setTitle(updatedEpic.getTitle());
        epic.setDescription(updatedEpic.getDescription());

        ArrayList<Status> subtaskStatus = new ArrayList<>();

        Instant epicStartTime = epic.getStartTime();
        Instant epicEndTime = epic.getEndTime();

        for (Integer subtaskId : epicList.get(updatedEpic.getId()).getSubtasksIds()) {
            subtaskStatus.add(subtaskList.get(subtaskId).getStatus());

            if (epicStartTime == Instant.EPOCH || subtaskList.get(subtaskId).getStartTime().isBefore(epicStartTime)) {
                epicStartTime = subtaskList.get(subtaskId).getStartTime();
            }
            if (subtaskList.get(subtaskId).getEndTime().isAfter(epicEndTime)) {
                epicEndTime = subtaskList.get(subtaskId).getEndTime();
            }
        }

        long epicDuration = (Duration.between(epicStartTime, epicEndTime)).toMinutes();

        epic.setDuration(epicDuration);
        epic.setStartTime(epicStartTime);
        epic.setEndTime(epicEndTime);

        if (epicList.get(updatedEpic.getId()).getSubtasksIds().isEmpty() || checkIsNew(subtaskStatus)) {
            epicList.get(updatedEpic.getId()).setStatus(Status.NEW);
        } else if (checkIsDone(subtaskStatus)) {
            epicList.get(updatedEpic.getId()).setStatus(Status.DONE);
        } else {
            epicList.get(updatedEpic.getId()).setStatus(Status.IN_PROGRESS);
        }

    }

    @Override
    public void updateSubtask(Subtask updatedSubtask) {
        if (!subtaskList.containsKey(updatedSubtask.getId())) {
            return;
        }

        Subtask subtask = subtaskList.get(updatedSubtask.getId());

        if (!updatedSubtask.getStartTime().equals(subtask.getStartTime())) {
            searchIntersections(updatedSubtask);
            subtask.setStartTime(updatedSubtask.getStartTime());
        }

        subtask.setTitle(updatedSubtask.getTitle());
        subtask.setDescription(updatedSubtask.getDescription());
        subtask.setStatus(updatedSubtask.getStatus());
        subtask.setDuration(updatedSubtask.getDuration());

        updateEpic(epicList.get(updatedSubtask.getEpicId()));
    }

    @Override
    public void deleteTaskById(int id) {
        if (!taskList.containsKey(id)) {
            return;
        }

        taskList.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        if (!epicList.containsKey(id)) {
            return;
        }

        Epic epic = epicList.remove(id);
        historyManager.remove(id);

        for (Integer subtaskId : epic.getSubtasksIds()) {
            subtaskList.remove(subtaskId);
            historyManager.remove(subtaskId);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        if (!subtaskList.containsKey(id)) {
            return;
        }

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