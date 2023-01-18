package tracker.service;

import tracker.model.Status;

public class Main {

    public static void main(String[] args) {
        doTest();
    }

    static void doTest() {
        Manager manager = new Manager();

        manager.createTask("Сделать зарядку", "В утреннее время - с 8:00 до 8:15");
        manager.createTask("Позвонить маме", "");
        manager.createEpic("Уборка", "");
        manager.createSubtask("Вымыть пол", "", 3);
        manager.createSubtask("Протереть пыль", "", 3);
        manager.createEpic("Учеба", "");
        manager.createSubtask("Написать план ТЗ", "", 6);

        manager.getTaskList();
        manager.getEpicList();

        manager.updateTask(1, "Сделать зарядку", "", Status.DONE);
        manager.updateTask(2, "Позвонить маме", "", Status.DONE);
        manager.updateSubtask(4, "Вымыть пол", "", Status.DONE);
        manager.updateSubtask(5, "Протереть пыль", "", Status.IN_PROGRESS);
        manager.updateSubtask(7, "Написать план ТЗ", "Классы подпакета model уже написаны", Status.DONE);
        manager.updateEpic(3, "Уборка", "");
        manager.updateEpic(6, "Учеба", "");

        manager.getTaskList();
        manager.getEpicList();

        manager.deleteTaskById(1);
        manager.deleteEpicById(3);
    }
}