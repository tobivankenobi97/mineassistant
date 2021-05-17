package net.robiotic.mineassistant;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;

public abstract class BlockUtil {
    public static boolean isPowered(Block block) {
        if (block.getBlockData() instanceof Powerable) {
            BlockState blockState = block.getState();
            Powerable powerable = (Powerable) blockState.getBlockData();
            // the result of isPowered is opposite to the actual powered status, so invert it
            return !powerable.isPowered();
        }

        return false;       // This is a bit weird but I guess if it isn't powereble, it isn't powered either
    }

    public static void setPowered(Block block, boolean powered) {
        BlockState blockState = block.getState();
        Powerable powerable = (Powerable) blockState.getBlockData();
        powerable.setPowered(powered);
        blockState.setBlockData(powerable);
        blockState.update();
    }

    public static boolean isOpen(Block block) {
        BlockState blockState = block.getState();
        Openable openable = (Openable) blockState.getBlockData();
        return openable.isOpen();
    }

    public static void setOpen(Block block, boolean open) {
        BlockState blockState = block.getState();
        Openable openable = (Openable) blockState.getBlockData();
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
}
