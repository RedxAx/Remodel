package redxax.Remodel.animation;

import redxax.Remodel.animation.BoneAnimation;

import java.util.HashMap;
import java.util.Map;

public class AnimationClip {
    public String name;
    public float length;
    public boolean loop;
    public Map<String, BoneAnimation> bones = new HashMap<>();
}