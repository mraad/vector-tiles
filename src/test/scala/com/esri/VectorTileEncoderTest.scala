package com.esri

import com.esri.core.geometry.{Envelope2D, Point, Polygon}
import org.scalatest._
import vector_tile.Tile
import vector_tile.Tile.GeomType

/**
  * TODO - add more test cases
  */
class VectorTileEncoderTest extends FlatSpec with Matchers {

  /*
    "This test" should "write a simple polygon to /tmp/from-scala.mvt" in {
      val envp = new Envelope2D(0, 0, 4096, 4096)
      val vte = VectorTileEncoder(clip = envp, envp = envp, yCoordDown = false, addPolyLastPoint = false)

      val polygon = new Polygon()
      polygon.startPath(0, 0)
      polygon.lineTo(0, 1)
      polygon.lineTo(1, 1)
      polygon.lineTo(1, 0)
      // polygon.lineTo(0, 0)
      polygon.closeAllPaths()
      // val attr = Map("uid" -> 123, "foo" -> "bar", "cat" -> "flew")
      vte.addFill("water", polygon, Map.empty[String, Any])

      val path = Paths.get("/tmp/from-scala.mvt")
      Files.write(path, vte.encode())
      Files.exists(path) shouldBe true
    }
  */

  it should "test for Beirut" in {
    val bytes = VectorTileEncoder(new Envelope2D(-180, -90, 180, 90))
      .addFeature("cities", new Point(35.4955, 33.8886), Map("id" -> 3533, "name" -> "Beirut"))
      .encode()

    val tile = Tile.parseFrom(bytes)
    val layers = tile.`layers`
    layers.length shouldBe 1
    val layer = layers.head
    layer.`name` shouldBe "cities"
    layer.`version` shouldBe 1
    layer.`extent` shouldBe Some(4096)

    layer.`keys` should contain allOf("id", "name")

    layer.`values`.length shouldBe 2
    val value0 = layer.`values`(0)
    value0.`intValue` shouldBe Some(3533L)
    val value1 = layer.`values`(1)
    value1.`stringValue` shouldBe Some("Beirut")

    val features = layer.`features`
    features.length shouldBe 1
    val feature = features.head
    feature.`type` shouldBe Some(GeomType.POINT)
    val geomDecoder = new GeometryDecoder(new Envelope2D(-180, -90, 180, 90), yOrigTop = false)
    val (_, points) = geomDecoder.decode(feature.`geometry`, GeomType.POINT)
    points.length shouldBe 1

    val x1 = (4096 * (35.4955 + 180.0) / 360.0).round.toInt
    val x2 = (360.0 * x1 / 4096) - 180.0

    val y1 = (4096 * (33.8886 + 90.0) / 180.0).round.toInt
    val y2 = (180.0 * y1 / 4096) - 90.0

    val point = points.head
    point.x shouldBe x2
    point.y shouldBe y2
  }

  it should "test for one polygon feature" in {
    val envp = new Envelope2D(0, 0, 4096, 4096)
    val vte = VectorTileEncoder(
      envp = envp,
      clip = envp,
      extent = 4096,
      yOrigAtTop = true,
      addPolygonLastPoint = false)

    val polygon = new Polygon()
    polygon.startPath(0, 0)
    polygon.lineTo(0, 1)
    polygon.lineTo(1, 1)
    polygon.lineTo(1, 0)
    polygon.closeAllPaths()
    vte.addFill("water", polygon, Map("uid" -> 123, "foo" -> "bar", "cat" -> "flew"))

    val tile = Tile.parseFrom(vte.encode())
    val layers = tile.`layers`
    layers.length shouldBe 1
    val layer = layers(0)
    layer.`name` shouldBe "water"
    layer.`version` shouldBe 1
    layer.`extent` shouldBe Some(4096)
    val features = layer.`features`
    features.length shouldBe 1
    val feature = features(0)
    feature.`type` shouldBe Some(GeomType.POLYGON)

    val geomDecoder = new GeometryDecoder(envp, 4096, yOrigTop = true)
    val (parts, _) = geomDecoder.decode(feature.`geometry`, GeomType.POLYGON)
    parts.nonEmpty shouldBe true
    val partHead = parts.head
    partHead.length shouldBe 5
    partHead(0).x = 0
    partHead(0).y = 0
    partHead(1).x = 0
    partHead(1).y = 1
    partHead(2).x = 1
    partHead(2).y = 1
    partHead(3).x = 1
    partHead(3).y = 0
    partHead(4).x = 0
    partHead(4).y = 0
  }

}
