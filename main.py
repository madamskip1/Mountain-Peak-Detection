import numpy as np
from OpenGL.GL import *
from OpenGL.GLUT import *
from PIL import Image, ImageDraw, ImageFont

from Camera import Camera
from TerrainModel import TerrainModel
from World import World
from Peaks import Peaks
from shaders_program import create_shader_program
from diffuse_lightning import set_diffuse_light

####################

obs_location = [49.3390454, 20.081936, 1000.0]
obs_angles = [144.31152, 2.5936904]

####################



terrain_map = TerrainModel(
    hgt_file_path="N49E020.hgt",
    hgt_size=3601,
    world_size=100,
    simplify_factor=5)

world = World(terrain_map)
peaks = Peaks(world)
shader_program = None

cam_pos_xyz = world.get_coord_from_geo(*obs_location)
cam_position = np.array([cam_pos_xyz[0], cam_pos_xyz[1], cam_pos_xyz[2]])

light_pos = np.array([cam_pos_xyz[0], cam_pos_xyz[1] + 1.0, cam_pos_xyz[2]])
light_color = [0.99, 0.99, 0.99]



camera = Camera(
    position=cam_position,
    fov_h=65,
    aspect_ratio=8.0 / 6.0,
    near=0.01,
    far=30,
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
    peaks_visible = peaks.get_peaks_in_frutsum()
    save(peaks_visible)
    glutSwapBuffers()


def save(peaks_visible):
    data =( GLubyte * (3*800*600) )(0)
    glReadPixels(0, 0, 800, 600, GL_RGB, GL_UNSIGNED_BYTE, data)
    image = Image.frombytes(mode="RGB", size=(800, 600), data=data)
    image = image.transpose(Image.FLIP_TOP_BOTTOM)

    image.save("output/rendered_scene.png")
    image = add_peaks_names(image, peaks_visible)
    image.save("output/rendered_scene_annotated.png")

def add_peaks_names(image, peaks_visible):
    image_draw = ImageDraw.Draw(image)
    peaks_visible[['name', 'screen_x', 'screen_y', 'screen_z']].apply(lambda x:
                add_text_to_img(image_draw, x['name'], [x['screen_x'], x['screen_y']]), axis=1)
    return image

def add_text_to_img(image_draw, text, position):
    position[0] = int(position[0])
    position[1] = int(position[1])
    position[1] = 600 - position[1]
    pos_y = np.random.randint(50, 550)
    image_draw.line((position[0], position[1], position[0], pos_y), fill=(255, 0, 0, 255))
    text = text.encode('latin-1', errors="ignore")
    image_draw.text((position[0], pos_y), text)




def keyboard_callback(key, *_):
    global camera
    camera.keyboard_callback(key)
    glutPostRedisplay()


def main():
    global shader_program, terrain_map, light_pos, light_color
    glutInit()
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH)
    glutInitWindowSize(800, 600)
    glutCreateWindow(b"Cube")
    glutDisplayFunc(display)
    glutSpecialFunc(keyboard_callback)
    shader_program = create_shader_program()
    glUseProgram(shader_program)
    terrain_map.prepare_buffers()
    set_diffuse_light(shader_program, light_pos, light_color)
    init()
    glutMainLoop()


if __name__ == "__main__":
    main()
