package net.robiotic.mineassistant;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Plugin-wide resources
 */
abstract class R {
    // Permissions
    static final String PERM_LINK = "hass.link";
    static final String PERM_USE = "hass.use";

    /// Maximum distance a player can be standing from the block they are linking.
    static final int MAX_TARGET_DIST = 5;

    /// Block types that can be linked. Matched by name so woods added after 1.16 are included.
    static final Map<Material, LinkType> linkTypes = buildLinkTypes();

    private static Map<Material, LinkType> buildLinkTypes() {
        final Map<Material, LinkType> types = new EnumMap<>(Material.class);
        for (Material material : Material.values()) {
            if (!material.isBlock() || material.name().startsWith("LEGACY_")) {
                continue;
            }
            final LinkType type = linkTypeFor(material.name());
            if (type != null) {
                types.put(material, type);
            }
        }
        return Map.copyOf(types);
    }

    /**
     * Trapdoors match {@code _DOOR} as well as doors. Wall signs and hanging signs match {@code _SIGN}.
     */
    private static LinkType linkTypeFor(String name) {
        if (name.equals("LEVER") || name.endsWith("_DOOR") || name.endsWith("_FENCE_GATE")) {
            return LinkType.SYNC;
        }
        if (name.endsWith("_SIGN")) {
            return LinkType.OUTPUT;
        }
        if (name.endsWith("_BUTTON")
                || name.endsWith("_PRESSURE_PLATE")
                || name.equals("TRIPWIRE_HOOK")
                || name.equals("DAYLIGHT_DETECTOR")) {
            return LinkType.INPUT;
        }
        return null;
    }

    static final Map<BlockFace, String> attachmentPoints = Map.of (
            BlockFace.UP, "floor",
            BlockFace.DOWN, "ceiling",
            BlockFace.NORTH, "north",
            BlockFace.SOUTH, "south",
            BlockFace.EAST, "east",
            BlockFace.WEST, "west"
    );
    static final List<BlockFace> cardinalFaces = List.of(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);
}

enum LinkType {
    INPUT,   // MC --> HA
    OUTPUT,  // MC <-- HA
    SYNC,    // MC <-> HA
}
