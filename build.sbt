name := "activator-akka-aspectj"

version := "1.0"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka"      %% "akka-actor"            % "2.2.0",
  "com.typesafe.akka"      %% "akka-slf4j"            % "2.2.0",
  "org.aspectj"             % "aspectjweaver"         % "1.8.5",
  "org.aspectj"             % "aspectjrt"             % "1.8.5",
  "org.specs2"             %% "specs2"                % "1.14"         % "test",
  "com.typesafe.akka"      %% "akka-testkit"          % "2.2.0"        % "test",
  "com.novocode"            % "junit-interface"       % "0.7"          % "test->default"
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

javaOptions in run += "-javaagent:" + System.getProperty("user.home") + "/.ivy2/cache/org.aspectj/aspectjweaver/jars/aspectjweaver-1.8.5.jar"

fork in run := true

connectInput in run := true
