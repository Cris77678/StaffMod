package me.drex.staffmod.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
                        // FIX: Interactuar con el ratón (cursor) y no con la mano en el mundo
                        ItemStack cursorItem = staff.containerMenu.getCarried();
                        ItemStack targetSlotItem = inv.getItem(slot).copy();
                        
                        inv.setItem(slot, cursorItem);
                        staff.containerMenu.setCarried(targetSlotItem);
                        
                        // Refrescar GUI para evitar glitches visuales
                        this.close();
                        new SpyGui(staff, target).open();
                    });
                }
                setSlot(i, builder.build());
            }
        }

        // Slots 36-39 → armadura (casco, pecho, pantalón, botas)
        String[] armorNames = {"Botas", "Pantalón", "Pechera", "Casco"};
        for (int i = 0; i < 4; i++) {
            final int armorSlot = i;
            ItemStack armor = inv.armor.get(i);
            int guiSlot = 36 + i;
            
            if (!armor.isEmpty()) {
                var builder = GuiElementBuilder.from(armor.copy())
                    .addLoreLine(Component.literal("§8Armadura: " + armorNames[i]));
                    
                if (canInteract) {
                    builder.setCallback((idx, type, action, gui) -> {
                        ItemStack cursorItem = staff.containerMenu.getCarried();
                        ItemStack targetArmorItem = inv.armor.get(armorSlot).copy();
                        inv.armor.set(armorSlot, cursorItem);
                        staff.containerMenu.setCarried(targetArmorItem);
                        this.close();
                        new SpyGui(staff, target).open();
                    });
                }
                setSlot(guiSlot, builder.build());
            }
        }

        // Slot 40 → offhand
        ItemStack offhand = inv.offhand.get(0);
        if (!offhand.isEmpty()) {
            var builder = GuiElementBuilder.from(offhand.copy())
                .addLoreLine(Component.literal("§8Mano secundaria"));
                
            if (canInteract) {
                builder.setCallback((idx, type, action, gui) -> {
                    ItemStack cursorItem = staff.containerMenu.getCarried();
                    ItemStack targetOffhandItem = inv.offhand.get(0).copy();
                    inv.offhand.set(0, cursorItem);
                    staff.containerMenu.setCarried(targetOffhandItem);
                    this.close();
                    new SpyGui(staff, target).open();
                });
            }
            setSlot(40, builder.build());
        }

        // Botón cerrar
        setSlot(44, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§cCerrar"))
            .setCallback((idx, type, action, gui) -> this.close())
            .build());
    }
}
