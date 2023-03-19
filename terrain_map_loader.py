import numpy as np


def load_terrain_map(file_path, rows, cols, simplify_factor=1):
    elevation = __load_hgt_file(file_path, rows, cols)
    if simplify_factor > 1:
        elevation, rows, cols = __simplify_elevation(elevation, simplify_factor)

    vertices = __generate_vertices(elevation, rows, cols)
    triangles = __generate_triangles(rows, cols)
    normals = __generate_triangles_normals(triangles, vertices)

    return elevation, vertices, triangles, normals


def __load_hgt_file(file_path, rows, cols):
    with open(file_path, 'rb') as file:
        elevation = np.fromfile(file, np.dtype('>i2'), rows * cols)
        elevation = elevation.reshape(rows, cols)
        elevation = elevation.T

    return elevation


def __simplify_elevation(elevation, simplify_factor):
    elevation = elevation[::simplify_factor, ::simplify_factor]
    rows = elevation.shape[0]
    cols = elevation.shape[1]

    return elevation, rows, cols


def __generate_vertices(elevation, rows, cols):
    origin = [-1.0, .0, -1.0]
    size = 2
    simplified_by = 3601 / rows
    origin_x, origin_y, origin_z = origin
    x_step = size / (cols - 1)
    y_step = size / (rows - 1)
    altitude_scale = x_step / 30 / simplified_by

    vertices = np.zeros((rows * cols * 3), dtype=np.float32)
    vertices_index = 0

    for (x, y), z in np.ndenumerate(elevation):
        x_coord = origin_x + x_step * x
        y_coord = origin_y + altitude_scale * z  # up is Y coord in OpenGL
        z_coord = origin_z + y_step * y

        vertices[vertices_index] = x_coord
        vertices[vertices_index + 1] = y_coord
        vertices[vertices_index + 2] = z_coord
        vertices_index = vertices_index + 3

    return vertices


def __generate_triangles(rows, cols):
    triangles_num = ((cols - 1) * (rows - 1) * 2)
    triangles_indices_num = triangles_num * 3

    triangles = np.zeros(triangles_indices_num, dtype=np.uint32)
    triangles_index = 0
    index = 0

    for x in range(cols - 1):
        for y in range(rows - 1):
            a = index
            b = index + 1
            c = index + cols + 1
            d = index + cols
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