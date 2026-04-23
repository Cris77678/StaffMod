package me.drex.staffmod.gui;

import me.drex.staffmod.config.DataStore;
import me.drex.staffmod.config.PlayerData;
import me.drex.staffmod.util.JailManager;
import me.drex.staffmod.util.PermissionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSources;

public class ActionExecutor {

    public static void kick(ServerPlayer staff, ServerPlayer target, String reason) {
        if (guard(staff, target)) return;
        target.connection.disconnect(Component.literal("§cHas sido expulsado.\n§fRazón: §e" + reason));
        broadcast(staff, "§c[Staff] §f" + staff.getName().getString()
            + " §ckickeó §fa §f" + target.getName().getString() + "§7. Razón: " + reason);
    }

    public static void mute(ServerPlayer staff, ServerPlayer target, String duration, String reason) {
        if (guard(staff, target)) return;
        PlayerData pd = DataStore.getOrCreate(target.getUUID(), target.getName().getString());
        pd.muted      = true;
        pd.muteExpiry = PlayerData.parseDuration(duration);
        DataStore.save();
        target.sendSystemMessage(Component.literal(
            "§c[Staff] Has sido muteado.\n§fRazón: §e" + reason
            + "\n§fDuración: §e" + PlayerData.formatExpiry(pd.muteExpiry)));
        broadcast(staff, "§e[Staff] §f" + staff.getName().getString()
            + " §emuteó §fa §f" + target.getName().getString()
            + "§7. Duración: " + duration + " | Razón: " + reason);
    }

    public static void unmute(ServerPlayer staff, ServerPlayer target) {
        PlayerData pd = DataStore.get(target.getUUID());
        if (pd == null || !pd.muted) {
            staff.sendSystemMessage(Component.literal("§c[Staff] Ese jugador no está muteado."));
            return;
        }
        pd.muted = false; pd.muteExpiry = -1;
        DataStore.save();
        target.sendSystemMessage(Component.literal("§a[Staff] Tu mute ha sido removido."));
        staff.sendSystemMessage(Component.literal("§a[Staff] Mute removido a " + target.getName().getString()));
    }

    public static void jail(ServerPlayer staff, ServerPlayer target, String jailName, String duration) {
        if (guard(staff, target)) return;
        PlayerData pd = DataStore.getOrCreate(target.getUUID(), target.getName().getString());
        pd.jailed     = true;
        pd.jailName   = jailName;
        pd.jailExpiry = PlayerData.parseDuration(duration);
        DataStore.save();
        JailManager.teleportToJail(target, jailName);
        target.sendSystemMessage(Component.literal(
            "§c[Staff] Has sido enviado a la cárcel §f" + jailName
            + "§c.\n§fDuración: §e" + PlayerData.formatExpiry(pd.jailExpiry)));
        broadcast(staff, "§6[Staff] §f" + staff.getName().getString()
            + " §6jaileó §fa §f" + target.getName().getString()
            + "§7. Cárcel: " + jailName + " | Duración: " + duration);
    }

    public static void unjail(ServerPlayer staff, ServerPlayer target) {
        PlayerData pd = DataStore.get(target.getUUID());
        if (pd == null || !pd.jailed) {
            staff.sendSystemMessage(Component.literal("§c[Staff] Ese jugador no está jaileado."));
            return;
        }
        pd.jailed = false; pd.jailExpiry = -1; pd.jailName = "";
        DataStore.save();
        var overworld = target.getServer().overworld();
        var spawn = overworld.getSharedSpawnPos();
        target.teleportTo(overworld, spawn.getX(), spawn.getY(), spawn.getZ(),
            target.getYRot(), target.getXRot());
        target.sendSystemMessage(Component.literal("§a[Staff] Has sido liberado de la cárcel."));
        staff.sendSystemMessage(Component.literal("§a[Staff] " + target.getName().getString() + " liberado."));
    }

