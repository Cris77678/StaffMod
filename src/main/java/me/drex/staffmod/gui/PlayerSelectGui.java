package me.drex.staffmod.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.staffmod.config.DataStore;
import me.drex.staffmod.config.PlayerData;
import me.drex.staffmod.util.PermissionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.List;

public class PlayerSelectGui extends SimpleGui {

    private final ServerPlayer staff;
    private final StaffAction action;
    private final SimpleGui parent;

    public PlayerSelectGui(ServerPlayer staff, StaffAction action, SimpleGui parent) {
        super(resolveSize(staff.getServer().getPlayerList().getPlayerCount()), staff, false);
        this.staff  = staff;
        this.action = action;
        this.parent = parent;
        setTitle(Component.literal("§8» §6Selecciona un jugador §7[" + action.name() + "]"));
        build();
    }

    private static MenuType<?> resolveSize(int count) {
        if (count <= 9)  return MenuType.GENERIC_9x1;
        if (count <= 18) return MenuType.GENERIC_9x2;
        if (count <= 27) return MenuType.GENERIC_9x3;
        if (count <= 36) return MenuType.GENERIC_9x4;
        if (count <= 45) return MenuType.GENERIC_9x5;
        return MenuType.GENERIC_9x6;
    }

    private void build() {
        List<ServerPlayer> online = staff.getServer().getPlayerList().getPlayers();
        for (int i = 0; i < online.size() && i < 54; i++) {
            ServerPlayer target = online.get(i);
            if (target.getUUID().equals(staff.getUUID())) continue;

            boolean isProtected = PermissionUtil.isProtected(target);
            PlayerData pd = DataStore.getOrCreate(target.getUUID(), target.getName().getString());

            GuiElementBuilder builder;
            if (isProtected) {
                builder = new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                    .setName(Component.literal("§c" + target.getName().getString() + " §7[Protegido]"));
            } else {
                builder = buildTargetSlot(target, pd);
            }

            final ServerPlayer finalTarget = target;
            builder.setCallback((idx, type, clickAction, gui) -> {
                if (isProtected) {
                    staff.sendSystemMessage(Component.literal("§c[Staff] No puedes realizar esta acción sobre un administrador."));
                    return;
                }

                // FASE 1: Acciones Rápidas con Shift
                if (type.isShift) {
                    switch (action) {
                        case MUTE -> { ActionExecutor.mute(staff, finalTarget, "5m", "Mute Rápido (5m)"); parent.open(); return; }
                        case BAN -> { ActionExecutor.ban(staff, finalTarget, "1d", "Ban Rápido (1d)"); parent.open(); return; }
                        case WARN -> { ActionExecutor.warn(staff, finalTarget, "Comportamiento inadecuado"); return; }
                        case FREEZE -> { ActionExecutor.freeze(staff, finalTarget); return; }
                        default -> {}
                    }
                }
                handleAction(finalTarget);
            });

            setSlot(i, builder.build());
        }

        int backSlot = getSize() - 1;
        setSlot(backSlot, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§7← Volver"))
            .setCallback((idx, type, action2, gui) -> parent.open())
            .build());
    }

    private GuiElementBuilder buildTargetSlot(ServerPlayer target, PlayerData pd) {
        boolean muted  = pd.isMuteActive();
        boolean jailed = pd.isJailActive();
        boolean frozen = pd.frozen;
        boolean banned = pd.isBanActive();

        var item = muted ? Items.YELLOW_STAINED_GLASS_PANE
                 : jailed ? Items.ORANGE_STAINED_GLASS_PANE
                 : frozen ? Items.LIGHT_BLUE_STAINED_GLASS_PANE
                 : banned ? Items.RED_STAINED_GLASS_PANE
                 : Items.LIME_STAINED_GLASS_PANE;

        GuiElementBuilder b = new GuiElementBuilder(item)
            .setName(Component.literal("§f§l" + target.getName().getString()));

        b.addLoreLine(Component.literal("§7Ping: §f" + target.connection.latency() + "ms"));
        b.addLoreLine(Component.literal("§7Estado: "
            + (muted  ? "§eMuteado " : "")
            + (jailed ? "§6Jaileado " : "")
            + (frozen ? "§bCongelado " : "")
            + (banned ? "§cBaneado " : "")
            + ((!muted && !jailed && !frozen && !banned) ? "§aNormal" : "")));

        b.addLoreLine(Component.literal(""));
        b.addLoreLine(Component.literal("§eClick normal: §f" + action.name()));
        
        // FASE 1: Lore descriptivo para Shift
        if (action == StaffAction.MUTE || action == StaffAction.BAN || action == StaffAction.WARN) {
            b.addLoreLine(Component.literal("§bShift + Click: §fCastigo rápido"));
        } else if (action == StaffAction.FREEZE) {
            b.addLoreLine(Component.literal("§bShift + Click: §fCongelar rápido"));
        }

        return b;
    }

    private void handleAction(ServerPlayer target) {
        switch (action) {
            case KICK      -> ActionExecutor.kick(staff, target, "Kickeado por staff.");
            case FREEZE    -> ActionExecutor.freeze(staff, target);
            case TELEPORT  -> ActionExecutor.teleport(staff, target);
            case KILL      -> ActionExecutor.kill(staff, target);
            case MUTE      -> new DurationReasonGui(staff, target, StaffAction.MUTE,  this).open();
            case UNMUTE    -> { ActionExecutor.unmute(staff, target); parent.open(); }
            case JAIL      -> new JailSelectGui(staff, target, this).open();
            case UNJAIL    -> { ActionExecutor.unjail(staff, target); parent.open(); }
            case BAN       -> new DurationReasonGui(staff, target, StaffAction.BAN,   this).open();
            case UNBAN     -> { ActionExecutor.unban(staff, target); parent.open(); }
            case WARN      -> new DurationReasonGui(staff, target, StaffAction.WARN,  this).open();
            case SPY       -> ActionExecutor.spy(staff, target);
        }
    }
}
