from OpenGL.GL import *


def create_shader_program():
    vertex_shader = __create_vertex_shader()
    fragment_shader = __create_fragment_shader()

    program = glCreateProgram()
    glAttachShader(program, vertex_shader)
    glAttachShader(program, fragment_shader)
    glLinkProgram(program)

    # glDeleteShader(vertex_shader)
    # glDeleteShader(fragment_shader)

    return program


def __create_shader(shader_type, source):
    shader = glCreateShader(shader_type)
    glShaderSource(shader, source)
    glCompileShader(shader)

    return shader


def __create_vertex_shader():
    shader_code = """
#version 330

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;

uniform mat4 projection;
uniform mat4 view;

out vec4 vertexColor;
out vec3 vertexNormal;
out vec3 vertexWorldPosition;

void main() {
    gl_Position = projection * view * vec4(position, 1.0);
    float color = 155 * (position.y * 50);
    if (color > 155)
        color = 155;
    color = (color + 100) / 255;
    color = 1.0;
    vertexColor = vec4(color, color, color, 1.0);
    vertexNormal = normal;
    vertexWorldPosition = position;
}
"""
    return __create_shader(GL_VERTEX_SHADER, shader_code)


def __create_fragment_shader():
    shader_code = """
#version 330

in vec4 vertexColor;
in vec3 vertexNormal;
in vec3 vertexWorldPosition;

uniform vec3 lightPos;
uniform vec3 lightColor;

void main() {
    vec3 norm = normalize(vertexNormal);
    vec3 lightDir = normalize(lightPos - vertexWorldPosition);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor;
    vec3 objColor = diffuse * vertexColor.xyz;
    gl_FragColor = vec4(objColor, 1.0);
}
"""
    return __create_shader(GL_FRAGMENT_SHADER, shader_code)