    public static void ban(ServerPlayer staff, ServerPlayer target, String duration, String reason) {
        if (guard(staff, target)) return;
        PlayerData pd = DataStore.getOrCreate(target.getUUID(), target.getName().getString());
        pd.banned    = true;
        pd.banExpiry = PlayerData.parseDuration(duration);
        pd.banReason = reason;
        DataStore.save();
        String expStr = PlayerData.formatExpiry(pd.banExpiry);
        target.connection.disconnect(Component.literal(
            "§cHas sido baneado.\n§fRazón: §e" + reason + "\n§fExpira: §e" + expStr));
        broadcast(staff, "§4[Staff] §f" + staff.getName().getString()
            + " §cbaneó §fa §f" + target.getName().getString()
            + "§7. Duración: " + duration + " | Razón: " + reason);
    }

    public static void unban(ServerPlayer staff, ServerPlayer target) {
        PlayerData pd = DataStore.get(target.getUUID());
        if (pd == null || !pd.banned) {
            staff.sendSystemMessage(Component.literal("§c[Staff] Ese jugador no está baneado."));
            return;
        }
        pd.banned = false; pd.banExpiry = -1; pd.banReason = "";
        DataStore.save();
        staff.sendSystemMessage(Component.literal("§a[Staff] Ban removido a " + target.getName().getString()));
    }

    public static void freeze(ServerPlayer staff, ServerPlayer target) {
        if (guard(staff, target)) return;
        PlayerData pd = DataStore.getOrCreate(target.getUUID(), target.getName().getString());
        pd.frozen = !pd.frozen;
        DataStore.save();
        if (pd.frozen) {
            target.sendSystemMessage(Component.literal("§b[Staff] Has sido congelado. No puedes moverte."));
            staff.sendSystemMessage(Component.literal("§b[Staff] " + target.getName().getString() + " congelado."));
        } else {
            target.sendSystemMessage(Component.literal("§a[Staff] Has sido descongelado."));
            staff.sendSystemMessage(Component.literal("§a[Staff] " + target.getName().getString() + " descongelado."));
        }
    }

    public static void spy(ServerPlayer staff, ServerPlayer target) {
        if (PermissionUtil.has(staff, "staffmod.spy.interact")) {
            new SpyGui(staff, target).open();
        } else {
            new SpyReadOnlyGui(staff, target).open();
        }
    }

    public static void warn(ServerPlayer staff, ServerPlayer target, String reason) {
        if (guard(staff, target)) return;
        PlayerData pd = DataStore.getOrCreate(target.getUUID(), target.getName().getString());
        pd.warns.add(new PlayerData.WarnEntry(reason, System.currentTimeMillis(), staff.getName().getString()));
        DataStore.save();
        target.sendSystemMessage(Component.literal(
            "§c[Staff] Has recibido una advertencia (#" + pd.warns.size() + ").\n§fRazón: §e" + reason));
        staff.sendSystemMessage(Component.literal(
            "§a[Staff] Advertencia enviada a " + target.getName().getString()
            + " (total: " + pd.warns.size() + ")"));
    }

    public static void teleport(ServerPlayer staff, ServerPlayer target) {
        staff.teleportTo(
            (net.minecraft.server.level.ServerLevel) target.level(),
            target.getX(), target.getY(), target.getZ(),
            staff.getYRot(), staff.getXRot());
        staff.sendSystemMessage(Component.literal(
            "§3[Staff] Teleportado a §f" + target.getName().getString()));
    }

    public static void kill(ServerPlayer staff, ServerPlayer target) {
        if (guard(staff, target)) return;
        target.hurt(target.damageSources().genericKill(), Float.MAX_VALUE);
        staff.sendSystemMessage(Component.literal("§c[Staff] Mataste a " + target.getName().getString()));
        broadcast(staff, "§c[Staff] §f" + staff.getName().getString()
            + " §cmató §fa §f" + target.getName().getString());
    }

    private static boolean guard(ServerPlayer staff, ServerPlayer target) {
        if (PermissionUtil.isProtected(target)) {
            staff.sendSystemMessage(Component.literal(
                "§c[Staff] No puedes realizar esta acción sobre un administrador."));
            return true;
        }
        return false;
    }

    // FASE 1: Notificar solo a staff activos
    private static void broadcast(ServerPlayer staff, String message) {
        for (ServerPlayer p : staff.getServer().getPlayerList().getPlayers()) {
            if (PermissionUtil.has(p, "staffmod.use") && DataStore.isOnDuty(p.getUUID())) {
                p.sendSystemMessage(Component.literal(message));
            }
        }
    }
}
