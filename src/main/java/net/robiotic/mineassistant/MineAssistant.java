package net.robiotic.mineassistant;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URISyntaxException;
import java.util.List;

public class MineAssistant extends JavaPlugin {
    private final LinkStore links = new LinkStore(getDataFolder());
    private HomeAssistant ha;
    private BlockUpdateHandler updateHandler;

    @Override
    public void onLoad() {
        saveDefaultConfig();

        final FileConfiguration config = getConfig();
        final String url = config.getString("ha_url", "");
        final String token = config.getString("ha_token", "");
        final String inputMode = config.getString("input_mode", "interact");

        try {
            ha = new HomeAssistant(url, token, this);
        } catch (URISyntaxException uriSyntaxException) {
            getLogger().warning("Invalid URL");
        }

        updateHandler = new BlockUpdateHandler(ha, inputMode, links);
    }

    @Override
    public void onEnable() {
        if (ha == null || updateHandler == null) {
            // Not configured or initialized correctly
            return;
        }

        // Read persisted links
        links.read();

        // Connect to Home Assistant
        ha.connect();

        // Start listening for block events
        getServer().getPluginManager().registerEvents(updateHandler, this);
    }

    @Override
    public void onDisable() {
        // Stop listening for block events
        if (updateHandler != null) {
            HandlerList.unregisterAll(updateHandler);
            updateHandler = null;
        }

        // Disconnect from Home Assistant
        if (ha != null) {
            ha.close();
            ha = null;
        }

        // Save links persistently
        links.commit();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        //TODO: using args[0] is probably not a good idea
        return ha.searchedEntities(args[0]);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            return false;
        }

        if (ha == null) {
            sender.sendMessage("Home Assistant is not configured properly. See server log for details.");
            return true;
        }

        final String haEntity = args[0];

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command must be run from inside the game");
            return true;
        }

        final Player player = (Player) sender;

        if (!player.hasPermission(R.PERM_LINK)) {
            sender.sendMessage("You don't have permission to link Home Assistant entities");
            return true;
        }

        final Block target = player.getTargetBlockExact(R.MAX_TARGET_DIST);

        if (target == null) {
            sender.sendMessage(String.format("This command must be run while looking at a block to link from no more than %d blocks away", R.MAX_TARGET_DIST));
            return true;
        }

        final Material material = target.getType();
        if (!R.inputBlocks.contains(material) && !R.outputBlocks.contains(material)) {
            sender.sendMessage(String.format("Cannot link this block (%s) to Home Assistant", material));
            return true;
        }

        if (!ha.allEntities.contains(haEntity)) {
            sender.sendMessage(String.format("Entity '%s' does not exist!", haEntity));
            return true;
        }

        links.add(target, haEntity);
        getLogger().info(String.format("%s linked %s to Home Assistant/%s", player.getDisplayName(), target.getBlockData().getAsString(), haEntity));
        sender.sendMessage(String.format(
                "Linked %s as %s '%s'",
                material.name(),
                R.inputBlocks.contains(material) ? "input to" : "output of",
                haEntity
        ));

        // Request all entity states from HA, so the initial state of the block can be set.
        ha.fetchEntities();
        return true;
    }

    /**
     * Set the state of all blocks linked to an entity.
     *
     * @param entityID The ID of the entity to find links for
     * @param haState  The new state to apply to all linked blocks
     */
    private void updateBlocks(String entityID, String haState) {
        links.linkedBlocks(entityID, (LinkedBlock link) -> {
            final World world = getServer().getWorld(link.location.world);

            if (world == null) {
                return; // World is not loaded
            }

            final Block targetBlock = world.getBlockAt(link.location.x, link.location.y, link.location.z);
            updateBlock(targetBlock, entityID, haState);
        });
    }

    /**
     * Updates all the linked blocks from a dump of every HA state.
     * When a block is linked, this data dump is requested so the state of the block can be set immediately,
     * instead of waiting for an update to come in via subscription.
     *
     * @param states the 'result' array from the HA states dump
     */
    public void updateBlocksFromDump(JsonArray states) {
        states.forEach(state -> {
            final String entityID = state.getAsJsonObject().get("entity_id").getAsString();
            final String haState = state.getAsJsonObject().get("state").getAsString();

            // Iterate through the list of blocks this entity is linked to
            updateBlocks(entityID, haState);
        });
    }

    /**
     * Updates all the blocks linked to a single HA entity.
     * Used to process the subscription messages from HA.
     *
     * @param event the JsonObject of an event from HA
     */
    public void updateBlocksFromSubscription(JsonObject event) {
        if (event.get("event_type").getAsString().equals("state_changed")) {
            // Home Assistant has flipped something on or off
            final JsonObject evData = event.getAsJsonObject("data");
            final String entityID = evData.get("entity_id").getAsString();
            final String haState = evData.getAsJsonObject("new_state").getAsJsonPrimitive("state").getAsString();

            // Iterate through the list of blocks this entity is linked to
            updateBlocks(entityID, haState);
        }
    }

    /**
     * Update a block's state with the new one from HA.
     *
     * @param targetBlock the Block to update.
     * @param entityID    the HA entity ID. We don't need this yet but it might be useful later, e.g. for displaying on a sign.
     * @param haState     the new state as a string; now it's up to us to decide what to do with it.
     */
    public void updateBlock(Block targetBlock, String entityID, String haState) {
        // signs. This all has to be done asynchronously, even just checking the block type.
        // so this could certainly be improved!
        Bukkit.getScheduler().runTask(this, () -> {
            BlockState state = targetBlock.getState();
            if (state instanceof Sign) {
                // TODO: maybe some escape codes for multiple lines, or word wrapping?
                Sign sign = (Sign) state;
                sign.setLine(1, entityID);
                sign.setLine(2, haState);
                sign.update();
            }
        });

        if (targetBlock.getBlockData() instanceof Openable) {
            // it's a door, trapdoor, or something of the like
            // Have to run this with a one-tick delay, otherwise the MC world doesn't update.
            boolean newState = haState.equals("on");
            Bukkit.getScheduler().runTaskLater(this, () -> BlockUtil.setOpen(targetBlock, newState), 1);
            // if the new state is different, we have to play the sound manually.
            // TODO: find the right sounds!
            if (BlockUtil.isOpen(targetBlock) != newState) {
                if (targetBlock.getBlockData() instanceof Door) {
                    targetBlock.getWorld().playSound(targetBlock.getLocation(), newState ? Sound.BLOCK_WOODEN_DOOR_OPEN : Sound.BLOCK_WOODEN_DOOR_CLOSE, 1.0F, 0);
                } else if (targetBlock.getBlockData() instanceof TrapDoor) {
                    targetBlock.getWorld().playSound(targetBlock.getLocation(), newState ? Sound.BLOCK_WOODEN_TRAPDOOR_OPEN : Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 1.0F, 0);
                } else {
                    // anything else, just play the trap door sound for now
                    targetBlock.getWorld().playSound(targetBlock.getLocation(), newState ? Sound.BLOCK_WOODEN_TRAPDOOR_OPEN : Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 1.0F, 0);
                }
            }

        } else if (targetBlock.getBlockData() instanceof Powerable) {
            // powerable thing: lever, button etc
            boolean newState = haState.equals("on");
            Bukkit.getScheduler().runTask(this, () -> BlockUtil.setPowered(targetBlock, newState));
        }
    }
}
