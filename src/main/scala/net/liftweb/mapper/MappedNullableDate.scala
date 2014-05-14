package net.liftweb
package mapper

import net.liftweb.util.FatLazy
import java.util.Date
import net.liftweb.http.js.JsExp
import net.liftweb.http.js.JE
import net.liftweb.json.JsonAST
import net.liftweb.common._
import net.liftweb.util.Helpers._
import java.sql.Types
import scala.xml.NodeSeq
import net.liftweb.http.S
import java.lang.reflect.Method
import scala.math.BigInt.long2bigInt
import net.liftweb.http.LiftRules

abstract class MappedNullableDate[T<:Mapper[T]](val fieldOwner: T) extends MappedNullableField[Date, T] {
  private val data = FatLazy(defaultValue)
  private val orgData = FatLazy(defaultValue)

  /**
   * This defines the string parsing semantics of this field. Used in setFromAny.
   * By default uses LiftRules.dateTimeConverter's parseDate; override for field-specific behavior
   */
  def parse(s: String): Box[Date] = LiftRules.dateTimeConverter().parseDate(s)
  /**
   * This method defines the string parsing semantics of this field. Used in toString, _toForm.
   * By default uses LiftRules.dateTimeConverter's formatDate; override for field-specific behavior
   */
  def format(d: Date): String = LiftRules.dateTimeConverter().formatDate(d)

  protected def real_i_set_!(value: Box[Date]): Box[Date] = {
    if (value != data.get) {
      data() = value
      this.dirty_?( true)
    }
    data.get
  }

  def dbFieldClass = classOf[Box[Date]]


  def toLong: Long = get match {
    case Full(d: Date) if d != null => d.getTime / 1000L
    case _ => 0L
  }

  def asJsExp: JsExp = JE.Num(toLong)

  def asJsonValue: Box[JsonAST.JValue] = Full(get match {
    case Full(v: Date) if v != null => JsonAST.JInt(v.getTime)
    case _ => JsonAST.JNull
  })

  /**
   * Get the JDBC SQL Type for this field
   */
  def targetSQLType = Types.DATE

  def defaultValue: Box[Date] = Empty
  // private val defaultValue_i = new Date

  override def writePermission_? = true
  override def readPermission_? = true

  protected def i_is_! = data.get
  protected def i_was_! = orgData.get
  override protected[mapper] def doneWithSave() {orgData.setFrom(data)}

  protected def i_obscure_!(in : Box[Date]) : Box[Date] = Full(new Date(0L))


  /**
   * Create an input field for the item
   */
  override def _toForm: Box[NodeSeq] =
  S.fmapFunc({s: List[String] => this.setFromAny(s)}){funcName =>
  Full(appendFieldId(<input type={formInputType}
                     name={funcName}
                     value={get match {
                         case Full(s) if s != null => format(s)
                         case _ => "" }} />))
  }

  override def setFromAny(f : Any): Box[Date] = f match {
    case JsonAST.JNull => this.set(Empty)
    case JsonAST.JInt(v) => this.set(Full(new Date(v.longValue)))
    case n: Number => this.set(Full(new Date(n.longValue)))
    case "" | null => this.set(Empty)
    case s: String => parse(s).map(d => this.set(Full(d))).openOr(this.get)
    case (s: String) :: _ => parse(s).map(d => this.set(Full(d))).openOr(this.get)
    case d: Date => this.set(Full(d))
    case Some(d: Date) => this.set(Full(d))
    case Full(d: Date) => this.set(Full(d))
    case None | Empty | Failure(_, _, _) => this.set(Empty)
    case _ => this.get
  }

  def jdbcFriendly(field : String) = real_convertToJDBCFriendly(i_is_!)
  override def jdbcFriendly = real_convertToJDBCFriendly(i_is_!)

  def real_convertToJDBCFriendly(value: Box[Date]): Object = value match {
    case Full(d) if d != null => new java.sql.Date(d.getTime)
    case _ => null
  }

  private def st(in: Box[Date]): Unit = {
    data.set(in)
    orgData.set(in)
  }

  def buildSetActualValue(accessor: Method, v: AnyRef, columnName: String): (T, AnyRef) => Unit =
  (inst, v) => doField(inst, accessor, {case f: MappedNullableDate[_] => f.st(toDate(v))})

  def buildSetLongValue(accessor: Method, columnName: String): (T, Long, Boolean) => Unit =
  (inst, v, isNull) => doField(inst, accessor, {case f: MappedNullableDate[_] => f.st(if (isNull) Empty else Full(new Date(v)))})

  def buildSetStringValue(accessor: Method, columnName: String): (T, String) => Unit =
  (inst, v) => doField(inst, accessor, {case f: MappedNullableDate[_] => f.st(toDate(v))})

  def buildSetDateValue(accessor: Method, columnName: String): (T, Date) => Unit =
  (inst, v) => doField(inst, accessor, {case f: MappedNullableDate[_] => f.st(Full(v))})

  def buildSetBooleanValue(accessor: Method, columnName: String): (T, Boolean, Boolean) => Unit =
  (inst, v, isNull) => doField(inst, accessor, {case f: MappedNullableDate[_] => f.st(Empty)})

  /**
   * Given the driver type, return the string required to create the column in the database
   */
  def fieldCreatorString(dbType: DriverType, colName: String): String = colName + " " + dbType.dateColumnType + notNullAppender()

  def inFuture_? = data.get match {
    case Full(d) if d != null => d.getTime > millis
    case _ => false
  }

  def inPast_? = data.get match {
    case Full(d) if d != null => d.getTime < millis
    case _ => false
  }

}
