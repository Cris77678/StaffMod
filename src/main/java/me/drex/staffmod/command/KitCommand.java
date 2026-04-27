package me.drex.staffmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.drex.staffmod.config.Kit;
import me.drex.staffmod.features.KitManager;
import me.drex.staffmod.gui.KitEditorGui;
import me.drex.staffmod.util.PermissionUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class KitCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("staffkit")
            .requires(source -> {
                try {
                    return PermissionUtil.has(source.getPlayerOrException(), "staffmod.admin");
                } catch (Exception e) {
                    return false;
                }
            })
            .then(Commands.literal("create")
                .then(Commands.argument("id", StringArgumentType.word())
                .then(Commands.argument("nombreVisual", StringArgumentType.string())
                .then(Commands.argument("permiso", StringArgumentType.word())
                .executes(KitCommand::createKit)))))
            .then(Commands.literal("edit")
                .then(Commands.argument("id", StringArgumentType.word())
                .executes(KitCommand::editKit)))
        );
    }

    private static int createKit(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        String id = StringArgumentType.getString(context, "id");
        String name = StringArgumentType.getString(context, "nombreVisual");
        String perm = StringArgumentType.getString(context, "permiso");

        if (KitManager.getKit(id) != null) {
            player.sendSystemMessage(Component.literal("§cEl kit '" + id + "' ya existe. Usa /staffkit edit " + id));
            return 0;
        }

        // Creamos el Kit vacío por defecto (Ícono de cofre, 24 horas de cooldown)
        Kit newKit = new Kit(id, name, perm, 86400, "minecraft:chest", "");
        KitManager.createOrUpdateKit(newKit);

        // Abrimos el editor visual automáticamente
        new KitEditorGui(player, newKit, null).open();
        return 1;
    }

    private static int editKit(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        String id = StringArgumentType.getString(context, "id");

        Kit kit = KitManager.getKit(id);
        if (kit == null) {
            player.sendSystemMessage(Component.literal("§cNo existe ningún kit con esa ID."));
            return 0;
        }

        new KitEditorGui(player, kit, null).open();
        return 1;
    }
}