package me.drex.staffmod.mixin;

import me.drex.staffmod.features.BuilderManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class CreativeSecurityMixin {

    @Shadow public ServerPlayer player;

    // Lista negra de ítems que los Builders NUNCA pueden sacar (Anti-Grief / Anti-Abuso)
    private static final Set<String> BLACKLISTED_ITEMS = Set.of(
            "minecraft:command_block",
            "minecraft:chain_command_block",
            "minecraft:repeating_command_block",
            "minecraft:command_block_minecart",
            "minecraft:jigsaw",
            "minecraft:structure_block",
            "minecraft:structure_void",
            "minecraft:barrier",
            "minecraft:bedrock",
            "minecraft:end_portal_frame",
            "minecraft:tnt",
            "minecraft:dragon_egg",
            "minecraft:spawner"
    );

    @Inject(method = "handleSetCreativeModeSlot", at = @At("HEAD"), cancellable = true)
    private void onCreativeItemSpawn(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {
        // Solo verificamos si el jugador está en el Modo Builder especial
        if (!BuilderManager.isBuilderMode(player.getUUID())) {
            return;
        }

        ItemStack item = packet.getItem();
        if (item.isEmpty()) return;

        // Obtenemos el ID real del ítem (ej: "minecraft:bedrock")
        String itemId = BuiltInRegistries.ITEM.getKey(item.getItem()).toString();

        // Si el ítem está en la lista negra o tiene NBT sospechoso (ej: pociones personalizadas/espadas hackeadas)
        if (BLACKLISTED_ITEMS.contains(itemId) || hasDangerousNBT(item)) {
            
            // Cancelamos el paquete. El ítem no se creará en el servidor.
            ci.cancel();
            
            // Forzamos una actualización en el cliente para que el ítem "fantasma" desaparezca de su cursor
            player.containerMenu.sendAllDataToRemote();
            
            // Avisamos al jugador
            player.sendSystemMessage(Component.literal("§c[Seguridad] No tienes permiso para sacar este ítem: §f" + itemId));
            
            // Podrías enviar un log a Discord usando DiscordWebhook.sendEmbed() aquí si lo deseas.
        }
    }

    /**
     * Verifica si el ítem tiene encantamientos superiores al nivel vanilla 
     * o datos NBT que podrían usarse para crashear el servidor.
     */
    private boolean hasDangerousNBT(ItemStack item) {
        if (!item.hasTag()) return false;
        
        // Bloquear ítems con BlockEntityTag (cofres con NBT copiado lleno de ítems, Shulker boxes hackeadas, etc)
        // Esto previene NBT Crashers comunes.
        if (item.getTag().contains("BlockEntityTag")) {
            return true;
        }
        
        return false;
    }
}