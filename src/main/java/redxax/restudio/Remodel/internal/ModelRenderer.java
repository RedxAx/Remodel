package redxax.restudio.Remodel.internal;

import com.google.gson.*;
import org.lwjgl.opengl.GL11;
import redxax.restudio.Remodel.animation.AnimationClip;
import redxax.restudio.Remodel.animation.AnimationPlayer;
import redxax.restudio.Remodel.animation.BoneAnimation;
import redxax.restudio.Remodel.animation.InterpolationType;
import redxax.restudio.Remodel.animation.KeyFrame;
import redxax.restudio.Remodel.model.BBCube;
import redxax.restudio.Remodel.model.BBFace;
import redxax.restudio.Remodel.model.BBModel;
import redxax.restudio.Remodel.model.BBTexture;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ModelRenderer {

    private static class BoneInfo {
        String uuid;
        String name;
        float[] origin;
        float[] rotation = {0, 0, 0};
        String parentUuid;
        List<String> childUuids = new ArrayList<>();
        List<String> cubeUuids = new ArrayList<>();

        BoneInfo(String uuid, String name, float[] origin) {
            this.uuid = uuid;
            this.name = name;
            this.origin = origin;
        }
    }

    private static final float[] LIGHT_DIR = {0.5f, 0.8f, 0.5f};
    private static final float[] NORMALIZED_LIGHT_DIR;

    static {
        float len = (float) Math.sqrt(LIGHT_DIR[0] * LIGHT_DIR[0] + LIGHT_DIR[1] * LIGHT_DIR[1] + LIGHT_DIR[2] * LIGHT_DIR[2]);
        NORMALIZED_LIGHT_DIR = new float[]{LIGHT_DIR[0] / len, LIGHT_DIR[1] / len, LIGHT_DIR[2] / len};
    }

    private BBModel model;
    private final List<Integer> textureIds = new ArrayList<>();
    private final Map<String, BBCube> cubeMap = new HashMap<>();
    private final Map<String, BoneInfo> bones = new HashMap<>();
    private final List<String> rootBones = new ArrayList<>();
    private final Map<String, Integer> cubeMeshes = new HashMap<>();

    private float posX, posY, posZ;
    private float rotX, rotY, rotZ;
    private float scaleX = 1, scaleY = 1, scaleZ = 1;

    public AnimationPlayer player;
    private long lastNs;

    private final float[] color;
    private int customTextureId = -1;
    private final boolean shading;

    public ModelRenderer(String p) {
        this(p, null, false);
    }

    public ModelRenderer(String p, String hexColor) {
        this(p, hexColor, false);
    }

    public ModelRenderer(String p, String hexColor, boolean shading) {
        this.shading = shading;
        if (hexColor != null && hexColor.matches("^#[0-9a-fA-F]{6}$")) {
            int r = Integer.valueOf(hexColor.substring(1, 3), 16);
            int g = Integer.valueOf(hexColor.substring(3, 5), 16);
            int b = Integer.valueOf(hexColor.substring(5, 7), 16);
            this.color = new float[]{r / 255f, g / 255f, b / 255f};
        } else {
            this.color = null;
        }

        try {
            InputStream in;
            File file = new File(p);
            if (file.exists()) {
                in = new FileInputStream(file);
            } else {
                in = ModelRenderer.class.getClassLoader().getResourceAsStream(p);
            }

            if (in == null) {
                throw new FileNotFoundException("Model file not found at path: " + p);
            }

            try (InputStream stream = in) {
                String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();

                model = new Gson().fromJson(json, BBModel.class);
                if (model != null) {
                    if (model.elements != null) {
                        for (BBCube cube : model.elements) {
                            cubeMap.put(cube.uuid, cube);
                        }
                    }
                    if (this.color == null && model.textures != null) {
                        for (BBTexture t : model.textures) {
                            textureIds.add(TextureLoader.loadTexture64(t.source));
                        }
                    }
                }

                parseOutliner(root);
                buildMeshes();
                player = new AnimationPlayer(loadClips(root));

                if (player.hasClip("idle")) {
                    player.play("idle");
                } else {
                    player.playFirst();
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Model file not found at path: " + p, e);
        } catch (Exception e) {
            System.err.println("[ModelRenderer] Error loading model: " + e.getMessage());
            e.printStackTrace();
            if (player == null) {
                player = new AnimationPlayer(new HashMap<>());
            }
        }
    }

    private void addVertex(float u, float v, float x, float y, float z) {
        if (this.color == null) {
            GL11.glTexCoord2f(u, v);
        }
        GL11.glVertex3f(x, y, z);
    }

    private void buildMeshes() {
        for (BBCube cube : cubeMap.values()) {
            int id = GL11.glGenLists(1);
            GL11.glNewList(id, GL11.GL_COMPILE);

            float inf = cube.inflate != null ? cube.inflate / 16f : 0f;
            float x0 = (cube.from[0] / 16f) - inf;
            float y0 = (cube.from[1] / 16f) - inf;
            float z0 = (cube.from[2] / 16f) - inf;
            float x1 = (cube.to[0] / 16f) + inf;
            float y1 = (cube.to[1] / 16f) + inf;
            float z1 = (cube.to[2] / 16f) + inf;

            if (cube.faces == null) continue;

            for (Map.Entry<String, BBFace> entry : cube.faces.entrySet()) {
                String fn = entry.getKey();
                BBFace f = entry.getValue();

                if (shading) {
                    float[] normal = {0, 0, 0};
                    normal = switch (fn) {
                        case "north" -> new float[]{0, 0, 1};
                        case "south" -> new float[]{0, 0, -1};
                        case "west" -> new float[]{1, 0, 0};
                        case "east" -> new float[]{-1, 0, 0};
                        case "up" -> new float[]{0, 1, 0};
                        case "down" -> new float[]{0, -1, 0};
                        default -> normal;
                    };

                    float dot = normal[0] * NORMALIZED_LIGHT_DIR[0] + normal[1] * NORMALIZED_LIGHT_DIR[1] + normal[2] * NORMALIZED_LIGHT_DIR[2];

                    float ambientTerm = 0.6f;
                    float diffuseTerm = 0.6f;

                    ambientTerm *= (0.7f + normal[1] * 0.3f);

                    float intensity = ambientTerm + diffuseTerm * Math.max(0, dot);
                    intensity = Math.min(1.0f, intensity);

                    if (this.color != null) {
                        GL11.glColor3f(this.color[0] * intensity, this.color[1] * intensity, this.color[2] * intensity);
                    } else {
                        GL11.glColor3f(intensity, intensity, intensity);
                    }
                }


                float u1 = 0, v1 = 0, u2 = 0, v2 = 0;
                if(this.color == null && model.resolution != null && f.uv != null) {
                    u1 = f.uv[0] / model.resolution.width;
                    v1 = f.uv[1] / model.resolution.height;
                    u2 = f.uv[2] / model.resolution.width;
                    v2 = f.uv[3] / model.resolution.height;
                }

                GL11.glBegin(GL11.GL_QUADS);
                switch (fn) {
                    case "north":
                        GL11.glNormal3f(0, 0, -1);
                        addVertex(u2, v2, x0, y0, z0);
                        addVertex(u1, v2, x1, y0, z0);
                        addVertex(u1, v1, x1, y1, z0);
                        addVertex(u2, v1, x0, y1, z0);
                        break;
                    case "south":
                        GL11.glNormal3f(0, 0, 1);
                        addVertex(u1, v2, x0, y0, z1);
                        addVertex(u2, v2, x1, y0, z1);
                        addVertex(u2, v1, x1, y1, z1);
                        addVertex(u1, v1, x0, y1, z1);
                        break;
                    case "west":
                        GL11.glNormal3f(-1, 0, 0);
                        addVertex(u2, v2, x0, y0, z1);
                        addVertex(u1, v2, x0, y0, z0);
                        addVertex(u1, v1, x0, y1, z0);
                        addVertex(u2, v1, x0, y1, z1);
                        break;
                    case "east":
                        GL11.glNormal3f(1, 0, 0);
                        addVertex(u1, v2, x1, y0, z1);
                        addVertex(u2, v2, x1, y0, z0);
                        addVertex(u2, v1, x1, y1, z0);
                        addVertex(u1, v1, x1, y1, z1);
                        break;
                    case "up":
                        GL11.glNormal3f(0, 1, 0);
                        addVertex(u1, v2, x0, y1, z1);
                        addVertex(u2, v2, x1, y1, z1);
                        addVertex(u2, v1, x1, y1, z0);
                        addVertex(u1, v1, x0, y1, z0);
                        break;
                    case "down":
                        GL11.glNormal3f(0, -1, 0);
                        addVertex(u1, v1, x0, y0, z1);
                        addVertex(u2, v1, x1, y0, z1);
                        addVertex(u2, v2, x1, y0, z0);
                        addVertex(u1, v2, x0, y0, z0);
                        break;
                }
                GL11.glEnd();
            }

            GL11.glEndList();
            cubeMeshes.put(cube.uuid, id);
        }
    }

    private Map<String, AnimationClip> loadClips(JsonObject root) {
        Map<String, AnimationClip> clips = new HashMap<>();
        if (!root.has("animations")) {
            return clips;
        }

        JsonArray animationsArray = root.getAsJsonArray("animations");
        for (JsonElement el : animationsArray) {
            JsonObject a = el.getAsJsonObject();
            if (!a.has("name")) continue;

            AnimationClip clip = new AnimationClip();
            clip.name = a.get("name").getAsString();
            clip.length = a.has("length") ? a.get("length").getAsFloat() : 1.0f;
            clip.loop = a.has("loop") && !"once".equalsIgnoreCase(a.get("loop").getAsString());

            if (!a.has("animators")) continue;

            JsonObject animators = a.getAsJsonObject("animators");
            for (Map.Entry<String, JsonElement> e : animators.entrySet()) {
                String boneUuid = e.getKey();
                JsonObject animatorObj = e.getValue().getAsJsonObject();
                String boneName = animatorObj.has("name") ? animatorObj.get("name").getAsString() : "Unknown";
                if (!animatorObj.has("keyframes")) continue;
                BoneAnimation ba = new BoneAnimation();
                ba.boneName = boneName;
                ba.boneUuid = boneUuid;

                JsonArray keyframes = animatorObj.getAsJsonArray("keyframes");
                for (JsonElement kfe : keyframes) {
                    JsonObject kf = kfe.getAsJsonObject();
                    if (!kf.has("channel") || !kf.has("time")) continue;

                    String ch = kf.get("channel").getAsString();
                    if (!ch.equals("position") && !ch.equals("rotation") && !ch.equals("scale") && !ch.equals("visibility")) continue;

                    float t = kf.get("time").getAsFloat();
                    String interp = kf.has("interpolation") ? kf.get("interpolation").getAsString() : "linear";

                    JsonObject dataObj = kf;
                    if (kf.has("data_points") && kf.get("data_points").isJsonArray()) {
                        JsonArray dataPoints = kf.getAsJsonArray("data_points");
                        if (!dataPoints.isEmpty() && dataPoints.get(0).isJsonObject()) {
                            dataObj = dataPoints.get(0).getAsJsonObject();
                        }
                    }

                    if (!(dataObj.has("x") || dataObj.has("y") || dataObj.has("z"))) continue;

                    float defX = 0, defY = 0, defZ = 0;
                    if (ch.equals("scale")) defX = defY = defZ = 1;

                    float x = dataObj.has("x") ? parseFloatValue(dataObj.get("x")) : defX;
                    float y = dataObj.has("y") ? parseFloatValue(dataObj.get("y")) : defY;
                    float z = dataObj.has("z") ? parseFloatValue(dataObj.get("z")) : defZ;

                    ba.add(ch, new KeyFrame(t, new float[]{x, y, z}, InterpolationType.fromString(interp)));
                }
                clip.bones.put(boneUuid, ba);
            }

            for (BoneAnimation ba : clip.bones.values()) {
                for (List<KeyFrame> l : ba.channels.values()) {
                    l.sort(Comparator.comparingDouble(k -> k.time));
                }
            }
            clips.put(clip.name, clip);
        }
        return clips;
    }

    private float parseFloatValue(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            try {
                return Float.parseFloat(element.getAsString());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return element.getAsFloat();
    }

    private void parseOutliner(JsonObject root) {
        if (!root.has("outliner")) return;
        JsonArray outliner = root.getAsJsonArray("outliner");
        for (JsonElement el : outliner) {
            parseBone(el, null);
        }
    }

    private void parseBone(JsonElement el, String parentUuid) {
        if (el.isJsonObject()) {
            JsonObject o = el.getAsJsonObject();
            if (o.has("uuid") && o.has("name") && o.has("origin")) {
                String uuid = o.get("uuid").getAsString();
                String name = o.get("name").getAsString();
                JsonArray originArray = o.getAsJsonArray("origin");

                if (originArray.size() >= 3) {
                    float[] origin = new float[]{
                            originArray.get(0).getAsFloat(),
                            originArray.get(1).getAsFloat(),
                            originArray.get(2).getAsFloat()
                    };

                    BoneInfo bone = new BoneInfo(uuid, name, origin);

                    if (o.has("rotation")) {
                        JsonArray rotArray = o.getAsJsonArray("rotation");
                        if (rotArray.size() >= 3) {
                            bone.rotation = new float[]{
                                    rotArray.get(0).getAsFloat(),
                                    rotArray.get(1).getAsFloat(),
                                    rotArray.get(2).getAsFloat()
                            };
                        }
                    }

                    bone.parentUuid = parentUuid;
                    bones.put(uuid, bone);

                    if (parentUuid == null) {
                        rootBones.add(uuid);
                    } else if (bones.containsKey(parentUuid)) {
                        bones.get(parentUuid).childUuids.add(uuid);
                    }

                    if (o.has("children")) {
                        for (JsonElement child : o.getAsJsonArray("children")) {
                            if (child.isJsonPrimitive()) {
                                bone.cubeUuids.add(child.getAsString());
                            } else {
                                parseBone(child, uuid);
                            }
                        }
                    }
                }
            }
        }
    }

    public void setPosition(float x, float y, float z) {
        posX = x;
        posY = y;
        posZ = z;
    }

    public void setRotation(float x, float y, float z) {
        rotX = x;
        rotY = y;
        rotZ = z;
    }

    public void setScale(float x, float y, float z) {
        scaleX = x;
        scaleY = y;
        scaleZ = z;
    }

    public void setScale(float s) {
        scaleX = scaleY = scaleZ = s;
    }

    public void playAnimation(String name) {
        if (player != null)
            player.play(name);
    }

    public void setTexture(int textureId) {
        this.customTextureId = textureId;
    }

    public void clearTexture() {
        this.customTextureId = -1;
    }

    public void render() {
        if (model == null) return;

        long n = System.nanoTime();
        float dt = lastNs == 0 ? 0 : (n - lastNs) * 1e-9f;
        lastNs = n;

        if (player != null) player.update(dt);

        GL11.glPushMatrix();
        GL11.glTranslatef(posX, posY, posZ);
        GL11.glRotatef(rotX, 1, 0, 0);
        GL11.glRotatef(rotY, 0, 1, 0);
        GL11.glRotatef(rotZ, 0, 0, 1);
        GL11.glScalef(scaleX, scaleY, scaleZ);

        boolean isColored = this.color != null;
        if (isColored) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            if (!shading) {
                GL11.glColor3f(this.color[0], this.color[1], this.color[2]);
            }
        } else {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor3f(1f, 1f, 1f);
            if (customTextureId != -1) {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, customTextureId);
            } else if (!textureIds.isEmpty()) {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIds.getFirst());
            }
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.01f);

        if (bones.isEmpty()) {
            if (model.elements != null) {
                for (BBCube cube : model.elements) {
                    renderCube(cube);
                }
            }
        } else {
            for (String rootBoneUuid : rootBones) {
                renderBoneHierarchy(bones.get(rootBoneUuid));
            }
        }

        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);

        if (isColored) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        } else {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
        }
        GL11.glPopMatrix();
    }

    private void renderBoneHierarchy(BoneInfo bone) {
        if (bone == null)
            return;

        float[] p = player != null ? player.pos(bone.uuid) : new float[]{0, 0, 0};
        float[] r = player != null ? player.rot(bone.uuid) : new float[]{0, 0, 0};
        float[] s = player != null ? player.scl(bone.uuid) : new float[]{1, 1, 1};
        float visibility = player != null ? player.vis(bone.uuid) : 1f;

        GL11.glPushMatrix();

        GL11.glTranslatef((bone.origin[0] + p[0]) / 16f, (bone.origin[1] + p[1]) / 16f, (bone.origin[2] + p[2]) / 16f);

        GL11.glRotatef(bone.rotation[2] + r[2], 0, 0, 1);
        GL11.glRotatef(bone.rotation[1] + r[1], 0, 1, 0);
        GL11.glRotatef(bone.rotation[0] + r[0], 1, 0, 0);

        GL11.glScalef(s[0], s[1], s[2]);

        GL11.glTranslatef(-bone.origin[0] / 16f, -bone.origin[1] / 16f, -bone.origin[2] / 16f);

        if (visibility > 0) {
            for (String cubeUuid : bone.cubeUuids) {
                renderCube(cubeMap.get(cubeUuid));
            }

            for (String childUuid : bone.childUuids) {
                renderBoneHierarchy(bones.get(childUuid));
            }
        }

        GL11.glPopMatrix();
    }

    private void renderCube(BBCube cube) {
        if (cube == null || (cube.visibility != null && !cube.visibility) || (cube.name.equals("shadow") || cube.name.equals("hitbox"))) return;
        GL11.glPushMatrix();
        if (cube.origin != null) {
            GL11.glTranslatef(cube.origin[0] / 16f, cube.origin[1] / 16f, cube.origin[2] / 16f);
        }
        if (cube.rotation != null && cube.rotation.length >= 3) {
            GL11.glRotatef(cube.rotation[2], 0, 0, 1);
            GL11.glRotatef(cube.rotation[1], 0, 1, 0);
            GL11.glRotatef(cube.rotation[0], 1, 0, 0);
        }
        if (cube.origin != null) {
            GL11.glTranslatef(-cube.origin[0] / 16f, -cube.origin[1] / 16f, -cube.origin[2] / 16f);
        }
        Integer list = cubeMeshes.get(cube.uuid);
        if (list != null)
            GL11.glCallList(list);
        GL11.glPopMatrix();
    }
}