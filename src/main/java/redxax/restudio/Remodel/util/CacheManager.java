package redxax.restudio.Remodel.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import redxax.restudio.Remodel.model.CapeInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CacheManager {
    private static final String CACHE_DIR = ".remodel_cache";
    private static final long CACHE_EXPIRY_MS = TimeUnit.HOURS.toMillis(24);
    private static final Path CACHE_PATH = Paths.get(CACHE_DIR);
    private static final Path SKINS_PATH = CACHE_PATH.resolve("skins");
    private static final Path CAPES_PATH = CACHE_PATH.resolve("capes");
    private static final Path UUID_CACHE_PATH = CACHE_PATH.resolve("uuid_cache.json");

    private static final String UUID_API = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String PROFILE_API = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final Gson gson = new Gson();

    private final ConcurrentHashMap<String, BufferedImage> skinMemoryCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<CapeInfo>> capeMemoryCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> uuidCache = new ConcurrentHashMap<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(2, r -> {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        return t;
    });

    private static final CacheManager INSTANCE = new CacheManager();

    private CacheManager() {
        try {
            Files.createDirectories(SKINS_PATH);
            Files.createDirectories(CAPES_PATH);
            loadUuidCache();
        } catch (IOException e) {
            System.err.println("Failed to create cache directories or load UUID cache: " + e.getMessage());
        }
    }

    public static CacheManager getInstance() {
        return INSTANCE;
    }

    private void loadUuidCache() {
        if (Files.exists(UUID_CACHE_PATH)) {
            try (Reader reader = new FileReader(UUID_CACHE_PATH.toFile())) {
                Type type = new TypeToken<ConcurrentHashMap<String, String>>() {}.getType();
                ConcurrentHashMap<String, String> loadedMap = gson.fromJson(reader, type);
                if (loadedMap != null) {
                    uuidCache.putAll(loadedMap);
                }
            } catch (IOException e) {
                System.err.println("Failed to load UUID cache from disk: " + e.getMessage());
            }
        }
    }

    private synchronized void saveUuidCache() {
        try (Writer writer = new FileWriter(UUID_CACHE_PATH.toFile())) {
            gson.toJson(uuidCache, writer);
        } catch (IOException e) {
            System.err.println("Failed to save UUID cache to disk: " + e.getMessage());
        }
    }

    private String getUuid(String playerName) {
        String lowerCasePlayerName = playerName.toLowerCase();
        return uuidCache.computeIfAbsent(lowerCasePlayerName, name -> {
            try {
                URL url = new URL(UUID_API + name);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                if (conn.getResponseCode() != 200) {
                    System.err.println("Could not find player UUID for: " + name);
                    return null;
                }

                String response;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    response = reader.lines().collect(Collectors.joining());
                }
                conn.disconnect();

                JsonObject json = gson.fromJson(response, JsonObject.class);
                String uuid = json.get("id").getAsString();
                saveUuidCache();
                return uuid;
            } catch (Exception e) {
                System.err.println("Error fetching UUID for " + name + ": " + e.getMessage());
                return null;
            }
        });
    }

    private JsonObject getProfileProperties(String uuid) throws IOException {
        URL profileUrl = new URL(PROFILE_API + uuid);
        HttpURLConnection profileConn = (HttpURLConnection) profileUrl.openConnection();
        profileConn.setRequestMethod("GET");
        if (profileConn.getResponseCode() != 200) {
            System.err.println("Could not find profile for UUID: " + uuid);
            return null;
        }
        String profileResponse;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(profileConn.getInputStream()))) {
            profileResponse = reader.lines().collect(Collectors.joining());
        }
        profileConn.disconnect();

        JsonObject profileJson = gson.fromJson(profileResponse, JsonObject.class);
        JsonArray properties = profileJson.getAsJsonArray("properties");
        if (properties != null && !properties.isEmpty()) {
            String value = properties.get(0).getAsJsonObject().get("value").getAsString();
            String decoded = new String(Base64.getDecoder().decode(value));
            return gson.fromJson(decoded, JsonObject.class);
        }
        return null;
    }

    public BufferedImage getSkin(String playerName) {
        String uuid = getUuid(playerName);
        if (uuid == null) return null;

        if (skinMemoryCache.containsKey(uuid)) {
            return skinMemoryCache.get(uuid);
        }

        Path skinPath = SKINS_PATH.resolve(uuid + ".png");
        if (Files.exists(skinPath)) {
            try {
                long lastModified = Files.getLastModifiedTime(skinPath).toMillis();
                if (System.currentTimeMillis() - lastModified > CACHE_EXPIRY_MS) {
                    executor.submit(() -> fetchAndCacheSkin(uuid, playerName));
                }

                BufferedImage skin = ImageIO.read(skinPath.toFile());
                skinMemoryCache.put(uuid, skin);
                return skin;
            } catch (IOException e) {
                System.err.println("Failed to read skin from disk cache: " + e.getMessage());
            }
        }

        return fetchAndCacheSkin(uuid, playerName);
    }

    private BufferedImage fetchAndCacheSkin(String uuid, String playerNameForError) {
        try {
            JsonObject profile = getProfileProperties(uuid);
            if (profile == null || !profile.has("textures")) return null;

            JsonObject textures = profile.getAsJsonObject("textures");
            if (!textures.has("SKIN")) {
                System.err.println("Player " + playerNameForError + " does not have a skin.");
                return null;
            }

            String skinUrl = textures.getAsJsonObject("SKIN").get("url").getAsString();
            BufferedImage skinImage = ImageIO.read(new URL(skinUrl));

            Path skinPath = SKINS_PATH.resolve(uuid + ".png");
            ImageIO.write(skinImage, "png", skinPath.toFile());

            skinMemoryCache.put(uuid, skinImage);
            return skinImage;
        } catch (Exception e) {
            System.err.println("Error fetching skin for player " + playerNameForError + ": " + e.getMessage());
            return null;
        }
    }

    public List<CapeInfo> getCapes(String playerName) {
        String uuid = getUuid(playerName);
        if (uuid == null) return new ArrayList<>();

        if (capeMemoryCache.containsKey(uuid)) {
            return capeMemoryCache.get(uuid);
        }

        Path capeInfoPath = CAPES_PATH.resolve(uuid + ".json");
        if (Files.exists(capeInfoPath)) {
            try {
                long lastModified = Files.getLastModifiedTime(capeInfoPath).toMillis();
                if (System.currentTimeMillis() - lastModified > CACHE_EXPIRY_MS) {
                    executor.submit(() -> fetchAndCacheCapes(uuid, playerName));
                }

                String json = new String(Files.readAllBytes(capeInfoPath));
                JsonArray capeInfosJson = gson.fromJson(json, JsonArray.class);
                List<CapeInfo> capes = new ArrayList<>();

                for(JsonElement element : capeInfosJson) {
                    JsonObject capeData = element.getAsJsonObject();
                    String capeId = capeData.get("id").getAsString();
                    String capeUrl = capeData.get("url").getAsString();
                    String capeAlias = capeData.has("alias") ? capeData.get("alias").getAsString() : capeId;
                    Path capeImagePath = CAPES_PATH.resolve(capeId + ".png");

                    if(Files.exists(capeImagePath)) {
                        BufferedImage capeImage = ImageIO.read(capeImagePath.toFile());
                        capes.add(new CapeInfo(capeId, capeAlias, capeUrl, capeImage));
                    }
                }

                capeMemoryCache.put(uuid, capes);
                return capes;

            } catch (IOException e) {
                System.err.println("Failed to read cape from disk cache: " + e.getMessage());
            }
        }

        return fetchAndCacheCapes(uuid, playerName);
    }

    private List<CapeInfo> fetchAndCacheCapes(String uuid, String playerNameForError) {
        try {
            JsonObject profile = getProfileProperties(uuid);
            List<CapeInfo> capes = new ArrayList<>();

            if (profile != null && profile.has("textures")) {
                JsonObject textures = profile.getAsJsonObject("textures");
                if (textures.has("CAPE")) {
                    JsonObject capeObj = textures.getAsJsonObject("CAPE");
                    if (capeObj.has("url")) {
                        String capeUrl = capeObj.get("url").getAsString();
                        String[] segments = capeUrl.split("/");
                        String id = segments[segments.length - 1];

                        BufferedImage capeImage = ImageIO.read(new URL(capeUrl));
                        Path capeImagePath = CAPES_PATH.resolve(id + ".png");
                        ImageIO.write(capeImage, "png", capeImagePath.toFile());

                        capes.add(new CapeInfo(id, id, capeUrl, capeImage));
                    }
                }
            }

            JsonArray capeInfosJson = new JsonArray();
            for (CapeInfo cape : capes) {
                JsonObject capeInfoJson = new JsonObject();
                capeInfoJson.addProperty("id", cape.id);
                capeInfoJson.addProperty("alias", cape.alias);
                capeInfoJson.addProperty("url", cape.url);
                capeInfosJson.add(capeInfoJson);
            }

            Path capeListPath = CAPES_PATH.resolve(uuid + ".json");
            try (Writer writer = new FileWriter(capeListPath.toFile())) {
                gson.toJson(capeInfosJson, writer);
            }

            capeMemoryCache.put(uuid, capes);
            return capes;
        } catch (Exception e) {
            System.err.println("Error fetching capes for player " + playerNameForError + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}