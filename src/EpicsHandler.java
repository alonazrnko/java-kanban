import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public EpicsHandler(TaskManager manager, Gson gson) {
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
                List<Epic> epics = manager.getEpics();
                sendText(h, gson.toJson(epics), 200);

            } else if (parts.length == 3) {
                int id = Integer.parseInt(parts[2]);
                Optional<Epic> epicOpt = manager.getEpic(id);

                if (epicOpt.isPresent()) {
                    sendText(h, gson.toJson(epicOpt.get()), 200);
                } else {
                    sendNotFound(h);
                }

            } else if (parts.length == 4 && "subtasks".equals(parts[3])) {
                int id = Integer.parseInt(parts[2]);
                Optional<Epic> epicOpt = manager.getEpic(id);

                if (epicOpt.isPresent()) {
                    Epic epic = epicOpt.get();
                    List<Subtask> subtasks = new ArrayList<>();

                    for (Integer subId : epic.getSubtaskIds()) {
                        manager.getSubtask(subId).ifPresent(subtasks::add);
                    }

                    sendText(h, gson.toJson(subtasks), 200);
                } else {
                    sendNotFound(h);
                }

            } else {
                sendNotFound(h);
            }

        } catch (NumberFormatException e) {
            sendText(h, "Некорректный идентификатор эпика", 400);
        } catch (Exception e) {
            sendText(h, "Ошибка при обработке запроса: " + e.getMessage(), 500);
        }
    }

    private void handlePost(HttpExchange h) throws IOException {
        InputStream input = h.getRequestBody();
        String body = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        Epic epic = gson.fromJson(body, Epic.class);

        if (manager.getEpic(epic.getId()).isPresent()) {
            manager.updateEpic(epic);
        } else {
            manager.createEpic(epic);
        }
        sendText(h, "{\"result\":\"ok\"}", 201);
    }

    private void handleDelete(HttpExchange h, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length == 2) {
            manager.deleteEpics();
        } else if (parts.length == 3) {
            int id = Integer.parseInt(parts[2]);
            manager.deleteEpicById(id);
        }
        sendText(h, "{\"result\":\"deleted\"}", 200);
    }
}

