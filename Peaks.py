import pandas as pd
import numpy as np


class Peaks:
    def __init__(self, world):
        self.world = world
        self.dataframe = None
        self.__peaks_data_path = 'peaks_data/'
        self.__prepare_dataframe()
        self.__prepare_model_coords()

    def __prepare_model_coords(self):
        self.dataframe['vertex_num'] = np.NaN
        self.dataframe['vertex_num'] = self.dataframe[['latitude', 'longitude']].apply(lambda x:
                                                            self.world.get_vertex_num(x['latitude'], x['longitude']), axis=1)
        print(self.dataframe[self.dataframe['name'] == 'Lomnický štít'])

        self.dataframe['vertex_x'] = np.NaN
        self.dataframe['vertex_y'] = np.NaN
        self.dataframe['vertex_z'] = np.NaN
        self.dataframe[['vertex_x', 'vertex_y', 'vertex_z']] = self.dataframe['vertex_num'].apply(
            lambda x: pd.Series(self.world.get_vertex_coords(x)))

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
        latitude_start, latitude_end = self.world.get_latitude_range()
        longitude_start, longitude_end = self.world.get_longitude_range()
        latitude_dif = int(abs(latitude_start - latitude_end))
        longitude_dif = int(abs(longitude_start - longitude_end))
        files_names = []
        for lati in range(latitude_dif):
            for long in range(longitude_dif):
                latitude = int(latitude_start + lati)
                longitude = int(longitude_start + long)
                file_name = self.__peaks_data_path + str(latitude) + "_" + str(longitude) + ".csv"
                files_names.append(file_name)

        return files_names

    def __read_data(self, file_name):
        headers = ['name', 'latitude', 'longitude', 'feature_code', 'elevation', 'dem']
        data = pd.read_csv(file_name, delimiter=';', header=None, names=headers)
        return data
