package me.drex.staffmod.config;

import com.google.gson.reflect.TypeToken;
import me.drex.staffmod.StaffMod;
import me.drex.staffmod.storage.DataHandler;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RankManager {

    private static final Path RANKS_FILE = FabricLoader.getInstance().getConfigDir().resolve("staffmod/ranks.json");
    private static List<RankConfig> loadedRanks = new ArrayList<>();

    public static void loadRanks() {
        Type listType = new TypeToken<ArrayList<RankConfig>>(){}.getType();
        
        try {
            List<RankConfig> ranks = DataHandler.loadSafe(RANKS_FILE, listType);
            if (ranks == null || ranks.isEmpty()) {
                generateDefaultRanks();
            } else {
                loadedRanks = ranks;
                // Ordenar por prioridad (mayor = más importante)
                loadedRanks.sort(Comparator.comparingInt((RankConfig r) -> r.priority).reversed());
            }
            StaffMod.LOGGER.info("[StaffMod] {} rangos dinámicos cargados.", loadedRanks.size());
        } catch (Exception e) {
            StaffMod.LOGGER.error("[StaffMod] Error cargando rangos.", e);
            generateDefaultRanks();
        }
    }

    private static void generateDefaultRanks() {
        loadedRanks.clear();
        loadedRanks.add(new RankConfig("admin", "Administrador", "[Admin]", "§4", 100, "staffcore.rank.admin", List.of("ALL")));
        loadedRanks.add(new RankConfig("mod", "Moderador", "[Mod]", "§2", 50, "staffcore.rank.mod", List.of("TICKETS", "MUTE", "KICK", "FREEZE")));
        loadedRanks.add(new RankConfig("helper", "Ayudante", "[Helper]", "§b", 10, "staffcore.rank.helper", List.of("TICKETS")));
        loadedRanks.add(new RankConfig("builder", "Constructor", "[Builder]", "§e", 5, "staffcore.rank.builder", List.of("BUILDER_MODE")));
        
        DataHandler.saveAsync(loadedRanks, RANKS_FILE);
    }

    /**
     * Devuelve el rango más alto de un jugador basándose en LuckPerms.
     */
    public static RankConfig getHighestRank(net.minecraft.server.level.ServerPlayer player) {
        for (RankConfig rank : loadedRanks) {
            if (me.drex.staffmod.util.PermissionUtil.has(player, rank.requiredPermission)) {
                return rank;
            }
        }
        return null;
    }
}