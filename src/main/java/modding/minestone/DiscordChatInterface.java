package modding.minestone;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import java.awt.*;


public class DiscordChatInterface implements ModInitializer {
	public static final String MOD_ID = "discord-chat-interface";
	public static final Logger logger = LoggerFactory.getLogger(MOD_ID);

	private String lastServer;
	private String lastChannel;

	@Override
	public void onInitialize() {
		logger.info("Initializing Discord Chat Interface Mod");
		registerCommands();
        SettingsManager.load();
		logger.info("Successfully initialized Discord Chat Interface Mod");
	}

	private void registerCommands() {
		// registe the main message command
		CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> {
			dispatcher.register(Commands.literal("dm")
					.then(
							Commands.argument("server", StringArgumentType.string())
									.then(
											Commands.argument("channel", StringArgumentType.string())
													.then(
															Commands.argument("message", StringArgumentType.greedyString())
																	.executes(context -> {
				// get the arguments from the command
				String server = StringArgumentType.getString(context, "server");
				String channel = StringArgumentType.getString(context, "channel");
				String message = StringArgumentType.getString(context, "message");

				this.lastServer = server;
				this.lastChannel = channel;

				// get the player
				ServerPlayer player = context.getSource().getPlayer();

				// send message using custom function
				new Thread(() -> {
					DiscordBridge.sendMessage(
							server,
							channel,
							message,
							player
					);
				}).start();
				return 1;
			})))));
		});

		// register the msglast command
		CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> {
			dispatcher.register(Commands.literal("msglast")
					.then(
							Commands.argument("message", StringArgumentType.greedyString())
									.executes(context -> {
				ServerPlayer player = context.getSource().getPlayer();
                if (player == null) {
                    return 1;
                }

				// make sure there is a last channel and last server value
				if (this.lastChannel == null || this.lastServer == null) {
					player.sendSystemMessage(Component.literal("§cYou haven't messaged a server or channel yet."));
				}

				String message = StringArgumentType.getString(context,  "message");
				new Thread(() -> {
					DiscordBridge.sendMessage(
							this.lastServer,
							this.lastChannel,
							message,
							player
					);
				}).start();
				return 1;
			})));
		});

        // register the settings command
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> {
            dispatcher.register(Commands.literal("settings")
					.then(Commands.literal("set")
							.then(
									Commands.argument("option", StringArgumentType.word())
											.suggests(new SettingsSuggestionProvider())
											.then(
													Commands.argument("value", BoolArgumentType.bool())
															.executes(context -> {
                ServerPlayer player = context.getSource().getPlayer();
                if (player == null) {
                    return 1;
                }

                boolean value = BoolArgumentType.getBool(context, "value");
                String option = StringArgumentType.getString(context, "option");

                UserSettings settings = SettingsManager.getUserSettings(player.getUUID());
                switch (option) {
                    case "showConfirmation" -> settings.showConfirmation = value;
                    case "showErrors" -> settings.showError = value;
                    case "showUserMessages" -> settings.showUserMessages = value;
                    default -> {
                        player.sendSystemMessage(Component.literal(String.format("§cError: %s is not a valid setting name", option)));
                        return 1;
                    }
                }
                player.sendSystemMessage(Component.literal(String.format("Successfully set setting %s to %b", option, value)));
                SettingsManager.save();
                return 1;
            }))))
					.then(Commands.literal("get")
							.then(
									Commands.argument("option", StringArgumentType.word())
											.suggests(new SettingsSuggestionProvider())
											.executes(context -> {
						ServerPlayer player = context.getSource().getPlayer();
						if (player == null) {
							return 1;
						}

						String option = StringArgumentType.getString(context, "option");
						UserSettings settings = SettingsManager.getUserSettings(player.getUUID());

						switch (option) {
							case "showConfirmation" -> player.sendSystemMessage(Component.literal(String.format("Value: %b", settings.showConfirmation)));
							case "showErrors" -> player.sendSystemMessage(Component.literal(String.format("Value: %b", settings.showError)));
							case "showUserMessages" -> player.sendSystemMessage(Component.literal(String.format("Value: %b", settings.showUserMessages)));
							default -> player.sendSystemMessage(Component.literal(String.format("§cError: %s is not a valid setting name", option)));
						}

						return 1;
					})
					)));
        });
	}
}