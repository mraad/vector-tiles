#!/usr/bin/env python

import mapbox_vector_tile
from shapely.geometry import LineString
from shapely.geometry import Polygon

if __name__ == "__main__":
    poly = Polygon([(0, 0), (0, 1), (1, 1), (1, 0), (0, 0)])
    line = LineString([(159, 3877), (-1570, 3877)])

    layers = [
        {"name": "water", "features": [{"geometry": poly, "properties": {}}]}
    ]

    layer = {
        "name": "water",
        "features": [
            {"geometry": poly}
        ]
    }

    mvt = mapbox_vector_tile.encode(layer, y_coord_down=True)
    # path = 'src/test/resources/tile.mvt'
    path = '/tmp/from-py.mvt'
    with open(path, 'wb') as f:
        f.write(mvt)
