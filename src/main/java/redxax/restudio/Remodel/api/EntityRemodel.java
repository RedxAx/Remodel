package redxax.restudio.Remodel.api;

import org.lwjgl.opengl.GL11;
import redxax.restudio.Remodel.internal.TextureLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class EntityRemodel {
    private final BlockbenchRemodel model;
    private int customTextureId = -1;

    private EntityRemodel(BlockbenchRemodel model) {
        this.model = model;
    }

    public static EntityRemodel from(String modelPath) {
        return new EntityRemodel(BlockbenchRemodel.from(modelPath).withShading(true).build());
    }

    public void render() {
        model.render();
    }

    public void playAnimation(String name) {
        model.playAnimation(name);
    }

    public void setPosition(float x, float y, float z) {
        model.setPosition(x, y, z);
    }

    public void setRotation(float x, float y, float z) {
        model.setRotation(x, y, z);
    }

    public void setScale(float x, float y, float z) {
        model.setScale(x, y, z);
    }

    public void setScale(float scale) {
        model.setScale(scale);
    }

    public void setTexture(BufferedImage image) {
        clearTexture();
        if (image == null) {
            return;
        }
        customTextureId = TextureLoader.loadTexture(image);
        if (customTextureId != -1) {
            model.setTexture(customTextureId);
        }
    }

    public void setTexture(String path) throws IOException {
        setTexture(ImageIO.read(new File(path)));
    }

    public void clearTexture() {
        if (customTextureId != -1) {
            GL11.glDeleteTextures(customTextureId);
            customTextureId = -1;
        }
        model.clearTexture();
    }
}
