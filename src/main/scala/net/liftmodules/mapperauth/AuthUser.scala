package net.liftmodules.mapperauth

import scala.xml.Text
import net.liftweb._
import net.liftweb.common._
import net.liftweb.http.{CleanRequestVarOnSessionTransition, LiftResponse, RequestVar, S, SessionVar}
import net.liftweb.util.FieldError
import net.liftweb.util.Helpers
import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.IdPK
import net.liftweb.mapper.MappedEmail
import net.liftweb.mapper.MappedField
import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.mapper.CreatedUpdated
import net.liftweb.mapper.By
import net.liftweb.mapper.UserIdAsString
import net.liftmodules.mapperauth.model.ExtSession
import net.liftweb.mapper.MappedString
import net.liftweb.mapper.MappedBoolean
import net.liftweb.mapper.MappedPassword
import net.liftweb.mapper.OneToMany
import net.liftmodules.mapperauth.model.Role
import net.liftmodules.mapperauth.model.share.MappedRole
import net.liftmodules.mapperauth.model.Permission
import scala.xml.UnprefixedAttribute

/**
 * AuthUser is a base class that gives you a "User" that has roles and permissions.
 */
trait AuthUser {
  /*
   * String representing the User ID
   */
  def userIdAsString: String

  /*
   * A list of this user's permissions
   */
  def authPermissions: Set[APermission]

  /*
   * A list of this user's roles
   */
  def authRoles: Set[String]
}

trait AuthUserMeta[UserType <: AuthUser] {
  /*
   * True when the user request var is defined.
   */
  def isLoggedIn: Boolean
  /*
   * User logged in by supplying password. False if auto logged in by ExtSession or LoginToken.
   */
  def isAuthenticated: Boolean
  /*
   * Current user has the given role
   */
  def hasRole(role: String): Boolean
  def lacksRole(role: String): Boolean = !hasRole(role)
  def hasAnyRoles(roles: Seq[String]) = roles exists (r => hasRole(r.trim))

  /*
   * Current user has the given permission
   */
  def hasPermission(permission: APermission): Boolean
  def lacksPermission(permission: APermission): Boolean = !hasPermission(permission)

  def hasRole(user: UserType, role: String): Boolean
  def lacksRole(user: UserType, role: String): Boolean = !hasRole(user, role)
  def hasAnyRoles(user: UserType, roles: Seq[String]) = roles exists (r => hasRole(user, r.trim))

  /*
   * Current user has the given permission
   */
  def hasPermission(user: UserType, permission: APermission): Boolean
  def lacksPermission(user: UserType, permission: APermission): Boolean = !hasPermission(user, permission)

  /*
   * Log the current user out
   */
  def logUserOut(): Unit

  /*
   * Handle a LoginToken. Called from Locs.loginTokenLocParams
   */
  def handleLoginToken(): Box[LiftResponse] = Empty
}

/*
 * Trait that has login related code
 */
trait UserLifeCycle[UserType <: AuthUser] {

  /*
   * Given a String representing the User ID, find the user
   */
  def findByStringId(id: String): Box[UserType]

  // log in/out lifecycle callbacks
  def onLogIn: List[UserType => Unit] = Nil
  def onLogOut: List[Box[UserType] => Unit] = Nil

  // current userId stored in the session.
  private object curUserId extends SessionVar[Box[String]](Empty)
  def currentUserId: Box[String] = curUserId.is

  private object curUserIsAuthenticated extends SessionVar[Boolean](false)

  // Request var that holds the User instance
  private object curUser extends RequestVar[Box[UserType]](currentUserId.flatMap(findByStringId))
  with CleanRequestVarOnSessionTransition {
    override lazy val __nameSalt = Helpers.nextFuncName
  }
  def currentUser: Box[UserType] = curUser.is

  def isLoggedIn: Boolean = currentUserId.isDefined
  def isAuthenticated: Boolean = curUserIsAuthenticated.is

  def hasRole(role: String): Boolean = currentUser.map(u => hasRole(u, role)).openOr(false)

  def hasPermission(permission: APermission): Boolean = currentUser.map(u => hasPermission(u, permission)).openOr(false)

  def hasRole(user: UserType, role: String): Boolean = user.authRoles.exists(_ == role)

  def hasPermission(user: UserType, permission: APermission): Boolean = permission.implies(user.authPermissions)

