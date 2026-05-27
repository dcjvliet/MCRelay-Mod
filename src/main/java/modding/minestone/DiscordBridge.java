package modding.minestone;

import com.google.gson.Gson;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;


public class DiscordBridge {
    private static final long refreshInterval = 60_000;
    private static final HashMap<UUID, Long> lastRefresh = new HashMap<>();

    public static void sendMessage(String guild, String channel, String message, ServerPlayer player) {
        try {
            URL url = new URI(
                    "http://localhost:5000/send_message"
            ).toURL();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String json =
                    "{"
                            + "\"guild\":\"" + escape(guild) + "\","
                            + "\"channel\":\"" + escape(channel) + "\","
                            + "\"player\":\"" + escape(player.getName().getString()) + "\","
                            + "\"message\":\"" + escape(message) + "\""
                            + "}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.getBytes();
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            InputStream stream = (responseCode >= 200 && responseCode < 300) ? connection.getInputStream() : connection.getErrorStream();

            String response = readStream(stream);

            connection.disconnect();

            UserSettings settings = SettingsManager.getUserSettings(player.getUUID());
            handleResponse(player, responseCode, response, settings, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getData(ServerPlayer player, boolean showRateLimit) {
        long now = System.currentTimeMillis();
        Long latestRefresh = lastRefresh.get(player.getUUID());
        if (latestRefresh != null && now - latestRefresh < refreshInterval) {
            if (showRateLimit) {
                player.sendSystemMessage(Component.literal("§cYou're command too often. Please wait and try again."));
            }
            return;
        }
        lastRefresh.put(player.getUUID(), now);
        try {
            URL url = new URI("http://localhost:5000/get_user_data?username=" + URLEncoder.encode(player.getName().getString(), StandardCharsets.UTF_8)).toURL();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            int responseCode = connection.getResponseCode();
            InputStream stream = (responseCode >= 200 && responseCode < 300) ? connection.getInputStream() : connection.getErrorStream();

            String response = readStream(stream);

            connection.disconnect();

            UserSettings settings = SettingsManager.getUserSettings(player.getUUID());
            if (responseCode >= 200 && responseCode < 300) {
                Gson gson = new Gson();
                UserDataResponse dataResponse = gson.fromJson(response, UserDataResponse.class);
                settings.guilds = dataResponse.guilds;
                if (settings.showConfirmation) {
                    player.sendSystemMessage(Component.literal("§aSuccessfully loaded Discord servers and channels."));
                }
            } else {
                if (settings.showError) {
                    player.sendSystemMessage(Component.literal("§cDiscord error (" + responseCode + "): " + response));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String escape(String s) {
        return s.replace("\"", "\\\"");
    }

    private static String readStream(InputStream stream) throws IOException {
        if (stream == null) return "";

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }

    private static void handleResponse(ServerPlayer player, int responseCode, String response, UserSettings settings, String message) {
        if (responseCode >= 200 && responseCode < 300) {
            if (settings.showConfirmation) {
                player.sendSystemMessage(Component.literal("§aSent to Discord successfully."));
            }
            if (settings.showUserMessages) {
                player.sendSystemMessage(Component.literal(String.format("%s: %s", player.getName().getString(), message)));
            }
        } else {
            if (settings.showError) {
                player.sendSystemMessage(Component.literal("§cDiscord error (" + responseCode + "): " + response));
            }
        }
    }
}
