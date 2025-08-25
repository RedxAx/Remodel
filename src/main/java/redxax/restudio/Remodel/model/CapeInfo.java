package redxax.restudio.Remodel.model;

import java.awt.image.BufferedImage;

public class CapeInfo {
    public final String id;
    public final String alias;
    public final String url;
    public final BufferedImage image;

    public CapeInfo(String id, String alias, String url, BufferedImage image) {
        this.id = id;
        this.alias = alias != null ? alias : id;
        this.url = url;
        this.image = image;
    }
}