package me.outspending.biomesapi.nms;

import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_21_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_21_R1.CraftServer;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * This class provides the implementation for the NMS interface for version 1.20_R3.
 * It provides methods to interact with the game's chunks and biome registry.
 */
public class NMS_v1_21_R1 implements NMS {

    /**
     * Retrieves the registry for a given key.
     *
     * @param key The key for the registry to retrieve.
     * @return The registry associated with the given key.
     */
    private static <T> MappedRegistry<T> getRegistry(ResourceKey<Registry<T>> key) {
        DedicatedServer server = ((CraftServer) Bukkit.getServer()).getServer();
        return (MappedRegistry<T>) server.registryAccess().registryOrThrow(key);
    }

    /**
     * Updates a list of chunks asynchronously.
     * For each chunk, it creates a packet with the chunk's data and sends it to all players within the chunk's view distance.
     *
     * @param chunks The chunks to update.
     */
    @Override
    public void updateChunks(@NotNull List<Chunk> chunks) {
        CompletableFuture.runAsync(() -> {

            for (Chunk chunk : chunks) {
                LevelChunk levelChunk = (LevelChunk) ((CraftChunk) chunk).getHandle(ChunkStatus.BIOMES);
                LevelLightEngine levelLightEngine = levelChunk.getLevel().getLightEngine();

                ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(levelChunk, levelLightEngine, null, null);
                for (Player player : getPlayersInDistance(chunk)) {
                    ((CraftPlayer) player).getHandle().connection.send(packet);
                }
            }

        });
    }

    /**
     * Locks or unlocks the biome registry.
     * It uses reflection to access the private boolean field in the registry class and sets its value.
     *
     * @param isLocked true to lock the biome registry, false to unlock it.
     */
    @Override
    public void biomeRegistryLock(boolean isLocked) {
        MappedRegistry<Biome> biomes = getRegistry(Registries.BIOME);
        try {
            Class<?> registryBiomeClass = Class.forName("net.minecraft.core.RegistryMaterials");
            for (Field field : registryBiomeClass.getDeclaredFields()) {
                if (field.getType() == boolean.class) {
                    field.setAccessible(true);
                    field.setBoolean(biomes, isLocked);
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unlocks the registry, performs an operation supplied by the supplier, and then freezes the registry.
     * This method is useful for performing operations on the registry that require it to be unlocked.
     *
     * @param supplier The supplier that provides the operation to perform on the unlocked registry.
     */
    @Override
    public void unlockRegistry(@NotNull Supplier<?> supplier) {
        MappedRegistry<Biome> registry = getRegistry(Registries.BIOME);
        biomeRegistryLock(false);
        supplier.get();
        registry.freeze();
    }

    /**
     * Retrieves the biome registry from the Minecraft server.
     *
     * This method gets the server instance from the Bukkit API, accesses the registry of the server,
     * and retrieves the biome registry. If the biome registry cannot be retrieved, it throws a RuntimeException.
     *
     * @return The biome registry from the Minecraft server.
     * @throws RuntimeException if the biome registry cannot be retrieved.
     */
    @Override
    public @NotNull Registry<Biome> getRegistry() {
        return ((CraftServer) Bukkit.getServer()).getServer()
                .registryAccess()
                .registry(Registries.BIOME)
                .orElseThrow(() -> new RuntimeException("Could not retrieve biome registry"));
    }

    @Override
    public void updateBiome(@NotNull Location minLoc, @NotNull Location maxLoc, @NotNull NamespacedKey namespacedKey) {

    }

    @Override
    public boolean setBiome(NamespacedKey newBiomeKey, Location location) {
        Biome base;
        WritableRegistry<Biome> registrywritable = getRegistry(Registries.BIOME);

        ResourceLocation biomeKey = ResourceLocation.fromNamespaceAndPath(newBiomeKey.getNamespace(), newBiomeKey.getKey());

        ResourceKey<Biome> rkey = ResourceKey.create(Registries.BIOME, biomeKey);
        base = registrywritable.get(rkey);
        if(base == null) {
            ResourceKey<Biome> newrkey = ResourceKey.create(Registries.BIOME, biomeKey);
            base = registrywritable.get(newrkey);
            if(base == null) {
                return false;
            }
        }

        setBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ(), ((CraftWorld)location.getWorld()).getHandle(), base);
        return true;
    }

    private void setBiome(int x, int y, int z, Level w, Biome bb) {
        BlockPos pos = new BlockPos(x, 0, z);

        if (w.isLoaded(pos)) {

            LevelChunk chunk = w.getChunkAt(pos);
            if (chunk != null) {

                chunk.setBiome(x >> 2, y >> 2, z >> 2, Holder.direct(bb));
            }
        }
    }

}
