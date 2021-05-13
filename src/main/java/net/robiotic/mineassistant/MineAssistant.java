package net.robiotic.mineassistant;

import org.bukkit.block.Block;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URISyntaxException;

public class MineAssistant extends JavaPlugin {
    private static final int MAX_TARGET_DIST = 5;

    private HomeAssistant ha;

    @Override
    public void onLoad() {
        saveDefaultConfig();

        final FileConfiguration config = getConfig();
        final String url = config.getString("ha_url", "");
        final String token = config.getString("ha_token", "");

        try {
            ha = new HomeAssistant(url, token);
        } catch (URISyntaxException uriSyntaxException) {
            getLogger().warning("Invalid URL");
        }
    }

    @Override
    public void onEnable() {
        if (ha == null) {
            return;
        }

        ha.connect();
    }

    @Override
    public void onDisable() {
        if (ha == null) {
            return;
        }

        ha.close();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (ha == null) {
            sender.sendMessage("Home Assistant is not configured properly. See server log for details.");
            return false;
        }

        if (args.length != 1) {
            sender.sendMessage(String.format("Usage: /%s <Home Assistant entity ID>", label));
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command must be run from inside the game");
            return false;
        }

        final Block target = ((Player) sender).getTargetBlockExact(MAX_TARGET_DIST);

        if (target == null) {
            sender.sendMessage(String.format("This command must be run while looking at a block to link from no more than %d blocks away", MAX_TARGET_DIST));
            return false;
        }

        if (target instanceof Switch) {

        } else if (target instanceof Powerable) {

        }

        switch (target.getType()) {
            // Inputs
            case LEVER:
                break;
            case BIRCH_BUTTON:
            case ACACIA_BUTTON:
            case CRIMSON_BUTTON:
            case DARK_OAK_BUTTON:
            case JUNGLE_BUTTON:
            case OAK_BUTTON:
            case POLISHED_BLACKSTONE_BUTTON:
            case SPRUCE_BUTTON:
            case STONE_BUTTON:
            case WARPED_BUTTON:
                break;
            case ACACIA_PRESSURE_PLATE:
            case BIRCH_PRESSURE_PLATE:
            case CRIMSON_PRESSURE_PLATE:
            case DARK_OAK_PRESSURE_PLATE:
            case HEAVY_WEIGHTED_PRESSURE_PLATE:
            case JUNGLE_PRESSURE_PLATE:
            case LIGHT_WEIGHTED_PRESSURE_PLATE:
            case OAK_PRESSURE_PLATE:
            case POLISHED_BLACKSTONE_PRESSURE_PLATE:
            case SPRUCE_PRESSURE_PLATE:
            case STONE_PRESSURE_PLATE:
            case WARPED_PRESSURE_PLATE:
                break;
            case TARGET:
                break;
            case TRIPWIRE_HOOK:
                break;
            // Outputs
            case PISTON:
            case STICKY_PISTON:
                break;
            case REDSTONE_LAMP:
                break;
            case DISPENSER:
            case DROPPER:
                break;
            case NOTE_BLOCK:
                break;
            default:
                sender.sendMessage(String.format("Cannot link this block (%s) to Home Assistant", target.getType()));
                break;
        }

        return true;
    }
}
