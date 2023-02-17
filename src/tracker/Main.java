package tracker;

import tracker.service.*;

import static tracker.service.Managers.getDefault;

public class Main {

    public static void main(String[] args) {
        doTest();
    }

    static void doTest() {
        TaskManager manager = getDefault();

        manager.createTask("Сделать зарядку", "");
        manager.createTask("Позвонить маме", "");
        manager.createEpic("Уборка", "");
        manager.createSubtask("Вымыть пол", "", 3);
        manager.createSubtask("Протереть пыль", "", 3);
        manager.createSubtask("Вынести мусор", "", 3);
        manager.createEpic("Учеба", "");

        System.out.println(manager.getTaskById(1));
        System.out.println("История: " + manager.getHistory());
        System.out.println(manager.getEpicById(3));
        System.out.println("История: " + manager.getHistory());
        System.out.println(manager.getEpicById(7));
        System.out.println("История: " + manager.getHistory());
        System.out.println(manager.getTaskById(1));
        System.out.println("История: " + manager.getHistory());
        System.out.println(manager.getSubtaskById(4));
        System.out.println("История: " + manager.getHistory());
        System.out.println(manager.getSubtaskById(5));
        System.out.println("История: " + manager.getHistory());
        System.out.println(manager.getSubtaskById(5));
        System.out.println("История: " + manager.getHistory());
        System.out.println(manager.getTaskById(2));
        System.out.println("История: " + manager.getHistory());
        System.out.println(" ");
        manager.deleteTaskById(1);
        System.out.println("История: " + manager.getHistory());
        System.out.println(" ");
        manager.deleteEpicById(3);
        System.out.println("История: " + manager.getHistory());
    }
}