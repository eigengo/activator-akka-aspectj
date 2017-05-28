name := "activator-akka-aspectj"

version := "1.0"

scalaVersion := "2.12.2"

val aspectVersion = "1.8.10"

val akkaVersion = "2.5.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka"      %% "akka-actor"            % akkaVersion,
  "com.typesafe.akka"      %% "akka-slf4j"            % akkaVersion,
  "org.aspectj"             % "aspectjweaver"         % aspectVersion,
  "org.aspectj"             % "aspectjrt"             % aspectVersion
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

parallelExecution in Test := false

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

javaOptions in run += "-javaagent:" + System.getProperty("user.home") + "/.ivy2/cache/org.aspectj/aspectjweaver/jars/aspectjweaver-" + aspectVersion + ".jar"

fork in run := true

connectInput in run := true
