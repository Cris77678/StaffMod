package me.drex.staffmod.features;

import me.drex.staffmod.util.PermissionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VanishManager {

    private static final Set<UUID> vanishedPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }

    public static void toggleVanish(ServerPlayer staff) {
        UUID uuid = staff.getUUID();
        boolean isNowVanished = !vanishedPlayers.contains(uuid);

        if (isNowVanished) {
            vanishedPlayers.add(uuid);
            staff.setInvisible(true);
            hidePlayerFromOthers(staff);
            staff.sendSystemMessage(Component.literal("§a[Vanish] Estás invisible y oculto del tabulador."));
        } else {
            vanishedPlayers.remove(uuid);
            staff.setInvisible(false);
            showPlayerToOthers(staff);
            staff.sendSystemMessage(Component.literal("§c[Vanish] Vuelves a ser visible."));
        }
    }

    private static void hidePlayerFromOthers(ServerPlayer staff) {
        // Paquete que le dice al cliente: "Borra a este jugador del mundo"
        var packet = new ClientboundPlayerInfoRemovePacket(Collections.singletonList(staff.getUUID()));
        for (ServerPlayer player : staff.getServer().getPlayerList().getPlayers()) {
            if (!PermissionUtil.has(player, "staffmod.use") && !player.getUUID().equals(staff.getUUID())) {
                player.connection.send(packet);
            }
        }
    }

    private static void showPlayerToOthers(ServerPlayer staff) {
        // Paquete que vuelve a inicializar al jugador en los clientes
        var packet = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(Collections.singletonList(staff));
        for (ServerPlayer player : staff.getServer().getPlayerList().getPlayers()) {
            if (!PermissionUtil.has(player, "staffmod.use") && !player.getUUID().equals(staff.getUUID())) {
                player.connection.send(packet);
            }
        }
    }
}