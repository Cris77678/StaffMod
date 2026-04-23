package me.drex.staffmod;

import me.drex.staffmod.command.StaffCommand;
import me.drex.staffmod.command.TicketCommand;
import me.drex.staffmod.command.StaffChatCommand;
import me.drex.staffmod.config.DataStore;
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

        DataStore.load();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            StaffCommand.register(dispatcher);
            TicketCommand.register(dispatcher);
            StaffChatCommand.register(dispatcher);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> DataStore.save());
        ServerLifecycleEvents.SERVER_STARTED.register(server -> SERVER = server);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            DataStore.tickExpirations(server);
            DataStore.tickAnnouncements(server);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            DataStore.applyOnJoin(handler.player)
        );

        LOGGER.info("[StaffMod] Listo.");
    }
}
