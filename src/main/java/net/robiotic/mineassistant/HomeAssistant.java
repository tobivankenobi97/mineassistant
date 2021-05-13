package net.robiotic.mineassistant;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * A WebSocket connection to Home Assistant.
 */
public class HomeAssistant extends WebSocketClient {
    private final Gson gson = new Gson();
    private final String token;
    private final Logger logger;
    private final HashMap<Integer, Void> pendingChanges = new HashMap<>();
    private int id = 0;

    /**
     * @param url   Instance URL
     * @param token Long-lived access token
     * @throws URISyntaxException when the URL is invalid
     */
    public HomeAssistant(String url, String token) throws URISyntaxException {
        super(new URI(
                (url.endsWith("/") ? url : url + "/") // ensure trailing slash
                        .replaceFirst("^http", "ws") // use ws(s) instead of http(s)
                        + "api/websocket"
        ));
        this.token = token;
        this.logger = Logger.getLogger("Home Assistant");
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        logger.info("Home Assistant socket opened");
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        logger.info("Home Assistant socket closed");
    }

    @Override
    public void onError(Exception e) {
        logger.warning("Home Assistant socket error: " + e.toString());
    }

    @Override
    public void onMessage(String s) {
        final JsonObject message = gson.fromJson(s, JsonObject.class);

        switch (message.get("type").getAsString()) {
            case "auth_required":
                sendAuth();
                break;
            case "auth_invalid":
                logger.warning("Home Assistant authentication failed: " + message.get("message").getAsString());
                break;
            case "auth_ok":
                logger.info("Successfully authenticated with Home Assistant");
                break;
            case "result":
                logger.info("RESULT: " + s);
                handleResult(message);
                break;
            case "event":
                logger.info("EVENT: " + s);
                handleEvent(message);
                break;
        }
    }

    public void setState(String entity, boolean state) {
        final JsonObject serviceData = new JsonObject();
        serviceData.add("entity_id", new JsonPrimitive(entity));

        final JsonObject message = new JsonObject();
        message.add("id", new JsonPrimitive(++id));
        message.add("type", new JsonPrimitive("call_service"));
        message.add("domain", new JsonPrimitive(entity.split("\\.")[0]));
        message.add("service", new JsonPrimitive("turn_" + (state ? "on" : "off")));
        message.add("service_data", serviceData);

        final String json = gson.toJson(message);
        send(json);
        logger.info(json);
    }

    /**
     * Send our access token to the server.
     */
    private void sendAuth() {
        logger.fine("Home Assistant is requesting authentication...");
        final JsonObject message = new JsonObject();
        message.add("type", new JsonPrimitive("auth"));
        message.add("access_token", new JsonPrimitive(token));
        send(gson.toJson(message));
        logger.fine("...sent token");
    }

    private void handleResult(JsonObject message) {
        final boolean success = message.get("success").getAsBoolean();
        final int id = message.get("id").getAsInt();

        if (success) {
            pendingChanges.get(id); // TODO: update block state
        }

        pendingChanges.remove(id);
    }

    private void handleEvent(JsonObject message) {

    }
}
