package redxax.restudio.Remodel.animation;

import java.util.HashMap;
import java.util.Map;

public class AnimationClip {
    public String name;
    public float length;
    public boolean loop;
    public Map<String, BoneAnimation> bones = new HashMap<>();
}