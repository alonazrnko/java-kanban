import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public SubtasksHandler(TaskManager manager, Gson gson) {
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
                    handlePost(h);
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

        try {
            if (parts.length == 2) {
                List<Subtask> subs = manager.getSubtasks();
                sendText(h, gson.toJson(subs), 200);

            } else if (parts.length == 3) {
                int id = Integer.parseInt(parts[2]);
                Optional<Subtask> subOpt = manager.getSubtask(id);

                if (subOpt.isPresent()) {
                    sendText(h, gson.toJson(subOpt.get()), 200);
                } else {
                    sendNotFound(h);
                }

            } else {
                sendNotFound(h);
            }

        } catch (NumberFormatException e) {
            sendText(h, "Некорректный идентификатор подзадачи", 400);
        } catch (Exception e) {
            sendText(h, "Ошибка при обработке запроса: " + e.getMessage(), 500);
        }
    }

    private void handlePost(HttpExchange h) throws IOException {
        InputStream input = h.getRequestBody();
        String body = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        Subtask subtask = gson.fromJson(body, Subtask.class);

        if (manager.getSubtask(subtask.getId()).isPresent()) {
            manager.updateSubtask(subtask);
        } else {
            manager.createSubtask(subtask);
        }
        sendText(h, "{\"result\":\"ok\"}", 201);
    }

    private void handleDelete(HttpExchange h, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length == 2) {
            manager.deleteSubtasks();
        } else if (parts.length == 3) {
            int id = Integer.parseInt(parts[2]);
            manager.deleteSubtaskById(id);
        }
        sendText(h, "{\"result\":\"deleted\"}", 200);
    }
}

