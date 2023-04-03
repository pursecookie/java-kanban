package tracker.managers;

import tracker.models.Epic;
import tracker.models.Subtask;
import tracker.models.Task;

import java.util.List;
import java.util.Set;

public interface TaskManager {
    Task createTask(Task task);

    Epic createEpic(Epic epic);

    Subtask createSubtask(Subtask subtask);

    List<Task> getTaskList();

    List<Subtask> getSubtaskList();

    List<Epic> getEpicList();

    List<Subtask> getSubtaskListByEpic(int epicId);

    Task getTaskById(int id);

    Epic getEpicById(int id);

    Subtask getSubtaskById(int id);

    void updateTask(Task updatedTask);

    void updateEpicInfo(Epic updatedEpic);

    void updateSubtask(Subtask updatedSubtask);

    void deleteTaskById(int id);

    void deleteEpicById(int id);

    void deleteSubtaskById(int id);

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    List<Task> getHistory();

    Set<Task> getPrioritizedTasks();
}