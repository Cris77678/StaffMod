package me.drex.staffmod.features;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BuilderManager {

    // Guarda los inventarios de supervivencia mientras están en modo Builder
    private static final Map<UUID, ListTag> savedInventories = new ConcurrentHashMap<>();

    public static boolean isBuilderMode(UUID uuid) {
        return savedInventories.containsKey(uuid);
    }

    public static void toggleBuilderMode(ServerPlayer player) {
        UUID uuid = player.getUUID();

        if (isBuilderMode(uuid)) {
            // SALIR DEL MODO BUILDER
            player.getInventory().clearContent(); // Borramos los ítems creativos
            
            // Restauramos el inventario de supervivencia
            ListTag savedInv = savedInventories.remove(uuid);
            player.getInventory().load(savedInv);
            
            player.setGameMode(GameType.SURVIVAL);
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
            
            player.sendSystemMessage(Component.literal("§c[Builder] Has salido del modo constructor. Inventario restaurado."));
        } else {
            // ENTRAR AL MODO BUILDER
            // Guardamos el inventario actual (Supervivencia)
            ListTag currentInv = new ListTag();
            player.getInventory().save(currentInv);
            savedInventories.put(uuid, currentInv);
            
            // Limpiamos y damos creativo
            player.getInventory().clearContent();
            player.setGameMode(GameType.CREATIVE);
            
            player.sendSystemMessage(Component.literal("§a[Builder] Modo constructor activado."));
            player.sendSystemMessage(Component.literal("§eAtención: §fLos ítems peligrosos (Bedrock, Command Blocks) están bloqueados."));
        }
    }
}