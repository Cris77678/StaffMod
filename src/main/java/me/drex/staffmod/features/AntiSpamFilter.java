package me.drex.staffmod.features;

import me.drex.staffmod.cache.PlayerCache;
import me.drex.staffmod.config.PlayerData;
import me.drex.staffmod.util.PermissionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AntiSpamFilter {

    private static final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> spamWarnings = new ConcurrentHashMap<>();

    /**
     * @return TRUE si el mensaje es válido, FALSE si se bloquea por spam.
     */
    public static boolean checkChat(ServerPlayer player, String message) {
        // Los miembros del staff no son afectados por el anti-spam
        if (PermissionUtil.has(player, "staffmod.use")) return true;

        UUID uuid = player.getUUID();
        long now = System.currentTimeMillis();
        long last = lastMessageTime.getOrDefault(uuid, 0L);

        // Límite: Mínimo 1.5 segundos entre mensajes
        if (now - last < 1500) {
            int warnings = spamWarnings.getOrDefault(uuid, 0) + 1;
            spamWarnings.put(uuid, warnings);

            if (warnings >= 3) {
                // Auto-Mute de 5 minutos
                PlayerData pd = PlayerCache.getOrCreate(uuid, player.getName().getString());
                pd.muted = true;
                pd.muteExpiry = now + (5L * 60L * 1000L);
                PlayerCache.savePlayer(pd);
                
                player.sendSystemMessage(Component.literal("§c[AutoMod] Has sido muteado por 5 minutos debido a SPAM excesivo."));
                spamWarnings.put(uuid, 0); // Reiniciamos contador
                
                // Avisar al Staff
                notifyStaff(player.getServer(), player.getName().getString());
            } else {
                player.sendSystemMessage(Component.literal("§c[AutoMod] Estás enviando mensajes muy rápido. Advertencia " + warnings + "/3."));
            }
            return false;
        }

        lastMessageTime.put(uuid, now);
        
        // Si escribe bien, reducimos sus advertencias pasadas
        if (now - last > 5000 && spamWarnings.containsKey(uuid)) {
            spamWarnings.put(uuid, Math.max(0, spamWarnings.get(uuid) - 1));
        }
        return true;
    }

    private static void notifyStaff(net.minecraft.server.MinecraftServer server, String playerName) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (PermissionUtil.has(p, "staffmod.use")) {
                p.sendSystemMessage(Component.literal("§8[§cAlert§8] §eAutoMod silenció a §f" + playerName + " §epor SPAM."));
            }
        }
    }
}