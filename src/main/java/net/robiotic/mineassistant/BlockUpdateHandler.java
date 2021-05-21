package net.robiotic.mineassistant;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.*;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Locale;
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

    /**
     * Detect explosions caused by an entity (Creeper, TNT etc)
     *
     * @param event The explosion event
     */
    @EventHandler()
    public void onEntityExplode(EntityExplodeEvent event) {
        links.removeMatchingBlocks(event.blockList(), event.getEntityType().name() + " blew it up");
    }

    /**
     * Detect explosions caused by an unknown source
     * Can't find any info on if/when this gets called, but it's here for safety
     *
     * @param event The explosion event
     */
    @EventHandler()
    public void onBlockExplode(BlockExplodeEvent event) {
        links.removeMatchingBlocks(event.blockList(), "An unknown source blew it up");
    }

    /**
     * Find out if one block face has something attached to it (button, lever etc).
     *
     * @param anchorBlock The block with the potential attachments.
     * @param bf The face of the block to check (up, down, north, south, east, west).
     * @return true if the block has an attachment.
     */
    public boolean isAttached(Block anchorBlock, BlockFace bf) {
        Block adjacentBlock = anchorBlock.getRelative(bf); // get the block we need to check
        if (FaceAttachable.class.isAssignableFrom(adjacentBlock.getBlockData().getClass())) {
            // it's an "attachable"
            String face = BlockUtil.getBlockDataTag(adjacentBlock.getBlockData(), "face");        // floor, ceiling, wall
            String facing = BlockUtil.getBlockDataTag(adjacentBlock.getBlockData(), "facing");    // north, south, east, west
            // detect top and bottom first, we don't care about the compass direction for these
            if (face.equals("floor") && bf.equals(BlockFace.UP)) return true;
            if (face.equals("ceiling") && bf.equals(BlockFace.DOWN)) return true;
            // detect compass directions; face=="wall" for all of these so look at the "facing" tag instead
            return facing.equals(bf.name().toLowerCase());
        } else if (adjacentBlock.getBlockData().getMaterial().toString().endsWith("_WALL_SIGN")) {
            // wall signs can only be facing N/S/E/W, check if that matches our anchor block
            String facing = BlockUtil.getBlockDataTag(adjacentBlock.getBlockData(), "facing");    // north, south, east, west
            return facing.equals(bf.name().toLowerCase());
        } else if (adjacentBlock.getBlockData().getMaterial().toString().endsWith("_SIGN")) {
            // signs can only be on top
            return bf == BlockFace.UP;
        }
        return false;
    }

    /**
     * Detect a block being broken by a player. If the block is linked to HA, remove it from the link store.
     *
     * @param event The block break event
     */
    @EventHandler()
    public void onBlockBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();
        final String haEntity = links.getEntity(block);

        // Check for any attached blocks that will be broken as well (these don't raise a break event of their own)
        for (BlockFace bf : R.attachmentPoints.keySet()) {
            if (isAttached(block, bf)) {
                final String attachedEntity = links.getEntity(block.getRelative(bf));
                if (attachedEntity != null) {
                    // Attached block is in the link store - remove it
                    BlockLocation bLoc = new BlockLocation(block.getRelative(bf));
                    links.remove(bLoc);
                    event.getPlayer().sendMessage(String.format(
                            "'%s' unlinked from %s",
                            attachedEntity,
                            block.getRelative(bf).getBlockData().getMaterial().name().toLowerCase().replace("_", " ")
                    ));
                }
            }
        }

        if (haEntity == null) {
            return; // Not linked
        }

        links.remove(new BlockLocation(block));
        event.getPlayer().sendMessage(String.format(
                "'%s' unlinked from %s",
                haEntity,
                block.getBlockData().getMaterial().name().toLowerCase().replace("_", " ")
        ));
    }

    /**
     * Detect updates when in 'interact' mode.
     *
     * @param event The player interaction
     */
    @EventHandler()
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return; // Only respond to "use" actions
        }

        if (!mode.equals("interact")) {
            return; // Not in the 'interact' input mode
        }

        final Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        final Player player = event.getPlayer();

        if (!player.hasPermission(R.PERM_USE)) {
            event.setCancelled(true);
            return;
        }

        final String haEntity = links.getEntity(block);

        if (haEntity == null) {
            return; // Not linked
        }

        if (haEntity.contains("sensor.")) {
            event.setCancelled(true);
            return;
        }

        if (block.getBlockData() instanceof Powerable) {
            // set power on/off
            final boolean state = BlockUtil.isPowered(block);
            ha.setState(haEntity, state);
            logger.info(String.format("%s set Home Assistant/%s = %s", player.getDisplayName(), haEntity, state ? "ON" : "OFF"));
        } else if (block.getBlockData() instanceof Openable) {
            final boolean state = BlockUtil.isOpen(block);
            ha.setState(haEntity, state);
            logger.info(String.format("%s set Home Assistant/%s = %s", player.getDisplayName(), haEntity, state ? "OPEN" : "CLOSED"));
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
        final LinkType linkType = R.linkTypes.get(block.getType());

        if (linkType != LinkType.INPUT && linkType != LinkType.SYNC) {
            return; // Not an input block
        }

        final String haEntity = links.getEntity(block);

        if (haEntity == null) {
            return; // Not linked
        }

        ha.setState(haEntity, BlockUtil.isPowered(block));
    }
}
