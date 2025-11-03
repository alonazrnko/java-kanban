import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public PrioritizedHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            if (!h.getRequestMethod().equals("GET")) {
                sendText(h, "{\"error\":\"Invalid method\"}", 405);
                return;
            }
            sendText(h, gson.toJson(manager.getPrioritizedTasks()), 200);
        } catch (Exception e) {
            e.printStackTrace();
            sendInternalError(h);
        }
    }
}

