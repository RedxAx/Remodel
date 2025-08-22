package redxax.restudio.Remodel.animation;

public class KeyFrame {
    public float time;
    public float[] value;
    public InterpolationType interpolation;

    public KeyFrame(float time, float[] value, InterpolationType interpolation) {
        this.time = time;
        this.value = value;
        this.interpolation = interpolation;
    }
}