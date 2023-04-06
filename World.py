import numpy as np
from OpenGL.GLU import *
from ctypes import *
from enum import Enum


class World:
    def __init__(self, terrain_model):
        self.terrain_model = terrain_model
        self.latitude_range = [49.0, 50.0]
        self.longitude_range = [20.0, 21.0]
        self.vertices = terrain_model.get_vertices()
        self.model_x_step, self.model_y_step, self.model_z_step = terrain_model.get_steps()
        self.hgt_size = terrain_model.get_hgt_size()
        self.world_size = terrain_model.get_world_size()
        self.geo_x_step = abs(self.longitude_range[1] - self.longitude_range[0]) / self.hgt_size
        self.geo_z_step = abs(self.latitude_range[1] - self.latitude_range[0]) / self.hgt_size
        self.viewport = np.array([0, 0, 800, 600])
        self.view_matrix = None
        self.perspective_matrix = None

    def get_latitude_range(self):
        return self.latitude_range

    def get_longitude_range(self):
        return self.longitude_range

    def set_mvp_matrices(self, view, perspective):
        self.view_matrix = view
        self.perspective_matrix = perspective

    def get_vertex_num(self, latitude, longitude):
        z = int((latitude - self.latitude_range[0]) / self.geo_z_step)
        x = int((longitude - self.longitude_range[0]) / self.geo_x_step)
        vertex_num = (x + 1) * self.hgt_size - z - 1

        return vertex_num

    def get_vertex_coords(self, vertex_num):
        vertex_coords_start = vertex_num * 3
        return self.vertices[vertex_coords_start: vertex_coords_start + 3]

    def check_vertex_frutsum_vertex_num(self, vertex_num):
        coords = self.get_vertex_coords(vertex_num)
        return self.check_vertex_frutsum_coords(coords[0], coords[1], coords[2])

    def check_vertex_frutsum_coords(self, x, y, z):
        screen_position = gluProject(x, y, z, self.view_matrix, self.perspective_matrix, self.viewport)
        return ((0 <= screen_position[0] <= self.viewport[2])
                and (0 <= screen_position[1] <= self.viewport[3]))

    def get_coord_from_geo(self, latitude, longitude, altitude):
        origin_chord = [0.0, 0.0]
        origin_geo = [49.0, 20.0]

        z = (longitude - origin_geo[1]) * self.world_size
        x = (origin_geo[0] + 1.0 - latitude) * self.world_size
        simplified_by = 3601 / 361
        altitude_scale = self.model_x_step / 30 / simplified_by
        y = (altitude + 200) * self.model_y_step
        print(x, y, z)
        return x, y, z