import math

import numpy as np

latitude_approximation = 111.2
longitude_approximation = 71.0


class TerrainData:
    def __init__(self, hgt_init_size, simplify_factor, max_distance, observer_location):
        self.hgt_size = hgt_init_size
        self.max_distance = max_distance
        self.scale = None
        self.vertices = None
        self.triangles = None
        self.normals = None
        self.observer_location = observer_location
        self.rows = 0
        self.cols = 0
        self.offset_x = 0
        self.offset_z = 0
        self.terrain_origin = None
        self.__init_terrain_data(simplify_factor)

    def get_vertices(self, start_index, end_index):
        return self.vertices[start_index: end_index]

    def get_vertex_index(self, local_x, local_z):
        x = local_x / self.scale[0]
        z = local_z / self.scale[2]
        x = int(x)
        z = int(z)
        return x, z

    def __init_terrain_data(self, simplify_factor):
        path = "N49E020.hgt"

        elevation = self.__load_hgt_file(path, simplify_factor)
        self.__generate_vertices(elevation)
        self.__generate_triangles()
        self.__generate_normals()

    def __load_hgt_file(self, path, simplify_factor):
        with open(path, 'rb') as file:
            elevation = np.fromfile(file, np.dtype('>i2'), self.hgt_size * self.hgt_size)
            elevation = elevation.reshape(self.hgt_size, self.hgt_size)

        if simplify_factor > 0:
            elevation = elevation[::simplify_factor, ::simplify_factor]
            self.hgt_size = elevation.shape[0]

        return elevation

    def __generate_vertices(self, elevation):
        self.__calc_terrain_scale()
        elevation = self.__drop_unused_data(elevation)

        vertices = np.zeros((self.rows * self.cols * 3), dtype=np.float32)
        vertices_index = 0

        for (x, z), y in np.ndenumerate(elevation):
            # x - latitude
            # y - altitude
            # z - longitude
            x_coord = self.terrain_origin[0] + self.scale[0] * x
            y_coord = self.terrain_origin[1] + self.scale[1] * y  # up is Y coord in OpenGL
            z_coord = self.terrain_origin[2] + self.scale[2] * z
            vertices[vertices_index] = x_coord
            vertices[vertices_index + 1] = y_coord
            vertices[vertices_index + 2] = z_coord
            vertices_index = vertices_index + 3

        self.vertices = vertices

    def __calc_terrain_scale(self):
        x_scale = latitude_approximation / (self.hgt_size - 1)
        y_scale = 1.0 / 1000
        z_scale = longitude_approximation / (self.hgt_size - 1)
        self.scale = (x_scale, y_scale, z_scale)

    def __drop_unused_data(self, elevation):
        observer_vertex_x, observer_vertex_z = self.get_vertex_index(self.observer_location[0],
                                                                     self.observer_location[2])
        range_x = math.ceil(self.max_distance / self.scale[0])
        range_z = math.ceil(self.max_distance / self.scale[2])

        x_start = observer_vertex_x - range_x
        x_end = observer_vertex_x + range_x
        x_start = max(0, x_start)
        x_end = min(x_end, self.hgt_size)

        z_start = observer_vertex_z - range_z
        z_end = observer_vertex_z + range_z
        z_start = max(0, z_start)
        z_end = min(z_end, self.hgt_size)

        elevation = elevation[x_start:x_end + 1, z_start:z_end + 1]
        init_origin = (0.0, 0.0, 0.0)
        origin_x = init_origin[0] + x_start * self.scale[0]
        origin_z = init_origin[2] + z_start * self.scale[2]
        self.terrain_origin = (origin_x, init_origin[1], origin_z)
        self.rows = elevation.shape[0]  # x
        self.cols = elevation.shape[1]  # z
        self.offset_x = x_start
        self.offset_z = z_start

        return elevation

    def __generate_triangles(self):
        triangles_num = ((self.rows - 1) * (self.cols - 1) * 2)
        triangles_indices_num = triangles_num * 3
        triangles = np.zeros(triangles_indices_num, dtype=np.uint32)
        triangles_index = 0
        index = 0

        for x in range(self.rows - 1):
            for y in range(self.cols - 1):
                a = index
                b = index + 1
                c = index + self.cols + 1
                d = index + self.cols
                index = index + 1

                triangles[triangles_index] = a
                triangles[triangles_index + 1] = b
                triangles[triangles_index + 2] = c

                triangles[triangles_index + 3] = a
                triangles[triangles_index + 4] = c
                triangles[triangles_index + 5] = d

                triangles_index = triangles_index + 6
            index = index + 1

        self.triangles = triangles

    def __generate_normals(self):
        vertices_triangles_normals = [[] for _ in range(int(len(self.vertices) / 3))]
        vertices_np = np.array(self.vertices).reshape(-1, 3)
        vertices_normal = np.zeros_like(vertices_np)

        for A, B, C in zip(*[iter(self.triangles)] * 3):
            AB = vertices_np[B] - vertices_np[A]
            AC = vertices_np[C] - vertices_np[A]
            triangle_normal = np.cross(AB, AC)
            triangle_normal = triangle_normal / np.linalg.norm(triangle_normal)

            vertices_triangles_normals[A].append(triangle_normal)
            vertices_triangles_normals[B].append(triangle_normal)
            vertices_triangles_normals[C].append(triangle_normal)

        for index, vertex_triangles_normals in enumerate(vertices_triangles_normals):
            vertex_normal = np.add.reduce(vertex_triangles_normals)
            vertices_normal[index] = vertex_normal

        self.normals = vertices_normal.flatten()
