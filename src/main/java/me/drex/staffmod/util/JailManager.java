package me.drex.staffmod.util;

import me.drex.staffmod.config.DataStore;
import me.drex.staffmod.config.JailZone;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JailManager {

    // Selecciones pos1/pos2 en progreso por jugador staff
    private static final Map<UUID, BlockPos> pos1 = new HashMap<>();
    private static final Map<UUID, BlockPos> pos2 = new HashMap<>();

    public static void setPos1(UUID staffUuid, BlockPos pos) { pos1.put(staffUuid, pos); }
    public static void setPos2(UUID staffUuid, BlockPos pos) { pos2.put(staffUuid, pos); }
    public static BlockPos getPos1(UUID staffUuid) { return pos1.get(staffUuid); }
    public static BlockPos getPos2(UUID staffUuid) { return pos2.get(staffUuid); }

    /** Crea la zona de jail con las posiciones seleccionadas. Devuelve error o null si OK. */
    public static String createJail(ServerPlayer staff, String name) {
        BlockPos p1 = pos1.get(staff.getUUID());
        BlockPos p2 = pos2.get(staff.getUUID());
        if (p1 == null || p2 == null) return "Debes seleccionar pos1 y pos2 primero.";
        String dim = staff.level().dimension().location().toString();
        JailZone zone = new JailZone(name, dim,
            p1.getX(), p1.getY(), p1.getZ(),
            p2.getX(), p2.getY(), p2.getZ());
        DataStore.addJail(zone);
        pos1.remove(staff.getUUID());
        pos2.remove(staff.getUUID());
        return null;
    }

    /** Teleporta al jugador al centro de la cárcel indicada. */
    public static boolean teleportToJail(ServerPlayer player, String jailName) {
        JailZone zone = DataStore.getJail(jailName);
        if (zone == null) {
            // Si no existe la zona, usar la primera disponible
            zone = DataStore.getJails().values().stream().findFirst().orElse(null);
            if (zone == null) return false;
        }
        player.teleportTo(
            player.getServer().overworld(),
            zone.spawnX, zone.spawnY, zone.spawnZ,
            player.getYRot(), player.getXRot());
        return true;
    }

    /**
     * Verifica si un jugador jaileado salió de su zona y lo devuelve.
     * Llamado desde MovementMixin.
     */
    public static void checkJailBounds(ServerPlayer player) {
        var pd = DataStore.get(player.getUUID());
        if (pd == null || !pd.isJailActive()) return;
        JailZone zone = DataStore.getJail(pd.jailName);
        if (zone == null) return;
        Vec3 pos = player.position();
        if (!zone.contains(pos.x, pos.y, pos.z)) {
            double[] clamped = zone.clamp(pos.x, pos.y, pos.z);
            player.teleportTo(clamped[0], clamped[1], clamped[2]);
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("§c[Staff] No puedes salir de la cárcel."));
        }
    }
}
