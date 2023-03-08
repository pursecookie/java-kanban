package tracker;

import tracker.managers.FileBackedTasksManager;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        doTest();
    }

    static void doTest() {
        /* Патимат, большое спасибо за похвалу и обратную связь, это очень ценно для меня!
        Поздравляю Вас с Международным женским днём! Желаю вам быть самой счастливой, здоровой и вечно влюбленной.
        Особенно в Java :)

        P.S. с счетчиком id идея показалась логичной (чтобы перенести данную логику в отдельный класс), так что
        поиграюсь на досуге. Присваивание статуса эпика менять не буду. */

        FileBackedTasksManager manager1 = new FileBackedTasksManager();
        manager1.createTask("Сделать зарядку", "описание задачи1");
        manager1.createTask("Позвонить маме", "описание задачи2");
        manager1.createEpic("Уборка", "описание задачи3");
        manager1.createSubtask("Вымыть пол", "описание задачи4", 3);
        manager1.createSubtask("Протереть пыль", "описание задачи5", 3);
        manager1.createSubtask("Вынести мусор", "описание задачи6", 3);
        manager1.createEpic("Учеба", "описание задачи7");

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

        // Сделала дополнительные проверки вне ТЗ для себя
        manager2.deleteTaskById(1);
        manager2.createEpic("Переезд", "описание задачи8");
        manager2.createSubtask("Собрать коробки", "описание задачи9", 8);
        manager2.createTask("Оплатить подписку Яндекс.Музыка", "описание задачи10");
        manager2.createSubtask("Написать план ТЗ", "описание задачи11", 7);
        System.out.println(manager2.getEpicById(3));
        System.out.println(manager2.getTaskById(10));
        System.out.println(manager2.getSubtaskById(9));
        System.out.println("История: " + manager2.getHistory());

        FileBackedTasksManager manager3 = FileBackedTasksManager.loadFromFile(file);
        System.out.println("История2: " + manager2.getHistory());
        System.out.println("История3: " + manager3.getHistory());

    }
}