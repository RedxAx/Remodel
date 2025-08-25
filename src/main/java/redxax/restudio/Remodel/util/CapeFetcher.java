package redxax.restudio.Remodel.util;

import redxax.restudio.Remodel.model.CapeInfo;

import java.util.List;

public class CapeFetcher {
    public static List<CapeInfo> getCapes(String playerName) {
        return CacheManager.getInstance().getCapes(playerName);
    }
}