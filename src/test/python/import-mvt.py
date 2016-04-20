#!/usr/bin/env python

import mapbox_vector_tile

if __name__ == "__main__":
    with open('/tmp/from-scala.mvt', 'rb') as f:
        bytes = f.read()
        tile = mapbox_vector_tile.decode(bytes)
        print repr(tile)
    with open('/tmp/from-py.mvt', 'rb') as f:
        bytes = f.read()
        tile = mapbox_vector_tile.decode(bytes)
        print repr(tile)
