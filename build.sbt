import LiftModule.{liftVersion, liftEdition}

name := "mapperauth"

organization := "net.liftmodules"

version := "0.6-SNAPSHOT"

liftVersion := "3.2.0"

liftEdition := liftVersion.value.substring(0,3)

moduleName := name.value + "_" + liftEdition.value

scalaVersion := "2.12.6"

crossScalaVersions := Seq("2.12.6", "2.11.11")

resolvers ++= Seq(
  "CB Central Mirror"            at "http://repo.cloudbees.com/content/groups/public",
  "Sonatype OSS Release"         at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype Snapshot"            at "https://oss.sonatype.org/content/repositories/snapshots"
)

libraryDependencies ++=
  "net.liftweb"             %% "lift-mapper"         % liftVersion.value % "provided" ::
  "net.liftweb"             %% "lift-webkit"         % liftVersion.value % "provided" ::
  "ch.qos.logback"          %  "logback-classic"     % "1.0.6"     % "provided" ::
  "org.scalatest"           %% "scalatest"           % "3.0.1"     % "test" ::
  Nil

scalacOptions := Seq("-deprecation", "-unchecked")

// Sonatype publishing set up below this point

publishTo := { version.value.endsWith("SNAPSHOT") match {
 	case true  => Some("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
 	case false => Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
}}

credentials += Credentials( file("sonatype.credentials") )

credentials += Credentials( file(System.getenv().get("HOME") + "/Keys/sonatype.credentials") )

credentials ++= (for {
  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
	<url>https://github.com/tuhlmann/lift-mapperauth</url>
	<licenses>
		<license>
	      <name>Apache 2.0 License</name>
	      <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
	      <distribution>repo</distribution>
	    </license>
	 </licenses>
	 <scm>
	    <url>git@github.com:tuhlmann/lift-mapperauth.git</url>
	    <connection>scm:git:git@github.com:tuhlmann/lift-mapperauth.git</connection>
	 </scm>
	 <developers>
	    <developer>
	      <id>tuhlmann</id>
	      <name>Torsten Uhlmann</name>
	      <url>http://agynamix.de</url>
	 	</developer>
	 </developers>
 )

// Remove Java directories, otherwise sbteclipse generates them
unmanagedSourceDirectories in Compile := Seq((scalaSource in Compile).value)

unmanagedSourceDirectories in Test := Seq((scalaSource in Test).value)
