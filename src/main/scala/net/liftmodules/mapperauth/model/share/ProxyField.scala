package net.liftmodules.mapperauth.model.share

import net.liftweb.util.BaseField
import scala.xml.NodeSeq
import net.liftweb.common._
import net.liftweb.util.Helpers._

/**
 * A trait for a value that appears to be on an object
 * but is injected from somewhere else.
 */
trait ProxyField[T] extends BaseField {
  type ValueType = T

  private var _value: ValueType = _

  def validate = Nil
  def validations = Nil
  def setFilter = Nil

  def set(in: ValueType): ValueType = {_value = in; in }
  def get: ValueType = _value
  def is = get
  def toForm = Empty

}