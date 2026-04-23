package me.drex.staffmod;

import me.drex.staffmod.command.StaffCommand;
import me.drex.staffmod.config.DataStore;
import me.drex.staffmod.util.JailManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaffMod implements ModInitializer {

    public static final String MOD_ID = "staffmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static MinecraftServer SERVER;

    @Override
    public void onInitialize() {
        LOGGER.info("[StaffMod] Iniciando...");

        // Cargar datos persistentes
        DataStore.load();

        // Registrar comandos
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            StaffCommand.register(dispatcher)
        );

        // Guardar datos al cerrar el servidor
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> DataStore.save());

        // Guardar referencia al servidor
        ServerLifecycleEvents.SERVER_STARTED.register(server -> SERVER = server);

        // Tick: comprobar expiraciones de mute/ban/jail/freeze
        ServerTickEvents.END_SERVER_TICK.register(server -> DataStore.tickExpirations(server));

        // Al conectar: aplicar restricciones activas (mute, jail, freeze)
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            DataStore.applyOnJoin(handler.player)
        );

        LOGGER.info("[StaffMod] Listo.");
    }
}
