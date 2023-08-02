import numpy as np
from OpenGL.GLU import *


class ScreenManager:
    def __init__(self, width, height):
        self.viewport = np.array([0, 0, width, height])
        self.view_matrix = None
        self.projection_matrix = None

    def set_MVP_matrices(self, view_matrix, projection_matrix):
        self.view_matrix = view_matrix
        self.projection_matrix = projection_matrix

    def get_screen_point(self, vertex_x, vertex_y, vertex_z):
        screen_position = gluProject(vertex_x, vertex_y, vertex_z, self.view_matrix, self.projection_matrix,
                                     self.viewport)
        return screen_position

    def check_if_point_on_screen(self, screen_x, screen_y, screen_z):
        return ((0 <= screen_x <= self.viewport[2])
                and (0 <= screen_y <= self.viewport[3])
                and (0 <= screen_z <= 1))
