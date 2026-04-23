package me.drex.staffmod.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.staffmod.config.DataStore;
import me.drex.staffmod.config.JailZone;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Primero elige la zona de jail, luego la duración.
 */
public class JailSelectGui extends SimpleGui {

    private final ServerPlayer staff;
    private final ServerPlayer target;
    private final SimpleGui parent;

    public JailSelectGui(ServerPlayer staff, ServerPlayer target, SimpleGui parent) {
        super(MenuType.GENERIC_9x2, staff, false);
        this.staff  = staff;
        this.target = target;
        this.parent = parent;
        setTitle(Component.literal("§8» §6Selecciona la cárcel"));
        build();
    }

    private void build() {
        List<JailZone> zones = new ArrayList<>(DataStore.getJails().values());

        if (zones.isEmpty()) {
            setSlot(4, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§cNo hay cárceles configuradas."))
                .addLoreLine(Component.literal("§7Usa §f/staff pos1§7, §f/staff pos2"))
                .addLoreLine(Component.literal("§7y §f/staff createjail <nombre>"))
                .build());
        } else {
            for (int i = 0; i < zones.size() && i < 8; i++) {
                JailZone zone = zones.get(i);
                setSlot(i, new GuiElementBuilder(Items.IRON_BARS)
                    .setName(Component.literal("§6§l" + zone.name))
                    .addLoreLine(Component.literal("§7Dimensión: §f" + zone.dimension))
                    .addLoreLine(Component.literal(String.format("§7De §f%.0f,%.0f,%.0f §7a §f%.0f,%.0f,%.0f",
                        zone.x1, zone.y1, zone.z1, zone.x2, zone.y2, zone.z2)))
                    .addLoreLine(Component.literal("§eClick para jailear aquí"))
                    .setCallback((idx, type, action, gui) ->
                        new DurationSelectForJailGui(staff, target, zone, parent).open())
                    .build());
            }
        }

        // Volver
        setSlot(17, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§7← Volver"))
            .setCallback((idx, type, action, gui) -> parent.open())
            .build());
    }
}
