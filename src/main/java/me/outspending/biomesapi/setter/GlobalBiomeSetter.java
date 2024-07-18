package me.outspending.biomesapi.setter;

import com.google.common.base.Preconditions;
import me.outspending.biomesapi.BiomeUpdater;
import me.outspending.biomesapi.biome.CustomBiome;
import me.outspending.biomesapi.misc.PointRange3D;
import me.outspending.biomesapi.nms.NMSHandler;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.NotThreadSafe;

public class GlobalBiomeSetter implements BiomeSetter {

    @SuppressWarnings("deprecation")
    private static final UnsafeValues UNSAFE = Bukkit.getUnsafe();
    private static final BiomeUpdater BIOME_UPDATER = BiomeUpdater.of();

    @Override
    public void setBlockBiome(@NotNull Block block, @NotNull CustomBiome customBiome) {
        setBlockBiome(block, customBiome, false);
    }

    @Override
    public void setBlockBiome(@NotNull Block block, @NotNull CustomBiome customBiome, boolean updateBiome) {
        Preconditions.checkNotNull(block, "block cannot be null");
        Preconditions.checkNotNull(customBiome, "customBiome cannot be null");

        Location location = block.getLocation();

        NMSHandler.executeNMS(nms -> nms.setBiome(customBiome.toNamespacedKey(), location));

        if (updateBiome) {
            BIOME_UPDATER.updateChunk(location.getChunk());
        }
    }

    @Override
    public void setChunkBiome(@NotNull Chunk chunk, @NotNull CustomBiome customBiome) {
        setChunkBiome(chunk, MIN_HEIGHT, MAX_HEIGHT, customBiome);
    }

    @Override
    public void setChunkBiome(@NotNull Chunk chunk, @NotNull CustomBiome customBiome, boolean updateBiome) {
        setChunkBiome(chunk, MIN_HEIGHT, MAX_HEIGHT, customBiome, updateBiome);
    }

    @Override
    public void setChunkBiome(@NotNull Chunk chunk, int minHeight, int maxHeight, @NotNull CustomBiome customBiome) {
        setChunkBiome(chunk, minHeight, maxHeight, customBiome, false);
    }

    @Override
    public void setChunkBiome(@NotNull Chunk chunk, int minHeight, int maxHeight, @NotNull CustomBiome customBiome, boolean updateBiome) {
        Preconditions.checkNotNull(chunk, "chunk cannot be null");
        Preconditions.checkNotNull(customBiome, "customBiome cannot be null");

        RegionAccessor accessor = chunk.getWorld();
        NamespacedKey key = customBiome.toNamespacedKey();

        int minX = chunk.getX() << 4;
        int maxX = minX + 16;

        int minZ = chunk.getZ() << 4;
        int maxZ = minZ + 16;

        for (int x = minX; x < maxX; x++) {
            for (int y = minHeight; y < maxHeight; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    // Set the biome of each block to the custom biome
                    Location location = new Location(chunk.getWorld(), x, y, z);
                    NMSHandler.executeNMS(nms -> nms.setBiome(key, location));
                }
            }
        }

        if (updateBiome) {
            BIOME_UPDATER.updateChunk(chunk);
        }
    }

    @Override
    public void setBoundingBoxBiome(@NotNull World world, @NotNull BoundingBox boundingBox, @NotNull CustomBiome customBiome) {
        setRegionBiome(world, boundingBox.getMin(), boundingBox.getMax(), customBiome);
    }

    @Override
    public void setRegionBiome(@NotNull Location from, @NotNull Location to, @NotNull CustomBiome customBiome) {
        World world = from.getWorld();
        if (!world.equals(to.getWorld())) {
            throw new IllegalArgumentException("Locations must be in the same world!");
        }

        setRegionBiome(world, from, to, customBiome, false);
    }

    @Override
    public void setRegionBiome(@NotNull Location from, @NotNull Location to, @NotNull CustomBiome customBiome, boolean updateBiome) {
        World world = from.getWorld();
        if (!world.equals(to.getWorld())) {
            throw new IllegalArgumentException("Locations must be in the same world!");
        }

        setRegionBiome(world, from, to, customBiome, updateBiome);
    }

    @Override
    public void setRegionBiome(@NotNull World world, @NotNull Vector from, @NotNull Vector to, @NotNull CustomBiome customBiome) {
        setRegionBiome(world, from, to, customBiome, false);
    }

    @Override
    public void setRegionBiome(@NotNull World world, @NotNull Vector from, @NotNull Vector to, @NotNull CustomBiome customBiome, boolean updateBiome) {
        setRegionBiome(world, from.toLocation(world), to.toLocation(world), customBiome, updateBiome);
    }

    @Override
    public void setRegionBiome(@NotNull World world, @NotNull Location from, @NotNull Location to, @NotNull CustomBiome customBiome, boolean updateBiome) {
        Preconditions.checkNotNull(world, "world cannot be null");
        Preconditions.checkNotNull(from, "from cannot be null");
        Preconditions.checkNotNull(to, "to cannot be null");
        Preconditions.checkNotNull(customBiome, "customBiome cannot be null");

        if (!from.getWorld().equals(to.getWorld())) {
            throw new RuntimeException("Locations must be in the same world!");
        } else {
            NamespacedKey key = customBiome.toNamespacedKey();
            PointRange3D range = PointRange3D.of(from, to);

            for (int x = range.minX(); x <= range.maxX(); x++) {
                for (int y = range.minY(); y <= range.maxY(); y++) {
                    for (int z = range.minZ(); z <= range.maxZ(); z++) {
                        Location location = new Location(from.getWorld(), x, y, z);
                        NMSHandler.executeNMS(nms -> nms.setBiome(key, location));
                    }
                }
            }

            if (updateBiome) {
                BIOME_UPDATER.updateChunks(from, to);
            }
        }
    }

}
