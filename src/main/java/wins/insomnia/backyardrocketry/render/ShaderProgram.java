package wins.insomnia.backyardrocketry.render;


import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {
    private int shaderProgramHandle;

    public ShaderProgram(String vertexSourcePath, String fragmentSourcePath) {
        String vertexShaderSource = loadShaderSource(vertexSourcePath);
        String fragmentShaderSource = loadShaderSource(fragmentSourcePath);

        createAndLinkShaderProgram(vertexShaderSource, fragmentShaderSource);
    }

    public void use() {
        glUseProgram(shaderProgramHandle);
    }

    public void setUniform(String uniformName, boolean value) {
        glUniform1i(glGetUniformLocation(shaderProgramHandle, uniformName), value ? 1 : 0);
    }

    public void setUniform(String uniformName, Matrix4f value) {
        float[] matrixValues = new float[16];
        matrixValues = value.get(matrixValues);
        glUniformMatrix4fv(glGetUniformLocation(shaderProgramHandle, uniformName), false, matrixValues);
    }

    public void setUniform(String uniformName, int value) {
        glUniform1i(glGetUniformLocation(shaderProgramHandle, uniformName), value);
    }

    public void setUniform(String uniformName, float value) {
        glUniform1f(glGetUniformLocation(shaderProgramHandle, uniformName), value);
    }

    public void setUniform(String uniformName, Vector2f value) {
        glUniform2f(glGetUniformLocation(shaderProgramHandle, uniformName), value.x, value.y);
    }

    public void setUniform(String uniformName, Vector3f value) {
        glUniform3f(glGetUniformLocation(shaderProgramHandle, uniformName), value.x, value.y, value.z);
    }

    public void setUniform(String uniformName, Vector4f value) {
        glUniform4f(glGetUniformLocation(shaderProgramHandle, uniformName), value.x, value.y, value.z, value.w);
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

        shaderProgramHandle = glCreateProgram();

        glAttachShader(shaderProgramHandle, vertexShader);
        glAttachShader(shaderProgramHandle, fragmentShader);

        glLinkProgram(shaderProgramHandle);

        glGetProgramiv(shaderProgramHandle, GL_LINK_STATUS, linkingSuccess);
        if (linkingSuccess[0] == 0) {
            String error = glGetProgramInfoLog(shaderProgramHandle);
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

    public int getProgramHandle() {
        return shaderProgramHandle;
    }

}
