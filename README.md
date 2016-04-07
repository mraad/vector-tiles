# Heuristic Experiment on Encoding Mapbox Vector Tiles In Scala

This _work in progress_ is a [Scala](http://www.scala-lang.org/) based encoder of the [Mapbox Vector Tile specification](https://github.com/mapbox/vector-tile-spec) given a set of [Esri Geometries](https://github.com/Esri/geometry-api-java) and attributes. It is largely based on [this](https://github.com/ElectronicChartCentre/java-vector-tile) Java implementation. 

## Encoding a Feature

```scala
val bytes = VectorTileEncoder(new Envelope2D(-180, -90, 180, 90))
  .addFeature("cities", new Point(35.4955, 33.8886), Map("id" -> 3533, "name" -> "Beirut"))
  .encode()
```

In the above example, a encoder is created with an envelope that is spanning the world in geographic coordinates.
A point feature with lat/lon of [Beirut](https://en.wikipedia.org/wiki/Beirut) is added to the `cities` layer with attribute values for the keys `id` and `name`.
The `encode()` method returns an array of bytes.

The `VectorTileEncoder` accepts the following parameters:
- `envp` : The vector tile envelope in "world" coordinates (Geographic, Mercator or Tile relative)
- `clip` : A clipping envelope - useful for polygons and polyline features and usually slightly bigger than the parameter `envp`.
- `extent`: The number of units comprise the width and height of the square tile.  The default value is `4096`
- `yOrigAtTop`: Specifies whether `y=0` should be at the top of the tile (per the specification) or at the bottom - The latter makes it easier to display world values.  The default is `false`.
- `addPolygonLastPoint`: Specifies if the last point in a polygon should be encoded.  The specification dictates that this should *not* be done (thus the `CLOSE` command). However, I've seen situations in data sources where the last point should be encoded, as it is not the same as the first point. 

## Project Dependencies

- [Esri Java Geometry API](https://github.com/Esri/geometry-api-java)
- [The Scala Protocol Buffers (protobuf) Compiler](https://github.com/SandroGrzicic/ScalaBuff)

## Building the Project

```bash
mvn install
```

[Maven](https://maven.apache.org/) uses the [exec plugin](http://www.mojohaus.org/exec-maven-plugin/) to spawn [ScalaBuff](https://github.com/SandroGrzicic/ScalaBuff) to generate the stubs based the [Protocol Buffer](https://developers.google.com/protocol-buffers/docs/overview) messages in `src/main/protobuf/vector_tile.proto`.

#### See Also

- <https://github.com/ElectronicChartCentre/java-vector-tile>
- <https://github.com/mapbox/awesome-vector-tiles>
- <https://github.com/mapzen/mapbox-vector-tile>
- <http://www.maptiler.org/google-maps-coordinates-tile-bounds-projection/>
- <http://stackoverflow.com/questions/19688289/are-there-any-ways-to-generate-scala-code-from-protobuf-files-in-a-maven-build>
- <https://pavelfatin.com/scala-collections-tips-and-tricks/>
- <http://docs.scala-lang.org/overviews/collections/performance-characteristics.html>
- <https://gist.github.com/odoe/ce6a150658526901ef27#file-vector-tile-pr-md>
- <https://cgoldberg.github.io/python-unittest-tutorial/>
- <http://blog.berczuk.com/2009/12/continuous-integration-of-python-code.html>
- <http://mapboxvectortileuploaderthingymajig.azurewebsites.net/>

## License

[Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
