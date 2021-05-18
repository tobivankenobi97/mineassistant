package net.robiotic.mineassistant;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.Switch;
import org.bukkit.block.data.type.TrapDoor;

import java.util.logging.Logger;

public abstract class BlockUtil {
    private static final Logger logger = Logger.getLogger("BlockUtil");

    public static boolean isPowered(Block block) {
        if (block.getBlockData() instanceof Powerable) {
            final BlockState blockState = block.getState();
            final Powerable powerable = (Powerable) blockState.getBlockData();
            // the result of isPowered is opposite to the actual powered status, so invert it
            return !powerable.isPowered();
        }

        return false;       // This is a bit weird but I guess if it isn't powereble, it isn't powered either
    }

    public static void setPowered(Block block, boolean powered) {
        final BlockState blockState = block.getState();
        final Powerable powerable = (Powerable) blockState.getBlockData();
        powerable.setPowered(powered);
        blockState.setBlockData(powerable);
        blockState.update();
    }

    public static boolean isOpen(Block block) {
        final BlockState blockState = block.getState();
        final Openable openable = (Openable) blockState.getBlockData();
        return openable.isOpen();
    }

    public static void setOpen(Block block, boolean open) {
        final BlockState blockState = block.getState();
        final Openable openable = (Openable) blockState.getBlockData();
        openable.setOpen(open);
        blockState.setBlockData(openable);
        blockState.update();
    }

    /**
     * Retrieve the value of a tag in the specified BlockData.
     * ** maybe not needed any more? **
     *
     * @param bd  BlockData object to search
     * @param tag tag to return
     * @return the value of the specified tag. Null if the tag is not found.
     */
    public static String getBlockDataTag(BlockData bd, String tag) {
        String bdAsString = bd.getAsString();
        int valuePos = bdAsString.indexOf(tag + "=") + (tag + "=").length();
        if (valuePos == -1) return null;        // cannot find tag
        return bdAsString.substring(valuePos).split("[,\\]]")[0];
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
    public static BlockData setBlockDataTag(BlockData bd, String tag, String value) {
        String newBDString = bd.getAsString();
        if (!newBDString.matches(".*" + tag + "=.+?(?=[,\\]]).*")) return bd;
        newBDString = newBDString.replaceAll(tag + "=.+?(?=[,\\]])", tag + "=" + value);
        return Bukkit.createBlockData(newBDString);
    }

    public static void makeSound(Block block, boolean newState) {
        final BlockData data = block.getBlockData();
        Sound sound = null;

        if (data instanceof Door) {
            if (block.getType() == Material.IRON_DOOR) {
                sound = newState ? Sound.BLOCK_IRON_DOOR_OPEN : Sound.BLOCK_IRON_DOOR_CLOSE;
            } else {
                sound = newState ? Sound.BLOCK_WOODEN_DOOR_OPEN : Sound.BLOCK_WOODEN_DOOR_CLOSE;
            }
        } else if (data instanceof TrapDoor) {
            if (block.getType() == Material.IRON_TRAPDOOR) {
                sound = newState ? Sound.BLOCK_IRON_TRAPDOOR_OPEN : Sound.BLOCK_IRON_TRAPDOOR_CLOSE;
            } else {
                sound = newState ? Sound.BLOCK_WOODEN_TRAPDOOR_OPEN : Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE;
            }
        } else if (data instanceof Gate) {
            sound = newState ? Sound.BLOCK_FENCE_GATE_OPEN : Sound.BLOCK_FENCE_GATE_CLOSE;
        } else if (data instanceof Switch) {
            sound = Sound.BLOCK_LEVER_CLICK;
        }

        if (sound == null) {
            sound = newState ? Sound.BLOCK_WOODEN_TRAPDOOR_OPEN : Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE;
            logger.warning(String.format(
                    "Missing state update sound for block = %s, newState = %s",
                    block.getBlockData().getClass().getName(),
                    newState
            ));
        }

        block.getWorld().playSound(block.getLocation(), sound, SoundCategory.BLOCKS, 1, 0);
    }
}
