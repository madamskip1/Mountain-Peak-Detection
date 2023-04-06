import numpy as np


def load_terrain_map(file_path, hgt_size, simplify_factor=1):
    elevation = __load_hgt_file(file_path, hgt_size)

    if simplify_factor > 1:
        elevation, hgt_size = __simplify_elevation(elevation, simplify_factor)

    vertices, steps = __generate_vertices(elevation, hgt_size)
    triangles = __generate_triangles(hgt_size)
    normals = __generate_triangles_normals(triangles, vertices)
    print("Steps: ", steps)
    return elevation, vertices, triangles, normals, hgt_size, steps


def __load_hgt_file(file_path, hgt_size):
    with open(file_path, 'rb') as file:
        elevation = np.fromfile(file, np.dtype('>i2'), hgt_size * hgt_size)
        elevation = elevation.reshape(hgt_size, hgt_size)

    return elevation


def __simplify_elevation(elevation, simplify_factor):
    elevation = elevation[::simplify_factor, ::simplify_factor]
    hgt_size = elevation.shape[0]

    return elevation, hgt_size


def __generate_vertices(elevation, hgt_size):
    size = 100
    origin = [0.0, .0, 0.0]
    direction = [1.0, 1.0, -1.0]
    simplified_by = 3601 / hgt_size
    origin_x, origin_y, origin_z = origin
    origin_x = 0  # -size / 2
    origin_z = 0
    direction_x, direction_y, direction_z = direction
    x_step = size / (hgt_size - 1)
    y_step = x_step
    altitude_scale = x_step / 30 / simplified_by

    vertices = np.zeros((hgt_size * hgt_size * 3), dtype=np.float32)
    vertices_index = 0

    for (x, y), altitude in np.ndenumerate(elevation):
        x_coord = origin_x + x_step * x
        y_coord = origin_y + altitude_scale * altitude  # up is Y coord in OpenGL
        z_coord = origin_z + y_step * y
        vertices[vertices_index] = x_coord
        vertices[vertices_index + 1] = y_coord
        vertices[vertices_index + 2] = z_coord
        vertices_index = vertices_index + 3

    print("DU:")
    print([x_step, altitude_scale, y_step])
    return vertices, [x_step, altitude_scale, y_step]


def __generate_triangles(hgt_size):
    triangles_num = ((hgt_size - 1) * (hgt_size - 1) * 2)
    triangles_indices_num = triangles_num * 3

    triangles = np.zeros(triangles_indices_num, dtype=np.uint32)
    triangles_index = 0
    index = 0

    for x in range(hgt_size - 1):
        for y in range(hgt_size - 1):
            a = index
            b = index + 1
            c = index + hgt_size + 1
            d = index + hgt_size
            index = index + 1

            triangles[triangles_index] = a
            triangles[triangles_index + 1] = b
            triangles[triangles_index + 2] = c

            triangles[triangles_index + 3] = a
            triangles[triangles_index + 4] = c
            triangles[triangles_index + 5] = d

            triangles_index = triangles_index + 6
        index = index + 1

    return triangles


def __generate_triangles_normals(triangles, vertices):
    vertices_triangles_normals = [[] for i in range(int(len(vertices) / 3))]
    vertices_np = np.array(vertices).reshape(-1, 3)
    vertices_normal = np.zeros_like(vertices_np)

    for A, B, C in zip(*[iter(triangles)] * 3):
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

    return vertices_normal.flatten()
