package me.drex.staffmod.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.staffmod.StaffMod;
import me.drex.staffmod.config.RankManager;
import me.drex.staffmod.features.KitManager;
import me.drex.staffmod.logging.AuditLogManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

public class DevPanelGui extends SimpleGui {

    public DevPanelGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x3, player, false);
        setTitle(Component.literal("§8❖ §d§lPANEL DE DESARROLLADOR §8❖"));
        build();
    }

    private void build() {
        for (int i = 0; i < getSize(); i++) {
            setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Component.literal(" ")).build());
        }

        // --- Recarga de Configuración ---
        setSlot(10, new GuiElementBuilder(Items.REPEATING_COMMAND_BLOCK)
            .setName(Component.literal("§a§lRecargar Módulos"))
            .addLoreLine(Component.literal("§7Recarga Rangos, Kits y Configs"))
            .addLoreLine(Component.literal("§7sin reiniciar el servidor."))
            .setCallback((idx, type, action, gui) -> {
                RankManager.loadRanks();
                KitManager.load();
                player.sendSystemMessage(Component.literal("§a[Dev] Módulos recargados exitosamente."));
                this.close();
            }).build());

        // --- Monitor de Memoria RAM ---
        long maxMem = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        long totalMem = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long freeMem = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        long usedMem = totalMem - freeMem;

        setSlot(13, new GuiElementBuilder(Items.COMPARATOR)
            .setName(Component.literal("§e§lEstado de la JVM"))
            .addLoreLine(Component.literal("§7Uso de RAM: §f" + usedMem + "MB / " + maxMem + "MB"))
            .addLoreLine(Component.literal("§7Hilos Activos: §f" + Thread.activeCount()))
            .build());

        // --- Exportación de Auditoría ---
        setSlot(16, new GuiElementBuilder(Items.HOPPER)
            .setName(Component.literal("§b§lExportar Logs a CSV"))
            .addLoreLine(Component.literal("§7Genera un archivo .csv en la carpeta"))
            .addLoreLine(Component.literal("§7del servidor con todas las acciones."))
            .setCallback((idx, type, action, gui) -> {
                String name = "audit_" + System.currentTimeMillis();
                AuditLogManager.exportToCSV(name);
                player.sendSystemMessage(Component.literal("§a[Dev] Logs exportados a: §fstaffmod_exports/" + name + ".csv"));
                this.close();
            }).build());

        setSlot(26, new GuiElementBuilder(Items.BARRIER).setName(Component.literal("§cCerrar")).setCallback((idx, type, action, gui) -> this.close()).build());
    }
}