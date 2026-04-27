package me.drex.staffmod.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.staffmod.cache.PlayerCache;
import me.drex.staffmod.config.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

public class PlayerProfileGui extends SimpleGui {

    private final ServerPlayer staff;
    private final ServerPlayer target; // Podría adaptarse para jugadores offline usando su UUID
    private final PlayerData targetData;

    public PlayerProfileGui(ServerPlayer staff, ServerPlayer target, SimpleGui parent) {
        super(MenuType.GENERIC_9x3, staff, false);
        this.staff = staff;
        this.target = target;
        this.targetData = PlayerCache.getOrCreate(target.getUUID(), target.getName().getString());
        
        setTitle(Component.literal("§8❖ §3Perfil: §f" + target.getName().getString()));
        build(parent);
    }

    private void build(SimpleGui parent) {
        // Decoración
        for (int i = 0; i < getSize(); i++) {
            setSlot(i, new GuiElementBuilder(Items.LIGHT_BLUE_STAINED_GLASS_PANE).setName(Component.literal(" ")).build());
        }

        // Slot 11: Cabeza e Información General
        setSlot(11, new GuiElementBuilder(Items.PLAYER_HEAD)
            .setName(Component.literal("§b§l" + target.getName().getString()))
            .addLoreLine(Component.literal("§8UUID: " + target.getUUID().toString()))
            .addLoreLine(Component.literal(" "))
            .addLoreLine(Component.literal("§7Ping: §a" + target.connection.latency() + "ms"))
            .addLoreLine(Component.literal("§7Modo de Juego: §e" + target.gameMode.getGameModeForPlayer().getName()))
            .build());

        // Slot 13: Historial de Sanciones
        boolean isMuted = targetData.isMuteActive();
        boolean isJailed = targetData.jailed;
        int warns = targetData.warnCount; // Asegúrate de tener esta variable en PlayerData

        setSlot(13, new GuiElementBuilder(Items.WRITTEN_BOOK)
            .setName(Component.literal("§6§lHistorial Administrativo"))
            .addLoreLine(Component.literal("§7Advertencias (Warns): " + (warns > 0 ? "§c" : "§a") + warns))
            .addLoreLine(Component.literal("§7Estado Mute: " + (isMuted ? "§cSilenciado" : "§aLimpio")))
            .addLoreLine(Component.literal("§7Estado Cárcel: " + (isJailed ? "§cEn Prisión" : "§aLibre")))
            .build());

        // Slot 15: Acciones Rápidas (Ir al menú de castigos para este jugador)
        setSlot(15, new GuiElementBuilder(Items.ANVIL)
            .setName(Component.literal("§c§lModerar Jugador"))
            .addLoreLine(Component.literal("§7Click para abrir el panel de"))
            .addLoreLine(Component.literal("§7castigos directo para este usuario."))
            .setCallback((idx, type, action, gui) -> {
                // Aquí lo conectamos a tu menú de moderación directo
                staff.sendSystemMessage(Component.literal("§eAbriendo moderación rápida..."));
                this.close();
                // new PunishmentGui(staff, target).open(); // (Depende de cómo manejes tu selección final)
            })
            .build());

        // Botón Volver
        if (parent != null) {
            setSlot(26, new GuiElementBuilder(Items.DARK_OAK_DOOR)
                .setName(Component.literal("§cVolver atrás"))
                .setCallback((idx, type, action, gui) -> parent.open())
                .build());
        } else {
            setSlot(26, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§cCerrar"))
                .setCallback((idx, type, action, gui) -> this.close())
                .build());
        }
    }
}