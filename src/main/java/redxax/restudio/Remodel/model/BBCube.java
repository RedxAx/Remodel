package redxax.restudio.Remodel.model;

import java.util.Map;

public class BBCube {
    public String name;
    public String uuid;
    public float[] from;
    public float[] to;
    public float[] rotation;
    public float[] origin;
    public Map<String, BBFace> faces;
    public Boolean visibility;
}