package me.drex.staffmod.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.drex.staffmod.config.PlayerData;
import me.drex.staffmod.storage.DataHandler;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerCache {

    // Cambiamos de un solo players.json a archivos individuales para evitar corromper toda la base de datos a la vez
    private static final Path PLAYERS_DIR = FabricLoader.getInstance().getConfigDir().resolve("staffmod/players");

    // Caché inteligente: Expira perfiles inactivos y los guarda automáticamente
    private static final Cache<UUID, PlayerData> CACHE = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .removalListener((UUID uuid, PlayerData data, var cause) -> {
                if (data != null) {
                    savePlayer(data);
                }
            })
            .build();

    /**
     * Obtiene al jugador de la RAM. Si no está, lo carga del disco. Si no existe, lo crea.
     */
    public static PlayerData getOrCreate(UUID uuid, String lastName) {
        return CACHE.get(uuid, k -> {
            Path file = PLAYERS_DIR.resolve(uuid.toString() + ".json");
            PlayerData loaded = DataHandler.loadSafe(file, PlayerData.class);
            if (loaded != null) {
                loaded.lastName = lastName;
                return loaded;
            }
            return new PlayerData(uuid, lastName);
        });
    }

    /**
     * Obtiene el perfil SOLO si está cargado en RAM (ideal para revisiones rápidas o eventos repetitivos).
     */
    public static PlayerData getIfPresent(UUID uuid) {
        return CACHE.getIfPresent(uuid);
    }

    public static void savePlayer(PlayerData data) {
        Path file = PLAYERS_DIR.resolve(data.uuid.toString() + ".json");
        DataHandler.saveAsync(data, file);
    }

    /**
     * Fuerza el guardado de todos los datos en RAM. Útil al apagar el servidor.
     */
    public static void saveAll() {
        CACHE.asMap().values().forEach(PlayerCache::savePlayer);
    }
}