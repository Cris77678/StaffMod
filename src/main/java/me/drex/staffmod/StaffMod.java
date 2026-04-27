package me.drex.staffmod;

import me.drex.staffmod.command.StaffCommand;
import me.drex.staffmod.command.TicketCommand;
import me.drex.staffmod.command.StaffChatCommand;
import me.drex.staffmod.config.DataStore;
import me.drex.staffmod.config.PlayerData;
import me.drex.staffmod.core.StaffModAsync; // NUEVO: Importación del motor asíncrono (Fase 1)
import me.drex.staffmod.gui.ActionExecutor;
import me.drex.staffmod.util.PermissionUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaffMod implements ModInitializer {

    public static final String MOD_ID = "staffmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static MinecraftServer SERVER;

    @Override
    public void onInitialize() {
        LOGGER.info("[StaffMod] Iniciando núcleo de rendimiento premium...");

        // Carga inicial del DataStore actual
        DataStore.load();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            StaffCommand.register(dispatcher);
            TicketCommand.register(dispatcher);
            StaffChatCommand.register(dispatcher);
        });

        // Bloqueo de chat para Mute y StaffChat
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            PlayerData pd = DataStore.get(sender.getUUID());
            if (pd != null && pd.isMuteActive()) {
                sender.sendSystemMessage(Component.literal("§c[Staff] Estás muteado. Expira: §e" + PlayerData.formatExpiry(pd.muteExpiry)));
                return false; 
            }

            if (DataStore.isStaffChatToggled(sender.getUUID()) && PermissionUtil.has(sender, "staffmod.use")) {
                ActionExecutor.sendStaffChatMessage(sender, message.signedContent());
                return false;
            }
            return true;
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> SERVER = server);

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("[StaffMod] Servidor deteniéndose. Guardando datos y cerrando hilos asíncronos...");
            DataStore.save();
            // NUEVO: Apagado seguro de los hilos asíncronos para evitar memory leaks (Fase 1)
            StaffModAsync.shutdown();
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            DataStore.tickExpirations(server);
            DataStore.tickAnnouncements(server);
        });

        // CORRECCIÓN: Ban check realizado al unirse para evitar problemas de acceso privado en el Login
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PlayerData pd = DataStore.get(handler.player.getUUID());
            if (pd != null && pd.isBanActive()) {
                handler.disconnect(Component.literal("§cEstás baneado del servidor.\n§fRazón: §e" + pd.banReason + "\n§fExpira: §e" + PlayerData.formatExpiry(pd.banExpiry)));
                return;
            }
            DataStore.applyOnJoin(handler.player);
        });

        LOGGER.info("[StaffMod] Listo.");
    }
}