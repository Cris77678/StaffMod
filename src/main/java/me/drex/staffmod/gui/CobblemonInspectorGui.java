package me.drex.staffmod.gui;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

public class CobblemonInspectorGui extends SimpleGui {

    private final ServerPlayer staff;
    private final ServerPlayer target;

    public CobblemonInspectorGui(ServerPlayer staff, ServerPlayer target) {
        super(MenuType.GENERIC_9x3, staff, false);
        this.staff = staff;
        this.target = target;
        setTitle(Component.literal("§8❖ §3Party de §f" + target.getName().getString()));
        build();
    }

    private void build() {
        // Decoración de fondo
        for (int i = 0; i < getSize(); i++) {
            setSlot(i, new GuiElementBuilder(Items.LIGHT_BLUE_STAINED_GLASS_PANE).setName(Component.literal(" ")).build());
        }

        // Obtener la party del jugador usando la API nativa de Cobblemon
        PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(target);

        // Los slots 10 al 15 son el centro de la fila de 9x3
        for (int i = 0; i < party.size(); i++) {
            Pokemon pokemon = party.get(i);
            if (pokemon == null) continue;

            int slot = 10 + i;
            
            // Construcción del ícono visual
            boolean isShiny = pokemon.getShiny();
            String name = (isShiny ? "§e✨ " : "§b") + pokemon.getSpecies().getName();
            
            // Cálculos básicos de Stats
            int totalIvs = pokemon.getIvs().getHp() + pokemon.getIvs().getAttack() + pokemon.getIvs().getDefence() 
                         + pokemon.getIvs().getSpecialAttack() + pokemon.getIvs().getSpecialDefence() + pokemon.getIvs().getSpeed();
            int ivPercentage = (totalIvs * 100) / 186; // 31 * 6 = 186

            setSlot(slot, new GuiElementBuilder(Items.PAPER) // Podrías cambiarlo por el ítem de la foto del Pokémon si Cobblemon expone el ItemStack
                .setName(Component.literal("§l" + name + " §7(Nv. " + pokemon.getLevel() + ")"))
                .addLoreLine(Component.literal("§8----------------------"))
                .addLoreLine(Component.literal("§7Naturaleza: §f" + pokemon.getNature().getName().getPath()))
                .addLoreLine(Component.literal("§7Habilidad: §f" + pokemon.getAbility().getName()))
                .addLoreLine(Component.literal("§7Shiny: " + (isShiny ? "§aSí" : "§cNo")))
                .addLoreLine(Component.literal("§8----------------------"))
                .addLoreLine(Component.literal("§d§lIVs §7(Perfectos: " + ivPercentage + "%)"))
                .addLoreLine(Component.literal("§cHP: " + pokemon.getIvs().getHp() + " §7| §6ATK: " + pokemon.getIvs().getAttack() + " §7| §eDEF: " + pokemon.getIvs().getDefence()))
                .addLoreLine(Component.literal("§9SPA: " + pokemon.getIvs().getSpecialAttack() + " §7| §aSPD: " + pokemon.getIvs().getSpecialDefence() + " §7| §bSPE: " + pokemon.getIvs().getSpeed()))
                .build());
        }

        // Botón Cerrar
        setSlot(26, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§cCerrar Inspector"))
            .setCallback((idx, type, action, gui) -> this.close())
            .build());
    }
}