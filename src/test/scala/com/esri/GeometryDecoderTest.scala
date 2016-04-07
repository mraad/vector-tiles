package com.esri

import org.scalatest._
import vector_tile.Tile.GeomType

/**
  */
class GeometryDecoderTest extends FlatSpec with Matchers {

  it should "decode single point" in {
    val geomDecoder = new GeometryDecoder()
    val (_, points) = geomDecoder.decode(Seq(9, 50, 34), GeomType.POINT)
    points.nonEmpty shouldBe true
    points(0).x shouldBe 25
    points(0).y shouldBe 17
  }

  it should "decode multi point" in {
    val geomDecoder = new GeometryDecoder()
    val (_, points) = geomDecoder.decode(Seq(17, 10, 14, 3, 9), GeomType.POINT)
    points.length shouldBe 2
    points(0).x shouldBe 5
    points(0).y shouldBe 7
    points(1).x shouldBe 3
    points(1).y shouldBe 2
  }

  it should "decode single linestring" in {
    val geomDecoder = new GeometryDecoder()
    val (parts, _) = geomDecoder.decode(Seq(9, 4, 4, 18, 0, 16, 16, 0), GeomType.LINESTRING)
    parts.nonEmpty shouldBe true
    val partHead = parts.head
    partHead.length shouldBe 3
    partHead(0).x shouldBe 2
    partHead(0).y shouldBe 2
    partHead(1).x shouldBe 2
    partHead(1).y shouldBe 10
    partHead(2).x shouldBe 10
    partHead(2).y shouldBe 10
  }

  it should "decode multi linestring" in {
    val geomDecoder = new GeometryDecoder()
    val (parts, _) = geomDecoder.decode(Seq(9, 4, 4, 18, 0, 16, 16, 0, 9, 17, 17, 10, 4, 8), GeomType.LINESTRING)
    parts.nonEmpty shouldBe true

    val partHead = parts.head
    partHead.length shouldBe 3
    partHead(0).x shouldBe 2
    partHead(0).y shouldBe 2
    partHead(1).x shouldBe 2
    partHead(1).y shouldBe 10
    partHead(2).x shouldBe 10
    partHead(2).y shouldBe 10

    val partLast = parts.last
    partLast.length shouldBe 2
    partLast(0).x shouldBe 1
    partLast(0).y shouldBe 1
    partLast(1).x shouldBe 3
    partLast(1).y shouldBe 5
  }

  it should "decode single polyline" in {
    val geomDecoder = new GeometryDecoder()
    val (parts, _) = geomDecoder.decode(Seq(9, 6, 12, 18, 10, 12, 24, 44, 15), GeomType.POLYGON)
    parts.nonEmpty shouldBe true
    val partHead = parts.head
    partHead.length shouldBe 4
    partHead(0).x shouldBe 3
    partHead(0).y shouldBe 6
    partHead(1).x shouldBe 8
    partHead(1).y shouldBe 12
    partHead(2).x shouldBe 20
    partHead(2).y shouldBe 34
    partHead(3).x shouldBe 3
    partHead(3).y shouldBe 6
  }

}
