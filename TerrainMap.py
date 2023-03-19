from terrain_map_loader import load_terrain_map
from OpenGL.GL import *


class TerrainMap:
    def __init__(self, hgt_file_path, rows, cols, simplify_factor=1):
        self.vao = None
        self.vbo_vertices = None
        self.vbo_normals = None
        self.ibo = None
        self.simplify_factor = simplify_factor
        self.rows = rows
        self.cols = cols
        self.data, self.vertices, self.triangles, self.normals, size, self.steps = load_terrain_map(hgt_file_path, rows, cols,
                                                                                              simplify_factor)
        self.rows, self.cols = size
        self.num_triangles = len(self.triangles)

    def prepare_buffers(self):
        self.__gen_vao()
        self.__gen_vbo_vertices()
        self.__gen_vbo_normals()
        self.__gen_ibo()
        glBindVertexArray(0)

    def draw(self, view, perspective, shader_program):
        glBindVertexArray(self.vao)

        view_loc = glGetUniformLocation(shader_program, "view")
        glUniformMatrix4fv(view_loc, 1, GL_FALSE, view)
        perspective_loc = glGetUniformLocation(shader_program, "projection")
        glUniformMatrix4fv(perspective_loc, 1, GL_FALSE, perspective)

        glDrawElements(GL_TRIANGLES, self.num_triangles, GL_UNSIGNED_INT, None)

        glBindVertexArray(0)

    def get_vertices(self):
        print(self.vertices)
        return self.vertices

    def get_triangles(self):
        return self.triangles

    def get_size(self):
        print(self.rows, self.cols)
        return [self.rows, self.cols]

    def get_steps(self):
        return self.steps  # [x_step, y_step, z_step]

    def __gen_vbo_vertices(self):
        self.vbo_vertices = glGenBuffers(1)
        glBindBuffer(GL_ARRAY_BUFFER, self.vbo_vertices)
        glBufferData(GL_ARRAY_BUFFER, self.vertices.nbytes, self.vertices, GL_STATIC_DRAW)
        stride = 3 * self.vertices.itemsize
        offset = ctypes.c_void_p(0)
        glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, stride, offset)
        glEnableVertexAttribArray(0)

    def __gen_vbo_normals(self):
        self.vbo_normals = glGenBuffers(1)
        glBindBuffer(GL_ARRAY_BUFFER, self.vbo_normals)
        glBufferData(GL_ARRAY_BUFFER, self.normals.nbytes, self.normals, GL_STATIC_DRAW)
        stride = 3 * self.vertices.itemsize
        offset = ctypes.c_void_p(0)
        glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, stride, offset)
        glEnableVertexAttribArray(1)

    def __gen_ibo(self):
        self.ibo = glGenBuffers(1)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, self.ibo)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, self.triangles.nbytes, self.triangles, GL_STATIC_DRAW)

    def __gen_vao(self):
        self.vao = glGenVertexArrays(1)
        glBindVertexArray(self.vao)
