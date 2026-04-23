package me.drex.staffmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.drex.staffmod.config.DataStore;
import me.drex.staffmod.config.TicketEntry;
import me.drex.staffmod.util.PermissionUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class TicketCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ticket")
            .then(Commands.argument("mensaje", StringArgumentType.greedyString())
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    
                    // FIX BUG 2: Prevenir SPAM comprobando si ya tiene un ticket activo
                    boolean hasOpenTicket = DataStore.getAllTickets().stream()
                        .anyMatch(t -> t.creatorUuid.equals(player.getUUID()) && t.status.equals("ABIERTO"));
                    
                    if (hasOpenTicket) {
                        player.sendSystemMessage(Component.literal("§c[Tickets] Ya tienes un ticket abierto. Por favor espera a que un staff lo atienda."));
                        return 1;
                    }

                    String message = StringArgumentType.getString(ctx, "mensaje");
                    TicketEntry ticket = DataStore.createTicket(player.getUUID(), player.getName().getString(), message);
                    
                    player.sendSystemMessage(Component.literal("§a[Tickets] Tu ticket (#" + ticket.id + ") ha sido enviado al staff."));

                    for (ServerPlayer p : player.getServer().getPlayerList().getPlayers()) {
                        if (PermissionUtil.has(p, "staffmod.use") && DataStore.isOnDuty(p.getUUID())) {
                            p.sendSystemMessage(Component.literal("§e[Tickets] §fNuevo ticket de §b" + player.getName().getString() + "§f: " + message));
                            p.sendSystemMessage(Component.literal("§7(Revisa el panel /staff para atenderlo)"));
                        }
                    }

                    return 1;
                }))
        );
    }
}
