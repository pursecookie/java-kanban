package tracker;

import tracker.model.*;
import tracker.service.*;

import static tracker.service.Managers.getDefault;

public class Main {

    public static void main(String[] args) {
        doTest();
    }

    static void doTest() {
        TaskManager manager = getDefault();

        manager.createTask("Сделать зарядку", "В утреннее время - с 8:00 до 8:15");
        manager.createTask("Позвонить маме", "");
        manager.createEpic("Уборка", "");
        manager.createSubtask("Вымыть пол", "", 3);
        manager.createSubtask("Протереть пыль", "", 3);
        manager.createEpic("Учеба", "");
        manager.createSubtask("Написать план ТЗ", "", 6);

        System.out.println(manager.getTaskList());
        System.out.println(manager.getHistory());
        System.out.println(manager.getSubtaskList());
        System.out.println(manager.getHistory());
        System.out.println(manager.getSubtaskList());
        System.out.println(manager.getHistory());
        System.out.println(manager.getTaskById(1));
        System.out.println(manager.getHistory());
        System.out.println(manager.getEpicById(3));
        System.out.println(manager.getHistory());
        System.out.println(manager.getEpicById(6));
        System.out.println(manager.getHistory());
        System.out.println(manager.getTaskById(1));
        System.out.println(manager.getHistory());
        System.out.println(manager.getSubtaskById(4));
        System.out.println(manager.getHistory());
        System.out.println(manager.getSubtaskById(5));
        System.out.println(manager.getHistory());

        Task task = new Task("Сделать зарядку", "", 1, Status.DONE);
        manager.updateTask(task);
        task = new Task("Позвонить маме", "", 2, Status.DONE);
        manager.updateTask(task);

        Subtask subtask = new Subtask("Вымыть пол", "", 4, Status.DONE, 3);
        manager.updateSubtask(subtask);
        subtask = new Subtask("Протереть пыль", "", 5, Status.IN_PROGRESS, 3);
        manager.updateSubtask(subtask);
        subtask = new Subtask("Написать план ТЗ", "Классы подпакета model уже написаны",
                7, Status.DONE, 6);
        manager.updateSubtask(subtask);

        Epic epic = new Epic("Уборка", "", 3);
        manager.updateEpic(epic);
        epic = new Epic("Учеба", "", 6);
        manager.updateEpic(epic);

        System.out.println(manager.getTaskList());
        System.out.println(manager.getEpicList());
        System.out.println(manager.getSubtaskList());

        manager.deleteTaskById(1);
        manager.deleteEpicById(3);
    }
}