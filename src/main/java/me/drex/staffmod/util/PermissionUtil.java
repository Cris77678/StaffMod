package me.drex.staffmod.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.server.level.ServerPlayer;
import me.drex.staffmod.StaffMod;

public class PermissionUtil {

    private static LuckPerms api;

    public static void init() {
        try {
            api = LuckPermsProvider.get();
            StaffMod.LOGGER.info("[StaffMod] LuckPerms hook activado correctamente.");
        } catch (IllegalStateException e) {
            StaffMod.LOGGER.error("[StaffMod CRÍTICO] LuckPerms NO ENCONTRADO. El mod requiere LuckPerms de forma obligatoria.");
        }
    }

    /**
     * Verifica permisos usando la memoria ultra-rápida de LuckPerms.
     */
    public static boolean has(ServerPlayer player, String permission) {
        if (api == null) return player.hasPermissions(2); // Fallback extremo
        
        try {
            User user = api.getUserManager().getUser(player.getUUID());
            if (user != null) {
                return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
            }
        } catch (Exception e) {
            StaffMod.LOGGER.error("[StaffMod] Fallo al consultar LuckPerms para {}", player.getName().getString());
        }
        return false;
    }

    /**
     * ¿El objetivo está protegido contra acciones de staff?
     * Totalmente basado en nodos en lugar del OP level.
     */
    public static boolean isProtected(ServerPlayer target) {
        return has(target, "staffcore.protected");
    }
}