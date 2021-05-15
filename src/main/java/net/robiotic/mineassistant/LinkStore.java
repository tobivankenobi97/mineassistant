package net.robiotic.mineassistant;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.block.Block;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LinkStore {
    private static final Type storedType = new TypeToken<ArrayList<LinkedBlock>>() {
    }.getType();

    private final Logger logger = Logger.getLogger("Home Assistant Link Store");
    private final File file;
    private final Gson gson = new Gson();
    private Map<BlockLocation, LinkedBlock> blocks;

    public LinkStore(File folder) {
        file = new File(folder, "links.json");
    }

    /**
     * Read blocks list from disk.
     */
    public void read() {
        try {
            final FileReader reader = new FileReader(file);
            final ArrayList<LinkedBlock> values = gson.fromJson(reader, storedType);
            blocks = values.stream().collect(Collectors.toMap(lb -> lb.location, lb -> lb));
            reader.close();
        } catch (IOException exception) {
            if (!(exception instanceof FileNotFoundException)) {
                logger.warning("Could not read Home Assistant links: " + exception);
            }

            blocks = new HashMap<>();
        }
    }

    /**
     * Save the blocks list to disk.
     */
    public void commit() {
        try {
            final FileWriter writer = new FileWriter(file);
            gson.toJson(blocks.values(), writer);
            writer.close();
        } catch (IOException exception) {
            logger.warning("Could not commit Home Assistant links: " + exception);
        }
    }

    /**
     * Operate on all blocks assigned to a specific entity.
     *
     * @param haEntity The ID of the Home Assistant entity
     */
    public void linkedBlocks(String haEntity, Consumer<LinkedBlock> action) {
        for (LinkedBlock block : blocks.values()) {
            if (block.entity.equals(haEntity)) {
                action.accept(block);
            }
        }
    }

    /**
     * Get the linked entity for a block at a specific location.
     *
     * @param block the block to get the entity for
     * @return null if not linked or the string entity ID if linked
     */
    public String getEntity(Block block) {
        final BlockLocation location = new BlockLocation(block);
        final LinkedBlock link = blocks.get(location);

        if (link == null) {
            return null; // This block is not linked
        }

        if (!link.material.equals(block.getType().name())) {
            // The material doesn't match, so the block was replaced
            remove(link);
            return null;
        }

        return link.entity;
    }

    /**
     * Add a new link
     *
     * @param block    The in-world block
     * @param haEntity The Home Assistant entity ID
     */
    public void add(Block block, String haEntity) {
        final LinkedBlock link = new LinkedBlock(block, haEntity);
        blocks.put(link.location, link);
        commit();
    }

    /**
     * Remove an existing link.
     *
     * @param link The link object to remove
     */
    public void remove(LinkedBlock link) {
        blocks.remove(link.location);
        commit();
    }

    // remove an existing link by location only
    public void remove(BlockLocation blockLocation) {
        blocks.remove(blockLocation);
        commit();
    }
}

class LinkedBlock implements Serializable {
    BlockLocation location;
    String material;
    String entity;

    /**
     * Create a link between a block and an entity.
     *
     * @param block  The in-world block
     * @param entity The Home Assistant entity ID
     */
    LinkedBlock(Block block, String entity) {
        location = new BlockLocation(block);
        material = block.getType().name();
        this.entity = entity;
    }
}

class BlockLocation implements Serializable {
    UUID world;
    int x, y, z;

    BlockLocation(Block block) {
        world = block.getWorld().getUID();
        x = block.getX();
        y = block.getY();
        z = block.getZ();
    }

    /**
     * Uniquely identify this location.
     */
    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BlockLocation && hashCode() == other.hashCode();
    }
}
