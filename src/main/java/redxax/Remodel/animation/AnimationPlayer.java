package redxax.Remodel.animation;

import java.util.List;
import java.util.Map;

public class AnimationPlayer {

    private final Map<String, AnimationClip> clips;
    private AnimationClip current;
    private float time;
    public String lastAnimation;

    public AnimationPlayer(Map<String, AnimationClip> clips) {
        this.clips = clips;
    }

    public boolean hasClip(String n) {
        return clips.containsKey(n);
    }

    public void playFirst() {
        if (!clips.isEmpty()) {
            String firstName = clips.values().iterator().next().name;
            play(firstName);
        }
    }

    public void play(String n) {
        AnimationClip clip = clips.get(n);
        if (clip != null) {
            if (current == clip) return;
            current = clip;
            time = 0;
            this.lastAnimation = n;
        }
    }

    public void replay() {
        if (current != null) {
            time = 0;
        }
    }

    public void update(float dt) {
        if (current == null) return;
        time += dt;
        if (current.loop && current.length > 0) {
            time = time % current.length;
        } else if (!current.loop && time > current.length) {
            time = current.length;
        }
    }

    private float sampleChannel(String boneUuid, String ch, int ax) {
        if (current == null) return Float.NaN;
        BoneAnimation ba = current.bones.get(boneUuid);
        if (ba == null) return Float.NaN;
        List<KeyFrame> list = ba.channels.get(ch);
        if (list == null || list.isEmpty()) return Float.NaN;

        if (list.size() == 1) return list.getFirst().value[ax];

        if (time <= list.getFirst().time) return list.getFirst().value[ax];
        if (time >= list.getLast().time) return list.getLast().value[ax];

        int p_idx = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).time <= time) {
                p_idx = i;
            } else {
                break;
            }
        }
        int n_idx = p_idx + 1;

        KeyFrame p = list.get(p_idx);
        KeyFrame n = list.get(n_idx);

        float t = (n.time - p.time == 0) ? 0 : (time - p.time) / (n.time - p.time);
        switch (p.interpolation) {
            case STEP:
                return p.value[ax];
            case SMOOTH: {
                KeyFrame p0 = list.get(p_idx > 0 ? p_idx - 1 : p_idx);
                KeyFrame p3 = list.get(n_idx < list.size() - 1 ? n_idx + 1 : n_idx);

                float v0 = p0.value[ax];
                float v1 = p.value[ax];
                float v2 = n.value[ax];
                float v3 = p3.value[ax];

                float t2 = t * t;
                float t3 = t2 * t;

                return 0.5f * ((2 * v1) + (-v0 + v2) * t + (2 * v0 - 5 * v1 + 4 * v2 - v3) * t2 + (-v0 + 3 * v1 - 3 * v2 + v3) * t3);
            }
            case BEZIER:
                break;
            case LINEAR:
            default:
                break;
        }
        return p.value[ax] * (1 - t) + n.value[ax] * t;
    }

    private float sample(String boneUuid, String ch, int ax, float def) {
        float v = sampleChannel(boneUuid, ch, ax);
        if (!Float.isNaN(v)) return v;
        String axis = ax == 0 ? "x" : ax == 1 ? "y" : "z";
        v = sampleChannel(boneUuid, ch + "_" + axis, 0);
        if (!Float.isNaN(v)) return v;
        return def;
    }

    public float[] pos(String boneUuid) {
        return new float[]{
                sample(boneUuid, "position", 0, 0),
                sample(boneUuid, "position", 1, 0),
                sample(boneUuid, "position", 2, 0)
        };
    }

    public float[] rot(String boneUuid) {
        return new float[]{
                sample(boneUuid, "rotation", 0, 0),
                sample(boneUuid, "rotation", 1, 0),
                sample(boneUuid, "rotation", 2, 0)
        };
    }

    public float[] scl(String boneUuid) {
        return new float[]{
                sample(boneUuid, "scale", 0, 1),
                sample(boneUuid, "scale", 1, 1),
                sample(boneUuid, "scale", 2, 1)
        };
    }
}