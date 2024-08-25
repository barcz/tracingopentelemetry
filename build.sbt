import sbt.Keys.javacOptions

import scala.collection.Seq

val scala3Version = "3.5.0"

val PekkoVersion = "1.0.3"
val PekkoHttpVersion = "1.0.1"
val OpenTelemetryVersion = "1.41.0"
val OpenTelemetryInstrumentationVersion = "2.7.0"

ThisBuild / scalacOptions ++= Seq(
    "-encoding",
    "UTF-8",
    "-deprecation",
    "-feature",
    "-Xcheck-macros",
    "-Ycheck:all", // also for checking macros
    "-Ycheck-mods",
    "-Ydebug-type-error",
    "-Xprint-types", // Without this flag, we will not see error messages for exceptions during given-macro expansion!
    "-Yshow-print-errors",
    "-language:experimental.macros",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:namedTypeArguments",
    "-language:dynamics",
    "-Ykind-projector:underscores",
    "-unchecked"
  )

lazy val client = project
  .in(file("client"))
  .enablePlugins(JavaAgent)
  .settings(
    mainClass := Some("client"),
    name := "client",
    version := "0.0.1",
    javacOptions ++= Seq("-source", "17", "-target", "17"),
    scalaVersion := scala3Version,
    javaAgents += "io.opentelemetry.javaagent" % "opentelemetry-javaagent" % "2.7.0"  % Runtime,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "shapeless3-deriving" % "3.4.2",
      "org.apache.pekko" %% "pekko-actor-typed" % PekkoVersion,
      "org.apache.pekko" %% "pekko-stream" % PekkoVersion,
      "org.apache.pekko" %% "pekko-http" % PekkoHttpVersion,
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "io.opentelemetry" % "opentelemetry-exporter-otlp" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-sdk" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-extension-trace-propagators" % OpenTelemetryVersion % Runtime,
      "io.opentelemetry.instrumentation" % "opentelemetry-instrumentation-annotations" % OpenTelemetryInstrumentationVersion
    )
  ).dependsOn(common)


lazy val server = project
  .in(file("server"))
  .enablePlugins(JavaAgent)
  .settings(
    mainClass := Some("server"),
    name := "client",
    version := "0.0.1",
    javacOptions ++= Seq("-source", "17", "-target", "17"),
    scalaVersion := scala3Version,
    javaAgents += "io.opentelemetry.javaagent" % "opentelemetry-javaagent" % "2.7.0"  % Runtime,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "shapeless3-deriving" % "3.4.2",
      "org.apache.pekko" %% "pekko-actor-typed" % PekkoVersion,
      "org.apache.pekko" %% "pekko-stream" % PekkoVersion,
      "org.apache.pekko" %% "pekko-http" % PekkoHttpVersion,
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "io.opentelemetry" % "opentelemetry-exporter-otlp" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-sdk" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-extension-trace-propagators" % OpenTelemetryVersion % Runtime,
      "io.opentelemetry.instrumentation" % "opentelemetry-instrumentation-annotations" % OpenTelemetryInstrumentationVersion
    )
  ).dependsOn(common)

lazy val common = project.in(file("common"))
  .settings(
    name := "common",
    version := "0.0.1",
    javacOptions ++= Seq("-source", "17", "-target", "17"),
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "shapeless3-deriving" % "3.4.2",
      "org.apache.pekko" %% "pekko-actor-typed" % PekkoVersion,
      "org.apache.pekko" %% "pekko-stream" % PekkoVersion,
      "org.apache.pekko" %% "pekko-http" % PekkoHttpVersion,
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "io.opentelemetry" % "opentelemetry-exporter-otlp" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-sdk" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-extension-trace-propagators" % OpenTelemetryVersion % Runtime,
      "io.opentelemetry.instrumentation" % "opentelemetry-instrumentation-annotations" % OpenTelemetryInstrumentationVersion
    )


  )