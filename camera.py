import numpy as np
from OpenGL.GLUT import *

def rotate_up_vector(up_vector, angle, front):
    angle = np.deg2rad(angle)
    front = front / np.linalg.norm(front)
    cos_angle = np.cos(angle)
    sin_angle = np.sin(angle)
    x, y, z = front

    rotation_matrix = np.array([
      [ (cos_angle + x*x * (1 - cos_angle)), (x * y * (1 - cos_angle) - z * sin_angle), (x * z * (1 - cos_angle) + y * sin_angle) ],
      [ (y * x * (1 - cos_angle) + z * sin_angle), (cos_angle + y * y * (1 - cos_angle)), (y * z * (1 - cos_angle) - x * sin_angle) ],
      [ (z * x * (1 - cos_angle) - y * sin_angle), (z * y * (1 - cos_angle) + x * sin_angle), (cos_angle  + z * z * (1 - cos_angle)) ]
    ])
    up_vector = np.dot(rotation_matrix, up_vector)
    return up_vector

def fix_angles(yaw_degree, pitch_degree, roll_degree):
    yaw_degree = 180.0 - yaw_degree
    if yaw_degree < 0.0:
        yaw_degree = 360.0 + yaw_degree
    elif yaw_degree >= 360.0:
        yaw_degree = yaw_degree - 360.0

    pitch_degree = (-1) * pitch_degree
    roll_degree = (-1) * roll_degree
    return yaw_degree, pitch_degree, roll_degree


class Camera:
    def __init__(self, position, fov_h, aspect_ratio, near, far, target=np.array([0, 0, 0]),
                 up=np.array([0.0, 1.0, 0.0])):
        self.position = position

        self.target = target
        self.up = up
        self.direction = target - position
        self.yaw_degree = 0.0
        self.pitch_degree = 0.0
        self.roll_degree = 0.0

        self.fov_h = fov_h
        self.aspect_ratio = aspect_ratio
        self.near = near
        self.far = far

    def set_position(self, position):
        self.position = position

    def set_angles(self, yaw_degree, pitch_degree, roll_degree):
        yaw_degree, pitch_degree, roll_degree = fix_angles(yaw_degree, pitch_degree, roll_degree)
        self.yaw_degree = yaw_degree
        self.pitch_degree = pitch_degree
        self.roll_degree = roll_degree
        self.__update_direction()
        self.__update_target()
        self.__update_up()

    def calc_view_matrix(self):
        eye = self.position
        eye_target = eye - self.target

        z_matrix = eye_target / np.linalg.norm(eye_target)
        x_matrix = np.cross(self.up, z_matrix)
        x_matrix = x_matrix / np.linalg.norm(x_matrix)
        y_matrix = np.cross(z_matrix, x_matrix)
        y_matrix = y_matrix / np.linalg.norm(y_matrix)

        x_translation = np.dot(x_matrix, -eye)
        y_translation = np.dot(y_matrix, -eye)
        z_translation = np.dot(z_matrix, -eye)

        view_matrix = np.array([
            x_matrix[0], y_matrix[0], z_matrix[0], 0,  # col 1
            x_matrix[1], y_matrix[1], z_matrix[1], 0,  # col 2
            x_matrix[2], y_matrix[2], z_matrix[2], 0,  # col 3
            x_translation, y_translation, z_translation, 1  # col 4
        ])

        return view_matrix

    def calc_perspective_matrix(self):
        f = 1 / np.tan(np.deg2rad(self.fov_h / 2.0))
        projection_matrix = np.array([
            f / self.aspect_ratio, 0.0, 0.0, 0.0,  # col 1
            0.0, f, 0.0, 0.0,  # col 2
            0.0, 0.0, (self.far + self.near) / (self.near - self.far), -1.0,  # col 3
            0.0, 0.0, (2 * self.far * self.near) / (self.near - self.far), 0.0  # col 4
        ])

        return projection_matrix

    def keyboard_callback(self, key):
        if key == GLUT_KEY_LEFT:
            self.position[0] = self.position[0] - 0.1
        elif key == GLUT_KEY_RIGHT:
            self.position[0] = self.position[0] + 0.1
        elif key == GLUT_KEY_UP:
            self.position[2] = self.position[2] + 0.1
        elif key == GLUT_KEY_DOWN:
            self.position[2] = self.position[2] - 0.1

    def __update_direction(self):
        yaw_radians = np.deg2rad(self.yaw_degree)
        pitch_radians = np.deg2rad(self.pitch_degree)

        direction = np.zeros(3)
        direction[0] = np.cos(yaw_radians) * np.cos(pitch_radians)
        direction[1] = np.sin(pitch_radians)
        direction[2] = np.sin(yaw_radians) * np.cos(pitch_radians)

        self.direction = direction / np.linalg.norm(direction)

    def __update_target(self):
        self.target = self.position + self.direction

    def __update_up(self):
        self.up = rotate_up_vector(self.up, self.roll_degree, self.direction)
