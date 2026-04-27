package me.drex.staffmod.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.staffmod.config.DataStore;
import me.drex.staffmod.features.BuilderManager;
import me.drex.staffmod.util.PermissionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

public class StaffMainGui extends SimpleGui {

    private final ServerPlayer staff;

    public StaffMainGui(ServerPlayer staff) {
        // Ampliamos a 6 filas (9x6) para el dashboard premium
        super(MenuType.GENERIC_9x6, staff, false);
        this.staff = staff;
        setTitle(Component.literal("§8❖ §6§lDASHBOARD STAFF §8❖"));
        build();
    }

    private void build() {
        // Estética Premium: Rellenar todos los huecos con cristal negro
        for (int i = 0; i < getSize(); i++) {
            setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal(" ")).build());
        }

        // === SECCIÓN 1: ESTADÍSTICAS EN VIVO (Fila 2) ===
        int onlinePlayers = staff.getServer().getPlayerCount();
        long openTickets = DataStore.getAllTickets().stream().filter(t -> t.status.equals("ABIERTO")).count();
        
        // Cálculo preciso de TPS (Ticks Per Second) para saber el rendimiento del servidor en vivo
        float[] tickTimes = staff.getServer().getTickTimesNanos();
        double averageTickTime = 0;
        for (float time : tickTimes) averageTickTime += time;
        averageTickTime = (averageTickTime / tickTimes.length) * 1.0E-6D;
        double tps = Math.min(20.0, 1000.0 / Math.max(averageTickTime, 1.0));
        String tpsColor = tps > 19 ? "§a" : tps > 15 ? "§e" : "§c";

        setSlot(12, new GuiElementBuilder(Items.PLAYER_HEAD)
            .setName(Component.literal("§b§lJugadores Online"))
            .addLoreLine(Component.literal("§7Total conectados: §f" + onlinePlayers))
            .build());

        setSlot(13, new GuiElementBuilder(Items.CLOCK)
            .setName(Component.literal("§a§lRendimiento (TPS)"))
            .addLoreLine(Component.literal("§7TPS Actual: " + tpsColor + String.format("%.2f", tps)))
            .addLoreLine(Component.literal("§7Carga (MSPT): §f" + String.format("%.2f", averageTickTime) + "ms"))
            .build());

        setSlot(14, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§e§lTickets Pendientes"))
            .addLoreLine(Component.literal("§7Sin asignar: §f" + openTickets))
            .addLoreLine(Component.literal("§8Click para gestionar"))
            .setCallback((idx, type, action, gui) -> new TicketGui(staff, this).open())
            .build());

        // === SECCIÓN 2: HERRAMIENTAS DE MODERACIÓN (Fila 4) ===
        addToolSlot(27, "staffmod.kick", Items.IRON_BOOTS, "§c§lExpulsar", "Kickear a un jugador", StaffAction.KICK);
        addToolSlot(28, "staffmod.mute", Items.STRING, "§e§lSilenciar", "Mute temporal o permanente", StaffAction.MUTE);
        addToolSlot(29, "staffmod.jail", Items.IRON_BARS, "§6§lCárcel", "Enviar a prisión", StaffAction.JAIL);
        addToolSlot(30, "staffmod.ban", Items.TNT, "§4§lBanear", "Bloquear acceso al servidor", StaffAction.BAN);
        addToolSlot(31, "staffmod.warn", Items.OAK_SIGN, "§a§lAdvertencia", "Enviar un aviso oficial", StaffAction.WARN);
        addToolSlot(32, "staffmod.freeze", Items.PACKED_ICE, "§b§lCongelar", "Inmovilizar para revisión", StaffAction.FREEZE);
        addToolSlot(33, "staffmod.spy", Items.ENDER_EYE, "§d§lInvSpy", "Revisar inventario sigilosamente", StaffAction.SPY);
        
        // Restaurados: Teleport y Kill
        addToolSlot(34, "staffmod.teleport", Items.ENDER_PEARL, "§3§lTeleport", "Teletransportarte a un jugador", StaffAction.TELEPORT);
        addToolSlot(35, "staffmod.kill", Items.SKELETON_SKULL, "§c§lKill", "Matar a un jugador", StaffAction.KILL);

        // Fase 5: Herramienta de Inspección de Cobblemon (PokeSpy)
        addToolSlot(36, "staffmod.spy", Items.DRAGON_EGG, "§d§lPokeSpy", "Revisar equipo Pokémon (IVs/EVs)", StaffAction.POKESPY);

        // === SECCIÓN 3: UTILIDADES DE TURNO (Fila 5) ===
        boolean isScToggled = DataStore.isStaffChatToggled(staff.getUUID());
        setSlot(40, new GuiElementBuilder(isScToggled ? Items.YELLOW_DYE : Items.LIGHT_GRAY_DYE)
            .setName(Component.literal(isScToggled ? "§e§lStaff Chat: FIJO" : "§7§lStaff Chat: NORMAL"))
            .addLoreLine(Component.literal("§7Click para alternar."))
            .addLoreLine(Component.literal("§8Todo lo que escribas irá al canal privado."))
            .setCallback((idx, type, action, gui) -> {
                DataStore.toggleStaffChat(staff.getUUID());
                build(); // Recarga los íconos sin tener que cerrar y abrir la GUI
            }).build());

        boolean isDuty = DataStore.isOnDuty(staff.getUUID());
        setSlot(41, new GuiElementBuilder(isDuty ? Items.LIME_DYE : Items.GRAY_DYE)
            .setName(Component.literal(isDuty ? "§a§lModo Staff: ACTIVO" : "§7§lModo Staff: INACTIVO"))
            .addLoreLine(Component.literal("§7Estado del turno actual."))
            .addLoreLine(Component.literal("§8Desactívalo para no recibir notificaciones."))
            .setCallback((idx, type, action, gui) -> {
                DataStore.toggleDuty(staff.getUUID());
                build(); // Recarga los íconos dinámicamente
            }).build());

        // Botón extra: Estadísticas generales del staff
        setSlot(42, new GuiElementBuilder(Items.WRITTEN_BOOK)
            .setName(Component.literal("§6§lAuditoría Staff"))
            .addLoreLine(Component.literal("§7Ver el historial de acciones"))
            .addLoreLine(Component.literal("§7y rendimiento del staff."))
            .setCallback((idx, type, action, gui) -> new StaffStatsGui(staff, this).open())
            .build());

        // Fase 6: Botón de Modo Builder Restringido
        boolean isBuilder = BuilderManager.isBuilderMode(staff.getUUID());
        setSlot(43, new GuiElementBuilder(isBuilder ? Items.BRICKS : Items.BRICK)
            .setName(Component.literal(isBuilder ? "§a§lModo Builder: ACTIVO" : "§7§lModo Builder: INACTIVO"))
            .addLoreLine(Component.literal("§7Aísla tu inventario y otorga"))
            .addLoreLine(Component.literal("§7creativo seguro sin bloques ilegales."))
            .setCallback((idx, type, action, gui) -> {
                if (PermissionUtil.has(staff, "staffmod.builder")) {
                    BuilderManager.toggleBuilderMode(staff);
                    build();
                } else {
                    staff.sendSystemMessage(Component.literal("§cNo tienes permiso para usar el Modo Builder."));
                }
            }).build());

        // Fase 6: Botón de Kits Internos
        setSlot(44, new GuiElementBuilder(Items.CHEST)
            .setName(Component.literal("§e§lKits de Staff"))
            .addLoreLine(Component.literal("§7Reclama tus kits de herramientas"))
            .addLoreLine(Component.literal("§7según tu rango de administración."))
            .setCallback((idx, type, action, gui) -> new KitListGui(staff, this).open())
            .build());

        // NUEVO - Fase 8: Botón secreto para Desarrolladores
        if (PermissionUtil.has(staff, "staffmod.developer")) {
            setSlot(45, new GuiElementBuilder(Items.COMMAND_BLOCK)
                .setName(Component.literal("§d§lPanel de Desarrollador"))
                .addLoreLine(Component.literal("§7Herramientas técnicas de PokeLand."))
                .setCallback((idx, type, action, gui) -> new DevPanelGui(staff).open())
                .build());
        }

        // Botón de Cerrar Central (Fila 6)
        setSlot(49, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lCerrar Panel"))
            .setCallback((idx, type, action, gui) -> this.close())
            .build());
    }

    /**
     * Método auxiliar para reducir el código repetitivo al crear botones de herramientas.
     * Si no tiene permiso, muestra un botón bloqueado gris.
     */
    private void addToolSlot(int slot, String perm, net.minecraft.world.item.Item item, String name, String desc, StaffAction action) {
        if (PermissionUtil.has(staff, perm)) {
            setSlot(slot, new GuiElementBuilder(item)
                .setName(Component.literal(name))
                .addLoreLine(Component.literal("§7" + desc))
                .setCallback((idx, type, a, gui) -> new PlayerSelectGui(staff, action, this).open())
                .build());
        } else {
            setSlot(slot, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Component.literal("§8§m" + net.minecraft.ChatFormatting.stripFormatting(name)))
                .addLoreLine(Component.literal("§cNo tienes permiso para esto:"))
                .addLoreLine(Component.literal("§8" + perm))
                .build());
        }
    }
}