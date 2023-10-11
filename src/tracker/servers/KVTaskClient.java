package tracker.servers;

import tracker.exceptions.ManagerLoadException;
import tracker.exceptions.ManagerRegisterException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    private final String url;
    private final String apiToken;
    private final int port;

    public KVTaskClient(String url, int port) {
        this.url = url;
        this.port = port;
        URI uri = URI.create(url + port + "/register");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .header("Content-Type", "application/json")
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        HttpResponse<String> response;

        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ManagerRegisterException("Ошибка при регистрации клиента");
        }
        apiToken = response.body();
    }

    public void put(String key, String json) {
        URI uri = URI.create(url + port + "/save/" + key + "?API_TOKEN=" + apiToken);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder()
                .POST(body)
                .uri(uri)
                .header("Content-Type", "application/json")
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

        try {
            client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ManagerLoadException("Ошибка при сохранении состояния менеджера на сервер");
        }
    }

    public String load(String key) {
        URI uri = URI.create(url + port + "/load/" + key + "?API_TOKEN=" + apiToken);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .header("Content-Type", "application/json")
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        HttpResponse<String> response;

        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ManagerLoadException("Ошибка при загрузке состояния менеджера с сервера");
        }
        return response.body();
    }
}