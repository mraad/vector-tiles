package com.esri

import com.esri.core.geometry.{Envelope2D, Point2D}
import vector_tile.Tile.GeomType

import scala.collection.mutable.ArrayBuffer

/**
  * https://github.com/mapbox/vector-tile-spec/tree/master/2.1
  */
class GeometryDecoder(envp: Envelope2D = new Envelope2D(0, 0, 4096, 4096),
                      extent: Int = 4096,
                      yOrigTop: Boolean = true
                     ) {

  private val envpW = envp.getWidth
  private val envpH = envp.getHeight

  private def zigzag(p: Int) = {
    (p >> 1) ^ -(p & 1)
  }

  private def toX(x: Int) = {
    envp.xmin + envpW * x / extent
  }

  private def toYTop(y: Int) = {
    envp.ymin + envpH * y / extent
  }

  private def toYBot(y: Int) = {
    envp.ymin + envpH * (extent - y) / extent
  }

  private val toY: (Int) => Double = if (yOrigTop) toYTop else toYBot

  def decode(items: Seq[Int], geomType: GeomType.EnumVal) = {
    var dx = 0
    var dy = 0
    var i = 0

    val parts = ArrayBuffer.empty[Array[Point2D]]
    var points = ArrayBuffer.empty[Point2D]

    def insurePolygonIsClosed(): Unit = {
      if (geomType == GeomType.POLYGON && !points.head.isEqual(points.last)) {
        points += points.head
      }
    }

    def readXY(len: Int): Unit = {
      for (l <- 1 to len) {
        val ex = zigzag(items(i))
        i += 1
        val ey = zigzag(items(i))
        i += 1

        dx += ex
        dy += ey

        points += new Point2D(toX(dx), toY(dy))
      }
    }

    val max = items.length
    while (i < max) {
      val cmdLen = items(i)
      i += 1
      val len = cmdLen >> 3
      val cmd = cmdLen & 0x07
      cmd match {
        case Command.CLOSE => {
          if (points.nonEmpty) {
            insurePolygonIsClosed()
            parts += points.toArray
            points.clear()
          }
        }
        case Command.MOVETO => {
          if (points.nonEmpty) {
            insurePolygonIsClosed()
            parts += points.toArray
            points.clear()
          }
          readXY(len)
        }
        case Command.LINETO => {
          readXY(len)
          if (geomType == GeomType.LINESTRING) {
            parts += points.toArray
            points.clear()
          }
        }
        case _ => throw new Exception(s"Unexpected cmd $cmd")
      }
    }
    (parts.toArray, points.toArray)
  }
}
