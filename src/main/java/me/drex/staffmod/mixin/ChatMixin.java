package me.drex.staffmod.mixin;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;

/**
 * ARCHIVO VACÍO INTENCIONALMENTE.
 * La lógica de bloqueo de chat se ha movido a StaffMod.java usando 
 * ServerMessageEvents para evitar expulsiones por firmas inválidas en 1.21.1.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ChatMixin {
    // Mantén el archivo para no romper fabric.mod.json, pero déjalo vacío.
}