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
        getCurrentModel().render();
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
        defaultModel.setPosition(x, y, z);
        slimModel.setPosition(x, y, z);
        if (cape != null) {
            cape.setPosition(x, y, z);
        }
    }

    public void setRotation(float x, float y, float z) {
        defaultModel.setRotation(x, y, z);
        slimModel.setRotation(x, y, z);
        if (cape != null) {
            cape.setRotation(x, y, z);
        }
    }

    public void setScale(float x, float y, float z) {
        defaultModel.setScale(x, y, z);
        slimModel.setScale(x, y, z);
        if (cape != null) {
            cape.setScale(x, y, z);
        }
    }

    public void setScale(float s) {
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