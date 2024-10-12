# Fabric TwinSession

## Installing

Download/Build the .jar file and place it in the `/mods/` folder of your Minecraft Fabric server or client folder.


## Dependencies and Compatibility

This mod is built for Minecraft version 1.21 & 1.21.1 and is build using the following dependencies:

- Fabric Loader 0.15.11 or higher
- Fabric API 0.102.1+1.21.1 or higher

## Versions

- **Current Version (1.21 & 1.21.1)**: [Download v1.0.2-mc.1.21.1](https://github.com/TheMisterFish/Fabric_TwinSession/releases/tag/v1.0.2-mc.1.21.1)

    - Available on modrinth: [TwinSessions - Modrinth](https://modrinth.com/mod/twinsession)

## Download

Currently, the mod is only available through GitHub Releases. We plan to add it to Modrinth in the near future.

## Goal

The **Fabric TwinSession** mod enables players to connect to a Minecraft server multiple times using the same account. 

This functionality is particularly useful for those who want the advantages of multiple sessions, 
such as managing different in-game tasks simultaneously, without needing to purchase additional Minecraft accounts. 
When a player connects to the server with the same account for the second time, the mod automatically modifies the player’s username by 
adding a numerical prefix followed by an underscore (e.g., "1_PlayerName"). 

Each additional session increments this number (e.g., "2_PlayerName", "3_PlayerName", etc.).

## How to Use

### Multiplayer Servers
1. **First Session**: Join the server as usual with your Minecraft account.
2. **Second Session**: Open another Minecraft client and join the same server with the same account. The mod will automatically modify your username (e.g., "1_PlayerName") to allow the connection.
3. **Additional Sessions**: Repeat the process to join the server with more clients using the same account. Each additional session will receive a sequentially numbered username (e.g., "2_PlayerName", "3_PlayerName").

### Singleplayer (LAN)
1. **Set Up**: Start your Minecraft world and select "Open to LAN" from the pause menu.
2. **Connecting**: Follow the same process as in Multiplayer to connect with multiple clients using the same account.

This mod is ideal for players who wish to multitask effectively, such as managing in-game farms or automation, without the need for a separate Minecraft account.

## Config

Configuration can be found under `/config/twinsession.properties` and contains the following confiugration:

| Property              | Description                                                                                   | Type    | Default value |
|-----------------------|-----------------------------------------------------------------------------------------------|---------|---------------|
| maxPlayers            | Max amount of re-joins per client.                                                            | int     | 8             |
| autoWhitelist         | Automatically whitelist if whitelist is enabled.                                              | boolean | true          |
| autoOp                | Automatically op if original client is also op.                                               | boolean | true          |
| spawnNearPlayer       | Spawn near the player, if false new players will join at world spawn.                         | boolean | true          |
| spawnNearPlayerRadius | If spawnNearPlayer is enabled, this will set the radius on who close the player should spawn. | int     | 10            |
| copyTexture           | Copy the texture of the original player.                                                      | boolean | true          |
| prefixWithNumber      | Adds a `$_` prefix to the username (Highly recommended to keep on true)                       | boolean | true          |


## Reporting Issues

If you encounter any bugs or have suggestions for improvements, please create an issue on our GitHub repository. To create an issue:

1. Go to the [Issues](https://github.com/TheMisterFish/Fabric_TwinSession/issues) tab of this repository.
2. Click on "New Issue".
3. Choose between "Bug report" or "Feature request" template.
4. Fill out the template with as much detail as possible.
5. Submit the issue.

## Screenshot

![TwinSession_LanScreenshot](https://github.com/user-attachments/assets/dc5bb67f-4d5d-4246-8b4a-b2ef5922beea)

## Setup

For setup instructions, please see the [Fabric wiki page](https://fabricmc.net/wiki/tutorial:setup) relevant to your IDE.

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it into your own projects.
