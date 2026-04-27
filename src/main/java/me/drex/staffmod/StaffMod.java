package me.drex.staffmod;

import me.drex.staffmod.command.StaffCommand;
import me.drex.staffmod.command.TicketCommand;
import me.drex.staffmod.command.StaffChatCommand;
import me.drex.staffmod.config.DataStore;
import me.drex.staffmod.config.PlayerData;
import me.drex.staffmod.core.StaffModAsync; // Fase 1: Hilos asíncronos
import me.drex.staffmod.gui.ActionExecutor;
import me.drex.staffmod.util.PermissionUtil;
import me.drex.staffmod.config.RankManager; // Fase 2: Rangos dinámicos
import me.drex.staffmod.cache.PlayerCache; // Fase 2: Caché inteligente
import me.drex.staffmod.features.AntiSpamFilter; // Fase 4: Filtro de Spam
import me.drex.staffmod.punishment.ExpirationTask; // Fase 4: Limpieza asíncrona de castigos

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

import java.util.concurrent.TimeUnit;

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

        // NUEVO: Evento de chat optimizado con Anti-Spam y uso de caché (Fase 4)
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            
            // 1. Verificación rápida de Mute desde la Caché Inteligente o DataStore
            PlayerData pd = PlayerCache.getIfPresent(sender.getUUID());
            if (pd == null) pd = DataStore.get(sender.getUUID()); // Fallback de transición
            
            if (pd != null && pd.isMuteActive()) {
                sender.sendSystemMessage(Component.literal("§c[Staff] Estás muteado. Expira: §e" + PlayerData.formatExpiry(pd.muteExpiry)));
                return false; 
            }

            // 2. Comprobación de StaffChat
            if (DataStore.isStaffChatToggled(sender.getUUID()) && PermissionUtil.has(sender, "staffmod.use")) {
                ActionExecutor.sendStaffChatMessage(sender, message.signedContent());
                return false;
            }

            // 3. Validación Anti-Spam
            if (!AntiSpamFilter.checkChat(sender, message.signedContent())) {
                return false;
            }

            return true;
        });

        // Fase 2 y 4 - Inicialización de LuckPerms, Rangos y Tareas Asíncronas
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            SERVER = server;
            LOGGER.info("[StaffMod] Servidor iniciando. Conectando LuckPerms y Rangos...");
            
            // Hook de permisos obligatorio
            PermissionUtil.init();
            
            // Carga de rangos dinámicos
            RankManager.loadRanks();

            // Programamos la Tarea Asíncrona de Expiración (revisa castigos cada 10 segundos)
            StaffModAsync.scheduleAsync(
                new ExpirationTask(server), 
                10, 10, TimeUnit.SECONDS
            );
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("[StaffMod] Servidor deteniéndose. Guardando datos y cerrando hilos asíncronos...");
            DataStore.save();
            
            // Aseguramos que la RAM de jugadores se vuelque a disco antes de cerrar
            PlayerCache.saveAll();
            
            // Apagado seguro de los hilos asíncronos para evitar memory leaks (Fase 1)
            StaffModAsync.shutdown();
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // ELIMINADO: DataStore.tickExpirations(server); ya no consume TPS del servidor.
            // Ahora lo maneja ExpirationTask de manera asíncrona.
            DataStore.tickAnnouncements(server);
        });

        // Ban check realizado al unirse para evitar problemas de acceso privado en el Login
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