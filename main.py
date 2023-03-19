import numpy as np
from OpenGL.GL import *
from OpenGL.GLUT import *

from Camera import Camera
from TerrainMap import TerrainMap
from shaders_program import create_shader_program

camera = Camera(
    position=np.array([-1.5, 0.5, 1.5]),
    target=np.array([0.0, 0.0, 0.01]),
    fov_h=45,
    aspect_ratio=8.0 / 6.0,
    near=0.1,
    far=100
)

terrain_map = TerrainMap("N49E020.hgt", 3601, 3601, 4)
shader_program = None


def init():
    glClearColor(0.0, 0.0, 0.0, 0.0)
    glEnable(GL_DEPTH_TEST)


def display():
    global camera, terrain_map, shader_program
    glClearColor(0.1, 0.1, 0.1, 1.0)
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
    view_matrix = camera.calc_view_matrix()
    perspective_matrix = camera.calc_perspective_matrix()
    terrain_map.draw(view_matrix, perspective_matrix, shader_program)
    glutSwapBuffers()


def keyboard_callback(key, *_):
    global camera
    camera.keyboard_callback(key)
    glutPostRedisplay()


def main():
    global shader_program, terrain_map
    glutInit()
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH)
    glutInitWindowSize(800, 600)
    glutCreateWindow(b"Cube")
    glutDisplayFunc(display)
    glutSpecialFunc(keyboard_callback)
    shader_program = create_shader_program()
    glUseProgram(shader_program)
    terrain_map.prepare_buffers()

    init()
    glutMainLoop()


if __name__ == "__main__":
    main()
