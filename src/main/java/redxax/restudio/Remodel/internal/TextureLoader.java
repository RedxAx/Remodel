package redxax.restudio.Remodel.internal;

import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Base64;

public class TextureLoader {

    public static int loadTexture64(String base64) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            byte[] decodedBytes = Base64.getDecoder().decode(base64.split(",")[1]);
            ByteBuffer imageBuffer = stack.malloc(decodedBytes.length);
            imageBuffer.put(decodedBytes);
            imageBuffer.flip();

            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            ByteBuffer image = STBImage.stbi_load_from_memory(imageBuffer, w, h, comp, 4);
            if (image == null) {
                throw new RuntimeException("Failed to load texture image: " + STBImage.stbi_failure_reason());
            }

            int textureId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w.get(0), h.get(0), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image);

            STBImage.stbi_image_free(image);

            return textureId;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int loadTexture(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("BufferedImage cannot be null");
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            ByteBuffer imageBuffer = stack.malloc(imageBytes.length);
            imageBuffer.put(imageBytes);
            imageBuffer.flip();

            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            ByteBuffer decodedImage = STBImage.stbi_load_from_memory(imageBuffer, w, h, comp, 4);
            if (decodedImage == null) {
                throw new RuntimeException("Failed to load texture image: " + STBImage.stbi_failure_reason());
            }

            int textureId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w.get(0), h.get(0), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, decodedImage);

            STBImage.stbi_image_free(decodedImage);

            return textureId;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int loadTexture(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        return loadTexture(image);
    }
}