package me.drex.staffmod.features;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.pokemon.Pokemon;
import me.drex.staffmod.StaffMod;
import me.drex.staffmod.config.DataStore;
import me.drex.staffmod.util.DiscordWebhook;
import me.drex.staffmod.util.PermissionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class CobblemonAlerts {

    public static void registerEvents() {
        // Escuchamos el evento de captura de Pokémon
        CobblemonEvents.POKEMON_CAPTURED.subscribe(net.minecraft.world.entity.player.Player.class, event -> {
            Pokemon pokemon = event.getPokemon();
            ServerPlayer player = (ServerPlayer) event.getPlayer();

            boolean isLegendary = pokemon.isLegendary(); // O mythical
            boolean isShiny = pokemon.getShiny();
            
            // Revisamos si tiene IVs sospechosamente perfectos (6 IVs a 31)
            boolean perfectIvs = (pokemon.getIvs().getHp() == 31 && pokemon.getIvs().getAttack() == 31 &&
                                  pokemon.getIvs().getDefence() == 31 && pokemon.getIvs().getSpecialAttack() == 31 &&
                                  pokemon.getIvs().getSpecialDefence() == 31 && pokemon.getIvs().getSpeed() == 31);

            if (isLegendary || isShiny || perfectIvs) {
                String reason = "";
                if (isLegendary) reason += "Legendario ";
                if (isShiny) reason += "Shiny ";
                if (perfectIvs) reason += "Perfecto(6x31) ";

                String message = "§8[§cAlerta Sistema§8] §f" + player.getName().getString() + " §ecapturó un §d" + pokemon.getSpecies().getName() + " §7(" + reason.trim() + ")";

                // 1. Avisar a todos los staffs activos (On Duty) in-game
                for (ServerPlayer p : player.getServer().getPlayerList().getPlayers()) {
                    if (PermissionUtil.has(p, "staffmod.use") && DataStore.isOnDuty(p.getUUID())) {
                        p.sendSystemMessage(Component.literal(message));
                    }
                }

                // 2. Enviar log al Discord para registro histórico
                DiscordWebhook.sendEmbed(
                    "Captura Sospechosa/Extraordinaria", 
                    "**Jugador:** " + player.getName().getString() + "\n**Pokémon:** " + pokemon.getSpecies().getName() + "\n**Atributos:** " + reason.trim(),
                    0xFF00FF // Color Magenta
                );
            }
            
            return kotlin.Unit.INSTANCE;
        });

        StaffMod.LOGGER.info("[StaffMod] Módulo de alertas Cobblemon registrado.");
    }
}