package net.liftmodules.mapperauth.model.share

import java.util.Date
import org.joda.time.{ReadablePeriod, DateTime}
import net.liftweb._
import net.liftweb.common._
import net.liftweb.mapper.Mapper
import net.liftweb.mapper.MappedDate

class MappedExpiringDate[OwnerType<:Mapper[OwnerType]](val fo: OwnerType) extends MappedDate(fo) {

  def this(fo: OwnerType, period: ReadablePeriod) = {
    this(fo)
    set(periodToExpiresDate(period))
  }

  def periodToExpiresDate(period: ReadablePeriod): Date = ((new DateTime).plus(period.toPeriod)).toDate

  def apply(in: ReadablePeriod): OwnerType = apply(periodToExpiresDate(in))

  def isExpired: Boolean = (new DateTime).getMillis >= (new DateTime(this.is)).getMillis
}
