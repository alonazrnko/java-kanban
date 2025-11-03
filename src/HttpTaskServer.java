import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.lang.reflect.Type;

public class HttpTaskServer {
    private final HttpServer server;
    private final TaskManager manager;
    private final Gson gson;

    public HttpTaskServer(TaskManager manager, Gson gson) throws IOException {
        this.manager = manager;
        this.gson = buildGson();
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        // Пример: регистрируем обработчики
        server.createContext("/tasks", new TasksHandler(manager, gson));
        server.createContext("/subtasks", new SubtasksHandler(manager, gson));
        server.createContext("/epics", new EpicsHandler(manager, gson));
    }

    public void start() {
        System.out.println("HTTP-сервер запущен на порту 8080");
        server.start();
    }

    public void stop() {
        server.stop(1);
        System.out.println("HTTP-сервер остановлен");
    }

    public static Gson buildGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(Duration.class, new JsonSerializer<Duration>() {
            @Override
            public JsonElement serialize(Duration duration, Type type, JsonSerializationContext context) {
                return new JsonPrimitive(duration.toMinutes());
            }
        });
        gsonBuilder.registerTypeAdapter(Duration.class, new JsonDeserializer<Duration>() {
            @Override
            public Duration deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
                return Duration.ofMinutes(json.getAsLong());
            }
        });

        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public JsonElement serialize(LocalDateTime dateTime, Type type, JsonSerializationContext context) {
                return new JsonPrimitive(dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
        });
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
                return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
        });

        gsonBuilder.setPrettyPrinting();
        return gsonBuilder.create();
    }

    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefault();
        Gson gson = HttpTaskServer.buildGson();
        new HttpTaskServer(manager, gson).start();
    }
}

