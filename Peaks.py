import numpy as np
import pandas as pd
from OpenGL.GL import *


class Peaks:
    def __init__(self, terrain_data, screen_manager, coords_manager):
        self.terrain_data = terrain_data
        self.screen_manager = screen_manager
        self.coords_manager = coords_manager
        self.dataframe = None
        self.__peaks_data_path = 'peaks_data/'
        self.__prepare_dataframe()
        self.__prepare_model_coords()

    def get_visible_peaks(self):
        in_frustum = self.__frustum_test()
        occlusion_passed = self.__occlusion_test(in_frustum)

        if occlusion_passed.empty:
            return None

        visible_peaks_with_distance = self.__calc_distances(occlusion_passed)
        visible_peaks_with_distance.to_csv('output/visible_peaks.csv', sep=';', header=False, index=False)

        return visible_peaks_with_distance

    def __prepare_dataframe(self):
        files_names = self.__prepare_list_of_data_files_names()
        if len(files_names) == 1:
            self.dataframe = self.__read_data(files_names[0])
        else:
            dataframes = []
            for file_name in files_names:
                dataframes.append(self.__read_data(file_name))
            self.dataframe = pd.concat(dataframes)

    def __prepare_list_of_data_files_names(self):
        latitude_start, latitude_end = self.coords_manager.latitude_range
        longitude_start, longitude_end = self.coords_manager.longitude_range
        latitude_dif = int(abs(latitude_start - latitude_end))
        longitude_dif = int(abs(longitude_start - longitude_end))
        files_names = []

        for i_latitude in range(latitude_dif):
            for j_longitude in range(longitude_dif):
                latitude = int(latitude_start + i_latitude)
                longitude = int(longitude_start + j_longitude)
                file_name = self.__peaks_data_path + str(latitude) + "_" + str(longitude) + ".csv"
                files_names.append(file_name)

        return files_names

    def __prepare_model_coords(self):
        self.dataframe[['vertex_x', 'vertex_y', 'vertex_z']] = self.dataframe[['latitude', 'longitude']].apply(
            lambda x: pd.Series(self.__get_peak_vertex(
                x['latitude'],
                x['longitude']
            )),
            axis=1)

        self.dataframe = self.dataframe.loc[self.dataframe['vertex_x'] != -1]
        self.dataframe['vertex_y'] = self.dataframe['vertex_y'].apply(lambda x: x + 0.000001)

    def __read_data(self, file_name):
        headers = ['name', 'latitude', 'longitude', 'feature_code', 'elevation', 'dem']
        data = pd.read_csv(file_name, delimiter=';', header=None, names=headers)
        return data

    def __frustum_test(self):
        self.dataframe[['screen_x', 'screen_y', 'screen_z']] = self.dataframe[
            ['vertex_x', 'vertex_y', 'vertex_z']].apply(
            lambda x:
            pd.Series(self.screen_manager.get_screen_point(x['vertex_x'], x['vertex_y'], x['vertex_z'])),
            axis=1)

        peaks_in_frustum = self.dataframe[self.dataframe[
            ['screen_x', 'screen_y', 'screen_z']
        ].apply(lambda x:
                self.screen_manager.check_if_point_on_screen(
                    x['screen_x'], x['screen_y'], x['screen_z']
                ), axis=1)]
        return peaks_in_frustum

    def __occlusion_test(self, df):
        occlusion_peaks_passed = self.__occlusion_query(df)
        occlusion_passed = df[occlusion_peaks_passed]

        return occlusion_passed

    def __occlusion_query(self, df):
        df_len = len(df.index)
        occlusion_peaks_passed = np.full(df_len, False, dtype=bool)
        query_ids = glGenQueries(df_len)
        query_index = 0

        for index, row in df.iterrows():
            glBeginQuery(GL_SAMPLES_PASSED, query_ids[query_index])
            glBegin(GL_POINTS)
            vertex = np.array([row['vertex_x'], row['vertex_y'], row['vertex_z']])
            glVertex3fv(vertex)
            glEnd()
            glEndQuery(GL_SAMPLES_PASSED)
            query_result = glGetQueryObjectiv(query_ids[query_index], GL_QUERY_RESULT)

            if query_result > 0:
                occlusion_peaks_passed[query_index] = True
            query_index = query_index + 1

        return occlusion_peaks_passed

    def __calc_distances(self, df):
        df['distance'] = df[['latitude', 'longitude']].apply(
            lambda x: round(self.coords_manager.calc_distance_observer_to_point(x['latitude'], x['longitude']), 1),
            axis=1
        )
        return df

    def __get_peak_vertex(self, latitude, longitude):
        local_x, _, local_z = self.coords_manager.convert_geo_to_local_coords(latitude, longitude, 0.0)
        x = local_x / self.terrain_data.scale[0]
        z = local_z / self.terrain_data.scale[2]
        # x = int(x)
        # z = int(z)
        x = round(x)
        z = round(z)
        x = x - self.terrain_data.offset_x
        z = z - self.terrain_data.offset_z

        if x < 0 or z < 0 or x >= self.terrain_data.rows or z >= self.terrain_data.cols:
            return -1, -1, -1

        max_x, max_y, max_z = 0, 0, 0
        start_x = max(0, x - 1)
        start_z = max(0, z - 1)
        end_x = min(x + 1, self.terrain_data.rows - 1)
        end_z = min(z + 1, self.terrain_data.cols - 1)

        for x_loop in range(start_x, end_x + 1):
            for z_loop in range(start_z, end_z + 1):
                vertex_num = x_loop * self.terrain_data.cols + z_loop
                vertex_coords_start = vertex_num * 3
                vertex_coords_end = vertex_coords_start + 3
                vertex_coord = self.terrain_data.get_vertices(vertex_coords_start, vertex_coords_end)
                
                if vertex_coord[1] > max_y:
                    max_x = vertex_coord[0]
                    max_y = vertex_coord[1]
                    max_z = vertex_coord[2]

        return max_x, max_y, max_z
