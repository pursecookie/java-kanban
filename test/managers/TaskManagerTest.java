package managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tracker.managers.TaskManager;
import tracker.models.*;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    T taskManager;
    Task savedTaskId1;
    Epic savedEpicId2;
    Subtask savedSubtaskId3;
    Subtask savedSubtaskId4;
    Counter counter;

    @BeforeEach
    void createSavedTasks() {
        counter = new Counter();
        savedTaskId1 = new Task(counter.count(), "Сделать зарядку", Status.NEW, "описание задачи1",
                15, LocalDateTime.of(2023, Month.MARCH, 22, 8, 0));
        savedEpicId2 = new Epic(counter.count(), "Уборка", Status.NEW, "описание задачи2",
                0, null, new ArrayList<>());
        savedSubtaskId3 = new Subtask(counter.count(), "Вымыть пол", Status.NEW, "описание задачи3",
                20, LocalDateTime.of(2023, Month.MARCH, 22, 10, 0), 2);
        savedSubtaskId4 = new Subtask(counter.count(), "Вынести мусор", Status.NEW, "описание задачи4",
                10, LocalDateTime.of(2023, Month.MARCH, 22, 9, 0), 2);
    }

    @DisplayName("Проверка создания задачи")
    @Test
    public void shouldCreateTask() {
        Task testTask = taskManager.createTask(savedTaskId1);

        assertNotNull(testTask, "Задача не создалась");
        assertEquals(List.of(savedTaskId1), taskManager.getTaskList(), "Некорректное создание задачи");
        assertEquals(Set.of(savedTaskId1), taskManager.getPrioritizedTasks(),
                "Задача не добавилась в список задач по приоритету");
    }

    @DisplayName("Проверка создания задачи, если её время пересекается с другими задачами")
    @Test
    public void shouldThrowExceptionIfNewTaskIntersectedWithOtherTasks() {
        taskManager.createTask(savedTaskId1);

        final RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskManager.createTask(new Task(counter.count(), "Позвонить маме", Status.NEW,
                        "описание задачи2", 30,
                        LocalDateTime.of(2023, Month.MARCH, 22, 8, 10)))
        );

        assertEquals("Время задачи пересекается с другими задачами", exception.getMessage());
    }

    @DisplayName("Проверка расчета времени окончания задачи")
    @Test
    public void shouldReturnTaskEndTime() {
        Task testTask = taskManager.createTask(savedTaskId1);

        assertEquals(LocalDateTime.of(2023, Month.MARCH, 22, 8, 15), testTask.getEndTime(),
                "Неверный расчет времени окончания задачи");
    }

    @DisplayName("Проверка создания эпика")
    @Test
    public void shouldCreateEpic() {
        Epic testEpic = taskManager.createEpic(savedEpicId2);

        assertNotNull(testEpic, "Эпик не создался");
        assertEquals(List.of(savedEpicId2), taskManager.getEpicList(), "Некорректное создание эпика");
    }

    @DisplayName("Проверка создания подзадачи")
    @Test
    public void shouldCreateSubtask() {
        taskManager.createEpic(savedEpicId2);

        Subtask testSubtask = taskManager.createSubtask(savedSubtaskId3);

        assertNotNull(testSubtask, "Подзадача не создалась");
        assertEquals(List.of(savedSubtaskId3), taskManager.getSubtaskList(), "Некорректное создание подзадачи");
        assertEquals(Set.of(savedSubtaskId3), taskManager.getPrioritizedTasks(),
                "Подзадача не добавилась в список задач по приоритету");
    }

    @DisplayName("Проверка создания подзадачи, если её эпик не существует")
    @Test
    public void shouldNotCreateSubtaskIfItsEpicDoesNotExists() {
        taskManager.createSubtask(savedSubtaskId3);

        assertEquals(0, taskManager.getSubtaskList().size(), "Неверное количество подзадач в HashMap");

    }

    @DisplayName("Проверка получения списка задач по приоритету")
    @Test
    public void shouldReturnPrioritizedTasks() {
        taskManager.createEpic(savedEpicId2);
        Subtask testSubtask1 = taskManager.createSubtask(savedSubtaskId3);
        Subtask testSubtask2 = taskManager.createSubtask(savedSubtaskId4);
        Task testTask = taskManager.createTask(savedTaskId1);

        List<Task> sortedTasks = new ArrayList<>(taskManager.getPrioritizedTasks());

        assertEquals(List.of(testTask, testSubtask2, testSubtask1), sortedTasks,
                "Список задач не отсортирован");
    }

    @DisplayName("Проверка получения списка задач по приоритету, если у одной из задач не задано время старта")
    @Test
    public void shouldReturnPrioritizedTasksWhereTaskWithoutStartTimeAtTheEndOfTreeSet() {
        taskManager.createEpic(savedEpicId2);
        taskManager.createSubtask(savedSubtaskId3);
        taskManager.createSubtask(savedSubtaskId4);
        Task testTask1 = taskManager.createTask(new Task(counter.count(), "Заказать доставку", Status.NEW,
                "описание задачи5", 15, null));
        taskManager.createTask(savedTaskId1);

        List<Task> sortedTasks = new ArrayList<>(taskManager.getPrioritizedTasks());

        assertEquals(testTask1, sortedTasks.get(3), "Задача без времени старта не в конца списка");
    }

    @DisplayName("Проверка получения списка подзадач, если их эпик не существует")
    @Test
    public void shouldNotReturnSubtaskListByEpicIfEpicDoesNotExist() {
        assertEquals(0, taskManager.getSubtaskListByEpic(10).size(),
                "Список подзадач эпика не пустой");
    }

    @DisplayName("Проверка получения списка подзадач конкретного эпика")
    @Test
    public void shouldReturnSubtaskListByEpic() {
        taskManager.createEpic(savedEpicId2);
        Subtask testSubtask1 = taskManager.createSubtask(savedSubtaskId3);
        Subtask testSubtask2 = taskManager.createSubtask(savedSubtaskId4);

        assertEquals(List.of(testSubtask1, testSubtask2), taskManager.getSubtaskListByEpic(2),
                "Подзадачи не совпадают");
    }

    @DisplayName("Проверка обновления статуса задачи")
    @Test
    public void shouldUpdateTaskStatus() {
        taskManager.createTask(savedTaskId1);
        savedTaskId1.setStatus(Status.IN_PROGRESS);

        taskManager.updateTask(savedTaskId1);

        assertEquals(Status.IN_PROGRESS, taskManager.getTaskList().get(0).getStatus(),
                "Статус задачи не обновился");
        assertEquals(Set.of(savedTaskId1), taskManager.getPrioritizedTasks(),
                "Задача не обновилась в списке задач по приоритету");
    }

    @DisplayName("Проверка обновления статуса задачи, если такой задачи не существует")
    @Test
    public void shouldNotUpdateTaskStatusIfTaskDoesNotExist() {
        taskManager.createTask(savedTaskId1);

        taskManager.updateTask(new Task(10, "Сделать зарядку", Status.IN_PROGRESS, "описание задачи1",
                15, LocalDateTime.of(2023, Month.MARCH, 22, 8, 0)));

        assertEquals(Status.NEW, taskManager.getTaskList().get(0).getStatus(), "Статус задачи обновился");
    }

    @DisplayName("Проверка обновления описания эпика")
    @Test
    public void shouldUpdateEpicDescription() {
        taskManager.createEpic(savedEpicId2);
        savedEpicId2.setDescription(" ");

        taskManager.updateEpicInfo(savedEpicId2);

        assertEquals(" ", taskManager.getEpicList().get(0).getDescription(),
                "Описание эпика не обновилось");
    }

    @DisplayName("Проверка обновления описания эпика, если такого эпика не существует")
    @Test
    public void shouldNotUpdateEpicDescriptionIfEpicDoesNotExist() {
        taskManager.createEpic(savedEpicId2);

        taskManager.updateEpicInfo(new Epic(10,
                "Уборка", Status.NEW, " ", 0, null, new ArrayList<>()));

        assertEquals("описание задачи2", taskManager.getEpicList().get(0).getDescription(),
                "Описание эпика обновилось");
    }

    @DisplayName("Проверка обновления статуса подзадачи")
    @Test
    public void shouldUpdateSubtaskStatus() {
        taskManager.createEpic(savedEpicId2);
        taskManager.createSubtask(savedSubtaskId3);
        savedSubtaskId3.setStatus(Status.DONE);

        taskManager.updateSubtask(savedSubtaskId3);

        assertEquals(Status.DONE, taskManager.getSubtaskList().get(0).getStatus(),
                "Статус подзадачи не обновился");
        assertEquals(Set.of(savedSubtaskId3), taskManager.getPrioritizedTasks(),
                "Подзадача не обновилась в списке задач по приоритету");
    }

    @DisplayName("Проверка обновления статуса подзадачи, если такой подзадачи не существует")
    @Test
    public void shouldNotUpdateSubtaskStatusIfSubtaskDoesNotExist() {
        taskManager.createEpic(savedEpicId2);
        taskManager.createSubtask(savedSubtaskId3);

        taskManager.updateSubtask(savedSubtaskId4);

        assertEquals(Status.NEW, taskManager.getSubtaskList().get(0).getStatus(), "Статус подзадачи обновился");
    }

    @DisplayName("Проверка расчета статуса эпика, если у него нет подзадач")
    @Test
    public void shouldUpdateEpicStatusToNewIfThereIsNoSubtasks() {
        taskManager.createEpic(savedEpicId2);
        taskManager.createSubtask(savedSubtaskId3);

        taskManager.deleteSubtaskById(3);

        assertEquals(Status.NEW, taskManager.getEpicList().get(0).getStatus(), "Статус эпика не NEW");
    }

    @DisplayName("Проверка расчета статуса эпика, если все его подзадачи со статусом NEW")
    @Test
    public void shouldUpdateEpicStatusToNewIfAllSubtasksAreNew() {
        taskManager.createEpic(savedEpicId2);
        taskManager.createSubtask(savedSubtaskId3);
        taskManager.createSubtask(savedSubtaskId4);

        savedSubtaskId4.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(savedSubtaskId4);

        savedSubtaskId4.setStatus(Status.NEW);
        taskManager.updateSubtask(savedSubtaskId4);

        assertEquals(Status.NEW, taskManager.getEpicList().get(0).getStatus(), "Статус эпика не NEW");
    }

    @DisplayName("Проверка расчета статуса эпика, если все его подзадачи со статусом DONE")
    @Test
    public void shouldUpdateEpicStatusToDoneIfAllSubtasksAreDone() {
        taskManager.createEpic(savedEpicId2);
        taskManager.createSubtask(savedSubtaskId3);
        taskManager.createSubtask(savedSubtaskId4);
        savedSubtaskId3.setStatus(Status.DONE);
        savedSubtaskId4.setStatus(Status.DONE);

        taskManager.updateSubtask(savedSubtaskId3);
        taskManager.updateSubtask(savedSubtaskId4);

        assertEquals(Status.DONE, taskManager.getEpicList().get(0).getStatus(), "Статус эпика не DONE");
    }

    @DisplayName("Проверка расчета статуса эпика, если у него есть подзадачи со статусом NEW и DONE")
    @Test
    public void shouldUpdateEpicStatusToInProgressIfSubtasksAreNewAndDone() {
        taskManager.createEpic(savedEpicId2);
        taskManager.createSubtask(savedSubtaskId3);
        taskManager.createSubtask(savedSubtaskId4);
        savedSubtaskId3.setStatus(Status.DONE);

        taskManager.updateSubtask(savedSubtaskId3);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpicList().get(0).getStatus(),
                "Статус эпика не IN PROGRESS");
    }

    @DisplayName("Проверка расчета статуса эпика, если у него есть подзадача со статусом IN PROGRESS")
    @Test
    public void shouldUpdateEpicStatusToInProgressIfOneOfSubtasksIsInProgress() {
        taskManager.createEpic(savedEpicId2);
        taskManager.createSubtask(savedSubtaskId3);
        taskManager.createSubtask(savedSubtaskId4);
        savedSubtaskId3.setStatus(Status.IN_PROGRESS);

        taskManager.updateSubtask(savedSubtaskId3);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpicList().get(0).getStatus(),
                "Статус эпика не IN PROGRESS");
    }

    @DisplayName("Проверка расчета продолжительности эпика")
    @Test
    public void shouldUpdateEpicDuration() {
        Epic testEpic = taskManager.createEpic(savedEpicId2);
        taskManager.createSubtask(savedSubtaskId3);
        taskManager.createSubtask(savedSubtaskId4);

        assertEquals(80, testEpic.getDuration(), "Продолжительность эпика рассчитана неверно");
    }

    @DisplayName("Проверка расчета даты начала эпика")
    @Test
    public void shouldUpdateEpicStartTime() {
        Epic testEpic = taskManager.createEpic(savedEpicId2);
        taskManager.createSubtask(savedSubtaskId3);
        taskManager.createSubtask(savedSubtaskId4);

        assertEquals(LocalDateTime.of(2023, Month.MARCH, 22, 9, 0), testEpic.getStartTime(),
                "Дата начала эпика рассчитана неверно");
    }

    @DisplayName("Проверка расчета даты окончания эпика")
    @Test
    public void shouldUpdateEpicEndTime() {
        Epic testEpic = taskManager.createEpic(savedEpicId2);
        taskManager.createSubtask(savedSubtaskId3);
        taskManager.createSubtask(savedSubtaskId4);

        assertEquals(LocalDateTime.of(2023, Month.MARCH, 22, 10, 20), testEpic.getEndTime(),
                "Дата окончания эпика рассчитана неверно");
    }

    @DisplayName("Проверка удаления задачи")
    @Test
    public void shouldDeleteTask() {
        taskManager.createTask(savedTaskId1);

        taskManager.deleteTaskById(1);

        assertEquals(0, taskManager.getTaskList().size(), "Задача не удалилась");
        assertEquals(0, taskManager.getPrioritizedTasks().size(),
                "Задача не удалилась из списка задач по приоритету");
    }

    @DisplayName("Проверка удаления задачи, если ID задачи неверный")
    @Test
    public void shouldNotDeleteTaskIfWrongId() {
        taskManager.createTask(savedTaskId1);

        taskManager.deleteTaskById(10);

        assertEquals(1, taskManager.getTaskList().size(), "Задача удалилась");
    }

    @DisplayName("Проверка удаления эпика")
    @Test
    public void shouldDeleteEpic() {
        taskManager.createEpic(savedEpicId2);
        taskManager.createSubtask(savedSubtaskId3);
        taskManager.createSubtask(savedSubtaskId4);

        taskManager.deleteEpicById(2);

        assertEquals(0, taskManager.getEpicList().size(), "Эпик не удалился");
        assertEquals(0, taskManager.getSubtaskList().size(), "Подзадачи данного эпика не удалились");
    }

    @DisplayName("Проверка удаления эпика, если ID эпика неверный")
    @Test
    public void shouldNotDeleteEpicIfWrongId() {
        taskManager.createEpic(savedEpicId2);
        taskManager.createSubtask(savedSubtaskId3);
        taskManager.createSubtask(savedSubtaskId4);

        taskManager.deleteEpicById(10);

        assertEquals(1, taskManager.getEpicList().size(), "Эпик удалился");
    }

    @DisplayName("Проверка удаления подзадачи")
    @Test
    public void shouldDeleteSubtask() {
        Epic testEpic = taskManager.createEpic(savedEpicId2);
        taskManager.createSubtask(savedSubtaskId3);

        taskManager.deleteSubtaskById(3);

        assertEquals(0, taskManager.getSubtaskList().size(), "Подзадача не удалилась");
        assertEquals(0, taskManager.getPrioritizedTasks().size(),
                "Задача не удалилась из списка задач по приоритету");
        assertEquals(0, testEpic.getSubtasksIds().size(), "У эпика не удалилась данная подзадача");
    }

    @DisplayName("Проверка удаления подзадачи, если ID подзадачи неверный")
    @Test
    public void shouldNotDeleteSubtaskIfWrongId() {
        taskManager.createEpic(savedEpicId2);
        taskManager.createSubtask(savedSubtaskId3);

        taskManager.deleteSubtaskById(0);

        assertEquals(1, taskManager.getSubtaskList().size(), "Подзадача удалилась");
    }

    @DisplayName("Проверка удаления всех задач")
    @Test
    public void shouldDeleteAllTasks() {
        taskManager.createTask(savedTaskId1);
        taskManager.createTask(new Task(counter.count(), "Позвонить маме", Status.NEW, "описание задачи5",
                50, LocalDateTime.of(2023, Month.MARCH, 22, 13, 0)));

        taskManager.deleteAllTasks();

        assertEquals(0, taskManager.getTaskList().size(), "Удалились не все задачи");
        assertEquals(0, taskManager.getPrioritizedTasks().size(),
                "Задачи не удалились из списка задач по приоритету");
    }

    @DisplayName("Проверка удаления всех эпиков")
    @Test
    public void shouldDeleteAllEpics() {
        taskManager.createEpic(savedEpicId2);
        taskManager.createSubtask(savedSubtaskId3);
        taskManager.createSubtask(savedSubtaskId4);
        taskManager.createEpic(new Epic(counter.count(), "Учеба", Status.NEW, "описание задачи5",
                50, null, new ArrayList<>()));

        taskManager.deleteAllEpics();

        assertEquals(0, taskManager.getEpicList().size(), "Удалились не все эпики");
        assertEquals(0, taskManager.getSubtaskList().size(), "Подзадачи всех эпиков не удалились");
    }

    @DisplayName("Проверка удаления всех подзадач")
    @Test
    public void shouldDeleteAllSubtasks() {
        Epic testEpic = taskManager.createEpic(savedEpicId2);
        taskManager.createSubtask(savedSubtaskId3);
        taskManager.createSubtask(savedSubtaskId4);

        taskManager.deleteAllSubtasks();

        assertEquals(0, taskManager.getSubtaskList().size(), "Удалились не все подзадачи");
        assertEquals(0, taskManager.getPrioritizedTasks().size(),
                "Подзадачи не удалились из списка задач по приоритету");
        assertEquals(0, testEpic.getSubtasksIds().size(), "У эпика не очистился список его подзадач");
    }

}