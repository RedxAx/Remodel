# Remodel By ReStudio - Render 3D Block Bench Models And MC Skins With Ease

Remodel is a rendering library that supports most block bench features, as well as Minecraft Skins.

https://github.com/user-attachments/assets/de3e1807-c9a7-4849-bbd5-fe5ebbb7532d

## Features
- Supports Most BlockBench Models.
- Fully Support BlockBench Animations No Matter The Complexity.
- Supports Wide/Slim Minecraft Skins (Path, or Minecraft Name).
- Supports Capes, And Elytra Capes.
- Built-In Caching System For Minecraft Skins/Capes.
- Simple And Easy Builder-Based API.

## Requirements:
- [LWJGL](https://www.lwjgl.org): OpenGL, stb.
- [Gson](https://github.com/google/gson)


## Example Usage
```java
package redxax.restudio.Remodel;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import redxax.restudio.Remodel.api.BlockbenchRemodel;
import redxax.restudio.Remodel.api.PlayerRemodel;

public class Main {

    private long window;

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        window = GLFW.glfwCreateWindow(800, 600, "Remodel Example", 0, 0);
        if (window == 0) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(0);
        GLFW.glfwShowWindow(window);

        GL.createCapabilities();
    }

    private void loop() {
        BlockbenchRemodel model = BlockbenchRemodel.from("C:\\Cube Test.bbmodel").build();
        // You Can Also Ignore Model Textures And Apply a Color By Using `.withColor("#FF0000")`.
        // Now, You Can Use `model.playAnimation()`, `model.setPosition()`, `model.setRotation()`, And Many Others To Manipulate The Model.

        // As For Player API, Use:
        // PlayerRemodel model = PlayerRemodel.builder().withTextureFromPlayer("RedxAx").build();
        // You Can Also Use:
        // .withTexture("C:/skin.png")

        GL11.glClearColor(0.2f, 0.3f, 0.4f, 1.0f);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        // Basic Camera
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        float aspectRatio = 800f / 600f;
        GL11.glFrustum(-aspectRatio, aspectRatio, -1, 1, 1.0, 100.0);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);


        while (!GLFW.glfwWindowShouldClose(window)) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            GL11.glLoadIdentity();
            GL11.glTranslatef(0, -2, -5); // X Y Z, Adjust As Needed

            model.setRotation(0, (float)GLFW.glfwGetTime() * 30, 0); // Model Rotation.
            model.render();

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }

    private void cleanup() {
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
}
```

## Contact
- Join The Official [ReStudio Discord](https://discord.gg/UAEV8WgC6M).
- DM Me On Discord: `RedxAx`.

## Building From Source
Just run `./gradlew build`.

## License
This project is licensed under the MIT License.

# Happy ReRendering!
