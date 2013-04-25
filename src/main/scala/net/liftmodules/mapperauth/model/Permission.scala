package net.liftmodules.mapperauth.model

import net.liftweb._
import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.mapper.IdPK
import net.liftweb.mapper.By
import net.liftmodules.mapperauth.APermission
import net.liftweb.mapper.MappedStringForeignKey
import net.liftweb.mapper.MappedLong
import net.liftweb.mapper.MappedString


/*
 * Simple record for storing permissions.
 */
object Permission extends Permission with LongKeyedMetaMapper[Permission]  {

  def addUserPermission(uid: Long, aPerm: APermission) = {
    create.userId(uid).permission(aPerm.toString).saveMe
  }

  def removeAllUserPermissions(uid: Long) = {
    Permission.findAll(By(userId, uid)).map(_.delete_!)
  }

  def toAPermission(perm: Permission) = APermission.fromString(perm.permission.is)
  def fromAPermission(aPerm: APermission): Permission = Permission.create.permission(aPerm.toString)

  def userPermissions(uid: Long): List[APermission] = Permission.findAll(By(userId, uid)).map(toAPermission)

}

class Permission extends LongKeyedMapper[Permission] with IdPK {
  def getSingleton = Permission

  /**
   * This field is empty for permissions attached directly to the user
   */
  object roleId extends MappedStringForeignKey(this, Role, 32) {
    def foreignMeta = Role
  }

  /**
   * This field is empty for permissions attached to a role
   */
  object userId extends MappedLong(this) {
    override def dbIndexed_? = true
  }

  object permission extends MappedString(this, 1024)

}

