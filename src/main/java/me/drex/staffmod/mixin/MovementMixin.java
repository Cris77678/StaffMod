package me.drex.staffmod.mixin;

import me.drex.staffmod.config.DataStore;
import me.drex.staffmod.config.PlayerData;
import me.drex.staffmod.util.JailManager;
import net.minecraft.network.chat.Component;
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

    @Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true)
    private void staffmod$handleMovement(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        PlayerData pd = DataStore.get(player.getUUID());
        if (pd == null) return;

        // Congelado: cancelar movimiento y devolver al jugador a su posición
        if (pd.frozen) {
            player.connection.teleport(
                player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot());
            ci.cancel();
            return;
        }

        // Jaileado: comprobar que no salga de la zona
        if (pd.isJailActive()) {
            JailManager.checkJailBounds(player);
        }
    }
}
