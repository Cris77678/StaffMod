package me.drex.staffmod.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import me.drex.staffmod.StaffMod;
import me.drex.staffmod.core.StaffModAsync;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DataHandler {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final DateTimeFormatter BACKUP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Guarda un objeto a JSON asíncronamente con sistema de Backup previo.
     */
    public static void saveAsync(Object data, Path file) {
        StaffModAsync.runAsync(() -> {
            try {
                Files.createDirectories(file.getParent());

                // Sistema de Backups: Copia el archivo actual antes de sobreescribirlo
                if (Files.exists(file) && Files.size(file) > 0) {
                    Path backupDir = file.getParent().resolve("backups");
                    Files.createDirectories(backupDir);
                    
                    String backupName = file.getFileName().toString().replace(".json", "") 
                                      + "_" + LocalDateTime.now().format(BACKUP_FORMAT) + ".json";
                    Path backupFile = backupDir.resolve(backupName);
                    
                    Files.copy(file, backupFile, StandardCopyOption.REPLACE_EXISTING);
                    cleanOldBackups(backupDir); // Borra backups viejos para no llenar el disco
                }

                // Guardado atómico: escribe en un archivo temporal y luego lo reemplaza
                Path tempFile = file.resolveSibling(file.getFileName() + ".tmp");
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(tempFile.toFile()), "UTF-8")) {
                    GSON.toJson(data, writer);
                }
                Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);
                
            } catch (IOException e) {
                StaffMod.LOGGER.error("[StaffMod] Error crítico al guardar " + file.getFileName(), e);
            }
        });
    }

    /**
     * Carga un archivo JSON de forma segura. Si está corrupto, lo aísla y retorna null.
     */
    public static <T> T loadSafe(Path file, Class<T> clazz) {
        if (!Files.exists(file)) return null;

        try (Reader reader = new InputStreamReader(new FileInputStream(file.toFile()), "UTF-8")) {
            return GSON.fromJson(reader, clazz);
        } catch (JsonSyntaxException e) {
            StaffMod.LOGGER.error("==========================================");
            StaffMod.LOGGER.error("[StaffMod CRÍTICO] JSON CORRUPTO DETECTADO");
            StaffMod.LOGGER.error("Archivo: {}", file.getFileName());
            StaffMod.LOGGER.error("Detalle: {}", e.getMessage());
            StaffMod.LOGGER.error("Acción: Renombrando a .corrupted y reseteando caché...");
            StaffMod.LOGGER.error("==========================================");
            
            try {
                Files.move(file, file.resolveSibling(file.getFileName() + ".corrupted"));
            } catch (IOException ignored) {}
            return null; // El sistema que llame esto deberá generar una configuración vacía/default
        } catch (Exception e) {
            StaffMod.LOGGER.error("[StaffMod] Error de lectura en " + file.getFileName(), e);
            return null;
        }
    }

    /**
     * Limpia backups con más de 7 días de antigüedad.
     */
    private static void cleanOldBackups(Path backupDir) {
        long cutoffTime = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(backupDir)) {
            for (Path entry : stream) {
                if (Files.getLastModifiedTime(entry).toMillis() < cutoffTime) {
                    Files.deleteIfExists(entry);
                }
            }
        } catch (IOException ignored) {}
    }
}