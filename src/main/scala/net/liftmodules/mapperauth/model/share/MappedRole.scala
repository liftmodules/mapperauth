package net.liftmodules.mapperauth.model.share

import net.liftweb.mapper.Mapper
import net.liftweb.mapper.MappedString
import net.liftmodules.mapperauth.model.Role
import net.liftweb.common._
import scala.xml.Elem
import net.liftweb.http.SHtml._
import scala.xml.Text
import net.liftmodules.mapperauth.APermission

abstract class MappedRole[T<:Mapper[T]](_fieldOwner: T) extends MappedString[T](_fieldOwner, 1024) {

  def buildDisplayList: List[(String, String)] = {
    Role.allRoles(Role.CAT_TEAM) map (r => (r.id.is, r.displayName))
  }

  def choosenElement = names match {
    case name :: _ => Full(name)
    case _ => Empty
  }

  override def _toForm: Box[Elem] =
    Full(selectObj[String](buildDisplayList, choosenElement, v => this.setRole(v)))

  override def asHtml = firstRole.map(_.asHtml).openOr(Text(""))

  def permissions: List[APermission] = names.flatMap(n => Role.find(n).map(_.permissions.allPerms).openOr(Nil))
  def names: List[String] = is.split(",").toList
  def addRole(role: String*) = {
    val current = if (is == null) "" else is
    val add = (if (current.length() > 0) "," else "") + role.mkString(",")
    set(current+add)
    fieldOwner
  }

  def setRole(role: String): T = {
    removeAll
    addRole(role)
    fieldOwner
  }

  def setRole(role: Role): T = setRole(role.id.is)

  def removeAll = { set(""); this }

  def firstRole: Box[Role] = names.headOption.flatMap(name => Role.find(name))
}

