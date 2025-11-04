import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {

    protected void sendText(HttpExchange h, String text, int code) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(code, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendNotFound(HttpExchange h) throws IOException {
        sendText(h, "{\"error\":\"Not Found\"}", 404);
    }

    protected void sendHasInteractions(HttpExchange h) throws IOException {
        sendText(h, "{\"error\":\"Task overlaps with existing\"}", 406);
    }

    protected void sendInternalError(HttpExchange h) throws IOException {
        sendText(h, "{\"error\":\"Internal server error\"}", 500);
    }
}

