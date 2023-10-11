package tracker.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import tracker.converters.CsvConverter;
import tracker.managers.HistoryManager;
import tracker.managers.Managers;
import tracker.managers.impl.FileBackedTasksManager;
import tracker.managers.impl.HttpTaskManager;
import tracker.managers.impl.InMemoryHistoryManager;
import tracker.models.Task;

import java.io.IOException;
import java.util.List;

public class HistoryManagerAdapter extends TypeAdapter<InMemoryHistoryManager> {
    private final HttpTaskManager httpTaskManager;

    public HistoryManagerAdapter(HttpTaskManager httpTaskManager) {
        this.httpTaskManager = httpTaskManager;
    }

    @Override
    public void write(final JsonWriter jsonWriter, final InMemoryHistoryManager historyManager) throws IOException {
        jsonWriter.value(CsvConverter.historyToString(historyManager));
    }

    @Override
    public InMemoryHistoryManager read(final JsonReader jsonReader) throws IOException {
        HistoryManager historyManager = Managers.getDefaultHistory();
        String json = jsonReader.nextString();
        List<Integer> historyIds = CsvConverter.historyFromString(json);

        for (Integer id : historyIds) {
            historyManager.add(findTask(id, httpTaskManager));
        }

        return (InMemoryHistoryManager) historyManager;
    }

    public static Task findTask(Integer id, FileBackedTasksManager httpTaskManager) {
        Task task = null;

        if (httpTaskManager.taskList.containsKey(id)) {
            task = httpTaskManager.taskList.get(id);
        } else if (httpTaskManager.epicList.containsKey(id)) {
            task = httpTaskManager.epicList.get(id);
        } else if (httpTaskManager.subtaskList.containsKey(id)) {
            task = httpTaskManager.subtaskList.get(id);
        }

        return task;
    }
}