package redxax.restudio.Remodel.api;

import org.lwjgl.opengl.GL11;
import redxax.restudio.Remodel.internal.TextureLoader;
import redxax.restudio.Remodel.model.CapeInfo;
import redxax.restudio.Remodel.util.SkinFetcher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class PlayerRemodel {

    private final BlockbenchRemodel defaultModel;
    private final BlockbenchRemodel slimModel;
    private final CapeRemodel cape;

    private boolean slim = false;
    private int customTextureId = -1;
    private boolean showCape = true;
    private boolean idleAnimationEnabled;
    private PlayerEquipment equipment = PlayerEquipment.empty();
    private PlayerEquipmentRenderer equipmentRenderer;
    private float posX, posY, posZ;
    private float rotX, rotY, rotZ;
    private float scaleX = 1.0f, scaleY = 1.0f, scaleZ = 1.0f;

    private PlayerRemodel(BlockbenchRemodel defaultModel, BlockbenchRemodel slimModel, CapeRemodel cape) {
        this.defaultModel = defaultModel;
        this.slimModel = slimModel;
        this.cape = cape;
    }

    public static PlayerRemodel create() {
        return new PlayerRemodelBuilder().build();
    }

    public static PlayerRemodelBuilder builder() {
        return new PlayerRemodelBuilder();
    }

    private BlockbenchRemodel getCurrentModel() {
        return slim ? slimModel : defaultModel;
    }

    public void setSlim(boolean slim) {
        this.slim = slim;
    }

    public boolean isSlim() {
        return slim;
    }

    public void render() {
        if (idleAnimationEnabled) {
            applyIdleAnimationAge(defaultIdleAgeInTicks());
        }
        getCurrentModel().render();
        renderEquipment();
        if (showCape && cape != null) {
            cape.render();
        }
    }

    public void playAnimation(String name) {
        defaultModel.playAnimation(name);
        slimModel.playAnimation(name);
        if (cape != null) {
            cape.playAnimation(name);
        }
    }

    public void setPosition(float x, float y, float z) {
        posX = x;
        posY = y;
        posZ = z;
        defaultModel.setPosition(x, y, z);
        slimModel.setPosition(x, y, z);
        if (cape != null) {
            cape.setPosition(x, y, z);
        }
    }

    public void setRotation(float x, float y, float z) {
        rotX = x;
        rotY = y;
        rotZ = z;
        defaultModel.setRotation(x, y, z);
        slimModel.setRotation(x, y, z);
        if (cape != null) {
            cape.setRotation(x, y, z);
        }
    }

    public void setScale(float x, float y, float z) {
        scaleX = x;
        scaleY = y;
        scaleZ = z;
        defaultModel.setScale(x, y, z);
        slimModel.setScale(x, y, z);
        if (cape != null) {
            cape.setScale(x, y, z);
        }
    }

    public void setScale(float s) {
        scaleX = s;
        scaleY = s;
        scaleZ = s;
        defaultModel.setScale(s);
        slimModel.setScale(s);
        if (cape != null) {
            cape.setScale(s);
        }
    }

    private void clearSkinTexture() {
        if (this.customTextureId != -1) {
            GL11.glDeleteTextures(this.customTextureId);
            this.customTextureId = -1;
        }
        defaultModel.clearTexture();
        slimModel.clearTexture();
    }

    public void setTexture(BufferedImage image) {
        clearSkinTexture();
        if (image == null) return;
        this.customTextureId = TextureLoader.loadTexture(image);
        if (this.customTextureId != -1) {
            defaultModel.setTexture(this.customTextureId);
            slimModel.setTexture(this.customTextureId);
        }
    }

    public void setTexture(String path) throws IOException {
        setTexture(ImageIO.read(new File(path)));
    }

    public void setTextureByPlayerName(String playerName) {
        setTextureByPlayerName(playerName, true);
    }

    public void setTextureByPlayerName(String playerName, boolean updateCape) {
        BufferedImage skin = SkinFetcher.getSkin(playerName);
        setTexture(skin);
        if (cape != null && updateCape) {
            cape.setTextureByPlayerName(playerName);
        }
    }

    public void clearTexture() {
        clearSkinTexture();
        if (cape != null) {
            cape.clearTexture();
        }
    }

    public void setShowCape(boolean show) {
        this.showCape = show;
    }

    public boolean isCapeVisible() {
        return this.showCape;
    }

    public void setCapeMode(CapeMode mode) {
        if (cape != null) {
            cape.setMode(mode);
        }
    }

    public CapeMode getCapeMode() {
        if (cape != null) {
            return cape.getMode();
        }
        return CapeMode.NORMAL;
    }

    public List<CapeInfo> getAvailableCapes() {
        if (cape != null) {
            return cape.getAvailableCapes();
        }
        return Collections.emptyList();
    }

    public boolean setCape(int index) {
        return cape != null && cape.setCape(index);
    }

    public boolean setCape(String id) {
        return cape != null && cape.setCape(id);
    }

    public CapeRemodel getCape() {
        return cape;
    }

    public void setCapeTexture(BufferedImage image) {
        if (cape != null) {
            cape.setTexture(image);
        }
    }

    public void setShadePlayer(boolean shading) {
        defaultModel.setShading(shading);
        slimModel.setShading(shading);
    }

    public void setShadeCape(boolean shading) {
        if (cape != null) {
            cape.setShading(shading);
        }
    }

    public void setIdleAnimationEnabled(boolean enabled) {
        idleAnimationEnabled = enabled;
        defaultModel.setAnimationsEnabled(enabled);
        slimModel.setAnimationsEnabled(enabled);
        if (cape != null) {
            cape.setAnimationsEnabled(enabled);
        }
        if (!enabled) {
            clearIdleRotation();
        }
    }

    public void setIdleAnimationAge(float ageInTicks) {
        setIdleAnimationEnabled(true);
        applyIdleAnimationAge(ageInTicks);
    }

    private void applyIdleAnimationAge(float ageInTicks) {
        float rightZ = (float) Math.toDegrees(Math.cos(ageInTicks * 0.09f) * 0.05f + 0.05f);
        float rightX = (float) Math.toDegrees(Math.sin(ageInTicks * 0.067f) * 0.05f);
        setBoneRotation("Right Arm", rightX, 0.0f, rightZ);
        setBoneRotation("Left Arm", -rightX, 0.0f, -rightZ);
    }

    private float defaultIdleAgeInTicks() {
        return (System.currentTimeMillis() % 240000L) / 50.0f;
    }

    private void setBoneRotation(String bone, float x, float y, float z) {
        defaultModel.setBoneRotation(bone, x, y, z);
        slimModel.setBoneRotation(bone, x, y, z);
    }

    private void clearIdleRotation() {
        defaultModel.clearBoneRotation("Right Arm");
        defaultModel.clearBoneRotation("Left Arm");
        slimModel.clearBoneRotation("Right Arm");
        slimModel.clearBoneRotation("Left Arm");
    }

    public void setHeadRotation(float x, float y, float z) {
        defaultModel.setBoneRotation("Head", x, y, z);
        slimModel.setBoneRotation("Head", x, y, z);
    }

    public void clearHeadRotation() {
        defaultModel.clearBoneRotation("Head");
        slimModel.clearBoneRotation("Head");
    }

    public void setEquipment(PlayerEquipment equipment) {
        this.equipment = equipment == null ? PlayerEquipment.empty() : equipment;
    }

    public PlayerEquipment getEquipment() {
        return equipment;
    }

    public void setEquipmentRenderer(PlayerEquipmentRenderer equipmentRenderer) {
        this.equipmentRenderer = equipmentRenderer;
    }

    private void renderEquipment() {
        if (equipmentRenderer == null || equipment == null || equipment.isEmpty()) {
            return;
        }
        BlockbenchRemodel model = getCurrentModel();
        renderEquipmentSlotOnBone(model, PlayerEquipmentSlot.HEAD, "Head");
        renderEquipmentSlotOnBone(model, PlayerEquipmentSlot.CHEST, "Body");
        renderEquipmentSlotOnBone(model, PlayerEquipmentSlot.CHEST, "Right Arm");
        renderEquipmentSlotOnBone(model, PlayerEquipmentSlot.CHEST, "Left Arm");
        renderEquipmentSlotOnBone(model, PlayerEquipmentSlot.LEGS, "Body");
        renderEquipmentSlotOnBone(model, PlayerEquipmentSlot.LEGS, "Right Leg");
        renderEquipmentSlotOnBone(model, PlayerEquipmentSlot.LEGS, "Left Leg");
        renderEquipmentSlotOnBone(model, PlayerEquipmentSlot.FEET, "Right Leg");
        renderEquipmentSlotOnBone(model, PlayerEquipmentSlot.FEET, "Left Leg");
        renderEquipmentSlotOnBone(model, PlayerEquipmentSlot.MAIN_HAND, "Right Arm");
        renderEquipmentSlotOnBone(model, PlayerEquipmentSlot.OFF_HAND, "Left Arm");
    }

    private void renderEquipmentSlotOnBone(BlockbenchRemodel model, PlayerEquipmentSlot slot, String bone) {
        Object item = equipment.get(slot);
        if (item == null) {
            return;
        }
        String attachment = slim ? bone + ":slim" : bone;
        if (slot == PlayerEquipmentSlot.MAIN_HAND || slot == PlayerEquipmentSlot.OFF_HAND) {
            model.withBoneLocalTransform(bone, () -> equipmentRenderer.render(slot, item, attachment));
        } else {
            model.withBoneTransform(bone, () -> equipmentRenderer.render(slot, item, attachment));
        }
    }

    public static class PlayerRemodelBuilder {
        private String hexColor = null;
        private String texturePath;
        private BufferedImage textureImage;
        private String playerName;
        private boolean fetchSkinFromPlayer = false;
        private boolean shading = false;
        private CapeRemodel cape = null;

        public PlayerRemodelBuilder withColor(String hexColor) {
            this.hexColor = hexColor;
            return this;
        }

        public PlayerRemodelBuilder withTexture(String path) {
            this.texturePath = path;
            return this;
        }

        public PlayerRemodelBuilder withTexture(BufferedImage image) {
            this.textureImage = image;
            return this;
        }

        public PlayerRemodelBuilder withPlayer(String playerName) {
            this.playerName = playerName;
            return this;
        }

        public PlayerRemodelBuilder withTextureFromPlayer(String playerName) {
            this.playerName = playerName;
            this.fetchSkinFromPlayer = true;
            return this;
        }

        public PlayerRemodelBuilder withShading(boolean shading) {
            this.shading = shading;
            return this;
        }

        public PlayerRemodelBuilder withCape(CapeRemodel cape) {
            this.cape = cape;
            return this;
        }

        public PlayerRemodel build() {
            BlockbenchRemodel.BlockbenchModelBuilder defaultBuilder = BlockbenchRemodel.from("player.bbmodel");
            BlockbenchRemodel.BlockbenchModelBuilder slimBuilder = BlockbenchRemodel.from("player_slim.bbmodel");

            defaultBuilder.withShading(this.shading);
            slimBuilder.withShading(this.shading);

            if (hexColor != null) {
                defaultBuilder.withColor(hexColor);
                slimBuilder.withColor(hexColor);
            }

            BlockbenchRemodel defaultModel = defaultBuilder.build();
            BlockbenchRemodel slimModel = slimBuilder.build();

            CapeRemodel cape = this.cape;
            if (cape == null && playerName != null) {
                cape = CapeRemodel.builder().withPlayer(playerName).withShading(this.shading).build();
            }

            PlayerRemodel player = new PlayerRemodel(defaultModel, slimModel, cape);

            if (texturePath != null) {
                try {
                    player.setTexture(texturePath);
                } catch (IOException e) {
                    System.err.println("Failed to load texture from path: " + texturePath);
                    e.printStackTrace();
                }
            } else if (textureImage != null) {
                player.setTexture(textureImage);
            } else if (playerName != null && fetchSkinFromPlayer) {
                player.setTextureByPlayerName(playerName, this.cape == null);
            }

            return player;
        }
    }
}
