package redxax.restudio.Remodel.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import redxax.restudio.Remodel.model.PlayerSkin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SkinFetcher {

    private static final String UUID_API = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String SKIN_API = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final Gson gson = new Gson();

    private static final ConcurrentHashMap<String, BufferedImage> skinCache = new ConcurrentHashMap<>();

    public static BufferedImage getSkin(String playerName) {
        BufferedImage cachedSkin = skinCache.get(playerName);
        if (cachedSkin != null) {
            return cachedSkin;
        }

        try {
            URL uuidUrl = new URL(UUID_API + playerName);
            HttpURLConnection uuidConn = (HttpURLConnection) uuidUrl.openConnection();
            uuidConn.setRequestMethod("GET");
            if (uuidConn.getResponseCode() != 200) {
                System.err.println("Could not find player UUID for: " + playerName);
                return null;
            }
            String uuidResponse;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(uuidConn.getInputStream()))) {
                uuidResponse = reader.lines().collect(Collectors.joining());
            }
            uuidConn.disconnect();

            JsonObject uuidJson = gson.fromJson(uuidResponse, JsonObject.class);
            String uuid = uuidJson.get("id").getAsString();

            URL skinUrl = new URL(SKIN_API + uuid);
            HttpURLConnection skinConn = (HttpURLConnection) skinUrl.openConnection();
            skinConn.setRequestMethod("GET");
            if (skinConn.getResponseCode() != 200) {
                System.err.println("Could not find skin profile for UUID: " + uuid);
                return null;
            }
            String skinResponse;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(skinConn.getInputStream()))) {
                skinResponse = reader.lines().collect(Collectors.joining());
            }
            skinConn.disconnect();

            JsonObject profileJson = gson.fromJson(skinResponse, JsonObject.class);
            String base64Properties = profileJson.getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString();
            byte[] decodedBytes = Base64.getDecoder().decode(base64Properties);
            String decodedProperties = new String(decodedBytes);

            PlayerSkin playerSkin = gson.fromJson(decodedProperties, PlayerSkin.class);
            if (playerSkin.textures == null || playerSkin.textures.SKIN == null || playerSkin.textures.SKIN.url == null) {
                System.err.println("Player " + playerName + " does not have a skin.");
                return null;
            }

            BufferedImage skinImage = ImageIO.read(new URL(playerSkin.textures.SKIN.url));
            skinCache.put(playerName, skinImage);
            return skinImage;

        } catch (Exception e) {
            System.err.println("Error fetching skin for player " + playerName + ": " + e.getMessage());
            return null;
        }
    }
}