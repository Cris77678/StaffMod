package me.drex.staffmod.config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StaffProfile {

    public UUID uuid;
    public String name;
    
    // Contadores
    public int bans = 0;
    public int mutes = 0;
    public int warns = 0;
    public int jails = 0;
    public int kicks = 0;
    
    // Historial reciente
    public List<String> recentHistory = new ArrayList<>();

    public StaffProfile(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public void addAction(String action) {
        recentHistory.add(0, action); // Añade al inicio (más reciente primero)
        if (recentHistory.size() > 15) {
            recentHistory.remove(15); // Solo guardamos las últimas 15 acciones para no saturar
        }
    }
}
