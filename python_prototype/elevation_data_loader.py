import numpy as np
from CoordsManager import equirectangular_approximation

SRTM_DATA_DIR = "srtm_data"


def load_elevation_data(observer_latitude, observer_longitude, max_distance, hgt_size, simplify_factor):
    global SRTM_DATA_DIR
    coords = __prepare_coords(observer_latitude, observer_longitude, max_distance)
    coords_range = __get_coords_range(coords)
    world_size, grid_size = __calc_world_size(coords_range)
    files_names_grid = __prepare_files_names_grid(coords, int(observer_latitude), int(observer_longitude))
    elevation = __read_files_grid(SRTM_DATA_DIR, files_names_grid, hgt_size, simplify_factor)
    return elevation, coords_range, world_size, grid_size


def __prepare_coords(observer_latitude, observer_longitude, max_distance):
    int_latitude = int(observer_latitude)
    int_longitude = int(observer_longitude)
    coords_to_check = []
    coords_for_files_names = []

    # latitude: bottom, center, top
    if observer_latitude > 0:
        y = [int_latitude, observer_latitude, int_latitude + 1]
    else:
        y = [int_latitude - 1, observer_latitude, int_latitude]

    # longitude: left, center, right
    if observer_longitude > 0:
        x = [int_longitude, observer_longitude, int_longitude + 1]
    else:
        x = [int_longitude - 1, observer_longitude, int_longitude]

    if observer_latitude == int_latitude and observer_longitude == int_longitude:
        for i in range(2):
            for j in range(2):
                coords_for_files_names.append((int_latitude - i, int_longitude - j))
    elif observer_latitude == int_latitude:
        coords_for_files_names.append((int_latitude - 1, x[0]))
        coords_for_files_names.append((int_latitude, x[0]))
        for i in range(2):
            for j in [0, 2]:
                coords_to_check.append((int_latitude, x[j], int_latitude - i, x[0] + (j - 1)))
    elif observer_longitude == int_longitude:
        coords_for_files_names.append((y[0], int_longitude - 1))
        coords_for_files_names.append((y[0], int_longitude))

        for i in [0, 2]:
            for j in range(2):
                coords_to_check.append((y[i], int_longitude, y[0] + (i - 1), int_longitude - j))
    else:
        coords_for_files_names.append((int_latitude, int_longitude))
        for i in range(3):  # latitude
            y_coord_to_check = y[i]
            y_coord_file = y[0] + (i - 1)
            for j in range(3):  # longitude
                if not (i == 1 and j == 1):
                    x_coord_to_check = x[j]
                    x_coord_file = x[0] + (j - 1)
                    coords_to_check.append((y_coord_to_check, x_coord_to_check, y_coord_file, x_coord_file))

    for coord_to_check_info in coords_to_check:
        coord_to_check_distance = (coord_to_check_info[0], coord_to_check_info[1])
        coord_for_file_name = (coord_to_check_info[2], coord_to_check_info[3])
        if max_distance > equirectangular_approximation(observer_latitude, observer_longitude,
                                                        *coord_to_check_distance):
            coords_for_files_names.append(coord_for_file_name)

    return coords_for_files_names


def __prepare_files_names_grid(coords, central_latitude, central_longitude):
    files_names_grid = np.array([
        [None, None, None],
        [None, None, None],
        [None, None, None]]
    )
    for coord in coords:
        latitude_dif = int(coord[0] - central_latitude)
        longitude_dif = int(coord[1] - central_longitude)
        files_names_grid[1 - latitude_dif][1 + longitude_dif] = __convert_coords_to_file_name(*coord) + ".hgt"

    return files_names_grid


def __convert_coords_to_file_name(latitude, longitude):
    str_latitude = str(int(abs(latitude)))
    str_longitude = str(int(abs(longitude)))
    if -10 < latitude < 10:
        str_latitude = "0" + str_latitude
    if -100 < longitude < 100:
        if -10 < longitude < 10:
            str_longitude = "00" + str_longitude
        else:
            str_longitude = "0" + str_longitude

    if latitude >= 0:
        file_name = "N" + str_latitude
    else:
        file_name = "S" + str_latitude

    if longitude >= 0:
        file_name = file_name + "E" + str_longitude
    else:
        file_name = file_name + "W" + str_longitude

    return file_name


def __read_files_grid(srtm_data_dir, files_names_grid, hgt_size, simplify_factor):
    rows = []
    for i, row in enumerate(files_names_grid):
        to_concatenate = []
        for file in row:
            if file is not None:
                hgt_file = __load_hgt_file(srtm_data_dir + '/' + file, hgt_size, simplify_factor)
                to_concatenate.append(hgt_file)
        if len(to_concatenate) == 1:
            rows.append(to_concatenate[0])
        elif len(to_concatenate) > 1:
            rows.append(np.concatenate(to_concatenate, axis=1))
    if len(rows) == 1:
        result = rows[0]
    elif len(rows) > 1:
        result = np.concatenate(rows)
    else:
        result = None

    return result


def __load_hgt_file(path, hgt_size, simplify_factor):
    with open(path, 'rb') as file:
        elevation = np.fromfile(file, np.dtype('>i2'), hgt_size * hgt_size)
        elevation = elevation.reshape(hgt_size, hgt_size)

    if simplify_factor > 0:
        elevation = elevation[::simplify_factor, ::simplify_factor]

    return elevation


def __get_coords_range(coords):
    min_latitude = 1000
    max_latitude = -1000
    min_longitude = 1000
    max_longitude = -1000

    for coord in coords:
        min_latitude = min(min_latitude, coord[0])
        max_latitude = max(max_latitude, coord[0])
        min_longitude = min(min_longitude, coord[1])
        max_longitude = max(max_longitude, coord[1])
    max_longitude = max_longitude + 1
    max_latitude = max_latitude + 1
    return (min_latitude, max_latitude), (min_longitude, max_longitude)


def __calc_world_size(coords_range):
    latitude_range = coords_range[0]
    longitude_range = coords_range[1]
    latitude_distance = 0.0
    longitude_distance = 0.0

    for i in range(2):
        latitude_distance = latitude_distance + equirectangular_approximation(latitude_range[0], longitude_range[i],
                                                                              latitude_range[1], longitude_range[i])
        longitude_distance = longitude_distance + equirectangular_approximation(latitude_range[i], longitude_range[0],
                                                                                latitude_range[i], longitude_range[1])
    latitude_distance = latitude_distance / 2.0
    longitude_distance = longitude_distance / 2.0
    longitude_one_grid_size = longitude_distance / abs(longitude_range[1] - longitude_range[0])
    latitude_one_grid_size = latitude_distance / abs(latitude_range[1] - latitude_range[0])

    return (latitude_distance, longitude_distance), (latitude_one_grid_size, longitude_one_grid_size)
