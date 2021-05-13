package net.robiotic.mineassistant;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

import java.util.logging.Logger;

public class BlockUpdateHandler implements Listener {
    private final HomeAssistant ha;
    private final Logger logger;

    public BlockUpdateHandler(HomeAssistant ha) {
        this.ha = ha;
        logger = Logger.getLogger("Home Assistant Block Update Handler");
    }

    @EventHandler
    public void onRedstone(BlockRedstoneEvent event) { // TODO: permission
        final Block block = event.getBlock();

        if (!block.hasMetadata(R.LINK_META_KEY) || !R.inputBlocks.contains(block.getType())) {
            // Not linked or not an input block
            return;
        }

        final String haEntity = block.getMetadata(R.LINK_META_KEY).get(0).asString();
        ha.setState(haEntity, block.isBlockPowered()); // TODO: only reads false?
    }
}
