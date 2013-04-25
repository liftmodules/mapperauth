package net.liftmodules.mapperauth

class PermissionSpec extends BaseSpec {

  "Permissions" should {

    "handle simple permission" in {
      val perm1 = APermission("perm1")

      perm1.implies(APermission.all) should equal (true)
      perm1.implies(APermission.none) should equal (false)
      perm1.implies(APermission("perm1")) should equal (true)
      perm1.implies(APermission("a")) should equal (false)
    }

    "handle domain permission" in {
      val printer = APermission("printer", "manage")

      printer.implies(APermission.all) should equal (true)
      printer.implies(APermission.none) should equal (false)
      printer.implies(APermission("printer", "query")) should equal (false)
      printer.implies(APermission("printer", "*")) should equal (true)
      printer.implies(APermission("printer", "manage")) should equal (true)
    }

    "handle entity permission" in {
      val edit = APermission("users", "edit", "abcd1234")

      edit.implies(APermission.all) should equal (true)
      edit.implies(APermission.none) should equal (false)
      edit.implies(APermission("users", "create")) should equal (false)
      edit.implies(APermission("users", "update")) should equal (false)
      edit.implies(APermission("users", "view")) should equal (false)
      edit.implies(APermission("users", APermission.wildcardToken)) should equal (true)
      edit.implies(APermission("users", "edit")) should equal (true)
      edit.implies(APermission("users", "edit", "abc")) should equal (false)
      edit.implies(APermission("users", "edit", "abcd1234")) should equal (true)
    }
  }
}
