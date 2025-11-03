import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public TasksHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String method = h.getRequestMethod();
            String path = h.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    handleGet(h, path);
                    break;
                case "POST":
                    handlePost(h, path);
                    break;
                case "DELETE":
                    handleDelete(h, path);
                    break;
                default:
                    sendText(h, "{\"error\":\"Invalid method\"}", 405);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendInternalError(h);
        }
    }

    private void handleGet(HttpExchange h, String path) throws IOException {
        String[] parts = path.split("/");

        if (parts.length == 2) {
            List<Task> tasks = manager.getTasks();
            sendText(h, gson.toJson(tasks), 200);

        } else if (parts.length == 3) {
            try {
                int id = Integer.parseInt(parts[2]);

                Optional<Task> optionalTask = manager.getTask(id);

                if (optionalTask.isEmpty()) {
                    sendNotFound(h); //
                    return;
                }

                Task task = optionalTask.get();
                sendText(h, gson.toJson(task), 200);

            } catch (NumberFormatException e) {
                sendText(h, "Некорректный идентификатор задачи", 400);
            }
        } else {
            sendNotFound(h);
        }
    }

    private void handlePost(HttpExchange h, String path) throws IOException {
        try {
            String body = new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            if (body == null || body.isBlank() || !(body.trim().startsWith("{") && body.trim().endsWith("}"))) {
                sendText(h, "Некорректный JSON", 400);
                return;
            }

            Task task = gson.fromJson(body, Task.class);

            if (task == null || task.getTitle() == null || task.getDescription() == null) {
                sendText(h, "Неверные данные задачи", 400);
                return;
            }

            manager.createTask(task);
            sendText(h, "", 201);

        } catch (com.google.gson.JsonSyntaxException e) {
            sendText(h, "Некорректный JSON", 400);
        } catch (Exception e) {
            e.printStackTrace();
            sendText(h, "Ошибка при обработке POST-запроса", 500);
        }
    }

    private void handleDelete(HttpExchange h, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length == 2) {
            manager.deleteTasks();
            sendText(h, "{\"result\":\"all tasks deleted\"}", 200);
        } else if (parts.length == 3) {
            int id = Integer.parseInt(parts[2]);
            manager.deleteTaskById(id);
        }
        sendText(h, "{\"result\":\"deleted\"}", 200);
    }
}

