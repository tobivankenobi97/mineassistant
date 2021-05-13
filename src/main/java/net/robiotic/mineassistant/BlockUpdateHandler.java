package net.robiotic.mineassistant;

import com.google.common.base.Splitter;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

import java.util.Map;
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

        ha.setState(haEntity, Boolean.parseBoolean(result.get("powered")));
    }
}
