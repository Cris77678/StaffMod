package me.drex.staffmod.config;

import java.util.UUID;

public class Kit {
    public String id;
    public String displayName;
    public String permissionNode; // Ej: staffmod.kit.helper
    public long cooldownSeconds;
    public String displayIconId; // Ej: "minecraft:diamond_sword"
    
    // El inventario completo serializado en Base64 para no romper el JSON con caracteres raros
    public String base64Inventory; 

    public Kit(String id, String displayName, String permissionNode, long cooldownSeconds, String displayIconId, String base64Inventory) {
        this.id = id;
        this.displayName = displayName;
        this.permissionNode = permissionNode;
        this.cooldownSeconds = cooldownSeconds;
        this.displayIconId = displayIconId;
        this.base64Inventory = base64Inventory;
    }
}