package me.drex.staffmod.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.server.level.ServerPlayer;

/**
 * Utilidad de permisos con LuckPerms.
 *
 * Nodos de permiso:
 *   staffmod.use          - Abrir el panel /staff
 *   staffmod.kick         - Kickear jugadores
 *   staffmod.mute         - Mutear jugadores
 *   staffmod.unmute       - Desmutear jugadores
 *   staffmod.jail         - Jailear jugadores
 *   staffmod.unjail       - Desajailear jugadores
 *   staffmod.jail.manage  - Crear/eliminar zonas de cárcel
 *   staffmod.ban          - Banear jugadores
 *   staffmod.unban        - Desbanear jugadores
 *   staffmod.freeze       - Congelar jugadores
 *   staffmod.spy          - Ver inventario (modo solo lectura)
 *   staffmod.spy.interact - Interactuar con el inventario del jugador
 *   staffmod.warn         - Advertir jugadores
 *   staffmod.teleport     - Teleportar jugadores
 *   staffmod.kill         - Matar jugadores
 */
public class PermissionUtil {

    private static LuckPerms lp;

    private static LuckPerms lp() {
        if (lp == null) {
            try { lp = LuckPermsProvider.get(); }
            catch (Exception e) { return null; }
        }
        return lp;
    }

    /** ¿El jugador tiene el permiso dado? Fallback: nivel OP. */
    public static boolean has(ServerPlayer player, String permission) {
        LuckPerms api = lp();
        if (api == null) return player.hasPermissions(2);
        var user = api.getUserManager().getUser(player.getUUID());
        if (user == null) return player.hasPermissions(2);
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    /**
     * ¿El objetivo está protegido contra acciones de staff?
     * Un jugador con OP (nivel 4) no puede ser afectado por staff.
     */
    public static boolean isProtected(ServerPlayer target) {
        return target.hasPermissions(4);
    }
}
