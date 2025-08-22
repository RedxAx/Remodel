package redxax.restudio.Remodel.api;

import org.lwjgl.opengl.GL11;
import redxax.restudio.Remodel.internal.TextureLoader;
import redxax.restudio.Remodel.util.SkinFetcher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PlayerRemodel {

    private final BlockbenchRemodel defaultModel;
    private final BlockbenchRemodel slimModel;

    private boolean slim = false;
    private int customTextureId = -1;

    private PlayerRemodel(BlockbenchRemodel defaultModel, BlockbenchRemodel slimModel) {
        this.defaultModel = defaultModel;
        this.slimModel = slimModel;
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
        getCurrentModel().render();
    }

    public void playAnimation(String name) {
        defaultModel.playAnimation(name);
        slimModel.playAnimation(name);
    }

    public void setPosition(float x, float y, float z) {
        defaultModel.setPosition(x, y, z);
        slimModel.setPosition(x, y, z);
    }

    public void setRotation(float x, float y, float z) {
        defaultModel.setRotation(x, y, z);
        slimModel.setRotation(x, y, z);
    }

    public void setScale(float x, float y, float z) {
        defaultModel.setScale(x, y, z);
        slimModel.setScale(x, y, z);
    }

    public void setScale(float s) {
        defaultModel.setScale(s);
        slimModel.setScale(s);
    }

    public void setTexture(BufferedImage image) {
        clearTexture();
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
        BufferedImage skin = SkinFetcher.getSkin(playerName);
        setTexture(skin);
    }

    public void clearTexture() {
        if (this.customTextureId != -1) {
            GL11.glDeleteTextures(this.customTextureId);
            this.customTextureId = -1;
        }
        defaultModel.clearTexture();
        slimModel.clearTexture();
    }

    public static class PlayerRemodelBuilder {
        private String hexColor = null;
        private String texturePath;
        private BufferedImage textureImage;
        private String playerName;
        private boolean shading = false;

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

        public PlayerRemodelBuilder withTextureFromPlayer(String playerName) {
            this.playerName = playerName;
            return this;
        }

        public PlayerRemodelBuilder withShading(boolean shading) {
            this.shading = shading;
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
            PlayerRemodel player = new PlayerRemodel(defaultModel, slimModel);

            if (texturePath != null) {
                try {
                    player.setTexture(texturePath);
                } catch (IOException e) {
                    System.err.println("Failed to load texture from path: " + texturePath);
                    e.printStackTrace();
                }
            } else if (textureImage != null) {
                player.setTexture(textureImage);
            } else if (playerName != null) {
                player.setTextureByPlayerName(playerName);
            }

            return player;
        }
    }
}