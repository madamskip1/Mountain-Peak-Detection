import math


def equirectangular_approximation(lat1, long1, lat2, long2):
    EARTH_RADIUS = 6371
    delta_longitude = math.radians(long2 - long1)
    delta_latitude = math.radians(lat2 - lat1)
    sum_latitude = math.radians(lat1 + lat2)

    x = delta_longitude * math.cos(sum_latitude / 2.0)
    distance = EARTH_RADIUS * math.sqrt(x * x + delta_latitude * delta_latitude)
    return distance


class CoordsManager:
    def __init__(self, observer_location):
        self.latitude_range = [49.0, 50.0]
        self.longitude_range = [20.0, 21.0]
        self.world_size = [111.2, 71.0]
        self.observer_location_geo = observer_location

    def convert_geo_to_local_coords(self, latitude, longitude, altitude):
        z = (longitude - self.longitude_range[0]) * self.world_size[1]
        x = (self.latitude_range[0] + 1.0 - latitude) * self.world_size[0]
        altitude_scale = 1 / 1000
        y = altitude * altitude_scale

        return x, y, z

    def calc_distance_observer_to_point(self, latitude, longitude):
        return equirectangular_approximation(latitude, longitude,
                                             self.observer_location_geo[0], self.observer_location_geo[1])
