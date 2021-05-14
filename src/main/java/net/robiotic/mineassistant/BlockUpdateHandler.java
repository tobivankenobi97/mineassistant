package net.robiotic.mineassistant;

import com.google.common.base.Splitter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;
import java.util.logging.Logger;

public class BlockUpdateHandler implements Listener {
    private final HomeAssistant ha;
    private final Logger logger;

    @SuppressWarnings("UnstableApiUsage")
    public static boolean isBlockPowered(Block block) {
        String blockData = block.getBlockData().getAsString();
        blockData = blockData.substring(blockData.indexOf("[") + 1);
        blockData = blockData.substring(0, blockData.indexOf("]"));

        final Map<String, String> result = Splitter.on(",")
                .trimResults()
                .withKeyValueSeparator(
                        Splitter.on("=")
                                .limit(2)
                                .trimResults())
                .split(blockData);

        return !Boolean.parseBoolean(result.get("powered"));
    }

    public BlockUpdateHandler(HomeAssistant ha) {
        this.ha = ha;
        this.logger = Logger.getLogger("Home Assistant Block Update Handler");
    }

    @EventHandler()
    public void onInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (!player.hasPermission(R.PERM_USE)) {
            event.setCancelled(true);
            return;
        }

        final Block block = event.getClickedBlock();

        if (block == null || !block.hasMetadata(R.LINK_META_KEY) || !R.inputBlocks.contains(block.getType())) {
            // Not linked or not an input block
            return;
        }

        final String haEntity = block.getMetadata(R.LINK_META_KEY).get(0).asString();
        final boolean state = isBlockPowered(block);
        ha.setState(haEntity, state);
        logger.info(String.format("%s set Home Assistant/%s = %s", player.getDisplayName(), haEntity, state ? "ON" : "OFF"));
    }
}
