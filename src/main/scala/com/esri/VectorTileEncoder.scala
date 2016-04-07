package com.esri

import com.esri.core.geometry.Operator.Type
import com.esri.core.geometry._
import vector_tile.Tile
import vector_tile.Tile.{Feature, GeomType}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

final case class VectorTileEncoder(envp: Envelope2D, clip: Envelope2D, extent: Int = 4096, yOrigAtTop: Boolean = false, addPolygonLastPoint: Boolean = false) {

  private val operator = OperatorFactoryLocal.getInstance.getOperator(Type.Clip).asInstanceOf[OperatorClip]

  private val envpW = envp.getWidth

  private val envpH = envp.getHeight

  private val layers = mutable.HashMap.empty[String, VectorTileLayer]

  private val point2D = new Point2D()

  private val arrBuf = new ArrayBuffer[Int](1024)

  private def encodeZigZag(n: Int) = {
    (n << 1) ^ (n >> 31)
  }

  private def commandInteger(command: Int, count: Int) = {
    (count << 3) | command
  }

  private def toYBot(py: Double): Int = {
    extent - toYTop(py)
  }

  private def toYTop(py: Double): Int = {
    (extent * (py - envp.ymin) / envpH).round.toInt
  }

  private val toY: (Double) => Int = if (yOrigAtTop) toYTop else toYBot

  private def toX(px: Double): Int = {
    (extent * (px - envp.xmin) / envpW).round.toInt
  }

  private def toPathEndNoop(pathEnd: Int) = {
    pathEnd
  }

  private def toPathEndSpec(pathEnd: Int) = {
    pathEnd - 1
  }

  private val toPathEndPoly: (Int) => Int = if (addPolygonLastPoint) toPathEndSpec else toPathEndNoop

  private def closeLine(): Unit = {
  }

  private def closeFill(): Unit = {
    arrBuf += commandInteger(Command.CLOSE, 1)
  }

  private def addMultiPath(multiPath: MultiPath, toPathEnd: (Int) => Int, closePath: () => Unit) = {
    var cx = 0
    var cy = 0
    arrBuf.clear()
    val pathCount = multiPath.getPathCount
    for (pathIndex <- 0 until pathCount) {
      val pathStart = multiPath.getPathStart(pathIndex)
      val pathEnd = toPathEnd(multiPath.getPathEnd(pathIndex))

      multiPath.getXY(pathStart, point2D)
      val px = toX(point2D.x)
      val py = toY(point2D.y)
      val mx = px - cx
      val my = py - cy
      cx = px
      cy = py
      val xyBuf = new ArrayBuffer[Int]((pathEnd - pathStart) * 2) // TODO - can we reuse ?
      for (s <- pathStart + 1 until pathEnd) {
        multiPath.getXY(s, point2D)
        val px = toX(point2D.x)
        val py = toY(point2D.y)
        val dx = px - cx
        val dy = py - cy
        if (dx != 0 || dy != 0) {
          xyBuf += encodeZigZag(dx)
          xyBuf += encodeZigZag(dy)
          cx = px
          cy = py
        }
      }
      if (xyBuf.nonEmpty) {
        arrBuf += commandInteger(Command.MOVETO, 1)
        arrBuf += encodeZigZag(mx)
        arrBuf += encodeZigZag(my)
        arrBuf += commandInteger(Command.LINETO, xyBuf.length / 2)
        arrBuf.appendAll(xyBuf)
        closePath()
      }
    }
    arrBuf.nonEmpty
  }

  def addPoint(layerName: String, point: Point, attr: Map[String, Any]) = {
    if (clip.contains(point)) {
      val layer = layers.getOrElseUpdate(layerName, VectorTileLayer(layerName))
      val tags = attr.flatMap { case (k, v) => Seq(layer.getKeyTag(k), layer.getValTag(v)) }.toVector

      val x = toX(point.getX)
      val y = toY(point.getY)

      layer.features += Feature(
        `tags` = tags,
        `type` = Some(GeomType.POINT),
        `geometry` = Vector(
          commandInteger(Command.MOVETO, 1),
          encodeZigZag(x),
          encodeZigZag(y)
        ))
    }
    this
  }

  def addLine(layerName: String, line: MultiPath, attr: Map[String, Any]) = {
    val clipped = operator.execute(line, clip, null, null).asInstanceOf[MultiPath]
    if (!clipped.isEmpty) {
      if (addMultiPath(clipped, toPathEndNoop, closeLine)) {
        val layer = layers.getOrElseUpdate(layerName, VectorTileLayer(layerName))
        val tags = attr.flatMap { case (k, v) => Seq(layer.getKeyTag(k), layer.getValTag(v)) }.toVector
        layer.features += Feature(
          `tags` = tags,
          `type` = Some(GeomType.LINESTRING),
          `geometry` = arrBuf.toVector)
      }
    }
    this
  }

  def addFill(layerName: String, fill: MultiPath, attr: Map[String, Any]) = {
    val clipped = operator.execute(fill, clip, null, null).asInstanceOf[MultiPath]
    if (!clipped.isEmpty) {
      if (addMultiPath(clipped, toPathEndPoly, closeFill)) {
        val layer = layers.getOrElseUpdate(layerName, VectorTileLayer(layerName))
        val tags = attr.flatMap { case (k, v) => Seq(layer.getKeyTag(k), layer.getValTag(v)) }.toVector
        layer.features += Feature(
          `tags` = tags,
          `type` = Some(GeomType.POLYGON),
          `geometry` = arrBuf.toVector)
      }
    }
    this
  }

  def addFeature(layerName: String, geom: Geometry, attr: Map[String, Any]) = {
    if (!geom.isEmpty) {
      geom match {
        case point: Point => addPoint(layerName, point, attr)
        case line: Polyline => addLine(layerName, line, attr)
        case fill: Polygon => addFill(layerName, fill, attr)
        case _ => throw new IllegalArgumentException("Invalid geometry type, must be point or polyline or polygon")
      }
    }
    this
  }

  def toTile() = {
    Tile(`layers` = layers.values.map(_.toLayer(extent)).toVector)
  }

  def encode() = {
    toTile().toByteArray()
  }
}

object VectorTileEncoder {
  def apply(envp: Envelope2D) = new VectorTileEncoder(envp, envp)
}
