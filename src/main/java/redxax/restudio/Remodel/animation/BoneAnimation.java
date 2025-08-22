package redxax.restudio.Remodel.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoneAnimation {
    public String boneName;
    public String boneUuid;
    public Map<String, List<KeyFrame>> channels = new HashMap<>();

    public void add(String ch, KeyFrame kf) {
        channels.computeIfAbsent(ch, k -> new ArrayList<>()).add(kf);
    }
}