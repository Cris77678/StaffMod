package me.drex.staffmod.mixin;

import me.drex.staffmod.config.DataStore;
import me.drex.staffmod.config.PlayerData;
import me.drex.staffmod.util.JailManager;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MovementMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handleMovePlayer", at = @At("HEAD"))
    private void staffmod$handleMovement(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        PlayerData pd = DataStore.get(player.getUUID());
        if (pd == null) return;

        // FIX BUG 4: Evitar tormenta de paquetes y permitir mover la cámara al estar congelado
        if (pd.frozen) {
            if (packet.hasPosition()) {
                double dx = packet.getX(player.getX()) - player.getX();
                double dy = packet.getY(player.getY()) - player.getY();
                double dz = packet.getZ(player.getZ()) - player.getZ();
                
                // Solo teletransportar de regreso si se desplazó físicamente más de un hilo
                if (dx * dx + dy * dy + dz * dz > 0.001) {
                    player.connection.teleport(
                        player.getX(), player.getY(), player.getZ(),
                        packet.getYRot(player.getYRot()), packet.getXRot(player.getXRot())
                    );
                }
            }
            return;
        }

        if (pd.isJailActive()) {
            JailManager.checkJailBounds(player);
        }
    }
}