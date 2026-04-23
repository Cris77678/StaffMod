package me.drex.staffmod.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Versión de solo lectura del spy — no permite mover items.
 */
public class SpyReadOnlyGui extends SimpleGui {

    public SpyReadOnlyGui(ServerPlayer staff, ServerPlayer target) {
        super(MenuType.GENERIC_9x5, staff, false);
        setTitle(Component.literal("§d[Spy - Solo ver] §f" + target.getName().getString()));

        var inv = target.getInventory();

        for (int i = 0; i < 36; i++) {
            ItemStack item = inv.getItem(i);
            if (!item.isEmpty()) {
                setSlot(i, GuiElementBuilder.from(item.copy())
                    .addLoreLine(Component.literal("§8Slot: " + i))
                    .setCallback((idx, type, action, gui) -> {}) // bloquear click
                    .build());
            }
        }

        String[] armorNames = {"Botas", "Pantalón", "Pechera", "Casco"};
        for (int i = 0; i < 4; i++) {
            ItemStack armor = inv.armor.get(i);
            if (!armor.isEmpty()) {
                setSlot(36 + i, GuiElementBuilder.from(armor.copy())
                    .addLoreLine(Component.literal("§8Armadura: " + armorNames[i]))
                    .setCallback((idx, type, action, gui) -> {})
                    .build());
            }
        }

        ItemStack offhand = inv.offhand.get(0);
        if (!offhand.isEmpty()) {
            setSlot(40, GuiElementBuilder.from(offhand.copy())
                .addLoreLine(Component.literal("§8Mano secundaria"))
                .setCallback((idx, type, action, gui) -> {})
                .build());
        }

        setSlot(44, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§cCerrar"))
            .setCallback((idx, type, action, gui) -> this.close())
            .build());
    }
}
