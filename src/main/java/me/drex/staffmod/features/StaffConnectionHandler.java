package me.drex.staffmod.features;

import me.drex.staffmod.config.DataStore;
import me.drex.staffmod.config.RankConfig;
import me.drex.staffmod.config.RankManager;
import me.drex.staffmod.util.PermissionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import me.drex.staffmod.core.StaffModAsync;

import java.util.concurrent.TimeUnit;

public class StaffConnectionHandler {

    public static void onStaffJoin(ServerPlayer staff) {
        // Verificamos si es parte del equipo usando el RankManager
        RankConfig rank = RankManager.getHighestRank(staff);
        if (rank == null) return; // No es staff

        // Activamos su "Duty" (Turno) por defecto al entrar
        DataStore.setDuty(staff.getUUID(), true);

        // 1. Anuncio en el chat para todos (Opcional, depende del rango)
        String joinMessage = "§8[§bStaff§8] " + rank.color + rank.prefix + " " + staff.getName().getString() + " §eha entrado y está disponible.";
        for (ServerPlayer p : staff.getServer().getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.literal(joinMessage));
        }

        // 2. Retrasamos el resumen 3 segundos para que el jugador termine de cargar el mundo
        StaffModAsync.scheduleAsync(() -> {
            long openTickets = DataStore.getAllTickets().stream().filter(t -> t.status.equals("ABIERTO")).count();
            
            staff.sendSystemMessage(Component.literal("§8======================================="));
            staff.sendSystemMessage(Component.literal("§6§l¡Bienvenido a tu turno, " + staff.getName().getString() + "!"));
            staff.sendSystemMessage(Component.literal(" "));
            
            if (openTickets > 0) {
                staff.sendSystemMessage(Component.literal("§c⚠ Tienes §l" + openTickets + "§c tickets pendientes de revisión."));
                staff.sendSystemMessage(Component.literal("§7Usa §e/staff §7y abre el menú de tickets para atenderlos."));
            } else {
                staff.sendSystemMessage(Component.literal("§a✔ No hay tickets pendientes. ¡Buen trabajo!"));
            }
            
            staff.sendSystemMessage(Component.literal("§8======================================="));
        }, 3, 0, TimeUnit.SECONDS);
    }
}