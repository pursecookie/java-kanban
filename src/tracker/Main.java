package tracker;

import tracker.managers.FileBackedTasksManager;
import tracker.model.Status;
import tracker.model.Task;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        doTest();
    }

    static void doTest() {

        FileBackedTasksManager manager1 = new FileBackedTasksManager();
        //Task task1 = new Task(manager1.counter.counter, "Сделать зарядку", Status.NEW, "описание задачи1");
        //manager1.createTask(task1);
        //manager1.createTask("Позвонить маме", "описание задачи2");
        //manager1.createEpic("Уборка", "описание задачи3");
        //manager1.createSubtask("Вымыть пол", "описание задачи4", 3);
        //manager1.createSubtask("Протереть пыль", "описание задачи5", 3);
        //manager1.createSubtask("Вынести мусор", "описание задачи6", 3);
        //manager1.createEpic("Учеба", "описание задачи7");

        System.out.println(manager1.getTaskById(1));
        System.out.println(manager1.getEpicById(3));
        System.out.println(manager1.getEpicById(7));
        System.out.println(manager1.getTaskById(1));
        System.out.println(manager1.getSubtaskById(4));
        System.out.println(manager1.getSubtaskById(5));
        System.out.println(manager1.getSubtaskById(5));
        System.out.println(manager1.getTaskById(2));
        System.out.println("История: " + manager1.getHistory());

        File file = new File("managerData.src");

        FileBackedTasksManager manager2 = FileBackedTasksManager.loadFromFile(file);
        System.out.println("История1: " + manager1.getHistory());
        System.out.println("История2: " + manager2.getHistory());

        manager2.deleteTaskById(1);
        //manager2.createEpic("Переезд", "описание задачи8");
        //manager2.createSubtask("Собрать коробки", "описание задачи9", 8);
        //manager2.createTask("Оплатить подписку Яндекс.Музыка", "описание задачи10");
        //manager2.createSubtask("Написать план ТЗ", "описание задачи11", 7);
        System.out.println(manager2.getEpicById(3));
        System.out.println(manager2.getTaskById(10));
        System.out.println(manager2.getSubtaskById(9));
        System.out.println("История: " + manager2.getHistory());

        FileBackedTasksManager manager3 = FileBackedTasksManager.loadFromFile(file);
        System.out.println("История2: " + manager2.getHistory());
        System.out.println("История3: " + manager3.getHistory());

    }
}