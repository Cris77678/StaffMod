package me.drex.staffmod.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.server.level.ServerPlayer;

public class PermissionUtil {

    private static LuckPerms lp;

    private static LuckPerms lp() {
        if (lp == null) {
            try { lp = LuckPermsProvider.get(); }
            catch (Exception e) { return null; }
        }
        return lp;
    }

    public static boolean has(ServerPlayer player, String permission) {
        LuckPerms api = lp();
        if (api == null) return player.hasPermissions(2);
        var user = api.getUserManager().getUser(player.getUUID());
        if (user == null) return player.hasPermissions(2);
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    /**
     * ¿El objetivo está protegido contra acciones de staff?
     * FIX BUG 3: Ahora verifica si tiene Nivel OP 4 O el permiso de inmunidad de LuckPerms.
     */
    public static boolean isProtected(ServerPlayer target) {
        return target.hasPermissions(4) || has(target, "staffmod.protected");
    }
}
