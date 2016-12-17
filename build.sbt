import LiftModule.{liftVersion, liftEdition}

name := "mapperauth"

organization := "net.liftmodules"

version := "0.5-SNAPSHOT"

// liftVersion <<= liftVersion ?? "2.6-SNAPSHOT"

liftVersion := "3.0.1"

liftEdition := liftVersion.value.substring(0,3)

// name <<= (name, liftEdition) { (n, e) =>  n + "_" + e }

// Necessary beginning with sbt 0.13, otherwise Lift editions get messed up.
// E.g. "2.5" gets converted to "2-5"
// moduleName := name.value
moduleName := name.value + "_" + liftEdition.value

scalaVersion := "2.12.1"

crossScalaVersions := Seq("2.12.1", "2.11.8")

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

scalacOptions <<= scalaVersion map { sv: String =>
  if (sv.startsWith("2.10."))
    Seq("-deprecation", "-unchecked", "-feature", "-language:postfixOps")
  else
    Seq("-deprecation", "-unchecked")
}

// Sonatype publishing set up below this point

publishTo <<= version { _.endsWith("SNAPSHOT") match {
 	case true  => Some("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
 	case false => Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
}}

credentials += Credentials( file("sonatype.credentials") )

credentials += Credentials( file(System.getenv().get("HOME") + "/Keys/sonatype.credentials") )

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
unmanagedSourceDirectories in Compile <<= (scalaSource in Compile)(Seq(_))

unmanagedSourceDirectories in Test <<= (scalaSource in Test)(Seq(_))
