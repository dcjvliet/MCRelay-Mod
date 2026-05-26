package modding.minestone;

import java.util.ArrayList;
import java.util.List;

public class UserSettings {
    // settings values
    public boolean showConfirmation = true;
    public boolean showError = true;
    public boolean showUserMessages = true;

    // cached servers and channels
    public transient List<DiscordGuild> guilds = new ArrayList<>();
}
