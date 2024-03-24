package wins.insomnia.backyardrocketry.render;


import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {
    private int shaderProgram;

    public ShaderProgram(String vertexSourcePath, String fragmentSourcePath) {
        String vertexShaderSource = loadShaderSource(vertexSourcePath);
        String fragmentShaderSource = loadShaderSource(fragmentSourcePath);

        createAndLinkShaderProgram(vertexShaderSource, fragmentShaderSource);
    }

    private static String loadShaderSource(String sourcePath) {

        try {

            System.out.println("Loading shader: " + sourcePath + " . . .");

            InputStream inputStream = ShaderProgram.class.getResourceAsStream("/shaders/" + sourcePath);
            if (inputStream == null) {
                throw new RuntimeException("Failed to read shader source file: res://shaders/" + sourcePath);
            }

            byte[] fileBytes = inputStream.readAllBytes();
            inputStream.close();

            System.out.println("Loaded shader: " + sourcePath + "!");

            return new String(fileBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load shader: " + sourcePath + "!");
        }

    }

    private int compileShader(int shaderType, String source) {
        int[] compilationSuccess = new int[1];
        String shaderTypeString = (shaderType == 35633) ? "Vertex" : "Fragment";

        int shader = glCreateShader(shaderType);
        glShaderSource(shader, source);
        glCompileShader(shader);

        glGetShaderiv(shader, GL_COMPILE_STATUS, compilationSuccess);
        if (compilationSuccess[0] == 0) {
            String error = glGetShaderInfoLog(shader);
            System.out.println(shaderTypeString + " shader compilation error: " + error);
        } else {
            System.out.println(shaderTypeString + " shader compiled!");
        }

        return shader;
    }

    private void linkShaders(int vertexShader, int fragmentShader) {

        int[] linkingSuccess = new int[1];

        shaderProgram = glCreateProgram();

        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);

        glLinkProgram(shaderProgram);

        glGetProgramiv(shaderProgram, GL_LINK_STATUS, linkingSuccess);
        if (linkingSuccess[0] == 0) {
            String error = glGetProgramInfoLog(shaderProgram);
            System.out.println("Error linking shader: " + error);
        } else {
            System.out.println("Shader program linked!");
        }
    }

    public void createAndLinkShaderProgram(String vertexShaderSource, String fragmentShaderSource) {

        int vertexShader = compileShader(GL_VERTEX_SHADER, vertexShaderSource);
        int fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentShaderSource);

        linkShaders(vertexShader, fragmentShader);

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

    }

    public int getProgram() {
        return shaderProgram;
    }

}
