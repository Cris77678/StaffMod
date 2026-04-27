package me.drex.staffmod.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.staffmod.config.Kit;
import me.drex.staffmod.features.KitManager;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class KitEditorGui extends SimpleGui {

    private final ServerPlayer staff;
    private final Kit kit;
    private final SimpleGui parent;
    
    // Variables temporales para editar
    private long currentCooldown;

    public KitEditorGui(ServerPlayer staff, Kit kit, SimpleGui parent) {
        // Usamos una GUI abierta (allowInventoryInteraction = true) para que puedan meter ítems
        super(MenuType.GENERIC_9x6, staff, false);
        this.staff = staff;
        this.kit = kit;
        this.parent = parent;
        this.currentCooldown = kit.cooldownSeconds;
        
        setTitle(Component.literal("§8❖ §eEditando Kit: §f" + kit.displayName));
        
        // Bloqueamos clics automáticos en la zona de herramientas, pero permitimos arrastrar ítems
        setLockPlayerInventory(false);
        build();
        loadExistingItems();
    }

    private void loadExistingItems() {
        if (kit.base64Inventory != null && !kit.base64Inventory.isEmpty()) {
            NonNullList<ItemStack> items = KitManager.deserializeItems(kit.base64Inventory, 36, staff.serverLevel().registryAccess());
            for (int i = 0; i < 36; i++) {
                if (!items.get(i).isEmpty()) {
                    // Ponemos los ítems físicos en la GUI (Slots 0 al 35)
                    setSlotRedirect(i, new eu.pb4.sgui.api.elements.GuiElement(items.get(i), (idx, type, action, gui) -> {}));
                }
            }
        }
    }

    private void build() {
        // Fila 5: Separador visual
        for (int i = 36; i < 45; i++) {
            setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal(" ")).build());
        }

        // Fila 6: Controles
        for (int i = 45; i < 54; i++) {
            setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Component.literal(" ")).build());
        }

        // Botón: Guardar Kit
        setSlot(49, new GuiElementBuilder(Items.EMERALD_BLOCK)
            .setName(Component.literal("§a§lGUARDAR KIT"))
            .addLoreLine(Component.literal("§7Click para guardar los ítems"))
            .addLoreLine(Component.literal("§7de arriba y aplicar cambios."))
            .setCallback((idx, type, action, gui) -> saveKitAndClose())
            .build());

        // Botones: Ajustar Cooldown
        updateCooldownDisplay();
        
        setSlot(47, new GuiElementBuilder(Items.REDSTONE)
            .setName(Component.literal("§c-1 Hora de Cooldown"))
            .setCallback((idx, type, action, gui) -> {
                currentCooldown = Math.max(0, currentCooldown - 3600);
                updateCooldownDisplay();
            }).build());
            
        setSlot(51, new GuiElementBuilder(Items.GLOWSTONE_DUST)
            .setName(Component.literal("§a+1 Hora de Cooldown"))
            .setCallback((idx, type, action, gui) -> {
                currentCooldown += 3600;
                updateCooldownDisplay();
            }).build());

        // Botón: Cancelar
        setSlot(45, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§cCancelar y Volver"))
            .setCallback((idx, type, action, gui) -> {
                this.close();
                if (parent != null) parent.open();
            }).build());
    }

    private void updateCooldownDisplay() {
        long hours = currentCooldown / 3600;
        setSlot(48, new GuiElementBuilder(Items.CLOCK)
            .setName(Component.literal("§eCooldown Actual:"))
            .addLoreLine(Component.literal("§f" + hours + " Horas §7(" + currentCooldown + "s)"))
            .build());
    }

    private void saveKitAndClose() {
        // Leemos los 36 slots superiores donde el staff puso los ítems
        NonNullList<ItemStack> newItems = NonNullList.withSize(36, ItemStack.EMPTY);
        for (int i = 0; i < 36; i++) {
            ItemStack slotItem = this.getSlotRedirect(i) != null ? this.getSlotRedirect(i).getItemStack() : ItemStack.EMPTY;
            newItems.set(i, slotItem.copy());
        }

        // Serializamos y guardamos
        kit.base64Inventory = KitManager.serializeItems(newItems, staff.serverLevel().registryAccess());
        kit.cooldownSeconds = currentCooldown;
        
        KitManager.createOrUpdateKit(kit);
        staff.sendSystemMessage(Component.literal("§a[Kits] El kit §f" + kit.displayName + " §aha sido guardado exitosamente."));
        
        this.close();
        if (parent != null) parent.open();
    }
}