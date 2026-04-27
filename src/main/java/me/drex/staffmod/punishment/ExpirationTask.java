package me.drex.staffmod.punishment;

import me.drex.staffmod.cache.PlayerCache;
import me.drex.staffmod.config.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class ExpirationTask implements Runnable {

    private final MinecraftServer server;

    public ExpirationTask(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();

        // Iteramos los jugadores online de forma segura
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerData pd = PlayerCache.getIfPresent(player.getUUID());
            if (pd == null) continue;

            boolean changed = false;

            // Revisar expiración de Mute
            if (pd.muted && pd.muteExpiry != -1 && now >= pd.muteExpiry) {
                pd.muted = false;
                changed = true;
                player.sendSystemMessage(Component.literal("§a[StaffMod] Tu mute temporal ha expirado. Ya puedes hablar de nuevo."));
            }

            // Revisar expiración de Cárcel (Jail)
            if (pd.jailed && pd.jailExpiry != -1 && now >= pd.jailExpiry) {
                pd.jailed = false;
                pd.jailName = "";
                changed = true;
                player.sendSystemMessage(Component.literal("§a[StaffMod] Has cumplido tu tiempo en prisión. Eres libre."));
                
                // IMPORTANTE: El teletransporte SIEMPRE debe hacerse en el hilo principal
                server.execute(() -> {
                    var overworld = server.overworld();
                    var spawn = overworld.getSharedSpawnPos();
                    player.teleportTo(overworld, spawn.getX(), spawn.getY(), spawn.getZ(), player.getYRot(), player.getXRot());
                });
            }

            if (changed) {
                PlayerCache.savePlayer(pd);
            }
        }
    }
}