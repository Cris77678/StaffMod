package me.drex.staffmod.features;

import com.google.gson.reflect.TypeToken;
import me.drex.staffmod.StaffMod;
import me.drex.staffmod.config.Kit;
import me.drex.staffmod.storage.DataHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class KitManager {

    private static final Path KITS_FILE = FabricLoader.getInstance().getConfigDir().resolve("staffmod/kits.json");
    private static final Path COOLDOWNS_FILE = FabricLoader.getInstance().getConfigDir().resolve("staffmod/kit_cooldowns.json");

    private static Map<String, Kit> loadedKits = new ConcurrentHashMap<>();
    // Mapa: UUID_Jugador -> (Mapa: ID_Kit -> Timestamp_Próximo_Reclamo)
    private static Map<UUID, Map<String, Long>> playerCooldowns = new ConcurrentHashMap<>();

    public static void load() {
        Type kitMapType = new TypeToken<Map<String, Kit>>(){}.getType();
        Map<String, Kit> savedKits = DataHandler.loadSafe(KITS_FILE, kitMapType);
        if (savedKits != null) loadedKits.putAll(savedKits);

        Type cooldownType = new TypeToken<Map<UUID, Map<String, Long>>>(){}.getType();
        Map<UUID, Map<String, Long>> savedCooldowns = DataHandler.loadSafe(COOLDOWNS_FILE, cooldownType);
        if (savedCooldowns != null) playerCooldowns.putAll(savedCooldowns);

        StaffMod.LOGGER.info("[StaffMod] {} kits cargados exitosamente.", loadedKits.size());
    }

    public static void saveAllAsync() {
        DataHandler.saveAsync(loadedKits, KITS_FILE);
        DataHandler.saveAsync(playerCooldowns, COOLDOWNS_FILE);
    }

    public static Collection<Kit> getAllKits() {
        return loadedKits.values();
    }

    public static Kit getKit(String id) {
        return loadedKits.get(id.toLowerCase());
    }

    /**
     * Serializa un inventario de la GUI (NonNullList) a un String Base64 limpio.
     */
    public static String serializeItems(NonNullList<ItemStack> items, net.minecraft.core.HolderLookup.Provider provider) {
        try {
            CompoundTag tag = new CompoundTag();
            ContainerHelper.saveAllItems(tag, items, provider);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NbtIo.writeCompressed(tag, baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            StaffMod.LOGGER.error("Error serializando kit:", e);
            return "";
        }
    }

    /**
     * Deserializa el String Base64 de vuelta a los ítems originales.
     */
    public static NonNullList<ItemStack> deserializeItems(String base64, int size, net.minecraft.core.HolderLookup.Provider provider) {
        NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
        if (base64 == null || base64.isEmpty()) return items;

        try {
            byte[] data = Base64.getDecoder().decode(base64);
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            CompoundTag tag = NbtIo.readCompressed(bais, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            ContainerHelper.loadAllItems(tag, items, provider);
        } catch (Exception e) {
            StaffMod.LOGGER.error("Error deserializando kit:", e);
        }
        return items;
    }

    // --- LÓGICA DE RECLAMO Y COOLDOWNS ---

    public static boolean isOnCooldown(ServerPlayer player, Kit kit) {
        Map<String, Long> pCooldowns = playerCooldowns.getOrDefault(player.getUUID(), new HashMap<>());
        long expiry = pCooldowns.getOrDefault(kit.id, 0L);
        return System.currentTimeMillis() < expiry;
    }

    public static long getRemainingCooldown(ServerPlayer player, Kit kit) {
        Map<String, Long> pCooldowns = playerCooldowns.getOrDefault(player.getUUID(), new HashMap<>());
        long expiry = pCooldowns.getOrDefault(kit.id, 0L);
        return Math.max(0, expiry - System.currentTimeMillis());
    }

    public static void setCooldown(ServerPlayer player, Kit kit) {
        playerCooldowns.computeIfAbsent(player.getUUID(), k -> new HashMap<>())
            .put(kit.id, System.currentTimeMillis() + (kit.cooldownSeconds * 1000L));
        saveAllAsync();
    }

    public static void createOrUpdateKit(Kit kit) {
        loadedKits.put(kit.id.toLowerCase(), kit);
        saveAllAsync();
    }
}