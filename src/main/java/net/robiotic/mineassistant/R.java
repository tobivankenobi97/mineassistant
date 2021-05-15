package net.robiotic.mineassistant;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Plugin-wide resources
 */
abstract class R {
    // Permissions
    static final String PERM_LINK = "hass.link";
    static final String PERM_USE = "hass.use";

    /// Maximum distance a player can be standing from the block they are linking.
    static final int MAX_TARGET_DIST = 5;

    /// The block types that can be linked as inputs to Home Assistant.
    static final HashSet<Material> inputBlocks = new HashSet<>(Arrays.asList(
            // Inputs
            Material.LEVER,
            Material.BIRCH_BUTTON,
            Material.ACACIA_BUTTON,
            Material.CRIMSON_BUTTON,
            Material.DARK_OAK_BUTTON,
            Material.JUNGLE_BUTTON,
            Material.OAK_BUTTON,
            Material.POLISHED_BLACKSTONE_BUTTON,
            Material.SPRUCE_BUTTON,
            Material.STONE_BUTTON,
            Material.WARPED_BUTTON,
            Material.ACACIA_PRESSURE_PLATE,
            Material.BIRCH_PRESSURE_PLATE,
            Material.CRIMSON_PRESSURE_PLATE,
            Material.DARK_OAK_PRESSURE_PLATE,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Material.JUNGLE_PRESSURE_PLATE,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
            Material.OAK_PRESSURE_PLATE,
            Material.POLISHED_BLACKSTONE_PRESSURE_PLATE,
            Material.SPRUCE_PRESSURE_PLATE,
            Material.STONE_PRESSURE_PLATE,
            Material.WARPED_PRESSURE_PLATE,
            Material.TARGET,
            Material.TRIPWIRE_HOOK

    ));

    /// The block types that can be linked as outputs of Home Assistant.
    static final HashSet<Material> outputBlocks = new HashSet<>(Arrays.asList(
            Material.PISTON,
            Material.STICKY_PISTON,
            Material.REDSTONE_LAMP,
            Material.DISPENSER,
            Material.DROPPER,
            Material.NOTE_BLOCK,
            Material.BELL,
            Material.ACACIA_SIGN,
            Material.BIRCH_SIGN,
            Material.CRIMSON_SIGN,
            Material.DARK_OAK_SIGN,
            Material.JUNGLE_SIGN,
            Material.OAK_SIGN,
            Material.SPRUCE_SIGN,
            Material.WARPED_SIGN,
            Material.ACACIA_WALL_SIGN,
            Material.BIRCH_WALL_SIGN,
            Material.CRIMSON_WALL_SIGN,
            Material.DARK_OAK_WALL_SIGN,
            Material.JUNGLE_WALL_SIGN,
            Material.OAK_WALL_SIGN,
            Material.SPRUCE_WALL_SIGN,
            Material.WARPED_WALL_SIGN,
            Material.ACACIA_DOOR,
            Material.BIRCH_DOOR,
            Material.CRIMSON_DOOR,
            Material.DARK_OAK_DOOR,
            Material.IRON_DOOR,
            Material.JUNGLE_DOOR,
            Material.OAK_DOOR,
            Material.SPRUCE_DOOR,
            Material.WARPED_DOOR,
            Material.ACACIA_TRAPDOOR,
            Material.BIRCH_TRAPDOOR,
            Material.CRIMSON_TRAPDOOR,
            Material.DARK_OAK_TRAPDOOR,
            Material.IRON_TRAPDOOR,
            Material.JUNGLE_TRAPDOOR,
            Material.OAK_TRAPDOOR,
            Material.SPRUCE_TRAPDOOR,
            Material.WARPED_TRAPDOOR,
            Material.ACACIA_FENCE_GATE,
            Material.BIRCH_FENCE_GATE,
            Material.CRIMSON_FENCE_GATE,
            Material.DARK_OAK_FENCE_GATE,
            Material.JUNGLE_FENCE_GATE,
            Material.OAK_FENCE_GATE,
            Material.SPRUCE_FENCE_GATE,
            Material.WARPED_FENCE_GATE

    ));
}
