package tracker.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tracker.adapters.DurationAdapter;
import tracker.adapters.HistoryManagerAdapter;
import tracker.adapters.LocalDateTimeAdapter;
import tracker.servers.KVTaskClient;

import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskManager extends FileBackedTasksManager {
    private final String key;
    private final KVTaskClient kvTaskClient;
    public HttpTaskManager(String url, int port, String key) {
        kvTaskClient = new KVTaskClient(url, port);
        this.key = key;
    }
    @Override
    public void save() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(HistoryManager.class, new HistoryManagerAdapter(this))
                .create();
        String json = gson.toJson(this);
        kvTaskClient.put(key, json);
    }
}
