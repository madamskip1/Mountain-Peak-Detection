import numpy as np
from OpenGL.GL import *
from OpenGL.GLUT import *
from PIL import Image, ImageDraw, ImageOps
import cv2 as cv
from unidecode import unidecode

from Camera import Camera
from TerrainModel import TerrainModel
from World import World
from Peaks import Peaks
from shaders_program import create_shader_program
from diffuse_lightning import set_diffuse_light

####################

obs_location = [49.3390454, 20.081936, 991.1]
obs_angles = [144.31152, 2.5936904, 0.4797333]
window_size = [3840, 5120]
window_size = [768, 1024]

####################



terrain_map = TerrainModel(
    hgt_file_path="N49E020.hgt",
    hgt_size=3601,
    world_size=[100],
    simplify_factor=2)

world = World(terrain_map, obs_location)
peaks = Peaks(world)
shader_program = None

cam_pos_xyz = world.get_coord_from_geo(*obs_location)
cam_position = np.array([cam_pos_xyz[0], cam_pos_xyz[1], cam_pos_xyz[2]])

light_pos = np.array([cam_pos_xyz[0], cam_pos_xyz[1]+0.0, cam_pos_xyz[2]])
light_color = [0.99, 0.99, 0.99]



camera = Camera(
    position=cam_position,
    fov_h=66,
    aspect_ratio=window_size[0]/window_size[1],
    near=0.01,
    far=50,
)

camera.set_angles(*obs_angles)


def init():
    glClearColor(0.0, 0.0, 0.0, 0.0)
    glEnable(GL_DEPTH_TEST)


def display():
    global camera, terrain_map, shader_program, world, peaks
    glClearColor(0.0, 0.0, 0.0, 1.0)
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
    view_matrix = camera.calc_view_matrix()
    perspective_matrix = camera.calc_perspective_matrix()
    world.set_mvp_matrices(view_matrix, perspective_matrix)

    terrain_map.draw(view_matrix, perspective_matrix, shader_program)
    peaks_visible = peaks.get_visible_peaks()
    save(peaks_visible)
    glutSwapBuffers()


def save(peaks_visible):
    data =( GLubyte * (3 * window_size[0] * window_size[1]) )(0)
    glReadPixels(0, 0, window_size[0], window_size[1], GL_RGB, GL_UNSIGNED_BYTE, data)
    image = Image.frombytes(mode="RGB", size=(window_size[0], window_size[1]), data=data)
    image = image.transpose(Image.FLIP_TOP_BOTTOM)
    image = ImageOps.mirror(image)

    image.save("output/rendered_scene.png")
    save_edges_img(image)
    image = add_peaks_names(image, peaks_visible)
    image.save("output/rendered_scene_annotated.png")
    save_edges_img(image, "_annotated")

def add_peaks_names(image, peaks_visible):
    image_draw = ImageDraw.Draw(image)
    peaks_visible[['name', 'screen_x', 'screen_y', 'screen_z', 'distance']].apply(lambda x:
                add_text_to_img(image_draw, x['name'] + " (" + str(x['distance']) + ")", [x['screen_x'], x['screen_y']]), axis=1)
    return image

def save_edges_img(image, mod=""):
    opencv_image = cv.cvtColor(np.array(image), cv.COLOR_RGB2BGR)
    edges_image = cv.Canny(opencv_image, 100, 255)
    cv.imwrite("output/edges"+mod+".png", edges_image)

def add_text_to_img(image_draw, text, position):
    position[0] = int(position[0])
    position[0] = window_size[0] - position[0]
    position[1] = int(position[1])
    position[1] = window_size[1] - position[1]
    pos_y = np.random.randint(100, window_size[1] - 100)
    image_draw.line((position[0], position[1], position[0], pos_y), fill=(255, 0, 0, 255))
    text = unidecode(text)
    image_draw.text((position[0], pos_y), text)




def keyboard_callback(key, *_):
    global camera
    camera.keyboard_callback(key)
    glutPostRedisplay()


def main():
    global shader_program, terrain_map, light_pos, light_color
    glutInit()
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH | GLUT_MULTISAMPLE)
    glutInitWindowSize(window_size[0], window_size[1])
    glutCreateWindow(b"Cube")
    glutDisplayFunc(display)
    glutSpecialFunc(keyboard_callback)
    shader_program = create_shader_program()
    glUseProgram(shader_program)
    glEnable(GL_MULTISAMPLE)
    terrain_map.prepare_buffers()
    set_diffuse_light(shader_program, light_pos, light_color)
    init()
    glutMainLoop()


if __name__ == "__main__":
    main()
