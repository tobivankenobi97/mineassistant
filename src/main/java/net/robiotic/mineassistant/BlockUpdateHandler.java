package net.robiotic.mineassistant;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.logging.Logger;

public class BlockUpdateHandler implements Listener {
    private final HomeAssistant ha;
    private final Logger logger;
    private final String mode;
    private final LinkStore links;

    public BlockUpdateHandler(HomeAssistant ha, String mode, LinkStore links) {
        this.ha = ha;
        this.logger = Logger.getLogger("Home Assistant Block Update Handler");
        this.mode = mode;
        this.links = links;
    }

    public void setPowered(Block block, boolean powered) {
        BlockState blockState = block.getState();
        Powerable powerable = (Powerable) blockState.getBlockData();
        powerable.setPowered(powered);
        blockState.setBlockData(powerable);
        blockState.update();
    }

    public boolean isPowered(Block block) {
        if (block.getBlockData() instanceof Powerable) {
            BlockState blockState = block.getState();
            Powerable powerable = (Powerable) blockState.getBlockData();
            // the result of isPowered is opposite to the actual powered status, so invert it
            return !powerable.isPowered();
        }
        return false;       // This is a bit weird but I guess if it isn't powereble, it isn't powered either
    }

    public void setOpen(Block block, boolean open) {
        BlockState blockState = block.getState();
        Openable openable = (Openable) blockState.getBlockData();
        openable.setOpen(open);
        blockState.setBlockData(openable);
        blockState.update();
    }

    public boolean isOpen(Block block) {
        BlockState blockState = block.getState();
        Openable openable = (Openable) blockState.getBlockData();
        return openable.isOpen();
    }

    /**
     * Set the value of an existing tag in the specified BlockData.
     * ** maybe not needed any more? **
     *
     * @param bd    the BlockData object to operate on
     * @param tag   the tag whose value we want to update
     * @param value the new value for the tag
     * @return an updated BlockData object; if the tag is not found, just returns the original BlockData
     */
    public BlockData setBlockDataTag(BlockData bd, String tag, String value) {
        String newBDString = bd.getAsString();
        if (!newBDString.matches(".*" + tag + "=.+?(?=[,\\]]).*")) return bd;
        newBDString = newBDString.replaceAll(tag + "=.+?(?=[,\\]])", tag + "=" + value);
        return Bukkit.createBlockData(newBDString);
    }

    /**
     * Retrieve the value of a tag in the specified BlockData.
     * ** maybe not needed any more? **
     *
     * @param bd  BlockData object to search
     * @param tag tag to return
     * @return the value of the specified tag. Null if the tag is not found.
     */
    public String getBlockDataTag(BlockData bd, String tag) {
        String bdAsString = bd.getAsString();
        int valuePos = bdAsString.indexOf(tag + "=") + (tag + "=").length();
        if (valuePos == -1) return null;        // cannot find tag
        return bdAsString.substring(valuePos).split("[,\\]]")[0];
    }

    /**
     * Unlink any linked blocks in a list, and log a reason why
     *
     * @param blockList a list of blocks to search through.
     * @param reason    a text description of why we are removing blocks (e.g. "explosion")
     */
    public void removeMatchingBlocks(List<Block> blockList, String reason) {
        for (Block block : blockList) {
            String entityID = links.getEntity(block);
            if (entityID != null) {
                // this block is in the link store, remove it
                BlockLocation bl = new BlockLocation(block);
                links.remove(bl);
                logger.info(String.format(
                        "%s was unlinked from '%s' because %s",
                        block.getBlockData().getMaterial().name(),
                        entityID,
                        reason
                ));
            }
        }
    }

    /**
     * Detect explosions caused by an entity (Creeper, TNT etc)
     *
     * @param event The explosion event
     */
    @EventHandler()
    public void onEntityExplode(EntityExplodeEvent event) {
        removeMatchingBlocks(event.blockList(), event.getEntityType().name() + " blew it up");
    }

    /**
     * Detect explosions caused by an unknown source
     * Can't find any info on if/when this gets called, but it's here for safety
     *
     * @param event The explosion event
     */
    @EventHandler()
    public void onBlockExplode(BlockExplodeEvent event) {
        removeMatchingBlocks(event.blockList(), "An unknown source blew it up");
    }

    /**
     * Detect a block being broken by a player. If the block is linked to HA, remove it from the link store.
     *
     * @param event The block break event
     */
    @EventHandler()
    public void onBlockBreak(BlockBreakEvent event) {
        if (!mode.equals("interact")) {
            return; // Not in the 'interact' input mode
        }

        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (!R.inputBlocks.contains(block.getType()) && !R.outputBlocks.contains(block.getType())) {
            return; // Not an input or output block
        }

        final String haEntity = links.getEntity(block);

        if (haEntity == null) {
            return; // Not linked
        }

        BlockLocation brokenBlockLocation = new BlockLocation(block);
        links.remove(brokenBlockLocation);
        event.getPlayer().sendMessage(String.format(
                "%s was broken by %s and unlinked from '%s'",
                block.getBlockData().getMaterial().name(),
                player.getDisplayName(),
                haEntity
        ));
    }

    /**
     * Detect updates when in 'interact' mode.
     *
     * @param event The player interaction
     */
    @EventHandler()
    public void onInteract(PlayerInteractEvent event) {

        if (!mode.equals("interact")) {
            return; // Not in the 'interact' input mode
        }

        final Player player = event.getPlayer();

        if (!player.hasPermission(R.PERM_USE)) {
            event.setCancelled(true);
            return;
        }

        final Block block = event.getClickedBlock();

        if (block == null || (!R.inputBlocks.contains(block.getType()) && !R.outputBlocks.contains(block.getType()))) {
            return; // Not an input or output block
        }

        final String haEntity = links.getEntity(block);

        if (haEntity == null) {
            return; // Not linked
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {        // only respond to "use" actions
            final BlockState bs = block.getState();
            if (block.getBlockData() instanceof Openable) {
                    /*
                    do nothing for now, but we should potentially cancel this event as it will cause
                    the MC door to go out of sync with the real world door
                    */

            } else {
                // set power on/off
                final boolean state = isPowered(block);
                ha.setState(haEntity, state);
                logger.info(String.format("%s set Home Assistant/%s = %s", player.getDisplayName(), haEntity, state ? "ON" : "OFF"));
            }
        }
    }

    /**
     * Detect updates when in 'redstone' mode.
     *
     * @param event The redstone update
     */
    @EventHandler()
    public void onRedstone(BlockRedstoneEvent event) {
        if (!mode.equals("redstone")) {
            return; // Not in the 'redstone' input mode
        }

        final Block block = event.getBlock();

        if (!R.inputBlocks.contains(block.getType())) {
            return; // Not an input block
        }

        final String haEntity = links.getEntity(block);

        if (haEntity == null) {
            return; // Not linked
        }

        ha.setState(haEntity, isPowered(block));
    }
}
