package me.drex.staffmod.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.staffmod.config.DataStore;
import me.drex.staffmod.config.TicketEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Nueva versión de TicketGui que extiende PaginatedGui.
 * Soporta cientos de tickets dividiéndolos en páginas automáticamente.
 */
public class TicketGui extends PaginatedGui<TicketEntry> {

    private final ServerPlayer staff;

    public TicketGui(ServerPlayer staff, SimpleGui parent) {
        super(staff, parent, Component.literal("§8❖ §6Gestión de Tickets §8❖"), fetchTickets());
        this.staff = staff;
        build();
    }

    /**
     * Obtiene los tickets y los invierte para que los más nuevos aparezcan primero.
     */
    private static List<TicketEntry> fetchTickets() {
        List<TicketEntry> tickets = new ArrayList<>(DataStore.getAllTickets());
        Collections.reverse(tickets);
        return tickets;
    }

    /**
     * Define cómo se dibuja visualmente un ticket individual.
     */
    @Override
    protected GuiElementBuilder buildItem(TicketEntry t) {
        boolean isOpen = t.status.equals("ABIERTO");
        
        GuiElementBuilder builder = new GuiElementBuilder(isOpen ? Items.PAPER : Items.MAP)
            .setName(Component.literal((isOpen ? "§a§l" : "§e§l") + "Ticket #" + t.id + " - " + t.creatorName));
            
        // Formateo del mensaje para que no se salga de la pantalla
        String msg = t.message;
        int maxLineLength = 35;
        for (int i = 0; i < msg.length(); i += maxLineLength) {
            String chunk = msg.substring(i, Math.min(msg.length(), i + maxLineLength));
            builder.addLoreLine(Component.literal((i == 0 ? "§7Mensaje: §f" : "§f") + chunk));
        }

        builder.addLoreLine(Component.literal("§7Estado: " + (isOpen ? "§aABIERTO" : "§eTOMADO por " + t.handledBy)));
        builder.addLoreLine(Component.literal(""));

        if (isOpen) {
            builder.addLoreLine(Component.literal("§eClick Izquierdo: §fTomar ticket"));
        }
        builder.addLoreLine(Component.literal("§cClick Derecho: §fCerrar ticket"));

        // Manejo de clics
        builder.setCallback((idx, type, action, gui) -> {
            if (type.isRight) {
                DataStore.removeTicket(t.id);
                staff.sendSystemMessage(Component.literal("§a[Tickets] Ticket #" + t.id + " cerrado."));
            } else if (type.isLeft && isOpen) {
                t.status = "TOMADO";
                t.handledBy = staff.getName().getString();
                DataStore.save();
                staff.sendSystemMessage(Component.literal("§a[Tickets] Has tomado el ticket #" + t.id + "."));
            }
            
            // Refrescar los datos y redibujar la página actual
            this.data = fetchTickets();
            build();
        });

        return builder;
    }
    
    // Sobrescribimos build() solo para añadir el mensaje de "Vacío" si no hay datos
    @Override
    protected void build() {
        if (data.isEmpty()) {
            super.build(); // Dibuja la barra inferior
            setSlot(22, new GuiElementBuilder(Items.GLASS_BOTTLE)
                .setName(Component.literal("§aNo hay tickets pendientes."))
                .build());
        } else {
            super.build(); // Dibuja la página normal
        }
    }
}