import math

import numpy as np
from OpenGL.GLU import *
from ctypes import *
from enum import Enum

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
        self.vertices = terrain_model.get_vertices()
        self.model_x_step, self.model_y_step, self.model_z_step = terrain_model.get_steps()
        self.hgt_size = terrain_model.get_hgt_size()
        self.world_size = terrain_model.get_world_size()
        self.world_size = [111.2, 71.0]
        self.geo_x_step = abs(self.longitude_range[1] - self.longitude_range[0]) / self.hgt_size
        self.geo_z_step = abs(self.latitude_range[1] - self.latitude_range[0]) / self.hgt_size
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

    def get_vertex_num(self, latitude, longitude):
        x, _, z = self.get_coord_from_geo(latitude, longitude, 0.0)
        x = x / self.model_x_step
        z = z / self.model_z_step
        x = int(x)
        z = int(z)
        vertex_num = x * self.hgt_size + z

        return vertex_num

    def get_vertex_coord_peak(self, latitude, longitude):
        x, _, z = self.get_coord_from_geo(latitude, longitude, 0.0)
        x = x / self.model_x_step
        z = z / self.model_z_step
        x = int(x)
        z = int(z)
        max_x, max_y, max_z, max_vertex_num = 0, 0, 0, 0
        start_x = 0 if (x - 1) < 0 else (x - 1)
        start_z = 0 if (z - 1) < 0 else (z - 1)
        end_x = self.hgt_size if (x + 1) > self.hgt_size else (x + 1)
        end_z = self.hgt_size if (z + 1) > self.hgt_size else (z + 1)

        for x_loop in range(start_x, end_x + 1):
            for z_loop in range(start_z, end_z + 1):
                vertex_num = x * self.hgt_size + z
                vertex_coord = self.get_vertex_coords(vertex_num)
                if vertex_coord[1] > max_y:
                    max_x = vertex_coord[0]
                    max_y = vertex_coord[1]
                    max_z = vertex_coord[2]
                    max_vertex_num = vertex_num

        return max_x, max_y, max_z


    def __get_vertex_altitude(self, vertex_num):
        vertex_coords_start = vertex_num * 3
        vertex_altitude_index = vertex_coords_start + 1
        return self.vertices[vertex_altitude_index]

    def get_vertex_coords(self, vertex_num):
        vertex_coords_start = vertex_num * 3
        return self.vertices[vertex_coords_start: vertex_coords_start + 3]

    def check_vertex_frutsum_vertex_num(self, vertex_num):
        coords = self.get_vertex_coords(vertex_num)
        return self.check_vertex_frutsum_coords(coords[0], coords[1], coords[2])

    def check_vertex_frutsum_coords(self, x, y, z):
        screen_position = gluProject(x, y, z, self.view_matrix, self.perspective_matrix, self.viewport)
        return ((0 <= screen_position[0] <= self.viewport[2])
                and (0 <= screen_position[1] <= self.viewport[3])
                and (0 <= screen_position[2] <= 1))

    def get_screen_coords(self, vertex_x, vertex_y, vertex_z):
        screen_position = gluProject(vertex_x, vertex_y, vertex_z, self.view_matrix, self.perspective_matrix, self.viewport)
        return screen_position

    def get_coord_from_geo(self, latitude, longitude, altitude):
        origin_chord = [0.0, 0.0]
        origin_geo = [49.0, 20.0]

        #z = (longitude - origin_geo[1]) * self.world_size
        z = (longitude - origin_geo[1]) * self.world_size[1]
        #x = (origin_geo[0] + 1.0 - latitude) * self.world_size
        x = (origin_geo[0] + 1.0 - latitude) * self.world_size[0]
        simplified_by = 3601 / 361
        #altitude_scale = self.model_x_step / 30 / simplified_by
        altitude_scale = 1 / 1000
        y = (altitude + 20) * altitude_scale

        return x, y, z

    def calc_distance_between_2_geopoints(self, lat1, long1, lat2, long2):
        return equirectangular_approximation(lat1, long1, lat2, long2)

    def calc_distance_to_point(self, lat, long):
        return self.calc_distance_between_2_geopoints(lat, long, self.world_position[0], self.world_position[1])