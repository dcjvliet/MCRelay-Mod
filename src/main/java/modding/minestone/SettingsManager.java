package modding.minestone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SettingsManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Path file = Path.of("user_settings.json");
    private static final Map<UUID, UserSettings> settings = new HashMap<>();

    public static UserSettings getUserSettings(UUID uuid) {
        return settings.computeIfAbsent(uuid, u -> new UserSettings());
    }

    public static void load() {
        try {
            if (!Files.exists(file)) {
                save();
                return;
            }

            Reader reader = Files.newBufferedReader(file);
            Type type = new TypeToken<Map<UUID, UserSettings>>() {}.getType();

            Map<UUID, UserSettings> loaded = gson.fromJson(reader, type);
            reader.close();

            if (loaded != null) {
                settings.clear();
                settings.putAll(loaded);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(file.getParent());
            Writer writer = Files.newBufferedWriter(file);
            gson.toJson(settings, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
