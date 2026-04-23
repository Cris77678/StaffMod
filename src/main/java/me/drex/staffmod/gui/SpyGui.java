package me.drex.staffmod.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Muestra el inventario completo del target (36 slots + armadura + offhand).
 * Con staffmod.spy.interact permite mover/quitar items.
 */
public class SpyGui extends SimpleGui {

    private final ServerPlayer staff;
    private final ServerPlayer target;
    private final boolean canInteract;

    public SpyGui(ServerPlayer staff, ServerPlayer target) {
        super(MenuType.GENERIC_9x5, staff, false);
        this.staff       = staff;
        this.target      = target;
        this.canInteract = me.drex.staffmod.util.PermissionUtil.has(staff, "staffmod.spy.interact");
        setTitle(Component.literal("§d[Spy] §f" + target.getName().getString()
            + (canInteract ? " §7(R+W)" : " §7(Solo ver)")));
        build();
    }

    private void build() {
        var inv = target.getInventory();

        // Slots 0-35 → inventario principal
        for (int i = 0; i < 36; i++) {
            final int slot = i;
            ItemStack item = inv.getItem(i);
            if (item.isEmpty()) {
                setSlot(i, new GuiElementBuilder(Items.AIR).build());
            } else {
                var builder = GuiElementBuilder.from(item.copy())
                    .addLoreLine(Component.literal("§8Slot inventario: " + i));
                if (canInteract) {
                    builder.setCallback((idx, type, action, gui) -> {
                        // Intercambiar item entre staff y target
                        ItemStack staffHand = staff.getMainHandItem().copy();
                        ItemStack targetSlotItem = inv.getItem(slot).copy();
                        inv.setItem(slot, staffHand);
                        staff.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, targetSlotItem);
                    });
                }
                setSlot(i, builder.build());
            }
        }

        // Slots 36-39 → armadura (casco, pecho, pantalón, botas)
        String[] armorNames = {"Botas", "Pantalón", "Pechera", "Casco"};
        for (int i = 0; i < 4; i++) {
            ItemStack armor = inv.armor.get(i);
            int guiSlot = 36 + i;
            if (!armor.isEmpty()) {
                setSlot(guiSlot, GuiElementBuilder.from(armor.copy())
                    .addLoreLine(Component.literal("§8Armadura: " + armorNames[i]))
                    .build());
            }
        }

        // Slot 40 → offhand
        ItemStack offhand = inv.offhand.get(0);
        if (!offhand.isEmpty()) {
            setSlot(40, GuiElementBuilder.from(offhand.copy())
                .addLoreLine(Component.literal("§8Mano secundaria"))
                .build());
        }

        // Botón cerrar
        setSlot(44, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§cCerrar"))
            .setCallback((idx, type, action, gui) -> this.close())
            .build());
    }
}
