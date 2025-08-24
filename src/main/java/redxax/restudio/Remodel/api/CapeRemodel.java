package redxax.restudio.Remodel.api;

import org.lwjgl.opengl.GL11;
import redxax.restudio.Remodel.internal.TextureLoader;
import redxax.restudio.Remodel.model.CapeInfo;
import redxax.restudio.Remodel.util.CapeFetcher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CapeRemodel {

    private final BlockbenchRemodel capeModel;
    private final BlockbenchRemodel elytraModel;

    private CapeMode mode = CapeMode.NORMAL;
    private List<CapeInfo> availableCapes = new ArrayList<>();
    private int currentCapeIndex = -1;
    private int customTextureId = -1;

    private CapeRemodel(BlockbenchRemodel capeModel, BlockbenchRemodel elytraModel) {
        this.capeModel = capeModel;
        this.elytraModel = elytraModel;
    }

    public static CapeRemodel create() {
        return new CapeRemodelBuilder().build();
    }

    public static CapeRemodelBuilder builder() {
        return new CapeRemodelBuilder();
    }

    private BlockbenchRemodel getCurrentModel() {
        return mode == CapeMode.ELYTRA ? elytraModel : capeModel;
    }

    public void setMode(CapeMode mode) {
        this.mode = mode;
    }

    public CapeMode getMode() {
        return mode;
    }

    public void render() {
        if (customTextureId != -1) {
            getCurrentModel().render();
        }
    }

    public void playAnimation(String name) {
        capeModel.playAnimation(name);
        elytraModel.playAnimation(name);
    }

    public void setPosition(float x, float y, float z) {
        capeModel.setPosition(x, y, z);
        elytraModel.setPosition(x, y, z);
    }

    public void setRotation(float x, float y, float z) {
        capeModel.setRotation(x, y, z);
        elytraModel.setRotation(x, y, z);
    }

    public void setScale(float x, float y, float z) {
        capeModel.setScale(x, y, z);
        elytraModel.setScale(x, y, z);
    }

    public void setScale(float s) {
        capeModel.setScale(s);
        elytraModel.setScale(s);
    }

    private void applyTexture(BufferedImage image) {
        clearTexture();
        if (image == null) return;
        this.customTextureId = TextureLoader.loadTexture(image);
        if (this.customTextureId != -1) {
            capeModel.setTexture(this.customTextureId);
            elytraModel.setTexture(this.customTextureId);
        }
    }

    public void setTexture(BufferedImage image) {
        this.availableCapes.clear();
        this.currentCapeIndex = -1;
        applyTexture(image);
    }

    public void setTexture(String path) throws IOException {
        setTexture(ImageIO.read(new File(path)));
    }

    public void setTextureByPlayerName(String playerName) {
        this.availableCapes = CapeFetcher.getCapes(playerName);
        if (!this.availableCapes.isEmpty()) {
            setCape(0);
        } else {
            clearTexture();
        }
    }

    public List<CapeInfo> getAvailableCapes() {
        return availableCapes;
    }

    public boolean setCape(int index) {
        if (index >= 0 && index < availableCapes.size()) {
            CapeInfo cape = availableCapes.get(index);
            applyTexture(cape.image);
            this.currentCapeIndex = index;
            return true;
        }
        return false;
    }

    public boolean setCape(String id) {
        for (int i = 0; i < availableCapes.size(); i++) {
            if (availableCapes.get(i).id.equalsIgnoreCase(id)) {
                return setCape(i);
            }
        }
        return false;
    }

    public void clearTexture() {
        if (this.customTextureId != -1) {
            GL11.glDeleteTextures(this.customTextureId);
            this.customTextureId = -1;
        }
        capeModel.clearTexture();
        elytraModel.clearTexture();
        this.currentCapeIndex = -1;
    }

    public static class CapeRemodelBuilder {
        private String hexColor = null;
        private String texturePath;
        private BufferedImage textureImage;
        private String playerName;
        private boolean shading = false;

        public CapeRemodelBuilder withColor(String hexColor) {
            this.hexColor = hexColor;
            return this;
        }

        public CapeRemodelBuilder withTexture(String path) {
            this.texturePath = path;
            return this;
        }

        public CapeRemodelBuilder withTexture(BufferedImage image) {
            this.textureImage = image;
            return this;
        }

        public CapeRemodelBuilder withPlayer(String playerName) {
            this.playerName = playerName;
            return this;
        }

        public CapeRemodelBuilder withShading(boolean shading) {
            this.shading = shading;
            return this;
        }

        public CapeRemodel build() {
            BlockbenchRemodel.BlockbenchModelBuilder capeBuilder = BlockbenchRemodel.from("cape.bbmodel");
            BlockbenchRemodel.BlockbenchModelBuilder elytraBuilder = BlockbenchRemodel.from("elytra.bbmodel");

            capeBuilder.withShading(this.shading);
            elytraBuilder.withShading(this.shading);

            if (hexColor != null) {
                capeBuilder.withColor(hexColor);
                elytraBuilder.withColor(hexColor);
            }

            BlockbenchRemodel capeModel = capeBuilder.build();
            BlockbenchRemodel elytraModel = elytraBuilder.build();
            CapeRemodel capeRemodel = new CapeRemodel(capeModel, elytraModel);

            if (playerName != null) {
                capeRemodel.setTextureByPlayerName(playerName);
            } else if (texturePath != null) {
                try {
                    capeRemodel.setTexture(texturePath);
                } catch (IOException e) {
                    System.err.println("Failed to load texture from path: " + texturePath);
                    e.printStackTrace();
                }
            } else if (textureImage != null) {
                capeRemodel.setTexture(textureImage);
            }

            return capeRemodel;
        }
    }
}