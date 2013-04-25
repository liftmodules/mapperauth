package net.liftmodules.mapperauth.model

import net.liftweb.util.Helpers._
import scala.xml.Text
import java.text.SimpleDateFormat
import net.liftweb.common.Box
import net.liftweb.common.Full
import net.liftweb.common.Failure
import net.liftweb.http.S
import java.util.Date
import java.text.DateFormat
import net.liftweb.util.FieldError
import net.liftweb.json.JString
import net.liftweb.json.JInt
import net.liftweb.common.Empty
import net.liftweb.json.JsonAST.JValue
import net.liftweb.mapper.Mapper
import net.liftweb.mapper.IdPK
import net.liftweb.mapper.MappedLongForeignKey
import net.liftweb.mapper.MappedDate
import scala.xml.NodeSeq
import net.liftweb.mapper.MappedField
import net.liftweb.mapper.IHaveValidatedThisSQL
import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.BySql
import net.liftweb.mapper.IHaveValidatedThisSQL
import net.liftweb.mapper.MappedNullableDate
import net.liftweb.util.Helpers
import net.liftweb.util.BaseField

package share {

  object Implicits {
  }

  trait MapperWithId[OwnerType <: LongKeyedMapper[OwnerType] with IdPK] extends LongKeyedMapper[OwnerType] with IdPK {
    self: OwnerType =>

    def allRecordFields: Seq[BaseField] = allFields

  }

  trait DateFormField[OwnerType <: Mapper[OwnerType]] extends MappedNullableDate[OwnerType] {

    OwnerType =>

    def dateFormatter: DateFormat
    def fieldInputMask: String

    /**
     * This defines the string parsing semantics of this field. Used in setFromAny.
     * By default uses LiftRules.dateTimeConverter's parseDate; override for field-specific behavior
     */
    override def parse(in: String): Box[Date] = try {
      if (!in.trim.isEmpty()) {
        val d = dateFormatter.parse(in)
        Full(d)
      } else {
        Empty
        //Failure("Empty Date string.")
      }
    } catch {
      case _: Throwable => set(null); Failure("Invalid Date string: " + in)
    }

    /**
     * This method defines the string parsing semantics of this field. Used in toString, _toForm.
     * By default uses LiftRules.dateTimeConverter's formatDate; override for field-specific behavior
     */
    override def format(d: Date): String = dateFormatter.format(d)

    override def _toForm: Box[NodeSeq] =
      S.fmapFunc({s: List[String] => this.setFromAny(s)}){funcName =>
      Full(appendFieldId(<input type={formInputType} data-mask={ fieldInputMask }
                         name={funcName}
                         value={is match {
                           case Full(s) if s != null => format(s)
                           case _ => ""
                         }}/>))
      }

//    def elem = {
//      S.fmapFunc(S.SFuncHolder(this.setFromString(_))) { funcName =>
//        <input type="text" data-mask={ fieldInputMask } name={ funcName } value={ valueBox.map(v => dateFormatter.format(v)) openOr "" } tabindex={ tabIndex.toString }/>
//      }
//    }
//
//    override def toForm =
//      uniqueFieldId match {
//        case Full(id) => Full(elem % ("id" -> id))
//        case _        => Full(elem)
//      }
  }


}