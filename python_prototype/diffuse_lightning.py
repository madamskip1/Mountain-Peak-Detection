from OpenGL.GL import *


def set_diffuse_light(shader_program, light_position, light_color):
    lightPos_loc = glGetUniformLocation(shader_program, "lightPos")
    glUniform3fv(lightPos_loc, 1, light_position)

    lightColor_loc = glGetUniformLocation(shader_program, "lightColor")
    glUniform3fv(lightColor_loc, 1, light_color)
