package redxax.restudio.Remodel.animation;

public enum InterpolationType {
    STEP,
    LINEAR,
    SMOOTH,
    BEZIER;

    public static InterpolationType fromString(String s) {
        if (s == null) return LINEAR;
        return switch (s.toLowerCase()) {
            case "step" -> STEP;
            case "smooth" -> SMOOTH;
            case "bezier" -> BEZIER;
            default -> LINEAR;
        };
    }
}