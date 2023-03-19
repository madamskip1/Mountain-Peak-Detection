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

uniform mat4 projection;
uniform mat4 view;

out vec4 vertexColor;

void main() {
    gl_Position = projection * view * vec4(position, 1.0);
    float color = 155 * (position.y * 5);
    if (color > 155)
        color = 155;
    color = (color + 100) / 255;
    vertexColor = vec4(color, color, color, 1.0);
}
"""
    return __create_shader(GL_VERTEX_SHADER, shader_code)


def __create_fragment_shader():
    shader_code = """
#version 330

in vec4 vertexColor;

void main() {
    gl_FragColor = vertexColor;
}
"""
    return __create_shader(GL_FRAGMENT_SHADER, shader_code)
