package modding.minestone;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import java.awt.*;


public class DiscordChatInterface implements ModInitializer {
	public static final String MOD_ID = "discord-chat-interface";
	public static final Logger logger = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		logger.info("Initializing Discord Chat Interface Mod");
		registerCommands();
		logger.info("Successfully initialized Discord Chat Interface Mod");
	}

	private void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("dm").then(Commands.argument("server", StringArgumentType.string()).then(Commands.argument("channel", StringArgumentType.string()).then(Commands.argument("message", StringArgumentType.greedyString()).executes(context -> {
				// get the arguments from the command
				String server = StringArgumentType.getString(context, "server");
				String channel = StringArgumentType.getString(context, "channel");
				String message = StringArgumentType.getString(context, "message");

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
	}
}