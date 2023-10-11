package tracker.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tracker.adapters.DurationAdapter;
import tracker.adapters.HistoryManagerAdapter;
import tracker.adapters.LocalDateTimeAdapter;
import tracker.managers.impl.HttpTaskManager;
import tracker.managers.impl.InMemoryHistoryManager;

import java.time.Duration;
import java.time.LocalDateTime;

public class Managers {
    public static TaskManager getDefault(String url, int port, String key) {
        return new HttpTaskManager(url, port, key);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static Gson getGson(HttpTaskManager httpTaskManager) {
        return new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(HistoryManager.class, new HistoryManagerAdapter(httpTaskManager))
                .setPrettyPrinting()
                .create();
    }
}