  def logUserIn(who: UserType, isAuthed: Boolean = false, isRemember: Boolean = false) {
    curUserId.remove()
    curUserIsAuthenticated.remove()
    curUser.remove()
    curUserId(Full(who.userIdAsString))
    curUserIsAuthenticated(isAuthed)
    curUser(Full(who))
    onLogIn.foreach(_(who))
    if (isRemember) {
      ExtSession.createExtSession(who.userIdAsString)
    }
  }

  def logUserOut() {
    onLogOut.foreach(_(currentUser))
    curUserId.remove()
    curUserIsAuthenticated.remove()
    curUser.remove()
    S.session.foreach(_.destroySession())
  }
}

/*
 * Mapper version of AuthUser
 */
trait MapperAuthUser[T <: MapperAuthUser[T]] extends LongKeyedMapper[T] with IdPK with UserIdAsString with AuthUser with CreatedUpdated {
  self: T =>

  def userIdAsString: String = id.is.toString

  def id: MappedField[_, _]
  def email: MappedEmail[_]
}

/*
 * Mix this in for a simple user.
 */
trait ProtoAuthUser[T <: ProtoAuthUser[T]] extends MapperAuthUser[T] {
  self: T =>

  import Helpers._

  object username extends MappedString(this, 32) {
    override def displayName = "Username"
    override def setFilter = trim _ :: super.setFilter

    override def valUnique(msg: => String)(value: String): List[FieldError] = {
      if (value.length > 0)
        getSingleton.findAll(By(this, value)).filterNot(_.id.is == fieldOwner.id.is).map(u =>
          FieldError(this, Text(msg))
        )
      else
        Nil
    }

    override def validations =
      valUnique("Another user is already using that username, please enter a different one") _ ::
      valMinLen(3, "Username must be at least 3 characters") _ ::
      valMaxLen(32, "Username must be less than 33 characters") _ ::
      super.validations

    // Bootstrap 3
    override def toFormAppendedAttributes = new UnprefixedAttribute("class", "form-control", super.toFormAppendedAttributes)
  }

  /*
  * http://www.dominicsayers.com/isemail/
  */
  object email extends MappedEmail(this, 254) {
    override def displayName = "Email"
    override def setFilter = trim _ :: toLower _ :: super.setFilter

    override def valUnique(msg: => String)(value: String): List[FieldError] = {
      fieldOwner.getSingleton.findAll(By(this, value)).filter(_.id.is != fieldOwner.id.is).map(u =>
        FieldError(this, Text(msg))
      )
    }

    override def validations =
      valUnique("That email address is already registered with us") _  ::
      valMaxLen(254, "Email must be 254 characters or less") _ ::
      super.validations

    // Bootstrap 3
    override def toFormAppendedAttributes = new UnprefixedAttribute("class", "form-control", super.toFormAppendedAttributes)

  }

  // email address has been verified by clicking on a LoginToken link
  object verified extends MappedBoolean(this) {
    override def displayName = "Verified"

    // Bootstrap 3
    override def toFormAppendedAttributes = new UnprefixedAttribute("class", "form-control", super.toFormAppendedAttributes)
  }
  object password extends MappedPassword(this) {
    override def displayName = "Password"

    // Bootstrap 3
    override def toFormAppendedAttributes = new UnprefixedAttribute("class", "form-control", super.toFormAppendedAttributes)
  }

  object userRoles extends MappedRole(this)

//  object permissions extends MappedOneToMany(Permission, Permission.userId)
//  object roles extends StringRefListField(this, Role) {
//    def permissions: List[Permission] = objs.flatMap(_.permissions.is)
//    def names: List[String] = objs.map(_.id.is)
//  }
//
//  lazy val authPermissions: Set[Permission] = (permissions.is ::: roles.permissions).toSet
//  lazy val authRoles: Set[String] = roles.names.toSet

  /**
   * Using a lazy val means the user has to be reloaded if the attached roles or permissions change.
   */
  lazy val authPermissions: Set[APermission] = (Permission.userPermissions(id.is) ::: userRoles.permissions).toSet
  lazy val authRoles: Set[String] = userRoles.names.toSet

  lazy val fancyEmail = AuthUtil.fancyEmail(username.is, email.is)
}

trait ProtoAuthUserMeta[UserType <: MapperAuthUser[UserType]]
      extends LongKeyedMetaMapper[UserType] with AuthUserMeta[UserType] with UserLifeCycle[UserType] {

  self: UserType =>

}

