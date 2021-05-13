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

    /// Name of the block metadata property containing the Home Assistant entity that block is linked to.
    static final String LINK_META_KEY = "hass:entity";

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
            Material.BELL
    ));
}
