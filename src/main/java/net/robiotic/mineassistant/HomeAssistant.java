package net.robiotic.mineassistant;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * A WebSocket connection to Home Assistant.
 */
public class HomeAssistant extends WebSocketClient {
    private final Gson gson = new Gson();
    private final Logger logger = Logger.getLogger("Home Assistant");
    private final MineAssistant ma;

    /**
     * Home Assistant long-lived access token.
     */
    private final String token;

    /**
     * ID of the last command sent to Home Assistant.
     */
    private int id = 0;

    /**
     * @param url   Instance URL
     * @param token Long-lived access token
     * @throws URISyntaxException when the URL is invalid
     */
    public HomeAssistant(String url, String token, MineAssistant ma) throws URISyntaxException {
        super(new URI(
                (url.endsWith("/") ? url : url + "/") // ensure trailing slash
                        .replaceFirst("^http", "ws") // use ws(s) instead of http(s)
                        + "api/websocket"
        ));

        this.ma = ma;
        this.token = token;
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
                subscribe();
                break;
            case "result":
                handleResult(message);
                break;
            case "event":
                ma.checkEvent(message.getAsJsonObject("event"));
                break;
        }
    }

    /**
     * Change the state of an entity in Home Assistant.
     * @param entity The ID of the entity to update
     * @param state The new state (on or off)
     */
    public void setState(String entity, boolean state) {
        final JsonObject serviceData = new JsonObject();
        serviceData.add("entity_id", new JsonPrimitive(entity));

        final JsonObject message = new JsonObject();
        message.add("id", new JsonPrimitive(++id));
        message.add("type", new JsonPrimitive("call_service"));
        message.add("domain", new JsonPrimitive(entity.split("\\.")[0]));
        message.add("service", new JsonPrimitive("turn_" + (state ? "on" : "off")));
        message.add("service_data", serviceData);

        send(gson.toJson(message));
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

    /**
     * Ask the server to send state updates.
     */
    private void subscribe() {
        final JsonObject message = new JsonObject();
        message.add("id", new JsonPrimitive(++id));
        message.add("type", new JsonPrimitive("subscribe_events"));
        message.add("event_type", new JsonPrimitive("state_changed"));
        send(gson.toJson(message));
    }

    /**
     * Result of executing a command.
     */
    private void handleResult(JsonObject message) {
        if (!message.get("success").getAsBoolean()) {
            final String error = message.getAsJsonObject("error").get("message").getAsString();
            logger.warning("Home Assistant failed to execute command: " + error);
        }
    }
}
