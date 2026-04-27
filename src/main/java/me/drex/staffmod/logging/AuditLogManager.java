package me.drex.staffmod.logging;

import me.drex.staffmod.core.StaffModAsync;
import me.drex.staffmod.storage.DataHandler;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AuditLogManager {

    private static final Path LOG_FILE = FabricLoader.getInstance().getConfigDir().resolve("staffmod/audit_logs.json");
    private static final List<AuditEntry> sessionLogs = new ArrayList<>();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public record AuditEntry(String timestamp, String staffName, String action, String target, String details) {}

    /**
     * Registra una acción administrativa de forma asíncrona.
     */
    public static void log(String staff, String action, String target, String details) {
        StaffModAsync.runAsync(() -> {
            AuditEntry entry = new AuditEntry(LocalDateTime.now().format(TIME_FORMAT), staff, action, target, details);
            synchronized (sessionLogs) {
                sessionLogs.add(entry);
            }
            // Guardamos cada 10 entradas para no saturar el disco
            if (sessionLogs.size() % 10 == 0) {
                save();
            }
        });
    }

    public static void save() {
        DataHandler.saveAsync(sessionLogs, LOG_FILE);
    }

    /**
     * Exporta los logs actuales a un formato CSV legible por Excel.
     */
    public static void exportToCSV(String fileName) {
        StaffModAsync.runAsync(() -> {
            Path exportPath = FabricLoader.getInstance().getGameDir().resolve("staffmod_exports/" + fileName + ".csv");
            try {
                java.nio.file.Files.createDirectories(exportPath.getParent());
                StringBuilder csv = new StringBuilder("Fecha,Staff,Accion,Objetivo,Detalles\n");
                
                synchronized (sessionLogs) {
                    for (AuditEntry e : sessionLogs) {
                        csv.append(String.format("%s,%s,%s,%s,%s\n", e.timestamp, e.staffName, e.action, e.target, e.details));
                    }
                }
                
                java.nio.file.Files.writeString(exportPath, csv.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}