package me.drex.staffmod.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/**
 * GUI de 3 filas:
 *  Fila 1 → duraciones predefinidas (5m, 30m, 1h, 6h, 1d, 7d, 30d, Perm)
 *  Fila 2 → razones predefinidas
 *  Fila 3 → cancelar / confirmar con selección actual
 */
public class DurationReasonGui extends SimpleGui {

    private final ServerPlayer staff;
    private final ServerPlayer target;
    private final StaffAction action;
    private final SimpleGui parent;

    private String selectedDuration = "perm";
    private String selectedReason   = "Infracción de normas.";

    private static final String[] DURATIONS = {"5m","30m","1h","6h","12h","1d","7d","perm"};
    private static final String[] REASONS   = {
        "Infracción de normas.", "Lenguaje inapropiado.", "Spam.", "Hack / Trampa.",
        "Acoso a otros jugadores.", "Publicidad.", "Tóxico.", "Otro."
    };

    public DurationReasonGui(ServerPlayer staff, ServerPlayer target, StaffAction action, SimpleGui parent) {
        super(MenuType.GENERIC_9x3, staff, false);
        this.staff  = staff;
        this.target = target;
        this.action = action;
        this.parent = parent;
        setTitle(Component.literal("§8» §6" + action.name() + " §7a " + target.getName().getString()));
        build();
    }

    private void build() {
        // Fila 0 — duraciones
        for (int i = 0; i < DURATIONS.length; i++) {
            final String dur = DURATIONS[i];
            boolean selected = dur.equals(selectedDuration);
            setSlot(i, new GuiElementBuilder(selected ? Items.LIME_CONCRETE : Items.GRAY_CONCRETE)
                .setName(Component.literal((selected ? "§a§l" : "§7") + dur))
                .addLoreLine(Component.literal(selected ? "§aSeleccionado" : "§7Click para seleccionar"))
                .setCallback((idx, type, a, gui) -> {
                    selectedDuration = dur;
                    reopen();
                })
                .build());
        }
        // Fila 1 — razones
        for (int i = 0; i < REASONS.length; i++) {
            final String reason = REASONS[i];
            boolean selected = reason.equals(selectedReason);
            setSlot(9 + i, new GuiElementBuilder(selected ? Items.LIME_STAINED_GLASS_PANE : Items.GRAY_STAINED_GLASS_PANE)
                .setName(Component.literal((selected ? "§a§l" : "§7") + reason))
                .addLoreLine(Component.literal(selected ? "§aSeleccionada" : "§7Click para seleccionar"))
                .setCallback((idx, type, a, gui) -> {
                    selectedReason = reason;
                    reopen();
                })
                .build());
        }
        // Fila 2 — cancelar y confirmar
        setSlot(18, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lCancelar"))
            .setCallback((idx, type, a, gui) -> parent.open())
            .build());

        // Info en el centro
        setSlot(22, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§e§lResumen"))
            .addLoreLine(Component.literal("§7Acción: §f"  + action.name()))
            .addLoreLine(Component.literal("§7Jugador: §f" + target.getName().getString()))
            .addLoreLine(Component.literal("§7Duración: §a" + selectedDuration))
            .addLoreLine(Component.literal("§7Razón: §a"   + selectedReason))
            .build());

        setSlot(26, new GuiElementBuilder(Items.EMERALD_BLOCK)
            .setName(Component.literal("§a§lConfirmar"))
            .addLoreLine(Component.literal("§7" + action.name() + " a §f" + target.getName().getString()))
            .addLoreLine(Component.literal("§7Duración: §a" + selectedDuration))
            .addLoreLine(Component.literal("§7Razón: §a"    + selectedReason))
            .setCallback((idx, type, a, gui) -> confirm())
            .build());
    }

    private void confirm() {
        switch (action) {
            case MUTE -> ActionExecutor.mute(staff, target, selectedDuration, selectedReason);
            case BAN  -> ActionExecutor.ban(staff, target,  selectedDuration, selectedReason);
            case WARN -> ActionExecutor.warn(staff, target, selectedReason);
        }
        parent.open();
    }

    private void reopen() {
        // sgui no tiene clearSlots — rellenamos todos los slots con AIR primero
        for (int i = 0; i < getSize(); i++) setSlot(i, new GuiElementBuilder(net.minecraft.world.item.Items.AIR).build());
        build();
    }
}
