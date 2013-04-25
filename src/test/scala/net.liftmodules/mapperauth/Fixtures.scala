package net.liftmodules.mapperauth

import java.util.UUID
import net.liftweb._
import common._
import util.Helpers
import net.liftweb.mapper.MappedEmail
import net.liftweb.mapper.MetaMapper
import net.liftweb.mapper.LongKeyedMetaMapper

class CustomUser private () extends MapperAuthUser[CustomUser] {
  def getSingleton = CustomUser

  object email extends MappedEmail(this, 254)

  lazy val authPermissions: Set[APermission] = Set.empty
  lazy val authRoles: Set[String] = Set.empty

  override def userIdAsString: String = id.toString
}

object CustomUser extends CustomUser with ProtoAuthUserMeta[CustomUser] {
  /*
  def createUser(username: String, email: String, password: String, permissions: List[String]): Box[CustomUser] = {
    val newUser = createRecord
      .save

    Full(newUser)
  }
  */

  def findByStringId(id: String): Box[CustomUser] = Helpers
    .tryo(UUID.fromString(id)).flatMap(find(_))
}


class UltraCustomUser private () extends MapperAuthUser[UltraCustomUser] {
  def getSingleton = UltraCustomUser

  object email extends MappedEmail(this, 254)

  lazy val authPermissions: Set[APermission] = Set.empty
  lazy val authRoles: Set[String] = Set.empty

  override def userIdAsString: String = id.toString
}

object UltraCustomUser extends UltraCustomUser with LongKeyedMetaMapper[UltraCustomUser] {
  def findByStringId(id: String): Box[UltraCustomUser] =
    Helpers.tryo(id.toLong).flatMap(find(_))
}

