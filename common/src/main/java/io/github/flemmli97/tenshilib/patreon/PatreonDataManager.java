package io.github.flemmli97.tenshilib.patreon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.flemmli97.tenshilib.TenshiLib;
import net.minecraft.util.GsonHelper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class PatreonDataManager {

    private static final String url = "https://gist.githubusercontent.com/Flemmli97/81636b52dc2f031ee8319b145c6808a5/raw/c67fcb69d39a55d0b0b24983be37be49f329136b/patreon.json";
    private static Map<String, PatreonPlayerInfo> players;
    private static boolean reading = true;
    private static final Gson GSON = new GsonBuilder().create();

    public static void init() {
        if (players == null) {
            new Thread(() -> {
                JsonArray arr = null;
                try {
                    URLConnection conn = new URL(url).openConnection();
                    Reader reader = new InputStreamReader(conn.getInputStream());
                    arr = GSON.fromJson(reader, JsonArray.class);
                    reader.close();
                } catch (IOException e) {
                    TenshiLib.logger.error("Couldn't get patreon file");
                    e.printStackTrace();
                }
                if (arr != null) {
                    players = new HashMap<>();
                    arr.forEach(el -> {
                        if (el.isJsonObject()) {
                            JsonObject o = el.getAsJsonObject();
                            RenderLocation loc = null;
                            try {
                                loc = RenderLocation.valueOf(GsonHelper.getAsString(o, "defaultRenderLocation"));
                            } catch (IllegalArgumentException | JsonSyntaxException ignored) {
                            }
                            PatreonPlayerInfo info = new PatreonPlayerInfo(
                                    GsonHelper.getAsInt(o, "tier", 1),
                                    GsonHelper.getAsString(o, "defaultEffect", ""),
                                    loc,
                                    GsonHelper.getAsInt(o, "defaultColor", 0xFFFFFFFF));
                            players.put(GsonHelper.getAsString(o, "uuid"), info);
                        }
                    });
                }
                reading = false;
            }).run();
        }
    }

    public static PatreonPlayerInfo get(String uuid) {
        if (reading || players == null) {
            return PatreonPlayerInfo.nonPatreon;
        }
        return players.getOrDefault(uuid, PatreonPlayerInfo.nonPatreon);
    }

    public record PatreonPlayerInfo(int tier, String defaultEffect,
                                    RenderLocation defaultRenderLocation, int color) {

        static PatreonPlayerInfo nonPatreon = new PatreonPlayerInfo(0, "", RenderLocation.HAT, 0xFFFFFFFF);
    }
}
