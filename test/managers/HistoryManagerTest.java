package managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tracker.managers.HistoryManager;
import tracker.models.*;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tracker.managers.Managers.getDefaultHistory;

public class HistoryManagerTest {
    HistoryManager historyManager;
    Task savedTaskId1;
    Task savedTaskId2;
    Task savedTaskId3;
    Counter counter;

    @BeforeEach
    void createSavedTasks() {
        historyManager = getDefaultHistory();
        counter = new Counter();
        savedTaskId1 = new Task(counter.count(), "Сделать зарядку", Status.NEW, "описание задачи1",
                15, LocalDateTime.of(2023, Month.MARCH, 22, 8, 0));
        savedTaskId2 = new Task(counter.count(), "Заказать доставку", Status.NEW, "описание задачи2",
                5, LocalDateTime.of(2023, Month.MARCH, 22, 10, 0));
        savedTaskId3 = new Task(counter.count(), "Позвонить маме", Status.NEW, "описание задачи3",
                50, LocalDateTime.of(2023, Month.MARCH, 22, 15, 30));
    }

    @DisplayName("Проверка добавления задачи в историю просмотров")
    @Test
    public void shouldAddTaskToHistory() {
        historyManager.add(savedTaskId1);

        assertEquals(List.of(savedTaskId1), historyManager.getHistory(), "Задача не добавилась в историю");
    }

    @DisplayName("Проверка дублирования задачи в истории просмотров")
    @Test
    public void shouldAddExistingTaskToTheEndOfHistory() {
        historyManager.add(savedTaskId1);
        historyManager.add(savedTaskId2);

        historyManager.add(savedTaskId1);

        assertEquals(savedTaskId1, historyManager.getHistory().get(1),
                "Задача не переместилась в конец истории");
    }

    @DisplayName("Проверка отображения истории просмотров, если она пустая")
    @Test
    public void shouldReturnEmptyHistory() {
        assertEquals(0, historyManager.getHistory().size(), "Не возвращается пустая история");
    }

    @DisplayName("Проверка удаления задачи с неверным ID")
    @Test
    public void shouldNotRemoveTaskFromHistoryIfWrongId() {
        historyManager.add(savedTaskId1);

        historyManager.remove(2);

        assertEquals(List.of(savedTaskId1), historyManager.getHistory(), "Задача удалилась из истории");
    }

    @DisplayName("Проверка удаления последней задачи в истории просмотров")
    @Test
    public void shouldRemoveLastTaskFromHistory() {
        historyManager.add(savedTaskId1);
        historyManager.add(savedTaskId2);

        historyManager.remove(2);

        assertEquals(List.of(savedTaskId1), historyManager.getHistory(), "Задача не удалилась из истории");
    }

    @DisplayName("Проверка удаления первой задачи в истории просмотров")
    @Test
    public void shouldRemoveFirstTaskFromHistory() {
        historyManager.add(savedTaskId1);
        historyManager.add(savedTaskId2);

        historyManager.remove(1);

        assertEquals(List.of(savedTaskId2), historyManager.getHistory(), "Задача не удалилась из истории");
    }

    @DisplayName("Проверка удаления задачи из середины истории просмотров")
    @Test
    public void shouldRemoveMiddleTaskFromHistory() {
        historyManager.add(savedTaskId1);
        historyManager.add(savedTaskId2);
        historyManager.add(savedTaskId3);

        historyManager.remove(2);

        assertEquals(List.of(savedTaskId1, savedTaskId3), historyManager.getHistory(),
                "Задача не удалилась из истории");
    }
}