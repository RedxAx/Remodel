package redxax.restudio.Remodel.util;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SkinFetcher {

    public static BufferedImage getSkin(String playerName) {
        return CacheManager.getInstance().getSkin(playerName);
    }

    public static BufferedImage getSkinFace(String playerName) {
        BufferedImage skin = getSkin(playerName);
        if (skin == null) {
            return null;
        }
        try {
            BufferedImage face = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = face.createGraphics();
            g.drawImage(skin.getSubimage(8, 8, 8, 8), 0, 0, null);
            if (skin.getWidth() >= 48 && skin.getHeight() >= 16) {
                BufferedImage overlay = skin.getSubimage(40, 8, 8, 8);
                boolean hasVisible = false;
                for (int y = 0; y < 8 && !hasVisible; y++) {
                    for (int x = 0; x < 8; x++) {
                        if ((overlay.getRGB(x, y) >>> 24) > 0) {
                            hasVisible = true;
                            break;
                        }
                    }
                }
                if (hasVisible) {
                    g.drawImage(overlay, 0, 0, null);
                }
            }
            g.dispose();
            return face;
        } catch (Exception e) {
            return skin.getSubimage(8, 8, 8, 8);
        }
    }
}