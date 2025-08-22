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

    public void clearTexture() {
        renderer.clearTexture();
    }

    public static class BlockbenchModelBuilder {
        private final String modelPath;
        private String hexColor = null;

        public BlockbenchModelBuilder(String modelPath) {
            this.modelPath = modelPath;
        }

        public BlockbenchModelBuilder withColor(String hexColor) {
            this.hexColor = hexColor;
            return this;
        }

        public BlockbenchRemodel build() {
            return new BlockbenchRemodel(new ModelRenderer(modelPath, hexColor));
        }
    }
}