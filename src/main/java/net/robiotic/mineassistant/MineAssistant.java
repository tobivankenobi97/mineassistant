package net.robiotic.mineassistant;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

public class MineAssistant extends JavaPlugin {
    private HomeAssistant ha;
    private BlockUpdateHandler updateHandler;
    private final HashMap<String, Block> registeredEntities = new HashMap<>();

    @Override
    public void onLoad() {
        saveDefaultConfig();

        final FileConfiguration config = getConfig();
        final String url = config.getString("ha_url", "");
        final String token = config.getString("ha_token", "");

        try {
            ha = new HomeAssistant(url, token, this);
        } catch (URISyntaxException uriSyntaxException) {
            getLogger().warning("Invalid URL");
        }

        updateHandler = new BlockUpdateHandler(ha);
    }

    @Override
    public void onEnable() {
        if (ha == null || updateHandler == null) {
            // Not configured or initialized correctly
            return;
        }

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

        if (!R.inputBlocks.contains(target.getType()) && !R.outputBlocks.contains(target.getType())) {
            sender.sendMessage(String.format("Cannot link this block (%s) to Home Assistant", target.getType()));
            return true;
        }

        // TODO: persistent storage
        // TODO: two-way binding
        target.setMetadata(R.LINK_META_KEY, new FixedMetadataValue(this, haEntity));
        registeredEntities.put (haEntity, target);
        getLogger().info(String.format("%s linked %s to Home Assistant/%s", player.getDisplayName(), target.getBlockData().getAsString(), haEntity));
        return true;
    }

    public void checkEvent(JsonObject event) {
        if (event.get("event_type").getAsString().equals("state_changed")) {
            // Home Assistant has flipped something on or off
            JsonObject evData = event.getAsJsonObject("data");
            String entityID = evData.get("entity_id").getAsString();
            // check if we have linked this block previously with the /hass command
            if (registeredEntities.containsKey(entityID)) {
                String haState = evData.getAsJsonObject("new_state").getAsJsonPrimitive("state").getAsString();
                String mcState = haState.equals("on") ? "true" : "false";
                Block targetBlock = registeredEntities.get(entityID);

                getLogger().info(entityID + " was just set to " + haState + " by HA");

                // and now for a very cheesy way of rewriting the powered status in the block data string
                String bdStr = targetBlock.getBlockData().getAsString().replaceAll("powered=true|powered=false", "powered=" + mcState);
                BlockData newBlockData = Bukkit.createBlockData(bdStr);
                Bukkit.getScheduler().runTask(this, () -> targetBlock.setBlockData(newBlockData));
            }
        }
    }
}
