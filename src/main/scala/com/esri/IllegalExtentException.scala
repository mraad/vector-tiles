package com.esri

/**
  * Exception to be thrown if supplied extent is less than 1
  *
  * @param msg the exception message
  */
case class IllegalExtentException(msg: String) extends Exception(msg)