from OpenGL.GL import *


class TerrainModel:
    def __init__(self, terrain_data):
        self.terrain_data = terrain_data
        self.vao = None
        self.vbo_vertices = None
        self.vbo_normals = None
        self.ibo = None

    def draw(self, view, perspective, shader_program):
        glBindVertexArray(self.vao)

        view_loc = glGetUniformLocation(shader_program, "view")
        glUniformMatrix4fv(view_loc, 1, GL_FALSE, view)
        perspective_loc = glGetUniformLocation(shader_program, "projection")
        glUniformMatrix4fv(perspective_loc, 1, GL_FALSE, perspective)

        glDrawElements(GL_TRIANGLES, len(self.terrain_data.triangles), GL_UNSIGNED_INT, None)

        glBindVertexArray(0)

    def prepare_buffers(self):
        self.__gen_vao()
        self.__gen_vbo_vertices()
        self.__gen_vbo_normals()
        self.__gen_ibo()
        glBindVertexArray(0)

    def __gen_vbo_vertices(self):
        self.vbo_vertices = glGenBuffers(1)
        glBindBuffer(GL_ARRAY_BUFFER, self.vbo_vertices)
        glBufferData(GL_ARRAY_BUFFER, self.terrain_data.vertices.nbytes, self.terrain_data.vertices, GL_STATIC_DRAW)
        stride = 3 * self.terrain_data.vertices.itemsize
        offset = ctypes.c_void_p(0)
        glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, stride, offset)
        glEnableVertexAttribArray(0)

    def __gen_vbo_normals(self):
        self.vbo_normals = glGenBuffers(1)
        glBindBuffer(GL_ARRAY_BUFFER, self.vbo_normals)
        glBufferData(GL_ARRAY_BUFFER, self.terrain_data.normals.nbytes, self.terrain_data.normals, GL_STATIC_DRAW)
        stride = 3 * self.terrain_data.vertices.itemsize
        offset = ctypes.c_void_p(0)
        glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, stride, offset)
        glEnableVertexAttribArray(1)

    def __gen_ibo(self):
        self.ibo = glGenBuffers(1)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, self.ibo)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, self.terrain_data.triangles.nbytes, self.terrain_data.triangles,
                     GL_STATIC_DRAW)

    def __gen_vao(self):
        self.vao = glGenVertexArrays(1)
        glBindVertexArray(self.vao)
