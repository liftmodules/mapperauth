name := "mapperauth"

organization := "net.liftmodules"

version := "0.3-SNAPSHOT"

liftVersion <<= liftVersion ?? "2.6-SNAPSHOT"

// liftVersion <<= liftVersion ?? "3.0-SNAPSHOT"

liftEdition <<= liftVersion apply { _.substring(0,3) }

name <<= (name, liftEdition) { (n, e) =>  n + "_" + e }

scalaVersion := "2.10.3"

crossScalaVersions := Seq("2.9.2", "2.9.1-1", "2.9.1", "2.10.3")

//crossScalaVersions := Seq("2.10.3")

resolvers ++= Seq(
  "CB Central Mirror"            at "http://repo.cloudbees.com/content/groups/public",
  "Sonatype OSS Release"         at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype Snapshot"            at "https://oss.sonatype.org/content/repositories/snapshots"
)

libraryDependencies <++= (liftVersion) { liftVersion =>
  Seq(
    "net.liftweb"             %% "lift-mapper"         % liftVersion % "provided",
    "net.liftweb"             %% "lift-webkit"         % liftVersion % "provided",
    "ch.qos.logback"          %  "logback-classic"     % "1.0.6"     % "provided",
    "org.scalatest"           %% "scalatest"           % "2.0.M5b"   % "test"
  )
}

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

credentials += Credentials( file("/private/liftmodules/sonatype.credentials") )

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

credentials += Credentials( file("/private/liftmodules/cloudbees.credentials") )

// Remove Java directories, otherwise sbteclipse generates them
unmanagedSourceDirectories in Compile <<= (scalaSource in Compile)(Seq(_))

unmanagedSourceDirectories in Test <<= (scalaSource in Test)(Seq(_))

