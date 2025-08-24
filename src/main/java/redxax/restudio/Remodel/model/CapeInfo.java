package redxax.restudio.Remodel.model;

import java.awt.image.BufferedImage;

public class CapeInfo {
    public final String id;
    public final String url;
    public final BufferedImage image;

    public CapeInfo(String id, String url, BufferedImage image) {
        this.id = id;
        this.url = url;
        this.image = image;
    }
}