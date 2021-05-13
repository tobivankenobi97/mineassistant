package net.robiotic.mineassistant;

import com.google.common.base.Splitter;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

import java.util.Map;

public class BlockUpdateHandler implements Listener {
    private final HomeAssistant ha;

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

        return Boolean.parseBoolean(result.get("powered"));
    }

    public BlockUpdateHandler(HomeAssistant ha) {
        this.ha = ha;
    }

    @EventHandler
    public void onRedstone(BlockRedstoneEvent event) { // TODO: permission
        final Block block = event.getBlock();

        if (!block.hasMetadata(R.LINK_META_KEY) || !R.inputBlocks.contains(block.getType())) {
            // Not linked or not an input block
            return;
        }

        final String haEntity = block.getMetadata(R.LINK_META_KEY).get(0).asString();
        ha.setState(haEntity, isBlockPowered(block));
    }
}
