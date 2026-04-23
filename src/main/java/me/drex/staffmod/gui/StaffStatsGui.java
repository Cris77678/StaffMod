package me.drex.staffmod.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.staffmod.config.DataStore;
import me.drex.staffmod.config.StaffProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

public class StaffStatsGui extends SimpleGui {

    private final SimpleGui parent;

    public StaffStatsGui(ServerPlayer staff, SimpleGui parent) {
        super(MenuType.GENERIC_9x6, staff, false);
        this.parent = parent;
        setTitle(Component.literal("§8» §6Estadísticas de Staff"));
        build();
    }

    private void build() {
        int slot = 0;
        
        for (StaffProfile sp : DataStore.allStaffProfiles()) {
            if (slot >= 53) break; // Límite visual de la GUI
            
            GuiElementBuilder builder = new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("§b§l" + sp.name))
                .addLoreLine(Component.literal("§7Bans: §c" + sp.bans))
                .addLoreLine(Component.literal("§7Mutes: §e" + sp.mutes))
                .addLoreLine(Component.literal("§7Warns: §a" + sp.warns))
                .addLoreLine(Component.literal("§7Jails: §6" + sp.jails))
                .addLoreLine(Component.literal("§7Kicks: §f" + sp.kicks))
                .addLoreLine(Component.literal(""));
            
            builder.addLoreLine(Component.literal("§e§nÚltimas Acciones:"));
            if (sp.recentHistory.isEmpty()) {
                builder.addLoreLine(Component.literal("§7(Sin acciones recientes)"));
            } else {
                for (String action : sp.recentHistory) {
                    builder.addLoreLine(Component.literal(action));
                }
            }

            setSlot(slot++, builder.build());
        }

        // Botón volver
        setSlot(53, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§7← Volver"))
            .setCallback((idx, type, action, gui) -> parent.open())
            .build());
    }
}
