package redxax.restudio.Remodel.api;

import redxax.restudio.Remodel.internal.ModelRenderer;

public class BlockbenchRemodel {
    private final ModelRenderer renderer;

    private BlockbenchRemodel(ModelRenderer renderer) {
        this.renderer = renderer;
    }

    public static BlockbenchModelBuilder from(String modelPath) {
        return new BlockbenchModelBuilder(modelPath);
    }

    public void render() {
        renderer.render();
    }

    public void playAnimation(String name) {
        renderer.playAnimation(name);
    }

    public void setPosition(float x, float y, float z) {
        renderer.setPosition(x, y, z);
    }

    public void setRotation(float x, float y, float z) {
        renderer.setRotation(x, y, z);
    }

    public void setScale(float x, float y, float z) {
        renderer.setScale(x, y, z);
    }

    public void setScale(float s) {
        renderer.setScale(s);
    }

    public void setTexture(int textureId) {
        renderer.setTexture(textureId);
    }

    public void setShading(boolean shading) {
        renderer.setShading(shading);
    }

    public void setAnimationsEnabled(boolean animationsEnabled) {
        renderer.setAnimationsEnabled(animationsEnabled);
    }

    public void setBoneRotation(String boneName, float x, float y, float z) {
        renderer.setBoneRotation(boneName, x, y, z);
    }

    public void clearBoneRotation(String boneName) {
        renderer.clearBoneRotation(boneName);
    }

    public void clearTexture() {
        renderer.clearTexture();
    }

    public boolean withBoneTransform(String boneName, Runnable action) {
        return renderer.withBoneTransform(boneName, action);
    }

    public boolean withBoneLocalTransform(String boneName, Runnable action) {
        return renderer.withBoneLocalTransform(boneName, action);
    }

    public static class BlockbenchModelBuilder {
        private final String modelPath;
        private String hexColor = null;
        private boolean shading = false;

        public BlockbenchModelBuilder(String modelPath) {
            this.modelPath = modelPath;
        }

        public BlockbenchModelBuilder withColor(String hexColor) {
            this.hexColor = hexColor;
            return this;
        }

        public BlockbenchModelBuilder withShading(boolean shading) {
            this.shading = shading;
            return this;
        }

        public BlockbenchRemodel build() {
            return new BlockbenchRemodel(new ModelRenderer(modelPath, hexColor, shading));
        }
    }
}
