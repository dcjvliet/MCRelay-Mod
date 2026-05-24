package modding.minestone;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;


public class DiscordBridge {
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

            handleResponse(player, responseCode, response);
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

    private static void handleResponse(ServerPlayer player, int responseCode, String response) {
        if (responseCode >= 200 && responseCode < 300) {
            player.sendSystemMessage(Component.literal("§aSent to Discord successfully."));
        } else {
            player.sendSystemMessage(Component.literal("§cDiscord error (" + responseCode + "): " + response));
        }
    }
}
