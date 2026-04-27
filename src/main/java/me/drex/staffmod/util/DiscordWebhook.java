package me.drex.staffmod.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.drex.staffmod.StaffMod;
import me.drex.staffmod.core.StaffModAsync;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {

    // Configura aquí la URL de tu webhook de Discord (canal privado de logs de PokeLand)
    private static final String WEBHOOK_URL = "AQUI_TU_WEBHOOK_URL";

    /**
     * Envía un Embed a Discord de forma 100% asíncrona.
     */
    public static void sendEmbed(String title, String description, int colorHex) {
        if (WEBHOOK_URL.isEmpty() || WEBHOOK_URL.equals("AQUI_TU_WEBHOOK_URL")) return;

        StaffModAsync.runAsync(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Construcción del JSON nativo para Discord
                JsonObject embed = new JsonObject();
                embed.addProperty("title", title);
                embed.addProperty("description", description);
                embed.addProperty("color", colorHex);

                JsonArray embeds = new JsonArray();
                embeds.add(embed);

                JsonObject json = new JsonObject();
                json.add("embeds", embeds);
                json.addProperty("username", "StaffCore | PokeLand Auditor");

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                connection.getResponseCode(); // Ejecuta la petición
            } catch (Exception e) {
                StaffMod.LOGGER.error("[StaffMod] Fallo al enviar webhook a Discord:", e);
            }
        });
    }
}