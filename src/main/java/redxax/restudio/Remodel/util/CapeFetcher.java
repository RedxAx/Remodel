package redxax.restudio.Remodel.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import redxax.restudio.Remodel.model.CapeInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CapeFetcher {
    private static final String UUID_API = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String SKIN_API = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final Gson gson = new Gson();

    private static final ConcurrentHashMap<String, List<CapeInfo>> capeCache = new ConcurrentHashMap<>();

    public static List<CapeInfo> getCapes(String playerName) {
        if (capeCache.containsKey(playerName)) {
            return capeCache.get(playerName);
        }

        try {
            URL uuidUrl = new URL(UUID_API + playerName);
            HttpURLConnection uuidConn = (HttpURLConnection) uuidUrl.openConnection();
            uuidConn.setRequestMethod("GET");
            if (uuidConn.getResponseCode() != 200) {
                System.err.println("Could not find player UUID for: " + playerName);
                return Collections.emptyList();
            }
            String uuidResponse;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(uuidConn.getInputStream()))) {
                uuidResponse = reader.lines().collect(Collectors.joining());
            }
            uuidConn.disconnect();

            JsonObject uuidJson = gson.fromJson(uuidResponse, JsonObject.class);
            String uuid = uuidJson.get("id").getAsString();

            URL profileUrl = new URL(SKIN_API + uuid);
            HttpURLConnection profileConn = (HttpURLConnection) profileUrl.openConnection();
            profileConn.setRequestMethod("GET");
            if (profileConn.getResponseCode() != 200) {
                System.err.println("Could not find skin profile for UUID: " + uuid);
                return Collections.emptyList();
            }
            String profileResponse;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(profileConn.getInputStream()))) {
                profileResponse = reader.lines().collect(Collectors.joining());
            }
            profileConn.disconnect();

            JsonObject profileJson = gson.fromJson(profileResponse, JsonObject.class);
            String base64Properties = profileJson.getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString();
            byte[] decodedBytes = Base64.getDecoder().decode(base64Properties);
            String decodedProperties = new String(new String(decodedBytes));

            List<CapeInfo> capes = new ArrayList<>();
            JsonObject texturesJson = gson.fromJson(decodedProperties, JsonObject.class);
            if (texturesJson.has("textures")) {
                JsonObject texturesObj = texturesJson.getAsJsonObject("textures");
                if (texturesObj.has("CAPE")) {
                    JsonObject capeObj = texturesObj.getAsJsonObject("CAPE");
                    if (capeObj.has("url")) {
                        String capeUrl = capeObj.get("url").getAsString();
                        BufferedImage capeImage = ImageIO.read(new URL(capeUrl));
                        String[] segments = capeUrl.split("/");
                        String id = segments[segments.length - 1];
                        capes.add(new CapeInfo(id, capeUrl, capeImage));
                    }
                }
            }

            capeCache.put(playerName, capes);
            return capes;

        } catch (Exception e) {
            System.err.println("Error fetching capes for player " + playerName + ": " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}