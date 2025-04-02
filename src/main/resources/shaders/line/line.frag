#version 330 core

out vec4 FragColor;

uniform vec3 fs_lineColor;

void main() {
    FragColor = vec4(fs_lineColor, 1.0);
}