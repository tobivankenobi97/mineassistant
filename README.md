# Mine Assistant

Links Home Assistant entities to Minecraft blocks on a Paper server.

This is a Paper 26.3 update of [Mine Assistant by Andy Castille](https://gitlab.com/klikini/mineassistant). The original project is MIT licensed. See [LICENSE.txt](LICENSE.txt).

A linked lever, door, trapdoor, or fence gate follows the Home Assistant entity, and the entity follows the block. Signs show the entity name. Buttons, pressure plates, tripwire hooks, and daylight detectors can report into Home Assistant.

## Install

The server needs Paper 26.3, which runs on Java 25.

1. Put `MineAssistant.jar` in the server `plugins` folder.
2. Start the server once. It writes `plugins/MineAssistant/config.yml`.
3. Set `ha_url` and `ha_token`, then restart.

`ha_url` is the Home Assistant base URL as the Minecraft server sees it, for example `http://192.168.1.10:8123`. `ha_token` is a [long-lived access token](https://www.home-assistant.io/docs/authentication/#your-account-profile).

That generated config contains the token. Do not commit it. This repository ignores a filled-in `/config.yml` and anything under `/plugins/`. The only config in git is the empty sample at `src/main/resources/config.yml`.

## Linking a block

You need permission `hass.link`. Operators have it. Everyone else does not, until you grant it.

Stand within 5 blocks of the block, look at it, and run:

```
/hass light.kitchen
```

Tab completion lists entity ids from Home Assistant. An id that already has a link is marked ` (LINKED)`. The id has to exist. The plugin asks Home Assistant for the current state and applies it to the block right away.

Run the command again on the same block to point it at a different entity. Breaking the block removes its link. Replacing the block with a different material removes it too.

## Which blocks do what

The plugin matches materials by name, so wood types added after the original plugin (mangrove, cherry, bamboo, pale oak, poplar, copper doors, hanging signs, and later ones of the same shape) are included.

| Blocks | Direction | What happens |
| --- | --- | --- |
| Levers, doors, trapdoors, fence gates | Both ways | Home Assistant `on` powers a lever or opens a door, trapdoor, or gate. The block sends `turn_on` or `turn_off` back. |
| Buttons, pressure plates, tripwire hooks, daylight detectors | Minecraft to Home Assistant | A signal change calls `turn_on` or `turn_off` on the entity. Home Assistant does not move these blocks. |
| Signs, wall signs, hanging signs | Home Assistant to Minecraft | The sign shows the entity's friendly name. `&` color codes in that name are applied. Minecraft does not change the entity. |

`turn_on` and `turn_off` are called on the entity's domain (`light`, `switch`, and so on). A `sensor.*` entity cannot be toggled by clicking the block.

## Input mode

`input_mode` is read at startup. Change it in `plugins/MineAssistant/config.yml` and restart.

`interact` is the default. Only a player's right-click sends state to Home Assistant, and only if that player has `hass.use` (granted to everyone by default). Pressure plates only report the press, not the release. Tripwires do not report at all. The server log names the player.

`redstone` uses the linked block's redstone signal instead of the click. Levers, doors, trapdoors, fence gates, buttons, pressure plates, tripwire hooks, and daylight detectors then call `turn_on` or `turn_off` when their signal changes. No player permission is checked, and a mob or a redstone circuit can switch the Home Assistant entity. Signs are unchanged: they still only display state.

Home Assistant still updates linked levers, doors, trapdoors, gates, and signs in both modes.

## Permissions

| Permission | Default | Effect |
| --- | --- | --- |
| `hass.link` | operators | Use `/hass` and see entity suggestions. |
| `hass.use` | everyone | Click a linked block so it updates Home Assistant while `input_mode` is `interact`. |

## Build

Compile with JDK 25. The jar targets Java 21 bytecode, which the Java 25 server runs.

```
mvn package
```

The plugin jar is `target/MineAssistant-1.2.0.jar`. Paper API is `provided` and is not inside the jar. Java-WebSocket is.
