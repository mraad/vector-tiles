package com.esri

/**
  * Exception to be thrown if no layers were added to a tile
  */
case class EmptyLayersException() extends Exception()