package com.esri

import vector_tile.Tile.{Feature, Layer, Value}

import scala.collection.mutable

/**
  * Placeholder to accumulate the keys and values and features before encoding using integer references.
  */
final case class VectorTileLayer(name: String) {
  val keyTags = mutable.LinkedHashMap.empty[String, Int] // Insures the insert order is preserved
  val valTags = mutable.LinkedHashMap.empty[Any, Int]
  val features = mutable.ArrayBuffer.empty[Feature]

  def getKeyTag(key: String) = {
    keyTags.getOrElseUpdate(key, keyTags.size)
  }

  def getValTag(key: Any) = {
    valTags.getOrElseUpdate(key, valTags.size)
  }

  def keys() = {
    keyTags.keys.toVector
  }

  def values() = {
    valTags.keys.map {
      _ match {
        case intValue: Int => Value(`intValue` = Some(intValue))
        case sintValue: Long => Value(`sintValue` = Some(sintValue))
        case floatValue: Float => Value(`floatValue` = Some(floatValue))
        case doubleValue: Double => Value(`doubleValue` = Some(doubleValue))
        case boolValue: Boolean => Value(`boolValue` = Some(boolValue))
        case other => Value(`stringValue` = Some(other.toString))
      }
    }.toVector
  }

  def toLayer(extent: Int) = {
    Layer(
      `version` = 1,
      `name` = name,
      `keys` = keys(),
      `values` = values(),
      `extent` = Some(extent),
      `features` = features.toVector
    )
  }
}

