package net.robiotic.mineassistant;

import org.bukkit.Material;

import java.util.Map;

import static java.util.Map.entry;

/**
 * Plugin-wide resources
 */
abstract class R {
    // Permissions
    static final String PERM_LINK = "hass.link";
    static final String PERM_USE = "hass.use";

    /// Maximum distance a player can be standing from the block they are linking.
    static final int MAX_TARGET_DIST = 5;

    /// The block types that can be linked to Home Assistant.
    @SuppressWarnings("RedundantTypeArguments")
    static final Map<Material, LinkType> linkTypes = Map.<Material, LinkType>ofEntries(
            entry(Material.LEVER, LinkType.SYNC),
            entry(Material.TRIPWIRE_HOOK, LinkType.INPUT),
            entry(Material.CAULDRON, LinkType.INPUT),

            // Buttons
            entry(Material.BIRCH_BUTTON, LinkType.INPUT),
            entry(Material.ACACIA_BUTTON, LinkType.INPUT),
            entry(Material.CRIMSON_BUTTON, LinkType.INPUT),
            entry(Material.DARK_OAK_BUTTON, LinkType.INPUT),
            entry(Material.JUNGLE_BUTTON, LinkType.INPUT),
            entry(Material.OAK_BUTTON, LinkType.INPUT),
            entry(Material.POLISHED_BLACKSTONE_BUTTON, LinkType.INPUT),
            entry(Material.SPRUCE_BUTTON, LinkType.INPUT),
            entry(Material.STONE_BUTTON, LinkType.INPUT),
            entry(Material.WARPED_BUTTON, LinkType.INPUT),

            // Pressure plates
            entry(Material.ACACIA_PRESSURE_PLATE, LinkType.INPUT),
            entry(Material.BIRCH_PRESSURE_PLATE, LinkType.INPUT),
            entry(Material.CRIMSON_PRESSURE_PLATE, LinkType.INPUT),
            entry(Material.DARK_OAK_PRESSURE_PLATE, LinkType.INPUT),
            entry(Material.HEAVY_WEIGHTED_PRESSURE_PLATE, LinkType.INPUT),
            entry(Material.JUNGLE_PRESSURE_PLATE, LinkType.INPUT),
            entry(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, LinkType.INPUT),
            entry(Material.OAK_PRESSURE_PLATE, LinkType.INPUT),
            entry(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE, LinkType.INPUT),
            entry(Material.SPRUCE_PRESSURE_PLATE, LinkType.INPUT),
            entry(Material.STONE_PRESSURE_PLATE, LinkType.INPUT),
            entry(Material.WARPED_PRESSURE_PLATE, LinkType.INPUT),

            // Signs
            entry(Material.ACACIA_SIGN, LinkType.OUTPUT),
            entry(Material.BIRCH_SIGN, LinkType.OUTPUT),
            entry(Material.CRIMSON_SIGN, LinkType.OUTPUT),
            entry(Material.DARK_OAK_SIGN, LinkType.OUTPUT),
            entry(Material.JUNGLE_SIGN, LinkType.OUTPUT),
            entry(Material.OAK_SIGN, LinkType.OUTPUT),
            entry(Material.SPRUCE_SIGN, LinkType.OUTPUT),
            entry(Material.WARPED_SIGN, LinkType.OUTPUT),
            entry(Material.ACACIA_WALL_SIGN, LinkType.OUTPUT),
            entry(Material.BIRCH_WALL_SIGN, LinkType.OUTPUT),
            entry(Material.CRIMSON_WALL_SIGN, LinkType.OUTPUT),
            entry(Material.DARK_OAK_WALL_SIGN, LinkType.OUTPUT),
            entry(Material.JUNGLE_WALL_SIGN, LinkType.OUTPUT),
            entry(Material.OAK_WALL_SIGN, LinkType.OUTPUT),
            entry(Material.SPRUCE_WALL_SIGN, LinkType.OUTPUT),
            entry(Material.WARPED_WALL_SIGN, LinkType.OUTPUT),

            // Doors
            entry(Material.ACACIA_DOOR, LinkType.SYNC),
            entry(Material.BIRCH_DOOR, LinkType.SYNC),
            entry(Material.CRIMSON_DOOR, LinkType.SYNC),
            entry(Material.DARK_OAK_DOOR, LinkType.SYNC),
            entry(Material.IRON_DOOR, LinkType.SYNC),
            entry(Material.JUNGLE_DOOR, LinkType.SYNC),
            entry(Material.OAK_DOOR, LinkType.SYNC),
            entry(Material.SPRUCE_DOOR, LinkType.SYNC),
            entry(Material.WARPED_DOOR, LinkType.SYNC),

            // Trapdoors
            entry(Material.ACACIA_TRAPDOOR, LinkType.SYNC),
            entry(Material.BIRCH_TRAPDOOR, LinkType.SYNC),
            entry(Material.CRIMSON_TRAPDOOR, LinkType.SYNC),
            entry(Material.DARK_OAK_TRAPDOOR, LinkType.SYNC),
            entry(Material.IRON_TRAPDOOR, LinkType.SYNC),
            entry(Material.JUNGLE_TRAPDOOR, LinkType.SYNC),
            entry(Material.OAK_TRAPDOOR, LinkType.SYNC),
            entry(Material.SPRUCE_TRAPDOOR, LinkType.SYNC),
            entry(Material.WARPED_TRAPDOOR, LinkType.SYNC),

            // Fence gates
            entry(Material.ACACIA_FENCE_GATE, LinkType.SYNC),
            entry(Material.BIRCH_FENCE_GATE, LinkType.SYNC),
            entry(Material.CRIMSON_FENCE_GATE, LinkType.SYNC),
            entry(Material.DARK_OAK_FENCE_GATE, LinkType.SYNC),
            entry(Material.JUNGLE_FENCE_GATE, LinkType.SYNC),
            entry(Material.OAK_FENCE_GATE, LinkType.SYNC),
            entry(Material.SPRUCE_FENCE_GATE, LinkType.SYNC),
            entry(Material.WARPED_FENCE_GATE, LinkType.SYNC)
    );
}

enum LinkType {
    INPUT,   // MC --> HA
    OUTPUT,  // MC <-- HA
    SYNC,    // MC <-> HA
}
