package me.drex.staffmod.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.staffmod.util.PermissionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/**
 * Panel principal de staff — 9 slots (barra de herramientas).
 * Cada botón abre el selector de jugadores para esa acción.
 */
public class StaffMainGui extends SimpleGui {

    private final ServerPlayer staff;

    public StaffMainGui(ServerPlayer staff) {
        super(MenuType.GENERIC_9x1, staff, false);
        this.staff = staff;
        setTitle(Component.literal("§8⚔ §6Panel de Staff §8⚔"));
        build();
    }

    private void build() {
        // Kick
        if (PermissionUtil.has(staff, "staffmod.kick"))
            setSlot(0, new GuiElementBuilder(Items.IRON_BOOTS)
                .setName(Component.literal("§c§lKick"))
                .addLoreLine(Component.literal("§7Expulsar a un jugador del servidor."))
                .setCallback((idx, type, action, gui) -> new PlayerSelectGui(staff, StaffAction.KICK, this).open())
                .build());
        else setSlot(0, lockedSlot("Kick", "staffmod.kick"));

        // Mute
        if (PermissionUtil.has(staff, "staffmod.mute"))
            setSlot(1, new GuiElementBuilder(Items.STRING)
                .setName(Component.literal("§e§lMute"))
                .addLoreLine(Component.literal("§7Silenciar a un jugador."))
                .setCallback((idx, type, action, gui) -> new PlayerSelectGui(staff, StaffAction.MUTE, this).open())
                .build());
        else setSlot(1, lockedSlot("Mute", "staffmod.mute"));

        // Jail
        if (PermissionUtil.has(staff, "staffmod.jail"))
            setSlot(2, new GuiElementBuilder(Items.IRON_BARS)
                .setName(Component.literal("§6§lJail"))
                .addLoreLine(Component.literal("§7Enviar a un jugador a la cárcel."))
                .setCallback((idx, type, action, gui) -> new PlayerSelectGui(staff, StaffAction.JAIL, this).open())
                .build());
        else setSlot(2, lockedSlot("Jail", "staffmod.jail"));

        // Ban
        if (PermissionUtil.has(staff, "staffmod.ban"))
            setSlot(3, new GuiElementBuilder(Items.TNT)
                .setName(Component.literal("§4§lBan"))
                .addLoreLine(Component.literal("§7Banear a un jugador."))
                .setCallback((idx, type, action, gui) -> new PlayerSelectGui(staff, StaffAction.BAN, this).open())
                .build());
        else setSlot(3, lockedSlot("Ban", "staffmod.ban"));

        // Freeze
        if (PermissionUtil.has(staff, "staffmod.freeze"))
            setSlot(4, new GuiElementBuilder(Items.PACKED_ICE)
                .setName(Component.literal("§b§lFreeze"))
                .addLoreLine(Component.literal("§7Congelar/descongelar a un jugador."))
                .setCallback((idx, type, action, gui) -> new PlayerSelectGui(staff, StaffAction.FREEZE, this).open())
                .build());
        else setSlot(4, lockedSlot("Freeze", "staffmod.freeze"));

        // Spy
        if (PermissionUtil.has(staff, "staffmod.spy"))
            setSlot(5, new GuiElementBuilder(Items.ENDER_EYE)
                .setName(Component.literal("§d§lSpy"))
                .addLoreLine(Component.literal("§7Ver el inventario de un jugador."))
                .addLoreLine(Component.literal(PermissionUtil.has(staff, "staffmod.spy.interact")
                    ? "§aModo: §fVer + Interactuar" : "§eModo: §fSolo ver"))
                .setCallback((idx, type, action, gui) -> new PlayerSelectGui(staff, StaffAction.SPY, this).open())
                .build());
        else setSlot(5, lockedSlot("Spy", "staffmod.spy"));

        // Warn
        if (PermissionUtil.has(staff, "staffmod.warn"))
            setSlot(6, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("§a§lWarn"))
                .addLoreLine(Component.literal("§7Advertir a un jugador."))
                .setCallback((idx, type, action, gui) -> new PlayerSelectGui(staff, StaffAction.WARN, this).open())
                .build());
        else setSlot(6, lockedSlot("Warn", "staffmod.warn"));

        // Teleport
        if (PermissionUtil.has(staff, "staffmod.teleport"))
            setSlot(7, new GuiElementBuilder(Items.ENDER_PEARL)
                .setName(Component.literal("§3§lTeleport"))
                .addLoreLine(Component.literal("§7Teletransportarte a un jugador."))
                .setCallback((idx, type, action, gui) -> new PlayerSelectGui(staff, StaffAction.TELEPORT, this).open())
                .build());
        else setSlot(7, lockedSlot("Teleport", "staffmod.teleport"));

        // Kill
        if (PermissionUtil.has(staff, "staffmod.kill"))
            setSlot(8, new GuiElementBuilder(Items.SKULL_BANNER_PATTERN)
                .setName(Component.literal("§c§lKill"))
                .addLoreLine(Component.literal("§7Matar a un jugador."))
                .setCallback((idx, type, action, gui) -> new PlayerSelectGui(staff, StaffAction.KILL, this).open())
                .build());
        else setSlot(8, lockedSlot("Kill", "staffmod.kill"));
    }

    private eu.pb4.sgui.api.elements.GuiElement lockedSlot(String name, String perm) {
        return new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§8§l" + name + " §7(Sin permiso)"))
            .addLoreLine(Component.literal("§7Necesitas: §c" + perm))
            .build();
    }
}
