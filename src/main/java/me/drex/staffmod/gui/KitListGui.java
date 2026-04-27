package me.drex.staffmod.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.staffmod.config.Kit;
import me.drex.staffmod.features.KitManager;
import me.drex.staffmod.util.PermissionUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class KitListGui extends PaginatedGui<Kit> {

    private final ServerPlayer staff;

    public KitListGui(ServerPlayer staff, SimpleGui parent) {
        // Obtenemos todos los kits cargados en la memoria y los pasamos al paginador
        super(staff, parent, Component.literal("§8❖ §eReclamo de Kits §8❖"), new ArrayList<>(KitManager.getAllKits()));
        this.staff = staff;
        build();
    }

    @Override
    protected GuiElementBuilder buildItem(Kit kit) {
        // Resolvemos el ícono del kit o usamos un cofre por defecto
        var itemRes = ResourceLocation.tryParse(kit.displayIconId != null ? kit.displayIconId : "minecraft:chest");
        var iconItem = BuiltInRegistries.ITEM.getOptional(itemRes).orElse(Items.CHEST);

        boolean hasPermission = PermissionUtil.has(staff, kit.permissionNode);
        boolean onCooldown = KitManager.isOnCooldown(staff, kit);
        long remainingMs = KitManager.getRemainingCooldown(staff, kit);

        GuiElementBuilder builder = new GuiElementBuilder(iconItem)
            .setName(Component.literal("§6§l" + kit.displayName))
            .addLoreLine(Component.literal("§8Permiso requerido: " + kit.permissionNode))
            .addLoreLine(Component.literal(" "));

        if (!hasPermission) {
            builder.addLoreLine(Component.literal("§c§lBLOQUEADO"));
            builder.addLoreLine(Component.literal("§cNo posees el rango necesario."));
        } else if (onCooldown) {
            long hours = remainingMs / 3600000;
            long mins = (remainingMs % 3600000) / 60000;
            builder.addLoreLine(Component.literal("§e§lEN ESPERA"));
            builder.addLoreLine(Component.literal("§eDisponible en: §f" + hours + "h " + mins + "m"));
        } else {
            builder.addLoreLine(Component.literal("§a§lDISPONIBLE"));
            builder.addLoreLine(Component.literal("§aClick para reclamar tus herramientas."));
        }

        builder.setCallback((idx, type, action, gui) -> {
            if (!hasPermission) {
                staff.sendSystemMessage(Component.literal("§cNo tienes permisos para reclamar este kit."));
                return;
            }
            if (onCooldown) {
                staff.sendSystemMessage(Component.literal("§cDebes esperar a que termine tu cooldown."));
                return;
            }

            // Deserializamos el inventario Base64 en ítems reales de Minecraft
            var itemsToGive = KitManager.deserializeItems(kit.base64Inventory, 36, staff.serverLevel().registryAccess());
            
            // Entregar ítems sin borrar lo que ya tiene el jugador
            for (ItemStack i : itemsToGive) {
                if (!i.isEmpty()) {
                    staff.getInventory().placeItemBackInInventory(i.copy());
                }
            }

            // Aplicar el cooldown y guardar
            KitManager.setCooldown(staff, kit);
            staff.sendSystemMessage(Component.literal("§a¡Has reclamado el kit §f" + kit.displayName + "§a!"));
            
            // Recargar la GUI para actualizar los tiempos
            this.close();
            new KitListGui(staff, parent).open();
        });

        return builder;
    }

    @Override
    protected void build() {
        if (data.isEmpty()) {
            super.build(); // Dibuja la barra de navegación
            setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§cNo hay kits configurados."))
                .addLoreLine(Component.literal("§7Usa /staffkit create para crear uno."))
                .build());
        } else {
            super.build(); // Dibuja la página con los kits
        }
    }
}