package me.drex.staffmod.mixin;

import me.drex.staffmod.config.DataStore;
import me.drex.staffmod.config.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class CommandMixin {

    @Shadow public ServerPlayer player;

    /** Bloquear todos los comandos si el jugador está jaileado */
    @Inject(method = "handleChatCommand", at = @At("HEAD"), cancellable = true)
    private void staffmod$blockJailedCommands(ServerboundChatCommandPacket packet, CallbackInfo ci) {
        checkJail(ci);
    }

    @Inject(method = "handleSignedChatCommand", at = @At("HEAD"), cancellable = true)
    private void staffmod$blockJailedSignedCommands(ServerboundChatCommandSignedPacket packet, CallbackInfo ci) {
        checkJail(ci);
    }

    private void checkJail(CallbackInfo ci) {
        PlayerData pd = DataStore.get(player.getUUID());
        if (pd != null && pd.isJailActive()) {
            player.sendSystemMessage(Component.literal(
                "§c[Staff] Estás en la cárcel. No puedes usar comandos. Expira: §e"
                + PlayerData.formatExpiry(pd.jailExpiry)));
            ci.cancel();
        }
    }
}
