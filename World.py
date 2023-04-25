import math

import numpy as np
from OpenGL.GLU import *


def equirectangular_approximation(lat1, long1, lat2, long2):
    EARTH_RADIUS = 6371
    delta_longitude = math.radians(long2 - long1)
    delta_latitude = math.radians(lat2 - lat1)
    sum_latitude = math.radians(lat1 + lat2)

    x = delta_longitude * math.cos(sum_latitude / 2.0)
    distance = EARTH_RADIUS * math.sqrt(x * x + delta_latitude * delta_latitude)
    return distance


latitude_approximation = 111.2
longitude_approximation = 71.0


class World:
    def __init__(self, terrain_model, world_position=np.array([0.0, 0.0, 0.0])):
        self.terrain_model = terrain_model
        self.world_position = world_position
        self.latitude_range = [49.0, 50.0]
        self.longitude_range = [20.0, 21.0]
        self.model_x_step, self.model_y_step, self.model_z_step = terrain_model.get_steps()
        self.hgt_size = terrain_model.get_hgt_size()
        self.world_size = terrain_model.get_world_size()
        self.world_size = [111.2, 71.0]
        self.viewport = np.array([0, 0, 768, 1024])

        self.view_matrix = None
        self.perspective_matrix = None

    def get_latitude_range(self):
        return self.latitude_range

    def get_longitude_range(self):
        return self.longitude_range

    def set_mvp_matrices(self, view, perspective):
        self.view_matrix = view
        self.perspective_matrix = perspective

    def get_vertex_coord_peak(self, latitude, longitude):
        x, _, z = self.get_coord_from_geo(latitude, longitude, 0.0)
        x = x / self.model_x_step
        z = z / self.model_z_step
        x = round(x)
        z = round(z)
        max_x, max_y, max_z = 0, 0, 0
        start_x = max(0, x - 1)
        start_z = max(0, z - 1)
        end_x = min(x + 1, self.hgt_size)
        end_z = min(z + 1, self.hgt_size)

        for x_loop in range(start_x, end_x + 1):
            for z_loop in range(start_z, end_z + 1):
                vertex_num = x * self.hgt_size + z
                vertex_coord = self.__get_vertex_coords(vertex_num)
                if vertex_coord[1] > max_y:
                    max_x = vertex_coord[0]
                    max_y = vertex_coord[1]
                    max_z = vertex_coord[2]

        return max_x, max_y, max_z

    def check_if_point_in_viewport(self, screen_x, screen_y, screen_z):
        return ((0 <= screen_x <= self.viewport[2])
                and (0 <= screen_y <= self.viewport[3])
                and (0 <= screen_z <= 1))

    def get_screen_coords(self, vertex_x, vertex_y, vertex_z):
        screen_position = gluProject(vertex_x, vertex_y, vertex_z, self.view_matrix, self.perspective_matrix,
                                     self.viewport)
        return screen_position

    def get_coord_from_geo(self, latitude, longitude, altitude):
        z = (longitude - self.longitude_range[0]) * self.world_size[1]
        x = (self.latitude_range[0] + 1.0 - latitude) * self.world_size[0]
        altitude_scale = 1 / 1000
        y = (altitude + 20) * altitude_scale

        return x, y, z

    def calc_distance_to_point(self, lat, long):
        return equirectangular_approximation(lat, long, self.world_position[0], self.world_position[1])

    def __get_vertex_coords(self, vertex_num):
        vertex_coords_start = vertex_num * 3
        vertex_coords_end = vertex_coords_start + 3
        return self.terrain_model.get_vertices(vertex_coords_start, vertex_coords_end)
