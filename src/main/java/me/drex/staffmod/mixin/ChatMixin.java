package me.drex.staffmod.mixin;

import me.drex.staffmod.config.DataStore;
import me.drex.staffmod.config.PlayerData;
import me.drex.staffmod.gui.ActionExecutor;
import me.drex.staffmod.util.PermissionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.level.ServerPlayer;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ChatMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handleChat", at = @At("HEAD"), cancellable = true)
    private void staffmod$blockMutedChat(ServerboundChatPacket packet, CallbackInfo ci) {
        PlayerData pd = DataStore.get(player.getUUID());
        if (pd != null && pd.isMuteActive()) {
            player.sendSystemMessage(Component.literal(
                "§c[Staff] Estás muteado. Expira: §e" + PlayerData.formatExpiry(pd.muteExpiry)));
            ci.cancel();
            return;
        }

        // NUEVO: Redirigir el mensaje si el Staff Chat está fijado
        if (DataStore.isStaffChatToggled(player.getUUID()) && PermissionUtil.has(player, "staffmod.use")) {
            ActionExecutor.sendStaffChatMessage(player, packet.message());
            ci.cancel();
        }
    }
}
