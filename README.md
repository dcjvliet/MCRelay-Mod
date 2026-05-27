# Discord Chat Interface

## Setup

Install the .jar file on the server that the Minecraft server runs from. You must also run the Discord bot which is available at [this](https://github.com/dcjvliet/MCRelay) repository.

## Usage

In order to use the mod in-game, it must be installed server-side. In order for a player to use the mod, their Discord and Minecraft accounts must be linked through the Discord bot.
- `/dm <server> <channel> <message>`: Send the message to the specified server and channel. You must have permissions to send messages in that channel. If the server or channel arguments are multiple words, they must be in quotes.
- `/msglast <message>`: Send the message to the last messaged server and channel. You must have permissions to send messages in that channel.
- `/settings set <option> <value>`: Set the provided setting name to either true or false.
- `/settings get <option>`: Get the current settings value for the provided setting name.
- `/refresh`: Refresh the cached Discord servers and channels that are used to autofill the `/dm` command. Runs automatically when you join the server.

## License

This mod is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
