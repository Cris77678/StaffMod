package me.drex.staffmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.drex.staffmod.config.DataStore;
import me.drex.staffmod.gui.StaffMainGui;
import me.drex.staffmod.util.JailManager;
import me.drex.staffmod.util.PermissionUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class StaffCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("staff")
            .requires(src -> {
                try { return PermissionUtil.has(src.getPlayerOrException(), "staffmod.use"); }
                catch (Exception e) { return false; }
            })

            // /staff  → abre el panel principal
            .executes(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                new StaffMainGui(player).open();
                return 1;
            })

            // /staff pos1  → selecciona esquina 1 para jail
            .then(Commands.literal("pos1")
                .requires(src -> {
                    try { return PermissionUtil.has(src.getPlayerOrException(), "staffmod.jail.manage"); }
                    catch (Exception e) { return false; }
                })
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    BlockPos pos = player.blockPosition();
                    JailManager.setPos1(player.getUUID(), pos);
                    player.sendSystemMessage(Component.literal("§a[Staff] Pos1 establecida en " + pos.toShortString()));
                    return 1;
                }))

            // /staff pos2  → selecciona esquina 2 para jail
            .then(Commands.literal("pos2")
                .requires(src -> {
                    try { return PermissionUtil.has(src.getPlayerOrException(), "staffmod.jail.manage"); }
                    catch (Exception e) { return false; }
                })
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    BlockPos pos = player.blockPosition();
                    JailManager.setPos2(player.getUUID(), pos);
                    player.sendSystemMessage(Component.literal("§a[Staff] Pos2 establecida en " + pos.toShortString()));
                    return 1;
                }))

            // /staff createjail <nombre>
            .then(Commands.literal("createjail")
                .requires(src -> {
                    try { return PermissionUtil.has(src.getPlayerOrException(), "staffmod.jail.manage"); }
                    catch (Exception e) { return false; }
                })
                .then(Commands.argument("nombre", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        String name = StringArgumentType.getString(ctx, "nombre");
                        String error = JailManager.createJail(player, name);
                        if (error != null) {
                            player.sendSystemMessage(Component.literal("§c[Staff] " + error));
                        } else {
                            player.sendSystemMessage(Component.literal("§a[Staff] Cárcel '" + name + "' creada."));
                        }
                        return 1;
                    })))

            // /staff deletejail <nombre>
            .then(Commands.literal("deletejail")
                .requires(src -> {
                    try { return PermissionUtil.has(src.getPlayerOrException(), "staffmod.jail.manage"); }
                    catch (Exception e) { return false; }
                })
                .then(Commands.argument("nombre", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        DataStore.getJails().keySet().forEach(builder::suggest);
                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        String name = StringArgumentType.getString(ctx, "nombre");
                        boolean ok  = DataStore.removeJail(name);
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            ok ? "§a[Staff] Cárcel '" + name + "' eliminada."
                               : "§c[Staff] No existe esa cárcel."), false);
                        return 1;
                    })))
        );
    }
}
