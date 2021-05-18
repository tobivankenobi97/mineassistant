package net.robiotic.mineassistant;

import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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
     * Detect a block being broken by a player. If the block is linked to HA, remove it from the link store.
     *
     * @param event The block break event
     */
    @EventHandler()
    public void onBlockBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();
        final String haEntity = links.getEntity(block);

        if (haEntity == null) {
            return; // Not linked
        }

        links.remove(new BlockLocation(block));
        event.getPlayer().sendMessage(String.format(
                "%s was broken and unlinked from '%s'",
                block.getBlockData().getMaterial().name(),
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
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return; // Only respond to "use" actions
        }

        if (!mode.equals("interact")) {
            return; // Not in the 'interact' input mode
        }

        final Player player = event.getPlayer();

        if (!player.hasPermission(R.PERM_USE)) {
            event.setCancelled(true);
            return;
        }

        final Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        final String haEntity = links.getEntity(block);

        if (haEntity == null) {
            return; // Not linked
        }

        if (!haEntity.contains("sensor.")) {
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

        if (linkType != LinkType.INPUT) {
            return; // Not an input block
        }

        final String haEntity = links.getEntity(block);

        if (haEntity == null) {
            return; // Not linked
        }

        ha.setState(haEntity, BlockUtil.isPowered(block));
    }
}
