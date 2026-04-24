package me.drex.staffmod.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.staffmod.config.DataStore;
import me.drex.staffmod.config.TicketEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TicketGui extends SimpleGui {

    private final SimpleGui parent;
    private final ServerPlayer staff;

    public TicketGui(ServerPlayer staff, SimpleGui parent) {
        super(MenuType.GENERIC_9x6, staff, false);
        this.staff = staff;
        this.parent = parent;
        setTitle(Component.literal("§8» §6Gestión de Tickets"));
        build();
    }

    private void build() {
        List<TicketEntry> tickets = new ArrayList<>(DataStore.getAllTickets());
        
        Collections.reverse(tickets);
        
        if (tickets.isEmpty()) {
            setSlot(22, new GuiElementBuilder(Items.GLASS_BOTTLE)
                .setName(Component.literal("§aNo hay tickets pendientes."))
                .build());
        }

        int slot = 0;
        for (TicketEntry t : tickets) {
            if (slot >= 53) break;

            boolean isOpen = t.status.equals("ABIERTO");
            
            GuiElementBuilder builder = new GuiElementBuilder(isOpen ? Items.PAPER : Items.MAP)
                .setName(Component.literal((isOpen ? "§a§l" : "§e§l") + "Ticket #" + t.id + " - " + t.creatorName));
                
            // FIX: Dividir el texto en líneas de 35 caracteres para que no salga de la pantalla
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
                
                for (int i = 0; i < getSize(); i++) setSlot(i, new GuiElementBuilder(Items.AIR).build());
                build();
            });

            setSlot(slot++, builder.build());
        }

        setSlot(53, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§7← Volver"))
            .setCallback((idx, type, action, gui) -> parent.open())
            .build());
    }
}