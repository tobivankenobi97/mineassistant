package net.robiotic.mineassistant;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URISyntaxException;
import java.util.List;

public class MineAssistant extends JavaPlugin {
    private HomeAssistant ha;
    private BlockUpdateHandler updateHandler;
    private final LinkStore links = new LinkStore(getDataFolder());

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
        return ha.allEntities;
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

        links.add(target, haEntity);
        getLogger().info(String.format("%s linked %s to Home Assistant/%s", player.getDisplayName(), target.getBlockData().getAsString(), haEntity));
        sender.sendMessage(String.format(
                "Linked %s as %s '%s'",
                material.name(),
                R.inputBlocks.contains(material) ? "input to" : "output of",
                haEntity
        ));

        return true;
    }

    public void checkEvent(JsonObject event) {
        if (event.get("event_type").getAsString().equals("state_changed")) {
            // Home Assistant has flipped something on or off
            final JsonObject evData = event.getAsJsonObject("data");
            final String entityID = evData.get("entity_id").getAsString();
            final String haState = evData.getAsJsonObject("new_state").getAsJsonPrimitive("state").getAsString();
            final String mcState = haState.equals("on") ? "true" : "false";
            getLogger().info(entityID + " was just set to " + haState + " by HA");

            // Iterate through the list of blocks this entity is linked to
            links.linkedBlocks(entityID, (LinkedBlock link) -> {
                final World world = getServer().getWorld(link.location.world);

                if (world == null) {
                    return; // World is not loaded
                }

                final Block targetBlock = world.getBlockAt(link.location.x, link.location.y, link.location.z);

                if (!targetBlock.getType().name().equals(link.material)) {
                    // Block has been replaced with a different type, so remove it from the persistent store
                    links.remove(link);
                }

                // and now for a very cheesy way of rewriting the powered status in the block data string
                String bdStr = targetBlock.getBlockData().getAsString().replaceAll("powered=true|powered=false", "powered=" + mcState);
                BlockData newBlockData = Bukkit.createBlockData(bdStr);
                Bukkit.getScheduler().runTask(this, () -> targetBlock.setBlockData(newBlockData));
            });
        }
    }
}
