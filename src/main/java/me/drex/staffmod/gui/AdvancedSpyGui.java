package me.drex.staffmod.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.staffmod.StaffMod;
import me.drex.staffmod.core.StaffModAsync;
import me.drex.staffmod.util.PermissionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AdvancedSpyGui extends SimpleGui {

    private final ServerPlayer staff;
    private final ServerPlayer target;
    private final boolean canInteract;

    public AdvancedSpyGui(ServerPlayer staff, ServerPlayer target) {
        super(MenuType.GENERIC_9x5, staff, false);
        this.staff = staff;
        this.target = target;
        
        // Verificamos si tiene el permiso avanzado para editar inventarios
        this.canInteract = PermissionUtil.has(staff, "staffmod.spy.interact");
        
        setTitle(Component.literal("§d[Spy] §f" + target.getName().getString()
            + (canInteract ? " §a(Editor)" : " §7(Solo lectura)")));
        build();
    }

    private void build() {
        var inv = target.getInventory();

        // Slots 0-35 -> Inventario principal del jugador
        for (int i = 0; i < 36; i++) {
            final int slot = i;
            setSlot(i, buildSecureSlot(inv.getItem(i), "§8Slot: " + i, () -> {
                if (canInteract) handleSafeSwap(slot, false);
            }));
        }

        // Slots 36-39 -> Armadura
        String[] armorNames = {"Botas", "Pantalón", "Pechera", "Casco"};
        for (int i = 0; i < 4; i++) {
            final int armorSlot = i;
            int guiSlot = 36 + i;
            setSlot(guiSlot, buildSecureSlot(inv.armor.get(i), "§8Armadura: " + armorNames[i], () -> {
                if (canInteract) handleSafeSwap(armorSlot, true);
            }));
        }

        // Slot 40 -> Mano secundaria
        setSlot(40, buildSecureSlot(inv.offhand.get(0), "§8Mano Secundaria", () -> {
            if (canInteract) {
                ItemStack cursorItem = staff.containerMenu.getCarried();
                ItemStack targetItem = inv.offhand.get(0).copy();
                inv.offhand.set(0, cursorItem);
                staff.containerMenu.setCarried(targetItem);
                logAction("modificó la mano secundaria");
                reopenSafely();
            }
        }));

        // Botón Cerrar
        setSlot(44, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lCerrar Spy"))
            .setCallback((idx, type, action, gui) -> this.close())
            .build());
    }

    /**
     * Construye un botón de inventario blindado contra abusos (Shift-Click, Drag, Double Click).
     */
    private GuiElementBuilder buildSecureSlot(ItemStack item, String loreInfo, Runnable onSafeClick) {
        if (item.isEmpty()) item = new ItemStack(Items.AIR);

        return GuiElementBuilder.from(item.copy())
            .addLoreLine(Component.literal(loreInfo))
            .setCallback((idx, clickData, actionType, gui) -> {
                
                // Si es solo lectura, cancelamos cualquier intento de interacción
                if (!canInteract) return;

                // ANTIBUGS: Solo permitimos clics normales (PICKUP). Bloqueamos Shift, Clonar, Arrastrar, etc.
                if (actionType == ClickType.QUICK_MOVE || actionType == ClickType.SWAP || 
                    actionType == ClickType.CLONE || actionType == ClickType.QUICK_CRAFT || 
                    actionType == ClickType.PICKUP_ALL) {
                    
                    staff.sendSystemMessage(Component.literal("§c[Spy] Por seguridad, el Shift-Click y arrastre están deshabilitados aquí. Usa clics normales."));
                    
                    // Forzar resincronización para evitar desajustes visuales en el cliente del staff
                    gui.sendSlotUpdate(idx);
                    return;
                }

                // Si pasó el filtro de seguridad, ejecutamos la lógica de intercambio
                onSafeClick.run();
            });
    }

    /**
     * Lógica de intercambio manual altamente controlada.
     */
    private void handleSafeSwap(int targetSlot, boolean isArmor) {
        var inv = target.getInventory();
        ItemStack cursorItem = staff.containerMenu.getCarried();
        ItemStack targetItem;

        if (isArmor) {
            targetItem = inv.armor.get(targetSlot).copy();
            inv.armor.set(targetSlot, cursorItem);
        } else {
            targetItem = inv.getItem(targetSlot).copy();
            inv.setItem(targetSlot, cursorItem);
        }

        staff.containerMenu.setCarried(targetItem);
        logAction("modificó el slot " + targetSlot + (isArmor ? " (Armadura)" : ""));
        
        reopenSafely();
    }

    /**
     * Recarga la GUI forzando una actualización limpia.
     */
    private void reopenSafely() {
        this.close();
        new AdvancedSpyGui(staff, target).open();
    }

    /**
     * Registra silenciosamente el movimiento en el hilo asíncrono para la auditoría.
     */
    private void logAction(String detail) {
        StaffModAsync.runAsync(() -> {
            StaffMod.LOGGER.info("[Auditoría] El staff {} {} del jugador {}.", 
                staff.getName().getString(), detail, target.getName().getString());
            // En la Fase 4 conectaremos esto a la base de datos de logs
        });
    }
}