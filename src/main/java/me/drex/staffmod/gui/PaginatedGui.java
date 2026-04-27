package me.drex.staffmod.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Framework abstracto para cualquier GUI que requiera paginación.
 * Maneja automáticamente la navegación y el cálculo de índices.
 */
public abstract class PaginatedGui<T> extends SimpleGui {

    protected final SimpleGui parent;
    protected List<T> data;
    protected int currentPage = 0;
    protected static final int PAGE_SIZE = 45; // Usamos los primeros 45 slots (5 filas) para datos

    public PaginatedGui(ServerPlayer player, SimpleGui parent, Component title, List<T> data) {
        super(MenuType.GENERIC_9x6, player, false);
        this.parent = parent;
        this.data = data;
        setTitle(title);
    }

    /**
     * Dibuja los elementos de la página actual y la barra de navegación inferior.
     */
    protected void build() {
        // Limpiar GUI completa antes de redibujar
        for (int i = 0; i < getSize(); i++) clearSlot(i);

        int maxPages = Math.max(1, (int) Math.ceil((double) data.size() / PAGE_SIZE));
        if (currentPage >= maxPages) currentPage = maxPages - 1;
        if (currentPage < 0) currentPage = 0;

        int startIndex = currentPage * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, data.size());

        // Llenar datos de la página actual
        for (int i = startIndex; i < endIndex; i++) {
            int slot = i - startIndex;
            setSlot(slot, buildItem(data.get(i)));
        }

        buildNavigation(maxPages);
    }

    /**
     * Método que las clases hijas deben implementar para transformar un dato (T) en un Ítem Visual.
     */
    protected abstract GuiElementBuilder buildItem(T item);

    private void buildNavigation(int maxPages) {
        // Fondo de la barra inferior (Fila 6)
        for (int i = 45; i < 54; i++) {
            setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal(" ")).build());
        }

        // Botón Anterior
        if (currentPage > 0) {
            setSlot(48, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e← Página Anterior"))
                .setCallback((idx, type, action, gui) -> {
                    currentPage--;
                    build();
                }).build());
        }

        // Indicador de Página
        setSlot(49, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§aPágina " + (currentPage + 1) + " de " + maxPages))
            .addLoreLine(Component.literal("§7Total de registros: §f" + data.size()))
            .build());

        // Botón Siguiente
        if (currentPage < maxPages - 1) {
            setSlot(50, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§eSiguiente Página →"))
                .setCallback((idx, type, action, gui) -> {
                    currentPage++;
                    build();
                }).build());
        }

        // Botón Volver o Cerrar
        if (parent != null) {
            setSlot(53, new GuiElementBuilder(Items.DARK_OAK_DOOR)
                .setName(Component.literal("§cVolver al Menú"))
                .setCallback((idx, type, action, gui) -> parent.open())
                .build());
        } else {
            setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§cCerrar"))
                .setCallback((idx, type, action, gui) -> this.close())
                .build());
        }
    }
}