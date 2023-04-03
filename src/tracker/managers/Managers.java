package tracker.managers;

public class Managers {
    public static TaskManager getDefault(String url, int port, String key) {
        return new HttpTaskManager(url, port, key);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}