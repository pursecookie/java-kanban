package tracker.managers.impl;

import tracker.managers.Managers;
import tracker.servers.KVTaskClient;


public class HttpTaskManager extends FileBackedTasksManager {
    private final String key;
    private final KVTaskClient kvTaskClient;

    public HttpTaskManager(String url, int port, String key) {
        kvTaskClient = new KVTaskClient(url, port);
        this.key = key;
        load();
    }

    @Override
    public void save() {
        String json = Managers.getGson(this).toJson(this);
        kvTaskClient.put(key, json);
    }

    private void load() {
        kvTaskClient.load(key);
    }

}
