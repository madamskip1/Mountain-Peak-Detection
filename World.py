class World:
    def __init__(self, terrain_map):
        self.terrain_map = terrain_map
        self.latitude_range = [49.0, 50.0]
        self.longitude_range = [20.0, 21.0]
        self.vertices = terrain_map.get_vertices()
        self.x_step, self.y_step, self.z_step = terrain_map.get_steps()
        self.rows, self.cols = terrain_map.get_size()
        self.geo_x_step = abs(self.longitude_range[1] - self.longitude_range[0]) / self.cols
        self.geo_z_step = abs(self.latitude_range[1] - self.latitude_range[0]) / self.rows

    def get_latitude_range(self):
        return self.latitude_range

    def get_longitude_range(self):
        return self.longitude_range

    def get_vertex_num(self, latitude, longitude):
        z = int((latitude - self.latitude_range[0]) / self.geo_z_step)
        x = int((longitude - self.longitude_range[0]) / self.geo_x_step)
        vertex_num = (x + 1) * self.cols - z - 1

        return vertex_num

    def get_vertex_coords(self, vertex_num):
        vertex_coords_start = vertex_num * 3
        return self.vertices[vertex_coords_start: vertex_coords_start + 3]
